package com.finndog.moogs_structures.modinit;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.modinit.registry.ResourcefulRegistries;
import com.finndog.moogs_structures.modinit.registry.ResourcefulRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public final class MoogsStructuresProcessors {
    public static final ResourcefulRegistry<StructureProcessorType<?>> STRUCTURE_PROCESSOR = ResourcefulRegistries.create(BuiltInRegistries.STRUCTURE_PROCESSOR, MoogsStructuresCommon.MODID);

    //public static final RegistryEntry<StructureProcessorType<WaterloggingFixProcessor>> WATERLOGGING_FIX_PROCESSOR = STRUCTURE_PROCESSOR.register("waterlogging_fix_processor", () -> () -> WaterloggingFixProcessor.CODEC);
}
