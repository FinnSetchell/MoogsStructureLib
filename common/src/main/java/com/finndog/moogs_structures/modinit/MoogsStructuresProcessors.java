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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public final class MoogsStructuresProcessors {
    public static final ResourcefulRegistry<StructureProcessorType<?>> STRUCTURE_PROCESSOR = ResourcefulRegistries.create(BuiltInRegistries.STRUCTURE_PROCESSOR, MoogsStructuresCommon.MODID);

    public static final RegistryEntry<StructureProcessorType<PillarProcessor>> PILLAR_PROCESSOR = STRUCTURE_PROCESSOR.register("pillar_processor", () -> () -> PillarProcessor.CODEC);
    public static final RegistryEntry<StructureProcessorType<CloseOffFluidSourcesProcessor>> CLOSE_OFF_FLUID_SOURCES_PROCESSOR = STRUCTURE_PROCESSOR.register("close_off_fluid_sources_processor", () -> () -> CloseOffFluidSourcesProcessor.CODEC);
    public static final RegistryEntry<StructureProcessorType<RemoveFloatingBlocksProcessor>> REMOVE_FLOATING_BLOCKS_PROCESSOR = STRUCTURE_PROCESSOR.register("remove_floating_blocks_processor", () -> () -> RemoveFloatingBlocksProcessor.CODEC);
    public static final RegistryEntry<StructureProcessorType<RandomReplaceWithPropertiesProcessor>> RANDOM_REPLACE_WITH_PROPERTIES_PROCESSOR = STRUCTURE_PROCESSOR.register("random_replace_with_properties_processor", () -> () -> RandomReplaceWithPropertiesProcessor.CODEC);
    public static final RegistryEntry<StructureProcessorType<SuperGravityProcessor>> SUPER_GRAVITY_PROCESSOR = STRUCTURE_PROCESSOR.register("super_gravity_processor", () -> () -> SuperGravityProcessor.CODEC);
    public static final RegistryEntry<StructureProcessorType<FloodWithWaterProcessor>> FLOOD_WITH_WATER_PROCESSOR = STRUCTURE_PROCESSOR.register("flood_with_water_processor", () -> () -> FloodWithWaterProcessor.CODEC);
    public static final RegistryEntry<StructureProcessorType<SpawnerRandomizingProcessor>> SPAWNER_RANDOMIZING_PROCESSOR = STRUCTURE_PROCESSOR.register("spawner_randomizing_processor", () -> () -> SpawnerRandomizingProcessor.CODEC);
    public static final RegistryEntry<StructureProcessorType<EquipArmorStandProcessor>> EQUIP_ARMOR_STAND_PROCESSOR = STRUCTURE_PROCESSOR.register("equip_armor_stand_processor", () -> () -> EquipArmorStandProcessor.CODEC);
}
