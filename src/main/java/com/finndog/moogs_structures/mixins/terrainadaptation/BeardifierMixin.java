package com.finndog.moogs_structures.mixins.terrainadaptation;

import com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier.EnhancedBeardifierData;
import com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier.EnhancedBeardifierHelper;
import com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier.EnhancedBeardifierRigid;
import com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier.EnhancedJigsawJunction;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
//? if <26.1.2 {
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
//?} else {
/*import it.unimi.dsi.fastutil.objects.ObjectList;
*///?}
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.Beardifier;
//? if <26.3 {
import net.minecraft.world.level.levelgen.DensityFunction;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects enhanced (kernel-based) terrain adaptation behavior into vanilla's Beardifier.
 * Reduced port of YUNG's API BeardifierMixin (no aquifer-override / NoiseChunk handling).
 */
// Wrapped rather than injected at RETURN. YUNG's API cancels at RETURN, so whichever of the two ran
// first switched the other off. Priority below YUNG's keeps this wrapper innermost, so swapping out
// vanilla's shared EMPTY happens before any other mod has written to it.
@Mixin(value = Beardifier.class, priority = 900)
public class BeardifierMixin implements EnhancedBeardifierData {
    @Unique
    //? if <26.1.2 {
    private ObjectListIterator<EnhancedJigsawJunction> moogs_structures_enhancedJunctionIterator;
    //?} else {
    /*private ObjectList<EnhancedJigsawJunction> moogs_structures_enhancedJunctions;
    *///?}

    @Unique
    //? if <26.1.2 {
    private ObjectListIterator<EnhancedBeardifierRigid> moogs_structures_enhancedPieceIterator;
    //?} else {
    /*private ObjectList<EnhancedBeardifierRigid> moogs_structures_enhancedPieces;
    *///?}

    @WrapMethod(method = "forStructuresInChunk")
    private static Beardifier moogs_structures_supportEnhancedTerrainAdaptations(StructureManager structureManager, ChunkPos chunkPos, Operation<Beardifier> original) {
        return EnhancedBeardifierHelper.forStructuresInChunk(structureManager, chunkPos, original.call(structureManager, chunkPos));
    }

    // 26.3: the Beardifier is a DensitySampler rather than a DensityFunction. Both of its entry
    // points (sampleValue for single points, sampleVolume for whole noise cells) funnel through
    // sampleValueUnchecked, and both are gated on affectedBox first — which forStructuresInChunk
    // above already widens to cover the enhanced pieces.
    //? if <26.3 {
    @WrapMethod(method = "compute")
    private double moogs_structures_calculateDensity(DensityFunction.FunctionContext ctx, Operation<Double> original) {
        return EnhancedBeardifierHelper.computeDensity(ctx, original.call(ctx), this);
    //?} else {
    /*@WrapMethod(method = "sampleValueUnchecked")
    private float moogs_structures_calculateDensity(int blockX, int blockY, int blockZ, Operation<Float> original) {
        return (float) EnhancedBeardifierHelper.computeDensity(blockX, blockY, blockZ, original.call(blockX, blockY, blockZ), this);
    *///?}
    }

    @Unique
    @Override
    //? if <26.1.2 {
    public ObjectListIterator<EnhancedBeardifierRigid> moogs_structures_getEnhancedPieceIterator() {
        return this.moogs_structures_enhancedPieceIterator;
    //?} else {
    /*public ObjectList<EnhancedBeardifierRigid> moogs_structures_getEnhancedPieces() {
        return this.moogs_structures_enhancedPieces;
    *///?}
    }

    @Unique
    @Override
    //? if <26.1.2 {
    public void moogs_structures_setEnhancedPieceIterator(ObjectListIterator<EnhancedBeardifierRigid> enhancedPieceIterator) {
        this.moogs_structures_enhancedPieceIterator = enhancedPieceIterator;
    //?} else {
    /*public void moogs_structures_setEnhancedPieces(ObjectList<EnhancedBeardifierRigid> pieces) {
        this.moogs_structures_enhancedPieces = pieces;
    *///?}
    }

    @Unique
    @Override
    //? if <26.1.2 {
    public ObjectListIterator<EnhancedJigsawJunction> moogs_structures_getEnhancedJunctionIterator() {
        return this.moogs_structures_enhancedJunctionIterator;
    //?} else {
    /*public ObjectList<EnhancedJigsawJunction> moogs_structures_getEnhancedJunctions() {
        return this.moogs_structures_enhancedJunctions;
    *///?}
    }

    @Unique
    @Override
    //? if <26.1.2 {
    public void moogs_structures_setEnhancedJunctionIterator(ObjectListIterator<EnhancedJigsawJunction> enhancedJunctionIterator) {
        this.moogs_structures_enhancedJunctionIterator = enhancedJunctionIterator;
    //?} else {
    /*public void moogs_structures_setEnhancedJunctions(ObjectList<EnhancedJigsawJunction> junctions) {
        this.moogs_structures_enhancedJunctions = junctions;
    *///?}
    }
}
