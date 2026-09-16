package com.finndog.moogs_structures.modinit;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.modinit.registry.RegistryEntry;
import com.finndog.moogs_structures.modinit.registry.ResourcefulRegistries;
import com.finndog.moogs_structures.modinit.registry.ResourcefulRegistry;
import com.finndog.moogs_structures.world.placements.MinusEightPlacement;
import com.finndog.moogs_structures.world.placements.SnapToLowerNonAirPlacement;
import com.finndog.moogs_structures.world.placements.UnlimitedCountPlacement;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public final class MoogsStructuresPlacements {
	// 26.3 dropped PlacementModifierType; the registry holds the MapCodecs themselves.
	public static final ResourcefulRegistry<MapCodec<? extends PlacementModifier>> PLACEMENT_MODIFIER = ResourcefulRegistries.create(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, MoogsStructuresCommon.MODID);

	public static final RegistryEntry<MapCodec<MinusEightPlacement>> MINUS_EIGHT_PLACEMENT = PLACEMENT_MODIFIER.register("minus_eight_placement", () -> MinusEightPlacement.CODEC);
	public static final RegistryEntry<MapCodec<UnlimitedCountPlacement>> UNLIMITED_COUNT = PLACEMENT_MODIFIER.register("unlimited_count", () -> UnlimitedCountPlacement.CODEC);
	public static final RegistryEntry<MapCodec<SnapToLowerNonAirPlacement>> SNAP_TO_LOWER_NON_AIR_PLACEMENT = PLACEMENT_MODIFIER.register("snap_to_lower_non_air_placement", () -> SnapToLowerNonAirPlacement.CODEC);
}
