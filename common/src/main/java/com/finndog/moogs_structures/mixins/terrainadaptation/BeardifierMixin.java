package com.finndog.moogs_structures.mixins.terrainadaptation;

import com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier.EnhancedBeardifierData;
import com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier.EnhancedBeardifierHelper;
import com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier.EnhancedBeardifierRigid;
import com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier.EnhancedJigsawJunction;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Injects enhanced (kernel-based) terrain adaptation behavior into vanilla's Beardifier.
 * Reduced port of YUNG's API BeardifierMixin (no aquifer-override / NoiseChunk handling).
 */
@Mixin(Beardifier.class)
public class BeardifierMixin implements EnhancedBeardifierData {
    @Unique
    private ObjectListIterator<EnhancedJigsawJunction> moogs_structures_enhancedJunctionIterator;

    @Unique
    private ObjectListIterator<EnhancedBeardifierRigid> moogs_structures_enhancedPieceIterator;

    @Inject(method = "forStructuresInChunk", at = @At("RETURN"), cancellable = true, remap = false)
    private static void moogs_structures_supportEnhancedTerrainAdaptations(StructureManager structureManager, ChunkPos chunkPos, CallbackInfoReturnable<Beardifier> cir) {
        Beardifier enhancedBeardifier = EnhancedBeardifierHelper.forStructuresInChunk(structureManager, chunkPos, cir.getReturnValue());
        cir.setReturnValue(enhancedBeardifier);
    }

    @Inject(method = "compute", at = @At("RETURN"), cancellable = true, remap = false)
    private void moogs_structures_calculateDensity(DensityFunction.FunctionContext ctx, CallbackInfoReturnable<Double> cir) {
        double density = cir.getReturnValue();
        double newDensity = EnhancedBeardifierHelper.computeDensity(ctx, density, this);
        cir.setReturnValue(newDensity);
    }

    @Unique
    @Override
    public ObjectListIterator<EnhancedBeardifierRigid> getEnhancedPieceIterator() {
        return this.moogs_structures_enhancedPieceIterator;
    }

    @Unique
    @Override
    public void setEnhancedPieceIterator(ObjectListIterator<EnhancedBeardifierRigid> enhancedPieceIterator) {
        this.moogs_structures_enhancedPieceIterator = enhancedPieceIterator;
    }

    @Unique
    @Override
    public ObjectListIterator<EnhancedJigsawJunction> getEnhancedJunctionIterator() {
        return this.moogs_structures_enhancedJunctionIterator;
    }

    @Unique
    @Override
    public void setEnhancedJunctionIterator(ObjectListIterator<EnhancedJigsawJunction> enhancedJunctionIterator) {
        this.moogs_structures_enhancedJunctionIterator = enhancedJunctionIterator;
    }
}
