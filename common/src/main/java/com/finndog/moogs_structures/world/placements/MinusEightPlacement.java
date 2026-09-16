package com.finndog.moogs_structures.world.placements;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.function.Consumer;

// 26.3: PlacementModifier is an interface that pushes positions to a consumer instead of
// returning a stream, and the type registry holds MapCodecs directly (codec() replaces type()).
public class MinusEightPlacement implements PlacementModifier {
	private static final MinusEightPlacement INSTANCE = new MinusEightPlacement();
	public static final MapCodec<MinusEightPlacement> CODEC = MapCodec.unit(() -> INSTANCE);

	public static MinusEightPlacement subtractedEight() {
		return INSTANCE;
	}

	@Override
	public void modify(PlacementContext placementContext, RandomSource random, BlockPos blockPos, Consumer<BlockPos> output) {
		output.accept(new BlockPos(blockPos.getX() - 8, blockPos.getY(), blockPos.getZ() - 8));
	}

	@Override
	public MapCodec<MinusEightPlacement> codec() {
		return CODEC;
	}
}
