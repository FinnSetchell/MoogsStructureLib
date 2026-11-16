package com.finndog.moogs_structures.world.structures.terrainadaptation;

import java.util.Optional;

public interface PoolElementAdaptationOverride {
    Optional<EnhancedTerrainAdaptation> moogs_structures_getAdaptationOverride();

    /** The columns enhanced adaptation is limited to; empty means the piece's whole bounding box. */
    default Optional<AdaptationArea> moogs_structures_getAdaptationArea() {
        return Optional.empty();
    }
}
