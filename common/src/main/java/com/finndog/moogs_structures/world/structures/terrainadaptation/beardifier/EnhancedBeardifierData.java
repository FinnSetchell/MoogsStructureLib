package com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier;

import it.unimi.dsi.fastutil.objects.ObjectListIterator;

/**
 * Duck-typing interface added to vanilla's Beardifier via mixin, carrying the enhanced
 * (kernel-based) piece and junction iterators.
 */
public interface EnhancedBeardifierData {
    ObjectListIterator<EnhancedBeardifierRigid> moogs_structures_getEnhancedPieceIterator();
    void moogs_structures_setEnhancedPieceIterator(ObjectListIterator<EnhancedBeardifierRigid> enhancedPieceIterator);

    ObjectListIterator<EnhancedJigsawJunction> moogs_structures_getEnhancedJunctionIterator();
    void moogs_structures_setEnhancedJunctionIterator(ObjectListIterator<EnhancedJigsawJunction> enhancedJunctionIterator);
}
