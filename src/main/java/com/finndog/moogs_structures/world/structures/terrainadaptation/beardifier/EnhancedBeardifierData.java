package com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier;

//? if <26.1.2 {
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
//?} else {
/*import it.unimi.dsi.fastutil.objects.ObjectList;
*///?}
//? if <1.20.6 {
/*import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
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

    /** The lowest row the carve must not touch (the Nether roof's rows), or Integer.MAX_VALUE when there is no roof. */
    int moogs_structures_getRoofFloorY();
    void moogs_structures_setRoofFloorY(int roofFloorY);

    // 1.20 to 1.20.4: pieces and junctions of MSL structures that ask for encapsulate (EncapsulateBackport).
    // Kept as lists and walked with local cursors, so no shared iterator is advanced across threads.
    //? if <1.20.6 {
    /*ObjectList<BoundingBox> moogs_structures_getEncapsulatedPieces();
    ObjectList<JigsawJunction> moogs_structures_getEncapsulatedJunctions();
    void moogs_structures_setEncapsulated(ObjectList<BoundingBox> pieces, ObjectList<JigsawJunction> junctions);
    *///?}
}
