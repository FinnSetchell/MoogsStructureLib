package com.finndog.moogs_structures.world.placements;

//? if <26.3 {
import com.finndog.moogs_structures.modinit.MoogsStructuresPlacements;
//?}
//? if >=1.20.6 {
import com.mojang.serialization.MapCodec;
//?} else {
/*import com.mojang.serialization.Codec;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

//? if <26.3 {
public class SnapToLowerNonAirPlacement extends PlacementModifier {
//?} else {
/*public class SnapToLowerNonAirPlacement implements PlacementModifier {
*///?}
	private static final SnapToLowerNonAirPlacement INSTANCE = new SnapToLowerNonAirPlacement();
	//? if >=1.20.6 {
	public static final MapCodec<SnapToLowerNonAirPlacement> CODEC = MapCodec.unit(() -> INSTANCE);
	//?} else {
	/*public static final Codec<SnapToLowerNonAirPlacement> CODEC = Codec.unit(() -> INSTANCE);
	*///?}

	public static SnapToLowerNonAirPlacement snapToLowerNonAir() {
		return INSTANCE;
	}

	@Override
	//? if <26.3 {
	public final Stream<BlockPos> getPositions(PlacementContext placementContext, RandomSource random, BlockPos blockPos) {
	//?} else {
	/*public void modify(PlacementContext placementContext, RandomSource random, BlockPos blockPos, Consumer<BlockPos> output) {
	*///?}
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(blockPos);
		//? if <26.3 {
		while(placementContext.getBlockState(mutable).isAir() && mutable.getY() > placementContext.getMinGenY()) {
		//?} else {
		/*while(placementContext.getBlockState(mutable).isAir() && mutable.getY() > placementContext.getMinY()) {
		*///?}
			mutable.move(Direction.DOWN);
		}
		//? if <26.3 {
		return Stream.of(mutable.immutable());
		//?} else {
		/*output.accept(mutable.immutable());
		*///?}
	}

	@Override
	//? if <26.3 {
	public PlacementModifierType<?> type() {
		return MoogsStructuresPlacements.SNAP_TO_LOWER_NON_AIR_PLACEMENT.get();
	//?} else {
	/*public MapCodec<SnapToLowerNonAirPlacement> codec() {
		return CODEC;
	*///?}
	}
}