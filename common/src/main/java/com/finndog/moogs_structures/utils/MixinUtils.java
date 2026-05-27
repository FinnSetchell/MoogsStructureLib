package com.finndog.moogs_structures.utils;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Helpers for structure-aware worldgen feature mixins.
 * Ported from the implementation shared by TelepathicGrunt (RepurposedStructures) and
 * YUNG's API, allowing features (basalt columns, deltas, etc.) to be cancelled inside
 * structures that carry a given structure tag.
 */
public final class MixinUtils {
    private MixinUtils() {}

    /**
     * How far (in blocks) beyond an individual structure piece's bounding box a feature is still
     * suppressed. Keeps features from clipping the structure while letting them generate naturally
     * in the open areas inside the structure's (often much larger) overall bounding box.
     */
    private static final int STRUCTURE_PIECE_MARGIN = 4;

    /**
     * Checks if the provided position is inside a structure that is tagged with the provided tag.
     */
    public static boolean isPositionInTaggedStructure(WorldGenRegion worldGenRegion, BlockPos pos, TagKey<Structure> structureTagKey) {
        Registry<Structure> structureRegistry = worldGenRegion.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        SectionPos sectionPos = SectionPos.of(pos);

        // Ensure chunk has generated structure references
        ChunkAccess chunkAccess = worldGenRegion.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES);
        if (!chunkAccess.getHighestGeneratedStatus().isOrAfter(ChunkStatus.STRUCTURE_REFERENCES)) return false;

        // Check all structures referenced by this chunk, and return true if any match the provided tag
        Map<Structure, LongSet> allReferencesInChunk = chunkAccess.getAllReferences();
        for (Map.Entry<Structure, LongSet> entry : allReferencesInChunk.entrySet()) {
            Structure structure = entry.getKey();
            LongSet references = entry.getValue();

            Optional<ResourceKey<Structure>> structureKey = structureRegistry.getResourceKey(structure);
            boolean isTaggedStructure = structureKey.isPresent() && structureRegistry.getOrThrow(structureKey.get()).is(structureTagKey);

            if (isTaggedStructure) {
                // Only suppress the feature where an actual piece is (its bounding box + a small
                // margin), NOT across the structure's whole bounding box. The overall box can be
                // huge and mostly empty, and suppressing it entirely leaves unnatural feature-free
                // voids. The cheap overall-box check short-circuits positions far outside.
                if (isAnyReferenceValidStartForStructure(worldGenRegion, structure, references, structureStart -> isPositionNearAnyPiece(structureStart, pos))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * True if the position is inside any individual piece of the structure start (inflated by
     * {@link #STRUCTURE_PIECE_MARGIN}). The overall bounding-box check short-circuits cheaply for
     * positions nowhere near the structure.
     */
    private static boolean isPositionNearAnyPiece(StructureStart structureStart, BlockPos pos) {
        if (!structureStart.getBoundingBox().isInside(pos)) return false;
        for (StructurePiece piece : structureStart.getPieces()) {
            if (piece.getBoundingBox().inflatedBy(STRUCTURE_PIECE_MARGIN).isInside(pos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if any of the references contain a valid structure start for the provided structure
     * that also passes the provided filter. Based on vanilla's StructureManager#fillStartsForStructure.
     */
    private static boolean isAnyReferenceValidStartForStructure(WorldGenRegion worldGenRegion, Structure structure, LongSet references, Predicate<StructureStart> filter) {
        StructureManager structureManager = worldGenRegion.getLevel().structureManager();

        for (long reference : references) {
            SectionPos structureStartSectionPos = SectionPos.of(new ChunkPos(reference), worldGenRegion.getMinSectionY());
            if (!worldGenRegion.hasChunk(structureStartSectionPos.x(), structureStartSectionPos.z())) {
                continue;
            }

            ChunkAccess structureStartChunkAccess = worldGenRegion.getChunk(structureStartSectionPos.x(), structureStartSectionPos.z(), ChunkStatus.STRUCTURE_STARTS);

            StructureStart structureStart = structureManager.getStartForStructure(structureStartSectionPos, structure, structureStartChunkAccess);
            if (structureStart != null && structureStart.isValid() && filter.test(structureStart)) {
                return true;
            }
        }

        return false;
    }
}
