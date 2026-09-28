package com.finndog.moogs_structures.mixins.terrainadaptation;

//? if <1.21.5 {
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
//?}
import net.minecraft.world.level.levelgen.Beardifier;
//? if <1.21.5 {
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
//?}
//? if >=1.21.11 {
/*import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
*///?}
import org.spongepowered.asm.mixin.Mixin;
//? if <1.21.5 {
import org.spongepowered.asm.mixin.gen.Accessor;
//?}
//? if >=26.1.2 {
/*import org.spongepowered.asm.mixin.Mutable;
*///?}
//? if >=1.21.11 {
/*import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
*///?}

@Mixin(Beardifier.class)
public interface BeardifierAccessor {
    //? if <1.21.5 {
    @Accessor
    ObjectListIterator<Beardifier.Rigid> getPieceIterator();

    @Accessor
    ObjectListIterator<JigsawJunction> getJunctionIterator();
    //?}
    //? if >=1.21.11 {
    /*@Accessor
    List<Beardifier.Rigid> getPieces();

    @Accessor
    List<JigsawJunction> getJunctions();

    @Accessor
    BoundingBox getAffectedBox();
    *///?}

    //? if >=26.1.2 {
    /*@Mutable
    @Accessor("affectedBox")
    void setAffectedBox(BoundingBox affectedBox);
    *///?}
}
