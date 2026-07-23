package com.finndog.moogs_structures.mixins.terrainadaptation;

import com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier.EnhancedBeardifierData;
import com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier.EnhancedBeardifierHelper;
import com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier.EnhancedBeardifierRigid;
import com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier.EnhancedJigsawJunction;
import it.unimi.dsi.fastutil.objects.ObjectList;
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
// Priority 1500 so this applies after YUNG's API's BeardifierMixin (default 1000): both mods
// hook forStructuresInChunk at RETURN, so our handler must run on whatever instance YUNG's
// handler produced — see EnhancedBeardifierHelper.forStructuresInChunk.
@Mixin(value = Beardifier.class, priority = 1500)
public class BeardifierMixin implements EnhancedBeardifierData {
    // Lists, not iterators: forStructuresInChunk builds these once per chunk, but compute() runs
    // on world-gen worker threads and re-entrantly per noise cell. A stored cursor would be shared
    // mutable state — hasNext() could pass and next() then throw NoSuchElementException once another
    // thread drained it. computeDensity() iterates these locally instead, like modern vanilla does.
    @Unique
    private ObjectList<EnhancedJigsawJunction> moogs_structures_enhancedJunctions;

    @Unique
    private ObjectList<EnhancedBeardifierRigid> moogs_structures_enhancedPieces;

    @Inject(method = "forStructuresInChunk", at = @At("RETURN"), cancellable = true)
    private static void moogs_structures_supportEnhancedTerrainAdaptations(StructureManager structureManager, ChunkPos chunkPos, CallbackInfoReturnable<Beardifier> cir) {
        Beardifier enhancedBeardifier = EnhancedBeardifierHelper.forStructuresInChunk(structureManager, chunkPos, cir.getReturnValue());
        cir.setReturnValue(enhancedBeardifier);
    }

    @Inject(method = "compute", at = @At("RETURN"), cancellable = true)
    private void moogs_structures_calculateDensity(DensityFunction.FunctionContext ctx, CallbackInfoReturnable<Double> cir) {
        double density = cir.getReturnValue();
        double newDensity = EnhancedBeardifierHelper.computeDensity(ctx, density, this);
        cir.setReturnValue(newDensity);
    }

    @Unique
    @Override
    public ObjectList<EnhancedBeardifierRigid> moogs_structures_getEnhancedPieces() {
        return this.moogs_structures_enhancedPieces;
    }

    @Unique
    @Override
    public void moogs_structures_setEnhancedPieces(ObjectList<EnhancedBeardifierRigid> pieces) {
        this.moogs_structures_enhancedPieces = pieces;
    }

    @Unique
    @Override
    public ObjectList<EnhancedJigsawJunction> moogs_structures_getEnhancedJunctions() {
        return this.moogs_structures_enhancedJunctions;
    }

    @Unique
    @Override
    public void moogs_structures_setEnhancedJunctions(ObjectList<EnhancedJigsawJunction> junctions) {
        this.moogs_structures_enhancedJunctions = junctions;
    }
}
