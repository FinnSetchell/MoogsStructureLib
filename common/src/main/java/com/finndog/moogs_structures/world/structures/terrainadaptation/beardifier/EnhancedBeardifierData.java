package com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier;

import it.unimi.dsi.fastutil.objects.ObjectListIterator;

/**
 * Duck-typing interface added to vanilla's Beardifier via mixin, carrying the enhanced
 * (kernel-based) piece and junction iterators.
 */
public interface EnhancedBeardifierData {
    ObjectListIterator<EnhancedBeardifierRigid> getEnhancedPieceIterator();
    void setEnhancedPieceIterator(ObjectListIterator<EnhancedBeardifierRigid> enhancedPieceIterator);

    ObjectListIterator<EnhancedJigsawJunction> getEnhancedJunctionIterator();
    void setEnhancedJunctionIterator(ObjectListIterator<EnhancedJigsawJunction> enhancedJunctionIterator);
}
