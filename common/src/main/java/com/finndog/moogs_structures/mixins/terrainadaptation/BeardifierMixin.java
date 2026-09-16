package com.finndog.moogs_structures.mixins.terrainadaptation;

import com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier.EnhancedBeardifierData;
import com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier.EnhancedBeardifierHelper;
import com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier.EnhancedBeardifierRigid;
import com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier.EnhancedJigsawJunction;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.Beardifier;
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
    // Lists, not iterators: forStructuresInChunk builds these once per chunk, but sampling runs
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

    // 26.3: the Beardifier is a DensitySampler rather than a DensityFunction. Both of its entry
    // points (sampleValue for single points, sampleVolume for whole noise cells) funnel through
    // sampleValueUnchecked, and both are gated on affectedBox first — which forStructuresInChunk
    // above already widens to cover the enhanced pieces.
    @Inject(method = "sampleValueUnchecked", at = @At("RETURN"), cancellable = true)
    private void moogs_structures_calculateDensity(int blockX, int blockY, int blockZ, CallbackInfoReturnable<Float> cir) {
        float density = cir.getReturnValue();
        double newDensity = EnhancedBeardifierHelper.computeDensity(blockX, blockY, blockZ, density, this);
        cir.setReturnValue((float) newDensity);
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
