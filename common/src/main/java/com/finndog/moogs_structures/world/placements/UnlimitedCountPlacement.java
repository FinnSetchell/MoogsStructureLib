package com.finndog.moogs_structures.world.placements;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;

public class UnlimitedCountPlacement implements RepeatingPlacement {
    public static final MapCodec<UnlimitedCountPlacement> CODEC = IntProviders.codec(0, Integer.MAX_VALUE).fieldOf("count").xmap(UnlimitedCountPlacement::new, countPlacement -> countPlacement.count);
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
    public int count(RandomSource random, BlockPos blockPos) {
        return this.count.sample(random);
    }

    @Override
    public MapCodec<UnlimitedCountPlacement> codec() {
        return CODEC;
    }
}
