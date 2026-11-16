package com.finndog.moogs_structures.utils;

import com.finndog.moogs_structures.world.processors.FollowFloorProcessor;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
//? if >=1.20.6 {
import net.minecraft.world.level.chunk.status.ChunkStatus;
//?} else {
/*import net.minecraft.world.level.chunk.ChunkStatus;
*///?}
import net.minecraft.world.level.levelgen.structure.BoundingBox;
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
     * Checks if any block of the area is near a piece of a structure that is tagged with the
     * provided tag. A feature passes in every block it could write, so one that would reach into
     * the structure is caught however far its own origin is from it.
     */
    public static boolean isAreaNearTaggedStructure(WorldGenRegion worldGenRegion, BoundingBox area, TagKey<Structure> structureTagKey) {
        //? if <1.21.2 {
        Registry<Structure> structureRegistry = worldGenRegion.registryAccess().registryOrThrow(Registries.STRUCTURE);
        //?} else {
        /*Registry<Structure> structureRegistry = worldGenRegion.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        *///?}
        BoundingBox grown = area.inflatedBy(STRUCTURE_PIECE_MARGIN);

        // A start is referenced by every chunk its box overlaps, so the chunks under the grown area
        // name every start with a piece near the area. Features only write within one chunk of the
        // chunk being decorated, and only those chunks have their structure references settled.
        ChunkPos center = worldGenRegion.getCenter();
        int centerX = SectionPos.blockToSectionCoord(center.getMinBlockX());
        int centerZ = SectionPos.blockToSectionCoord(center.getMinBlockZ());
        int minChunkX = Math.max(centerX - 1, SectionPos.blockToSectionCoord(grown.minX()));
        int maxChunkX = Math.min(centerX + 1, SectionPos.blockToSectionCoord(grown.maxX()));
        int minChunkZ = Math.max(centerZ - 1, SectionPos.blockToSectionCoord(grown.minZ()));
        int maxChunkZ = Math.min(centerZ + 1, SectionPos.blockToSectionCoord(grown.maxZ()));

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                ChunkAccess chunkAccess = worldGenRegion.getChunk(chunkX, chunkZ, ChunkStatus.STRUCTURE_REFERENCES);
                if (!chunkAccess.getHighestGeneratedStatus().isOrAfter(ChunkStatus.STRUCTURE_REFERENCES)) continue;

                // Check all structures referenced by this chunk, and return true if any match the provided tag
                for (Map.Entry<Structure, LongSet> entry : chunkAccess.getAllReferences().entrySet()) {
                    Structure structure = entry.getKey();
                    LongSet references = entry.getValue();

                    Optional<ResourceKey<Structure>> structureKey = structureRegistry.getResourceKey(structure);
                    //? if <1.21.2 {
                    boolean isTaggedStructure = structureKey.isPresent() && structureRegistry.getHolderOrThrow(structureKey.get()).is(structureTagKey);
                    //?} else {
                    /*boolean isTaggedStructure = structureKey.isPresent() && structureRegistry.getOrThrow(structureKey.get()).is(structureTagKey);
                    *///?}

                    // Only suppress the feature where an actual piece is (its bounding box + a small
                    // margin), NOT across the structure's whole bounding box. The overall box can be
                    // huge and mostly empty, and suppressing it entirely leaves unnatural feature-free
                    // voids.
                    if (isTaggedStructure && isAnyReferenceValidStartForStructure(worldGenRegion, structure, references, structureStart -> isAreaNearAnyPiece(structureStart, grown))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * True if the area, already grown by {@link #STRUCTURE_PIECE_MARGIN}, overlaps any individual
     * piece of the structure start. The overall bounding-box check short-circuits cheaply for areas
     * nowhere near the structure.
     */
    private static boolean isAreaNearAnyPiece(StructureStart structureStart, BoundingBox grown) {
        BoundingBox startBox = structureStart.getBoundingBox();
        if (!grown.intersects(startBox.minX(), startBox.minZ(), startBox.maxX(), startBox.maxZ())) return false;
        for (StructurePiece piece : structureStart.getPieces()) {
            BoundingBox box = piece.getBoundingBox();
            if (!grown.intersects(box.minX(), box.minZ(), box.maxX(), box.maxZ())) continue;
            // A piece that follows the floor can end up above or below its own box, so it also
            // covers that reach.
            int reach = FollowFloorProcessor.verticalReach(piece);
            if (grown.maxY() >= box.minY() - reach && grown.minY() <= box.maxY() + reach) {
                return true;
            }
        }
        return false;
    }

    /** The largest value the provider can give, which is how far a feature sized by it can reach. */
    public static int maxOf(IntProvider provider) {
        //? if <26.1.2 {
        return provider.getMaxValue();
        //?} else {
        /*return provider.maxInclusive();
        *///?}
    }

    /**
     * Checks if any of the references contain a valid structure start for the provided structure
     * that also passes the provided filter. Based on vanilla's StructureManager#fillStartsForStructure.
     */
    private static boolean isAnyReferenceValidStartForStructure(WorldGenRegion worldGenRegion, Structure structure, LongSet references, Predicate<StructureStart> filter) {
        StructureManager structureManager = worldGenRegion.getLevel().structureManager();

        for (long reference : references) {
            //? if <1.21.2 {
            SectionPos structureStartSectionPos = SectionPos.of(new ChunkPos(reference), worldGenRegion.getMinSection());
            //?}
            //? if >=1.21.2 <26.1.2 {
            /*SectionPos structureStartSectionPos = SectionPos.of(new ChunkPos(reference), worldGenRegion.getMinSectionY());
            *///?}
            //? if >=26.1.2 {
            /*SectionPos structureStartSectionPos = SectionPos.of(new ChunkPos(ChunkPos.getX(reference), ChunkPos.getZ(reference)), worldGenRegion.getMinSectionY());
            *///?}
            if (!worldGenRegion.hasChunk(structureStartSectionPos.x(), structureStartSectionPos.z())) {
                continue;
            }

            ChunkAccess structureStartChunkAccess = worldGenRegion.getChunk(structureStartSectionPos.x(), structureStartSectionPos.z(), ChunkStatus.STRUCTURE_STARTS);

            //? if <26.3 {
            StructureStart structureStart = structureManager.getStartForStructure(structureStartSectionPos, structure, structureStartChunkAccess);
            //?} else {
            /*StructureStart structureStart = structureManager.getStartForStructure(structure, structureStartChunkAccess);
            *///?}
            if (structureStart != null && structureStart.isValid() && filter.test(structureStart)) {
                return true;
            }
        }

        return false;
    }
}
