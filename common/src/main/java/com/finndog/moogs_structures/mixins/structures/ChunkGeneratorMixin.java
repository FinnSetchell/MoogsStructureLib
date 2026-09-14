package com.finndog.moogs_structures.mixins.structures;

import com.finndog.moogs_structures.config.ReplaceVanillaManager;
import com.finndog.moogs_structures.replacement.ReplacementAliases;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Lets a replacement structure spawn what the vanilla structure it stands in for would have spawned,
 * and points structure searches (/locate, explorer maps, dolphins, eyes of ender) at the replacement.
 */
@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {

    @Inject(
            method = "getMobsAt(Lnet/minecraft/core/Holder;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/util/random/WeightedList;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void moogs_structures_inheritSpawnOverrides(Holder<Biome> biome, StructureManager structureManager, MobCategory mobCategory, BlockPos blockPos,
                                                        CallbackInfoReturnable<WeightedList<MobSpawnSettings.SpawnerData>> cir) {
        if (!ReplacementAliases.hasAny()) return;

        Map<Structure, LongSet> structuresAt = structureManager.getAllStructuresAt(blockPos);
        if (structuresAt.isEmpty()) return;

        ReplacementAliases.Snapshot aliases = ReplacementAliases.snapshot();
        Registry<Structure> registry = structureManager.registryAccess().lookupOrThrow(Registries.STRUCTURE);

        for (Map.Entry<Structure, LongSet> entry : structuresAt.entrySet()) {
            Structure structure = entry.getKey();
            ResourceLocation id = registry.getKey(structure);
            if (id == null) continue;

            ReplaceVanillaManager.Replacement replacement = aliases.forReplacement(id)
                    .filter(r -> r.options().inheritSpawnOverrides())
                    .orElse(null);
            if (replacement == null) continue;

            Structure vanilla = registry.getValue(replacement.vanillaStructure());
            if (vanilla == null) continue;
            StructureSpawnOverride vanillaOverride = vanilla.spawnOverrides().get(mobCategory);
            if (vanillaOverride == null) continue;
            if (!moogs_structures_insideStart(structureManager, structure, entry.getValue(), blockPos, vanillaOverride.boundingBox())) continue;

            StructureSpawnOverride ownOverride = structure.spawnOverrides().get(mobCategory);
            boolean ownApplies = ownOverride != null
                    && (ownOverride.boundingBox() == vanillaOverride.boundingBox()
                        || moogs_structures_insideStart(structureManager, structure, entry.getValue(), blockPos, ownOverride.boundingBox()));
            if (!ownApplies) {
                cir.setReturnValue(vanillaOverride.spawns());
                return;
            }

            List<Weighted<MobSpawnSettings.SpawnerData>> merged = new ArrayList<>(ownOverride.spawns().unwrap());
            Set<EntityType<?>> present = new HashSet<>();
            for (Weighted<MobSpawnSettings.SpawnerData> spawn : merged) present.add(spawn.value().type());
            for (Weighted<MobSpawnSettings.SpawnerData> spawn : vanillaOverride.spawns().unwrap()) {
                if (present.add(spawn.value().type())) merged.add(spawn);
            }
            cir.setReturnValue(WeightedList.of(merged));
            return;
        }
    }

    @Inject(
            method = "findNearestMapStructure(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/HolderSet;Lnet/minecraft/core/BlockPos;IZ)Lcom/mojang/datafixers/util/Pair;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void moogs_structures_redirectStructureSearch(ServerLevel serverLevel, HolderSet<Structure> holderSet, BlockPos blockPos, int searchRadius, boolean skipKnownStructures,
                                                          CallbackInfoReturnable<Pair<BlockPos, Holder<Structure>>> cir) {
        if (!ReplacementAliases.hasAny()) return;

        ReplacementAliases.Snapshot aliases = ReplacementAliases.snapshot();
        Registry<Structure> registry = serverLevel.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        Set<Holder<Structure>> redirected = new LinkedHashSet<>();
        boolean swapped = false;

        for (Holder<Structure> holder : holderSet) {
            Holder<Structure> target = holder;
            ResourceLocation id = holder.unwrapKey().map(ResourceKey::location).orElse(null);
            if (id != null) {
                ReplaceVanillaManager.Replacement replacement = aliases.forVanilla(id)
                        .filter(r -> r.options().redirectLocate())
                        .orElse(null);
                if (replacement != null) {
                    Holder<Structure> replacementHolder = registry
                            .get(ResourceKey.create(Registries.STRUCTURE, replacement.replacementStructure()))
                            .orElse(null);
                    if (replacementHolder != null) {
                        target = replacementHolder;
                        swapped = true;
                    }
                }
            }
            redirected.add(target);
        }

        if (!swapped) return;

        // Safe to re-enter: the replacement ids have no alias of their own, so the second call runs vanilla.
        ChunkGenerator self = (ChunkGenerator) (Object) this;
        cir.setReturnValue(self.findNearestMapStructure(serverLevel, HolderSet.direct(List.copyOf(redirected)), blockPos, searchRadius, skipKnownStructures));
    }

    private static boolean moogs_structures_insideStart(StructureManager structureManager, Structure structure, LongSet references, BlockPos blockPos,
                                                        StructureSpawnOverride.BoundingBoxType boundingBox) {
        MutableBoolean inside = new MutableBoolean(false);
        Predicate<StructureStart> predicate = boundingBox == StructureSpawnOverride.BoundingBoxType.PIECE
                ? structureStart -> structureManager.structureHasPieceAt(blockPos, structureStart)
                : structureStart -> structureStart.getBoundingBox().isInside(blockPos);
        structureManager.fillStartsForStructure(structure, references, structureStart -> {
            if (inside.isFalse() && predicate.test(structureStart)) inside.setTrue();
        });
        return inside.isTrue();
    }
}
