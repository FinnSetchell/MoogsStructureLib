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

@Mixin(Beardifier.class)
public interface BeardifierAccessor {
    //? if >=1.21.10 {
    /*@Accessor
    BoundingBox getAffectedBox();

    @Mutable
    @Accessor("affectedBox")
    void setAffectedBox(BoundingBox affectedBox);
    *///?}
}
