package com.finndog.moogs_structures.world.processors;

import com.finndog.moogs_structures.mixins.structures.SinglePoolElementAccessor;
//? if <26.2 {
import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
//?}
import com.finndog.moogs_structures.utils.StrictOptionalField;
import com.mojang.serialization.Codec;
//? if >=1.20.6 {
import com.mojang.serialization.MapCodec;
//?}
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
//? if <26.2 {
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
//?}
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

/**
 * Moves each column of a piece onto the floor found near the piece's own placement height, so the
 * piece follows uneven ground the way {@code terrain_matching} does, but also under the Nether's
 * bedrock roof, in caves and under overhangs.
 *
 * <p>Vanilla {@code terrain_matching} ({@code GravityProcessor}) and {@code super_gravity_processor}
 * take their height from the {@code WORLD_SURFACE} heightmap, which in the Nether is the top of the
 * bedrock roof. This processor reads the blocks of each column instead, starting at the piece's
 * placement height: from inside the ground it climbs to the top of that ground, from open space it
 * drops to the first ground below. Lava and water count as open space, so a column over a lava lake
 * or the sea settles on the floor under it, and the piece's air blocks leave the liquid in place. A
 * column with no floor within {@code search_range} is left out. The search never crosses the
 * dimension's logical height, so it can never reach the top of the Nether roof.
 *
 * <p>Every block of a column moves by the same amount, and entities move with the column they stand
 * in. Block entities keep their data, as the processor only changes a block's height.
 *
 * <p>A piece is left out whole when most of it would have no floor: MSL's land searches read one
 * column of the terrain before caves are carved, so they can pick a lone ledge over open space, or
 * one a cave later cuts away, which would leave a few stray columns standing. That is judged from
 * the terrain before carving and the carvers' own record of what they cut, never from the blocks,
 * so every chunk the piece covers comes to the same answer whatever order they generate in. It is
 * only judged for pieces no more than two chunks across, as wider ones reach chunks that aren't
 * carved yet.
 */
public class FollowFloorProcessor extends StructureEntityProcessor {

    //? if >=1.20.6 <26.2 {
    public static final MapCodec<FollowFloorProcessor> CODEC = RecordCodecBuilder.mapCodec(
    //?}
    //? if <1.20.6 {
    /*public static final Codec<FollowFloorProcessor> CODEC = RecordCodecBuilder.create(
    *///?}
    //? if >=26.2 {
    /*public static final MapCodec<FollowFloorProcessor> MAP_CODEC = RecordCodecBuilder.mapCodec(
    *///?}
            (instance) -> instance.group(
                    StrictOptionalField.of(Codec.intRange(1, 64), "search_range", 16).forGetter((p) -> p.searchRange),
                    StrictOptionalField.of(Codec.INT, "offset", 0).forGetter((p) -> p.offset)
            ).apply(instance, FollowFloorProcessor::new));

    /** Shift returned for a column that has no usable floor: its blocks and entities are left out. */
    private static final int NO_FLOOR = Integer.MIN_VALUE;
    private static final int NOT_MEASURED = Integer.MAX_VALUE;

    /**
     * The column shifts of the placement this thread is running. Every block of a column has to move
     * by the same amount, and entities are placed after the blocks, when their column already holds
     * the piece, so each column is measured once per placement and the result reused.
     */
    private static final ThreadLocal<ColumnShifts> COLUMN_SHIFTS = new ThreadLocal<>();

    private final int searchRange;
    private final int offset;

    public FollowFloorProcessor(int searchRange, int offset) {
        this.searchRange = searchRange;
        this.offset = offset;
    }

