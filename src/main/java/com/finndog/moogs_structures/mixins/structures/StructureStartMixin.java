//? if <1.21.6 {
package com.finndog.moogs_structures.mixins.structures;

import com.finndog.moogs_structures.world.structures.PlacingStructure;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Notes which structure is placing its pieces ({@link PlacingStructure}), so MSL's placement fixes only ever
 * touch MSL structures. Only on versions that need one: rails the game turns the wrong way, and liquid
 * settings before 1.21.
 */
@Mixin(StructureStart.class)
public abstract class StructureStartMixin {

    @Shadow
    public abstract Structure getStructure();

    @Inject(method = "placeInChunk", at = @At("HEAD"))
    private void moogs_structures$startPlacing(CallbackInfo ci) {
        PlacingStructure.start(this.getStructure());
    }

    @Inject(method = "placeInChunk", at = @At("RETURN"))
    private void moogs_structures$stopPlacing(CallbackInfo ci) {
        PlacingStructure.stop();
    }
}
//?}
