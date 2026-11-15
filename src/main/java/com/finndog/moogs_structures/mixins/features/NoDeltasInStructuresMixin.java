package com.finndog.moogs_structures.mixins.features;

import com.finndog.moogs_structures.modinit.MoogsStructuresTags;
import com.finndog.moogs_structures.utils.MixinUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
//? if >=26.3 {
/*import net.minecraft.util.RandomSource;
*///?}
import net.minecraft.util.valueproviders.IntProvider;
//? if >=26.3 {
/*import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
*///?}
import net.minecraft.world.level.levelgen.feature.DeltaFeature;
//? if <26.3 {
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;
//?}
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DeltaFeature.class)
public class NoDeltasInStructuresMixin {
    @Inject(
            //? if <26.3 {
            method = "place(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z",
            //?} else {
            /*method = "place(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z",
            *///?}
            at = @At(value = "HEAD"),
            cancellable = true
    )
    //? if <26.3 {
    private void moogs_structures_noDeltasInStructures(FeaturePlaceContext<DeltaFeatureConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        if (!(context.level() instanceof WorldGenRegion worldGenRegion)) return;
        BlockPos origin = context.origin();
        IntProvider size = context.config().size();
        IntProvider rimSize = context.config().rimSize();
    //?} else {
    /*private void moogs_structures_noDeltasInStructures(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin, CallbackInfoReturnable<Boolean> cir) {
        if (!(level instanceof WorldGenRegion worldGenRegion)) return;
        IntProvider size = ((DeltaFeature) (Object) this).size();
        IntProvider rimSize = ((DeltaFeature) (Object) this).rimSize();
    *///?}

        // A delta only writes at its origin's height: up to `size` blocks out each way, with its
        // contents shifted up to `rim_size` blocks further along +x and +z. Cancel it if any of that
        // would come near the structure, not just if its origin does.
        int radius = MixinUtils.maxOf(size);
        int shift = MixinUtils.maxOf(rimSize);
        BoundingBox reach = new BoundingBox(
                origin.getX() - radius, origin.getY(), origin.getZ() - radius,
                origin.getX() + radius + shift, origin.getY(), origin.getZ() + radius + shift);
        if (MixinUtils.isAreaNearTaggedStructure(worldGenRegion, reach, MoogsStructuresTags.NO_DELTA)) {
            cir.setReturnValue(false);
        }
    }
}
