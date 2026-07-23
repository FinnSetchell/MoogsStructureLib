package com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier;

import it.unimi.dsi.fastutil.objects.ObjectList;

/**
 * Duck-typing interface added to vanilla's Beardifier via mixin, carrying the enhanced
 * (kernel-based) piece and junction lists.
 * <p>
 * These are immutable lists rather than iterators on purpose: a Beardifier is evaluated
 * concurrently by world-gen worker threads, so any shared cursor state would be advanced
 * out from under a running compute(). Each computeDensity() call iterates locally instead.
 */
public interface EnhancedBeardifierData {
    ObjectList<EnhancedBeardifierRigid> moogs_structures_getEnhancedPieces();
    void moogs_structures_setEnhancedPieces(ObjectList<EnhancedBeardifierRigid> pieces);

    ObjectList<EnhancedJigsawJunction> moogs_structures_getEnhancedJunctions();
    void moogs_structures_setEnhancedJunctions(ObjectList<EnhancedJigsawJunction> junctions);
}
