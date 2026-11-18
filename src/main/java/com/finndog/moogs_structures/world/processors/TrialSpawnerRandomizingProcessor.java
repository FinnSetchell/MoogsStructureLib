//? if >=1.21.1 {
package com.finndog.moogs_structures.world.processors;

//? if <1.21.5 {
import com.finndog.moogs_structures.misc.trialspawnerconfig.TrialSpawnerConfigManager;
//?}
import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.utils.MobNbtUpgrader;
//? if <26.2 {
import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
//?}
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
//? if >=1.21.5 {
/*import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
*///?}
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
//? if <26.2 {
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
//?}
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

/**
 * Configures a {@code minecraft:trial_spawner} block entity at placement time. Strips runtime
 * state (so every placement starts fresh instead of inheriting the build-world's cooldown) and
 * writes {@code normal_config} / {@code ominous_config} resolved from ResourceLocation refs.
 *
 * <p>On MC 1.21.0&ndash;1.21.4 the block entity codec expects an inline NBT object for these
 * fields (not a registry ref like 1.21.5+), so the refs are looked up against
 * {@link TrialSpawnerConfigManager} and written inline. Authors keep one set of config JSONs at
 * {@code data/<ns>/trial_spawner/<path>.json} that work across the whole 1.21 range.
 *
 * <p>With {@code nbt_version}, the mobs in the configs are written for that Minecraft version and upgraded to the
 * running one (see {@link MobNbtUpgrader}). From 1.21.5 that means writing the config inline rather than as its id,
 * since the game loads a config's mobs from its JSON as they are.
 *
 * <p>The blockstate (including the {@code ominous} property set in the build world) is preserved.
 */
//? if <26.2 {
public class TrialSpawnerRandomizingProcessor extends StructureProcessor {
//?} else {
/*public class TrialSpawnerRandomizingProcessor implements StructureProcessor {
*///?}

    //? if <26.2 {
    public static final MapCodec<TrialSpawnerRandomizingProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    //?} else {
    /*public static final MapCodec<TrialSpawnerRandomizingProcessor> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    *///?}
            ResourceLocation.CODEC.fieldOf("normal_config").forGetter(p -> p.normalConfig),
            ResourceLocation.CODEC.optionalFieldOf("ominous_config").forGetter(p -> p.ominousConfig),
            MobNbtUpgrader.VERSION_CODEC.optionalFieldOf("nbt_version").forGetter(p -> p.nbtVersion)
    ).apply(instance, instance.stable(TrialSpawnerRandomizingProcessor::new)));

    public final ResourceLocation normalConfig;
    public final Optional<ResourceLocation> ominousConfig;
    /** The Minecraft version the configs' mobs are written for; they are upgraded to the running version. */
    public final Optional<MobNbtUpgrader.NbtVersion> nbtVersion;

    private TrialSpawnerRandomizingProcessor(ResourceLocation normalConfig, Optional<ResourceLocation> ominousConfig,
                                             Optional<MobNbtUpgrader.NbtVersion> nbtVersion) {
        this.normalConfig = normalConfig;
        this.ominousConfig = ominousConfig;
        this.nbtVersion = nbtVersion;
    }

    @Override
    //? if <26.2 {
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldView, BlockPos pos, BlockPos blockPos, StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings structurePlacementData) {
        if (!(structureBlockInfoWorld.state().getBlock() instanceof TrialSpawnerBlock)) {
            return structureBlockInfoWorld;
    //?} else {
    /*public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldView, BlockPos targetPosition, BlockPos referencePos, BlockPos templateRelativePos, StructureTemplate.StructureBlockInfo processedBlockInfo, StructurePlaceSettings structurePlacementData) {
        if (!(processedBlockInfo.state().getBlock() instanceof TrialSpawnerBlock)) {
            return processedBlockInfo;
    *///?}
        }

        //? if <26.2 {
        CompoundTag existing = structureBlockInfoWorld.nbt();
        //?} else {
        /*CompoundTag existing = processedBlockInfo.nbt();
        *///?}
        CompoundTag newNbt = existing != null ? existing.copy() : new CompoundTag();

        newNbt.remove("server_data");
        newNbt.remove("shared_data");
        newNbt.remove("spawn_data");
        newNbt.remove("cooldown_end_at_tick");
        newNbt.remove("next_mob_spawns_at");

        //? if <26.2 {
        BlockPos at = structureBlockInfoWorld.pos();
        //?} else {
        /*BlockPos at = processedBlockInfo.pos();
        *///?}
        putConfig(newNbt, "normal_config", normalConfig, worldView, at);
        ominousConfig.ifPresent(id -> putConfig(newNbt, "ominous_config", id, worldView, at));

        //? if <26.2 {
        return new StructureTemplate.StructureBlockInfo(structureBlockInfoWorld.pos(), structureBlockInfoWorld.state(), newNbt);
        //?} else {
        /*return new StructureTemplate.StructureBlockInfo(processedBlockInfo.pos(), processedBlockInfo.state(), newNbt);
        *///?}
    }

    /**
     * Writes a config: below 1.21.5 always inline (there is no config registry), from 1.21.5 as its id, or inline
     * when its mobs need upgrading.
     */
    private void putConfig(CompoundTag nbt, String key, ResourceLocation id, LevelReader worldView, BlockPos at) {
        //? if <1.21.5 {
        CompoundTag config = TrialSpawnerConfigManager.INSTANCE.get(id);
        if (config == null) {
            MoogsStructuresCommon.LOGGER.warn("Moog's Structure Lib: trial_spawner config '{}' not found at {}", id, at);
            return;
        }
        config = config.copy();
        if (nbtVersion.isPresent()) {
            MobNbtUpgrader.upgradeSpawnPotentials(config, nbtVersion.get());
        }
        nbt.put(key, config);
        //?} else {
        /*if (nbtVersion.isPresent()) {
            var registryAccess = worldView.registryAccess();
            var config = registryAccess.lookupOrThrow(Registries.TRIAL_SPAWNER_CONFIG)
                    .get(ResourceKey.create(Registries.TRIAL_SPAWNER_CONFIG, id))
                    .flatMap(holder -> TrialSpawnerConfig.DIRECT_CODEC
                            .encodeStart(registryAccess.createSerializationContext(NbtOps.INSTANCE), holder.value()).result());
            if (config.isPresent() && config.get() instanceof CompoundTag inline) {
                MobNbtUpgrader.upgradeSpawnPotentials(inline, nbtVersion.get());
                nbt.put(key, inline);
                return;
            }
            MoogsStructuresCommon.LOGGER.warn("Moog's Structure Lib: trial_spawner config '{}' not found at {}", id, at);
        }
        nbt.putString(key, id.toString());
        *///?}
    }

    @Override
    //? if <26.2 {
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.TRIAL_SPAWNER_RANDOMIZING_PROCESSOR.get();
    //?} else {
    /*public MapCodec<TrialSpawnerRandomizingProcessor> codec() {
        return MAP_CODEC;
    *///?}
    }
}

//?}