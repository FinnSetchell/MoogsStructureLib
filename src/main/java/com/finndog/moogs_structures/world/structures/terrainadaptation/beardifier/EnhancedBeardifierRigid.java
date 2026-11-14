package com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier;

import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Equivalent to vanilla's Beardifier.Rigid, but carrying an {@link EnhancedTerrainAdaptation}.
 * {@code areas}, when set, limits the shaping to those columns (world space) instead of the whole box.
 */
public record EnhancedBeardifierRigid(BoundingBox pieceBoundingBox,
                                      EnhancedTerrainAdaptation pieceTerrainAdaptation,
                                      int pieceGroundLevelDelta,
                                      Rotation rotation,
                                      @Nullable List<BoundingBox> areas) {
}
