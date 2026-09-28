package com.finndog.moogs_structures.world.placements;

//? if <26.3 {
import com.finndog.moogs_structures.modinit.MoogsStructuresPlacements;
//?}
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
//? if >=26.1.2 {
/*import net.minecraft.util.valueproviders.IntProviders;
*///?}
//? if <26.3 {
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
//?}
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;

//? if <26.3 {
public class UnlimitedCountPlacement extends RepeatingPlacement {
//?} else {
/*public class UnlimitedCountPlacement implements RepeatingPlacement {
*///?}
    //? if <26.1.2 {
    public static final MapCodec<UnlimitedCountPlacement> CODEC = IntProvider.NON_NEGATIVE_CODEC.fieldOf("count").xmap(UnlimitedCountPlacement::new, countPlacement -> countPlacement.count);
    //?} else {
    /*public static final MapCodec<UnlimitedCountPlacement> CODEC = IntProviders.codec(0, Integer.MAX_VALUE).fieldOf("count").xmap(UnlimitedCountPlacement::new, countPlacement -> countPlacement.count);
    *///?}
    private final IntProvider count;

    private UnlimitedCountPlacement(IntProvider intProvider) {
        this.count = intProvider;
    }

    public static UnlimitedCountPlacement of(IntProvider intProvider) {
        return new UnlimitedCountPlacement(intProvider);
    }

    public static UnlimitedCountPlacement of(int i) {
        return of(ConstantInt.of(i));
    }

    @Override
    //? if <26.3 {
    protected int count(RandomSource random, BlockPos blockPos) {
    //?} else {
    /*public int count(RandomSource random, BlockPos blockPos) {
    *///?}
        return this.count.sample(random);
    }

    @Override
    //? if <26.3 {
    public PlacementModifierType<?> type() {
        return MoogsStructuresPlacements.UNLIMITED_COUNT.get();
    //?} else {
    /*public MapCodec<UnlimitedCountPlacement> codec() {
        return CODEC;
    *///?}
    }
}
