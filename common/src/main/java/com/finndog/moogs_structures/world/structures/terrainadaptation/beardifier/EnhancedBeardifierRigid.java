package com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier;

import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Equivalent to vanilla's Beardifier.Rigid, but carrying an {@link EnhancedTerrainAdaptation}.
 */
public record EnhancedBeardifierRigid(BoundingBox pieceBoundingBox,
                                      EnhancedTerrainAdaptation pieceTerrainAdaptation,
                                      int pieceGroundLevelDelta,
                                      Rotation rotation) {
}
