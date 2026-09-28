package com.finndog.moogs_structures.world.placements;

//? if <26.3 {
import com.finndog.moogs_structures.modinit.MoogsStructuresPlacements;
//?}
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
//? if <26.3 {
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
//?}

//? if <26.3 {
import java.util.stream.Stream;
//?} else {
/*import java.util.function.Consumer;
*///?}

// 26.3: PlacementModifier is an interface that pushes positions to a consumer instead of
// returning a stream, and the type registry holds MapCodecs directly (codec() replaces type()).
//? if <26.3 {
public class MinusEightPlacement extends PlacementModifier {
//?} else {
/*public class MinusEightPlacement implements PlacementModifier {
*///?}
	private static final MinusEightPlacement INSTANCE = new MinusEightPlacement();
	public static final MapCodec<MinusEightPlacement> CODEC = MapCodec.unit(() -> INSTANCE);

	public static MinusEightPlacement subtractedEight() {
		return INSTANCE;
	}

	@Override
	//? if <26.3 {
	public Stream<BlockPos> getPositions(PlacementContext placementContext, RandomSource random, BlockPos blockPos) {
		return Stream.of(new BlockPos(blockPos.getX() - 8, blockPos.getY(), blockPos.getZ() - 8));
	//?} else {
	/*public void modify(PlacementContext placementContext, RandomSource random, BlockPos blockPos, Consumer<BlockPos> output) {
		output.accept(new BlockPos(blockPos.getX() - 8, blockPos.getY(), blockPos.getZ() - 8));
	*///?}
	}

	@Override
	//? if <26.3 {
	public PlacementModifierType<?> type() {
		return MoogsStructuresPlacements.MINUS_EIGHT_PLACEMENT.get();
	//?} else {
	/*public MapCodec<MinusEightPlacement> codec() {
		return CODEC;
	*///?}
	}
}
