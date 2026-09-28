package com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier;

//? if <26.1.2 {
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
//?} else {
/*import it.unimi.dsi.fastutil.objects.ObjectList;
*///?}

/**
 * Duck-typing interface added to vanilla's Beardifier via mixin, carrying the enhanced
 * (kernel-based) piece and junction iterators.
 */
public interface EnhancedBeardifierData {
    //? if <26.1.2 {
    ObjectListIterator<EnhancedBeardifierRigid> moogs_structures_getEnhancedPieceIterator();
    void moogs_structures_setEnhancedPieceIterator(ObjectListIterator<EnhancedBeardifierRigid> enhancedPieceIterator);
    //?} else {
    /*ObjectList<EnhancedBeardifierRigid> moogs_structures_getEnhancedPieces();
    void moogs_structures_setEnhancedPieces(ObjectList<EnhancedBeardifierRigid> pieces);
    *///?}

    //? if <26.1.2 {
    ObjectListIterator<EnhancedJigsawJunction> moogs_structures_getEnhancedJunctionIterator();
    void moogs_structures_setEnhancedJunctionIterator(ObjectListIterator<EnhancedJigsawJunction> enhancedJunctionIterator);
    //?} else {
    /*ObjectList<EnhancedJigsawJunction> moogs_structures_getEnhancedJunctions();
    void moogs_structures_setEnhancedJunctions(ObjectList<EnhancedJigsawJunction> junctions);
    *///?}
}
