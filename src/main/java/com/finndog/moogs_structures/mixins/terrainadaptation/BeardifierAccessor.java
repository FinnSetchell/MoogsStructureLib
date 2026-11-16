package com.finndog.moogs_structures.mixins.terrainadaptation;

import net.minecraft.world.level.levelgen.Beardifier;
//? if >=1.21.10 {
/*import net.minecraft.world.level.levelgen.structure.BoundingBox;
*///?}
import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.10 {
/*import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
*///?}
//? if <1.20.6 {
/*import org.spongepowered.asm.mixin.gen.Invoker;
*///?}

@Mixin(Beardifier.class)
public interface BeardifierAccessor {
    //? if <1.20.6 {
    /*/^* The game's beard kernel at a jigsaw junction, for the encapsulate backport (EncapsulateBackport). ^/
    @Invoker("getBeardContribution")
    static double moogs_structures_getBeardContribution(int x, int y, int z, int yToGround) {
        throw new AssertionError();
    }
    *///?}

    //? if >=1.21.10 {
    /*@Accessor
    BoundingBox getAffectedBox();

    @Mutable
    @Accessor("affectedBox")
    void setAffectedBox(BoundingBox affectedBox);
    *///?}
}
