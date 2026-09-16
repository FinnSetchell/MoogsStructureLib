package com.finndog.moogs_structures.mixins.features;

import com.finndog.moogs_structures.modinit.MoogsStructuresTags;
import com.finndog.moogs_structures.utils.MixinUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.DeltaFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 26.3 folded FeaturePlaceContext into place()'s parameters; the origin is the BlockPos argument.
@Mixin(DeltaFeature.class)
public class NoDeltasInStructuresMixin {
    @Inject(
            method = "place(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void moogs_structures_noDeltasInStructures(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin, CallbackInfoReturnable<Boolean> cir) {
        if (!(level instanceof WorldGenRegion worldGenRegion)) return;

        if (MixinUtils.isPositionInTaggedStructure(worldGenRegion, origin, MoogsStructuresTags.NO_DELTA)) {
            cir.setReturnValue(false);
        }
    }
}
