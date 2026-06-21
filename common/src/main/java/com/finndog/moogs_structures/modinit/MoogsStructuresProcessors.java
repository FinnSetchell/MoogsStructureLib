package com.finndog.moogs_structures.modinit;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.modinit.registry.RegistryEntry;
import com.finndog.moogs_structures.modinit.registry.ResourcefulRegistries;
import com.finndog.moogs_structures.modinit.registry.ResourcefulRegistry;
import com.finndog.moogs_structures.world.processors.CloseOffFluidSourcesProcessor;
import com.finndog.moogs_structures.world.processors.EquipArmorStandProcessor;
import com.finndog.moogs_structures.world.processors.FloodWithWaterProcessor;
import com.finndog.moogs_structures.world.processors.PillarProcessor;
import com.finndog.moogs_structures.world.processors.RandomReplaceWithPropertiesProcessor;
import com.finndog.moogs_structures.world.processors.RemoveFloatingBlocksProcessor;
import com.finndog.moogs_structures.world.processors.SpawnerRandomizingProcessor;
import com.finndog.moogs_structures.world.processors.SuperGravityProcessor;
import com.finndog.moogs_structures.world.processors.TrialSpawnerRandomizingProcessor;
import com.finndog.moogs_structures.world.processors.VaultRandomizingProcessor;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

public final class MoogsStructuresProcessors {
    public static final ResourcefulRegistry<MapCodec<? extends StructureProcessor>> STRUCTURE_PROCESSOR = ResourcefulRegistries.create(BuiltInRegistries.STRUCTURE_PROCESSOR, MoogsStructuresCommon.MODID);

    public static final RegistryEntry<MapCodec<PillarProcessor>> PILLAR_PROCESSOR = STRUCTURE_PROCESSOR.register("pillar_processor", () -> PillarProcessor.MAP_CODEC);
    public static final RegistryEntry<MapCodec<CloseOffFluidSourcesProcessor>> CLOSE_OFF_FLUID_SOURCES_PROCESSOR = STRUCTURE_PROCESSOR.register("close_off_fluid_sources_processor", () -> CloseOffFluidSourcesProcessor.MAP_CODEC);
    public static final RegistryEntry<MapCodec<RemoveFloatingBlocksProcessor>> REMOVE_FLOATING_BLOCKS_PROCESSOR = STRUCTURE_PROCESSOR.register("remove_floating_blocks_processor", () -> RemoveFloatingBlocksProcessor.MAP_CODEC);
    public static final RegistryEntry<MapCodec<RandomReplaceWithPropertiesProcessor>> RANDOM_REPLACE_WITH_PROPERTIES_PROCESSOR = STRUCTURE_PROCESSOR.register("random_replace_with_properties_processor", () -> RandomReplaceWithPropertiesProcessor.MAP_CODEC);
    public static final RegistryEntry<MapCodec<SuperGravityProcessor>> SUPER_GRAVITY_PROCESSOR = STRUCTURE_PROCESSOR.register("super_gravity_processor", () -> SuperGravityProcessor.MAP_CODEC);
    public static final RegistryEntry<MapCodec<FloodWithWaterProcessor>> FLOOD_WITH_WATER_PROCESSOR = STRUCTURE_PROCESSOR.register("flood_with_water_processor", () -> FloodWithWaterProcessor.MAP_CODEC);
    public static final RegistryEntry<MapCodec<SpawnerRandomizingProcessor>> SPAWNER_RANDOMIZING_PROCESSOR = STRUCTURE_PROCESSOR.register("spawner_randomizing_processor", () -> SpawnerRandomizingProcessor.MAP_CODEC);
    public static final RegistryEntry<MapCodec<EquipArmorStandProcessor>> EQUIP_ARMOR_STAND_PROCESSOR = STRUCTURE_PROCESSOR.register("equip_armor_stand_processor", () -> EquipArmorStandProcessor.MAP_CODEC);
    public static final RegistryEntry<MapCodec<TrialSpawnerRandomizingProcessor>> TRIAL_SPAWNER_RANDOMIZING_PROCESSOR = STRUCTURE_PROCESSOR.register("trial_spawner_randomizing_processor", () -> TrialSpawnerRandomizingProcessor.MAP_CODEC);
    public static final RegistryEntry<MapCodec<VaultRandomizingProcessor>> VAULT_RANDOMIZING_PROCESSOR = STRUCTURE_PROCESSOR.register("vault_randomizing_processor", () -> VaultRandomizingProcessor.MAP_CODEC);
}