    @Nullable
    @Override
    //? if <26.2 {
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos piecePos, BlockPos pivot, StructureTemplate.StructureBlockInfo localBlockInfo, StructureTemplate.StructureBlockInfo blockInfo, StructurePlaceSettings settings) {
    //?} else {
    /*public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos piecePos, BlockPos pivot, BlockPos templateRelativePos, StructureTemplate.StructureBlockInfo blockInfo, StructurePlaceSettings settings) {
    *///?}
        BlockPos pos = blockInfo.pos();
        ColumnShifts shifts = this.shiftsFor(settings, piecePos);
        if (!blockInfo.state().isAir()) {
            shifts.footprint.add(column(pos.getX(), pos.getZ()));
        }
        // Blocks outside the chunk being placed are thrown away after processing, and their columns
        // may not be generated yet, so leave them alone. Their own chunk's pass moves them.
        if (!isInChunk(settings, pos)) {
            return blockInfo;
        }
        int shift = this.columnShift(levelReader, shifts, piecePos, pos.getX(), pos.getZ());
        if (shift == NO_FLOOR) {
            return null;
        }
        BlockPos target = pos.offset(0, shift, 0);
        // A piece settled under lava or water keeps the liquid around it: its air doesn't carve holes.
        if (blockInfo.state().isAir() && !levelReader.getFluidState(target).isEmpty()) {
            return null;
        }
        if (shift == 0) {
            return blockInfo;
        }
        return new StructureTemplate.StructureBlockInfo(target, blockInfo.state(), blockInfo.nbt());
    }

    @Nullable
    @Override
    public StructureTemplate.StructureEntityInfo processEntity(ServerLevelAccessor serverLevelAccessor,
                                                               BlockPos structurePiecePos,
                                                               BlockPos structurePieceBottomCenterPos,
                                                               StructureTemplate.StructureEntityInfo localEntityInfo,
                                                               StructureTemplate.StructureEntityInfo globalEntityInfo,
                                                               StructurePlaceSettings structurePlaceSettings) {
        BlockPos blockPos = globalEntityInfo.blockPos;
        ColumnShifts shifts = this.shiftsFor(structurePlaceSettings, structurePiecePos);
        if (shifts.hollow) {
            return null;
        }
        if (!isInChunk(structurePlaceSettings, blockPos)) {
            return globalEntityInfo;
        }
        int shift = this.columnShift(serverLevelAccessor, shifts, structurePiecePos, blockPos.getX(), blockPos.getZ());
        if (shift == NO_FLOOR) {
            return null;
        }
        if (shift == 0) {
            return globalEntityInfo;
        }
        return new StructureTemplate.StructureEntityInfo(globalEntityInfo.pos.add(0, shift, 0), blockPos.offset(0, shift, 0), globalEntityInfo.nbt);
    }

    /**
     * Leaves the whole piece out when most of it would have no floor. Runs once every
     * block of the piece has been through {@link #processBlock}, so the whole footprint is known.
     */
    @Override
    public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor level, BlockPos piecePos, BlockPos pivot,
                                                                         List<StructureTemplate.StructureBlockInfo> originalBlockInfos,
                                                                         List<StructureTemplate.StructureBlockInfo> processedBlockInfos,
                                                                         StructurePlaceSettings settings) {
        ColumnShifts shifts = COLUMN_SHIFTS.get();
        if (shifts != null && shifts.isFor(this, settings, piecePos)) {
            // Vanilla runs the processors a second time over the piece's data markers: judge once.
            if (!shifts.judged) {
                shifts.judged = true;
                shifts.hollow = this.isMostlyHollow(level, settings, piecePos.getY(), shifts.footprint);
            }
            if (shifts.hollow) {
                return new ArrayList<>();
            }
        }
        return processedBlockInfos;
    }

    //? if >=26.2 {
    /*@Override
    public boolean evaluatesEntirePieceState() {
        // The whole footprint is needed to judge whether most of it would have no floor.
        return true;
    }
    *///?}

    /**
     * How far a piece's blocks can end up above or below its bounding box because it follows the
     * floor, or 0 when it doesn't. Lets checks that work from piece boxes (like keeping basalt and
     * deltas out of tagged structures) cover where the piece really is.
     */
    public static int verticalReach(StructurePiece piece) {
        if (!(piece instanceof PoolElementStructurePiece poolPiece) || !(poolPiece.getElement() instanceof SinglePoolElement element)) {
            return 0;
        }
        int reach = 0;
        for (StructureProcessor processor : ((SinglePoolElementAccessor) element).moogs_structures_getProcessors().value().list()) {
            if (processor instanceof FollowFloorProcessor follow) {
                reach = Math.max(reach, follow.searchRange + Math.abs(follow.offset));
            }
        }
        return reach;
    }

    private static boolean isInChunk(StructurePlaceSettings settings, BlockPos pos) {
        BoundingBox box = settings.getBoundingBox();
        return box == null || (pos.getX() >= box.minX() && pos.getX() <= box.maxX() && pos.getZ() >= box.minZ() && pos.getZ() <= box.maxZ());
    }

    private ColumnShifts shiftsFor(StructurePlaceSettings settings, BlockPos piecePos) {
        ColumnShifts shifts = COLUMN_SHIFTS.get();
        if (shifts == null || !shifts.isFor(this, settings, piecePos)) {
            shifts = new ColumnShifts(this, settings, piecePos.immutable());
            COLUMN_SHIFTS.set(shifts);
        }
        return shifts;
    }

    private static long column(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    private int columnShift(LevelReader level, ColumnShifts shifts, BlockPos piecePos, int x, int z) {
        long column = column(x, z);
        int shift = shifts.byColumn.get(column);
        if (shift == NOT_MEASURED) {
            shift = this.findShift(level, piecePos.getY(), x, z);
            shifts.byColumn.put(column, shift);
        }
        return shift;
    }

    /**
     * How far the column at x, z has to move so the piece's bottom layer lands on the floor (plus
     * {@code offset}), measured from the piece's placement height {@code baseY}.
     */
    private int findShift(LevelReader level, int baseY, int x, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, baseY, z);
        return this.findShift(level, baseY, y -> isSolid(level, pos.setY(y)));
    }

    /** {@link #findShift(LevelReader, int, int, int)} for any reading of which heights are solid. */
    private int findShift(LevelReader level, int baseY, IntPredicate solidAt) {
        //? if <1.21.2 {
        int minY = level.getMinBuildHeight();
        //?} else {
        /*int minY = level.getMinY();
        *///?}
        int maxY = minY + level.dimensionType().logicalHeight() - 1;
        if (baseY < minY || baseY > maxY) {
            // Placed outside the searchable height (on top of the Nether roof, say): leave it as placed.
            return 0;
        }

        boolean climb = solidAt.test(baseY);
        for (int step = 1; step <= this.searchRange; step++) {
            int y = climb ? baseY + step : baseY - step;
            if (y < minY || y > maxY) {
                return NO_FLOOR;
            }
            boolean solid = solidAt.test(y);
            if (climb) {
                // Inside the ground: the floor is the last solid block before open space or liquid.
                if (solid) continue;
                return (y - 1) - baseY + this.offset;
            }
            // In open space or liquid: the floor is the first solid block below.
            if (!solid) continue;
            return y - baseY + this.offset;
        }
        return NO_FLOOR;
    }

    /**
     * Whether more than half of the piece's visible columns would have no floor within reach: read
     * from the terrain before carving with the carved blocks taken out, so features and structures
     * placed since can't sway it. Only judged when the chunk being placed holds part of the piece and
     * the piece is no more than two chunks across, so every chunk it covers can read every other one.
     */
    private boolean isMostlyHollow(ServerLevelAccessor level, StructurePlaceSettings settings, int baseY, LongOpenHashSet footprint) {
        BoundingBox chunkBox = settings.getBoundingBox();
        if (footprint.isEmpty() || chunkBox == null) {
            return false;
        }
        int minChunkX = Integer.MAX_VALUE, maxChunkX = Integer.MIN_VALUE, minChunkZ = Integer.MAX_VALUE, maxChunkZ = Integer.MIN_VALUE;
        for (LongIterator it = footprint.iterator(); it.hasNext(); ) {
            long column = it.nextLong();
            int chunkX = ((int) (column >> 32)) >> 4;
            int chunkZ = ((int) column) >> 4;
            minChunkX = Math.min(minChunkX, chunkX);
            maxChunkX = Math.max(maxChunkX, chunkX);
            minChunkZ = Math.min(minChunkZ, chunkZ);
            maxChunkZ = Math.max(maxChunkZ, chunkZ);
        }
        int placingX = chunkBox.minX() >> 4;
        int placingZ = chunkBox.minZ() >> 4;
        if (maxChunkX - minChunkX > 1 || maxChunkZ - minChunkZ > 1
                || placingX < minChunkX || placingX > maxChunkX || placingZ < minChunkZ || placingZ > maxChunkZ) {
            return false;
        }

        ChunkGenerator generator = level.getLevel().getChunkSource().getGenerator();
        RandomState randomState = level.getLevel().getChunkSource().randomState();
        Long2ObjectOpenHashMap<CarvedTerrain.Mask> masks = new Long2ObjectOpenHashMap<>();
        int hollow = 0;
        int left = footprint.size();
        for (LongIterator it = footprint.iterator(); it.hasNext(); ) {
            long column = it.nextLong();
            int x = (int) (column >> 32);
            int z = (int) column;
            long chunkKey = column(x >> 4, z >> 4);
            CarvedTerrain.Mask mask = masks.get(chunkKey);
            if (mask == null) {
                mask = CarvedTerrain.of(level, level.getChunk(x >> 4, z >> 4));
                if (mask == null) {
                    return false;
                }
                masks.put(chunkKey, mask);
            }
            CarvedTerrain.Mask carved = mask;
            NoiseColumn terrain = generator.getBaseColumn(x, z, level, randomState);
            IntPredicate rock = y -> isRock(terrain.getBlock(y));
            if (this.findShift(level, baseY, y -> rock.test(y) && !carved.isCarved(x, y, z)) == NO_FLOOR) {
                hollow++;
            }
            left--;
            if (hollow * 2 > footprint.size()) {
                return true;
            }
            if ((hollow + left) * 2 <= footprint.size()) {
                return false;
            }
        }
        return false;
    }

    /** Terrain before carving holds only the dimension's stone, its fluids and air. */
    private static boolean isRock(BlockState state) {
        return !state.isAir() && state.getFluidState().isEmpty();
    }

    /**
     * Floor is anything with a collision box: stone, soul sand, nylium, slabs, waterlogged blocks.
     * Air, liquids, plants, fungi, roots, vines, fire and thin snow are open space.
     */
    private static boolean isSolid(LevelReader level, BlockPos pos) {
        return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    private static final class ColumnShifts {
        private final FollowFloorProcessor processor;
        private final StructurePlaceSettings settings;
        private final BlockPos piecePos;
        private final Long2IntOpenHashMap byColumn = new Long2IntOpenHashMap();
        /** Every column the piece has a visible (non-air) block in, inside the chunk being placed or not. */
        private final LongOpenHashSet footprint = new LongOpenHashSet();
        private boolean judged;
        /** Set once the piece is found to be mostly over a hollow: nothing of it is placed. */
        private boolean hollow;

        private ColumnShifts(FollowFloorProcessor processor, StructurePlaceSettings settings, BlockPos piecePos) {
            this.processor = processor;
            this.settings = settings;
            this.piecePos = piecePos;
            this.byColumn.defaultReturnValue(NOT_MEASURED);
        }

        private boolean isFor(FollowFloorProcessor processor, StructurePlaceSettings settings, BlockPos piecePos) {
            return this.processor == processor && this.settings == settings && this.piecePos.equals(piecePos);
        }
    }

    //? if <26.2 {
    @Override
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.FOLLOW_FLOOR_PROCESSOR.get();
    //?} else {
    /*@Override
    public MapCodec<FollowFloorProcessor> codec() {
        return MAP_CODEC;
    *///?}
    }
}
