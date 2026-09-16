package com.finndog.moogs_structures.world.placements;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.function.Consumer;

public class SnapToLowerNonAirPlacement implements PlacementModifier {
	private static final SnapToLowerNonAirPlacement INSTANCE = new SnapToLowerNonAirPlacement();
	public static final MapCodec<SnapToLowerNonAirPlacement> CODEC = MapCodec.unit(() -> INSTANCE);

	public static SnapToLowerNonAirPlacement snapToLowerNonAir() {
		return INSTANCE;
	}

	@Override
	public void modify(PlacementContext placementContext, RandomSource random, BlockPos blockPos, Consumer<BlockPos> output) {
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(blockPos);
		while(placementContext.getBlockState(mutable).isAir() && mutable.getY() > placementContext.getMinY()) {
			mutable.move(Direction.DOWN);
		}
		output.accept(mutable.immutable());
	}

	@Override
	public MapCodec<SnapToLowerNonAirPlacement> codec() {
		return CODEC;
	}
}
