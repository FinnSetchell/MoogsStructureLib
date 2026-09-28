package com.finndog.moogs_structures.mixins.features;

import com.finndog.moogs_structures.modinit.MoogsStructuresTags;
import com.finndog.moogs_structures.utils.MixinUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
//? if <26.3 {
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.feature.BasaltColumnsFeature;
//?} else {
/*import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.SteppedColumnClusterFeature;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 26.3 renamed basalt_columns to stepped_column_cluster and made the feature a record, so the
// placement check is an instance method now. Same hook, same tag.
//? if <26.3 {
@Mixin(BasaltColumnsFeature.class)
//?} else {
/*@Mixin(SteppedColumnClusterFeature.class)
*///?}
public class NoBasaltColumnsInStructuresMixin {
    @Inject(
            //? if <26.3 {
            method = "canPlaceAt(Lnet/minecraft/world/level/LevelAccessor;ILnet/minecraft/core/BlockPos$MutableBlockPos;)Z",
            //?} else {
            /*method = "canPlaceAt(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/core/BlockPos$MutableBlockPos;)Z",
            *///?}
            at = @At(value = "HEAD"),
            cancellable = true
    )
    //? if <26.3 {
    private static void moogs_structures_noBasaltColumnsInStructures(LevelAccessor levelAccessor, int seaLevel, BlockPos.MutableBlockPos mutableBlockPos, CallbackInfoReturnable<Boolean> cir) {
        if (!(levelAccessor instanceof WorldGenRegion worldGenRegion)) return;
    //?} else {
    /*private void moogs_structures_noBasaltColumnsInStructures(WorldGenLevel level, BlockPos.MutableBlockPos mutableBlockPos, CallbackInfoReturnable<Boolean> cir) {
        if (!(level instanceof WorldGenRegion worldGenRegion)) return;
    *///?}

        if (MixinUtils.isPositionInTaggedStructure(worldGenRegion, mutableBlockPos, MoogsStructuresTags.NO_BASALT)) {
            cir.setReturnValue(false);
        }
    }
}
