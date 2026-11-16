//? if <26.3 {
package com.finndog.moogs_structures.mixins.structures;

import com.finndog.moogs_structures.world.structures.HangingRockGuard;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Every carver (cave, Nether cave, canyon) cuts through {@code WorldCarver.carveEllipsoid}, which skips a block the
 * carving mask already has. A block a {@link HangingRockGuard} keeps counts as already carved, so it is neither cut nor
 * added to the mask: the mask stays a true record of what was cut. Not on 26.3, where the carvers write into a
 * carving record instead ({@code HangingRockCarversMixin}).
 */
@Mixin(WorldCarver.class)
public class HangingRockWorldCarverMixin {

    @WrapOperation(
            method = "carveEllipsoid",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/CarvingMask;get(III)Z")
    )
    private boolean moogs_structures_keepHangingRock(CarvingMask mask, int x, int y, int z, Operation<Boolean> original,
                                                     @Local(argsOnly = true) ChunkAccess chunk) {
        return original.call(mask, x, y, z) || HangingRockGuard.keepsWhileCarving(chunk.getPos(), x, y, z);
    }
}
//?}
