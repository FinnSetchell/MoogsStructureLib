//? if <1.21.6 {
package com.finndog.moogs_structures.mixins.structures;

import com.finndog.moogs_structures.world.processors.RailRotationProcessor;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Notes whether the structure placing its pieces is one of MSL's own, so {@link RailRotationProcessor}
 * only ever touches MSL structures. Only on versions where the game turns some rails the wrong way.
 */
@Mixin(StructureStart.class)
public abstract class StructureStartMixin {

    @Shadow
    public abstract Structure getStructure();

    @Inject(method = "placeInChunk", at = @At("HEAD"))
    private void moogs_structures$startPlacing(CallbackInfo ci) {
        RailRotationProcessor.startPlacing(this.getStructure());
    }

    @Inject(method = "placeInChunk", at = @At("RETURN"))
    private void moogs_structures$stopPlacing(CallbackInfo ci) {
        RailRotationProcessor.stopPlacing();
    }
}
//?}
