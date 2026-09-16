package com.finndog.moogs_structures.modinit;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.modinit.registry.RegistryEntry;
import com.finndog.moogs_structures.modinit.registry.ResourcefulRegistries;
import com.finndog.moogs_structures.modinit.registry.ResourcefulRegistry;
import com.finndog.moogs_structures.world.structures.placements.AdvancedRandomSpread;
import com.finndog.moogs_structures.world.structures.placements.ConditionalConcentricRings;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;


public final class MoogsStructuresStructurePlacementType {
    // 26.3 dropped StructurePlacementType; the registry holds the MapCodecs themselves.
    public static final ResourcefulRegistry<MapCodec<? extends StructurePlacement>> STRUCTURE_PLACEMENT_TYPE = ResourcefulRegistries.create(BuiltInRegistries.STRUCTURE_PLACEMENT, MoogsStructuresCommon.MODID);

    public static final RegistryEntry<MapCodec<AdvancedRandomSpread>> ADVANCED_RANDOM_SPREAD = STRUCTURE_PLACEMENT_TYPE.register("advanced_random_spread", () -> AdvancedRandomSpread.CODEC);

    public static final RegistryEntry<MapCodec<ConditionalConcentricRings>> CONDITIONAL_CONCENTRIC_RINGS = STRUCTURE_PLACEMENT_TYPE.register("conditional_concentric_rings", () -> ConditionalConcentricRings.CODEC);
}
