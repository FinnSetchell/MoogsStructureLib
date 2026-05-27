package com.finndog.moogs_structures.mixins.terrainadaptation;

import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Beardifier.class)
public interface BeardifierAccessor {
    @Accessor
    List<Beardifier.Rigid> getPieces();

    @Accessor
    List<JigsawJunction> getJunctions();

    @Accessor
    BoundingBox getAffectedBox();
}
