package com.finndog.moogs_structures.world.structures.terrainadaptation;

/**
 * Implemented by structures that expose an {@link EnhancedTerrainAdaptation}. The Beardifier mixin
 * checks for this interface to apply enhanced (kernel-based) terrain carving/bearding.
 */
public interface EnhancedTerrainAdaptationStructure {
    EnhancedTerrainAdaptation getEnhancedTerrainAdaptation();
}
