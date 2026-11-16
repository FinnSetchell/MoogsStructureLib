package com.finndog.moogs_structures.world.structures;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
//? if >=26.3 {
/*import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.CarverOutput;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import java.util.ArrayList;
import java.util.Map;
*///?}
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The rock right above the pieces of a structure that hangs from a ceiling ({@code "land_search_direction": "CEILING"}),
 * which the world's carvers (Nether caves, Overworld caves and canyons) leave alone. The hanging spot is chosen from the
 * terrain before carving, so that it is the same in any chunk order and {@code /locate} agrees with it; a tunnel cut later
 * through the rock over a piece would leave it hanging under nothing.
 *
 * <p>Only the few rows above each piece of such a structure, and only within the piece's footprint, are kept. Every other
 * block carves exactly as before, and a chunk without a hanging structure gets no guard at all. The structures a chunk
 * touches are fixed before carving starts, so the result is still the same in any chunk order.
 */
public final class HangingRockGuard {

    /** How many rows above the top of each piece the carvers leave alone. */
    public static final int ROWS = 3;

    private static final ThreadLocal<HangingRockGuard> CARVING = new ThreadLocal<>();

    private final int chunkX;
    private final int chunkZ;
    /** Six ints per kept box: min x, min z, max x, max z (inside the chunk, 0 to 15), min y, max y. */
    private final int[] boxes;

    private HangingRockGuard(ChunkPos pos, int[] boxes) {
        //? if <26.1.2 {
        this.chunkX = pos.x;
        this.chunkZ = pos.z;
        //?} else {
        /*this.chunkX = pos.x();
        this.chunkZ = pos.z();
        *///?}
        this.boxes = boxes;
    }

    /** The rock the carvers must leave in chunk {@code pos}, or null when no hanging structure reaches into it. */
    @Nullable
    public static HangingRockGuard of(StructureManager structureManager, ChunkPos pos) {
        //? if <26.3 {
        List<StructureStart> starts = structureManager.startsForStructure(pos, HangingRockGuard::hangs);
        //?} else {
        /*List<StructureStart> starts = structureManager.startsForStructure(pos.x(), pos.z(), HangingRockGuard::hangs);
        *///?}
        return from(pos, starts);
    }

    //? if >=26.3 {
    /*// The same, read while a chunk is placed (the carving record is gone by then and is worked out again). Start chunks
    // the region can't reach are skipped rather than loaded; the chunk being placed reaches all of its own.
    @Nullable
    public static HangingRockGuard of(WorldGenRegion region, ChunkAccess chunk) {
        StructureManager structureManager = region.getLevel().structureManager();
        List<StructureStart> starts = new ArrayList<>();
        for (Map.Entry<Structure, LongSet> entry : chunk.getAllReferences().entrySet()) {
            if (!hangs(entry.getKey())) {
                continue;
            }
            for (long reference : entry.getValue()) {
                int x = ChunkPos.getX(reference);
                int z = ChunkPos.getZ(reference);
                if (!region.hasChunk(x, z)) {
                    continue;
                }
                StructureStart start = structureManager.getStartForStructure(entry.getKey(), region.getChunk(x, z, ChunkStatus.STRUCTURE_STARTS));
                if (start != null && start.isValid()) {
                    starts.add(start);
                }
            }
        }
        return from(chunk.getPos(), starts);
    }
    *///?}

    @Nullable
    private static HangingRockGuard from(ChunkPos pos, List<StructureStart> starts) {
        if (starts.isEmpty()) {
            return null;
        }
        int minX = pos.getMinBlockX();
        int minZ = pos.getMinBlockZ();
        IntArrayList boxes = new IntArrayList();
        for (StructureStart start : starts) {
            for (StructurePiece piece : start.getPieces()) {
                BoundingBox box = piece.getBoundingBox();
                int x0 = Math.max(box.minX(), minX) - minX;
                int x1 = Math.min(box.maxX(), minX + 15) - minX;
                int z0 = Math.max(box.minZ(), minZ) - minZ;
                int z1 = Math.min(box.maxZ(), minZ + 15) - minZ;
                if (x0 <= x1 && z0 <= z1) {
                    boxes.add(x0);
                    boxes.add(z0);
                    boxes.add(x1);
                    boxes.add(z1);
                    boxes.add(box.maxY() + 1);
                    boxes.add(box.maxY() + ROWS);
                }
            }
        }
        return boxes.isEmpty() ? null : new HangingRockGuard(pos, boxes.toIntArray());
    }

    private static boolean hangs(Structure structure) {
        return structure instanceof GenericNetherJigsawStructure nether && nether.hangsFromCeiling();
    }

    /** Whether the carvers must leave the block at {@code x} and {@code z} inside the chunk (0 to 15) and world height {@code y}. */
    public boolean keeps(int x, int y, int z) {
        for (int i = 0; i < this.boxes.length; i += 6) {
            if (x >= this.boxes[i] && z >= this.boxes[i + 1] && x <= this.boxes[i + 2] && z <= this.boxes[i + 3]
                    && y >= this.boxes[i + 4] && y <= this.boxes[i + 5]) {
                return true;
            }
        }
        return false;
    }

    /** Called round the carving of one chunk, so the carvers can ask {@link #keepsWhileCarving}. */
    public static void startCarving(@Nullable HangingRockGuard guard) {
        CARVING.set(guard);
    }

    public static void stopCarving() {
        CARVING.remove();
    }

    /** The guard of the chunk being carved on this thread, or null. */
    @Nullable
    public static HangingRockGuard carving() {
        return CARVING.get();
    }

    /** Whether the chunk at {@code pos} that is being carved on this thread keeps the block at {@code x}, {@code y}, {@code z}. */
    public static boolean keepsWhileCarving(ChunkPos pos, int x, int y, int z) {
        HangingRockGuard guard = CARVING.get();
        //? if <26.1.2 {
        return guard != null && guard.chunkX == pos.x && guard.chunkZ == pos.z && guard.keeps(x, y, z);
        //?} else {
        /*return guard != null && guard.chunkX == pos.x() && guard.chunkZ == pos.z() && guard.keeps(x, y, z);
        *///?}
    }

    //? if >=26.3 {
    /*// output, leaving out the blocks this guard keeps (the carvers hand it x and z inside the chunk).
    public CarverOutput guarding(CarverOutput output) {
        return new CarverOutput() {
            @Override
            public void carve(int x, int y, int z) {
                if (!HangingRockGuard.this.keeps(x & 15, y, z & 15)) {
                    output.carve(x, y, z);
                }
            }

            @Override
            public int minY() {
                return output.minY();
            }

            @Override
            public int maxY() {
                return output.maxY();
            }
        };
    }
    *///?}
}
