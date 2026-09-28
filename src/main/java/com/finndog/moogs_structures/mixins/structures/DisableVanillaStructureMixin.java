package com.finndog.moogs_structures.mixins.structures;

import com.finndog.moogs_structures.config.MslConfig;
import com.finndog.moogs_structures.config.ReplaceVanillaManager;
import net.minecraft.core.RegistryAccess;
//? if <26.3 {
import net.minecraft.core.SectionPos;
//?}
//? if >=1.21.4 {
/*import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
*///?}
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.ChunkPos;
//? if >=26.3 {
/*import net.minecraft.world.level.biome.Climate;
*///?}
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkGenerator.class)
public class DisableVanillaStructureMixin {
    @Inject(method = "tryGenerateStructure", at = @At("HEAD"), cancellable = true)
    private void moogs_structures_disableReplacedVanilla(
            StructureSet.StructureSelectionEntry entry,
            StructureManager structureManager,
            RegistryAccess registryAccess,
            RandomState randomState,
            StructureTemplateManager structureTemplateManager,
            long seed,
            ChunkAccess chunk,
            ChunkPos chunkPos,
            //? if <26.3 {
            SectionPos sectionPos,
            //?}
            // tryGenerateStructure carries a trailing ResourceKey<Level> dimension param on this version;
            // the HEAD inject must mirror the full target signature or mixin apply fails.
            // 26.3 dropped the SectionPos and added a trailing Climate.Sampler; the HEAD inject must
            // mirror the full target signature or mixin apply fails.
            //? if >=1.21.4 {
            /*ResourceKey<Level> dimension,
            *///?}
            //? if >=26.3 {
            /*Climate.Sampler climateSampler,
            *///?}
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!ReplaceVanillaManager.hasAnyBindings() && !MslConfig.get().hasAnyDisabled()) return;
        entry.structure().unwrapKey().ifPresent(key -> {
            //? if <1.21.11 {
            if (ReplaceVanillaManager.shouldCancelVanilla(key.location()) || MslConfig.get().isStructureDisabled(key.location())) {
            //?} else {
            /*if (ReplaceVanillaManager.shouldCancelVanilla(key.identifier()) || MslConfig.get().isStructureDisabled(key.identifier())) {
            *///?}
                cir.setReturnValue(false);
            }
        });
    }
}
