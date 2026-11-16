package com.finndog.moogs_structures.mixins.features;

import com.finndog.moogs_structures.modinit.MoogsStructuresTags;
import com.finndog.moogs_structures.utils.MixinUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
//? if <26.3 {
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.feature.BasaltColumnsFeature;
//?} else {
/*import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.SteppedColumnClusterFeature;
*///?}
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 26.3 renamed basalt_columns to stepped_column_cluster and made the feature a record. Same hook, same tag.
//? if <26.3 {
@Mixin(BasaltColumnsFeature.class)
//?} else {
/*@Mixin(SteppedColumnClusterFeature.class)
*///?}
public class NoBasaltColumnsInStructuresMixin {
    // placeColumn writes every block of a cluster, so checking each call's whole reach also covers
    // the columns that climb up through solid ground to find air, which never pass the feature's own
    // floor check, and columns that rise into a structure from below.
    @Inject(
            //? if <26.3 {
            method = "placeColumn(Lnet/minecraft/world/level/LevelAccessor;ILnet/minecraft/core/BlockPos;II)Z",
            //?} else {
            /*method = "placeColumn(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/util/RandomSource;ILnet/minecraft/core/BlockPos;II)Z",
            *///?}
            at = @At(value = "HEAD"),
            cancellable = true
    )
    //? if <26.3 {
    private void moogs_structures_noBasaltColumnsInStructures(LevelAccessor levelAccessor, int seaLevel, BlockPos pos, int columnHeight, int reach, CallbackInfoReturnable<Boolean> cir) {
        if (!(levelAccessor instanceof WorldGenRegion worldGenRegion)) return;
    //?} else {
    /*private void moogs_structures_noBasaltColumnsInStructures(WorldGenLevel level, RandomSource random, int lavaSeaLevel, BlockPos pos, int columnHeight, int reach, CallbackInfoReturnable<Boolean> cir) {
        if (!(level instanceof WorldGenRegion worldGenRegion)) return;
    *///?}

        // Columns fill the square `reach` blocks round pos. Each starts where it finds a floor or air,
        // less than 2 * reach blocks above or below pos, and rises at most columnHeight blocks.
        BoundingBox columns = new BoundingBox(
                pos.getX() - reach, pos.getY() - 2 * reach, pos.getZ() - reach,
                pos.getX() + reach, pos.getY() + 2 * reach + columnHeight, pos.getZ() + reach);
        if (MixinUtils.isAreaNearTaggedStructure(worldGenRegion, columns, MoogsStructuresTags.NO_BASALT)) {
            cir.setReturnValue(false);
        }
    }
}
