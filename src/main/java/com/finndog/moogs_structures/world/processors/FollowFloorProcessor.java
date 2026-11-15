package com.finndog.moogs_structures.world.processors;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.mixins.structures.SinglePoolElementAccessor;
import com.finndog.moogs_structures.utils.GeneralUtils;
import com.finndog.moogs_structures.world.structures.pieces.VersionAwareSinglePoolElement;
import com.finndog.moogs_structures.world.structures.terrainadaptation.AdaptationArea;
import com.finndog.moogs_structures.world.structures.terrainadaptation.AdaptationArea.ColumnRect;
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
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
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

import java.util.ArrayDeque;
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
 *
 * <p>A pool element's {@code adaptation_area} with {@code outside_follows_ground} uses this processor
 * for the columns outside its area: the area's own columns stay exactly where the piece put them,
 * and when most of the following columns would have no floor, only those are left out.
 *
 * <p>{@code follow_ceiling_processor} is the same processor upside down: each column moves up or
 * down so the piece's top row sits just under the rock above it ({@code offset} moves it further
 * up, into the rock). It reads the terrain before features (the terrain noise with the carvers'
 * record taken out) rather than the blocks, so glowstone, basalt or lava springs from neighbouring
 * chunks can't change where a column ends up, whatever order chunks generate in. A column is left
 * out when there is no rock above within {@code search_range}, when liquid lies between it and the
 * rock or right under the rock, or when it would reach into the top six layers of the terrain (the
 * Nether's bedrock roof). Its air blocks leave lava and water in place. Placed into chunks that are
 * already finished (with {@code /place}), it reads the blocks there instead.
 */
public class FollowFloorProcessor extends StructureEntityProcessor {

    private static final int DEFAULT_SEARCH_RANGE = 16;

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
                    StrictOptionalField.of(Codec.intRange(1, 64), "search_range", DEFAULT_SEARCH_RANGE).forGetter((p) -> p.searchRange),
                    StrictOptionalField.of(Codec.INT, "offset", 0).forGetter((p) -> p.offset)
            ).apply(instance, FollowFloorProcessor::new));

    //? if >=1.20.6 <26.2 {
    public static final MapCodec<FollowFloorProcessor> CEILING_CODEC = RecordCodecBuilder.mapCodec(
    //?}
    //? if <1.20.6 {
    /*public static final Codec<FollowFloorProcessor> CEILING_CODEC = RecordCodecBuilder.create(
    *///?}
    //? if >=26.2 {
    /*public static final MapCodec<FollowFloorProcessor> CEILING_MAP_CODEC = RecordCodecBuilder.mapCodec(
    *///?}
            (instance) -> instance.group(
                    StrictOptionalField.of(Codec.intRange(1, 64), "search_range", DEFAULT_SEARCH_RANGE).forGetter((p) -> p.searchRange),
                    StrictOptionalField.of(Codec.INT, "offset", 0).forGetter((p) -> p.offset)
            ).apply(instance, (searchRange, offset) -> new FollowFloorProcessor(searchRange, offset, List.of(), true)));

    /** Shift returned for a column that has no usable floor: its blocks and entities are left out. */
    private static final int NO_FLOOR = Integer.MIN_VALUE;
    private static final int NOT_MEASURED = Integer.MAX_VALUE;
    /** A hanging piece whose height isn't known (placed some other way than as a template): left as placed. */
    private static final int UNKNOWN_TOP = Integer.MIN_VALUE;

    /**
     * The column shifts of the placement this thread is running. Every block of a column has to move
     * by the same amount, and entities are placed after the blocks, when their column already holds
     * the piece, so each column is measured once per placement and the result reused.
     */
    private static final ThreadLocal<ColumnShifts> COLUMN_SHIFTS = new ThreadLocal<>();

    /**
     * Templates this thread is placing that hang from a ceiling, newest last, with their heights. A
     * processor only learns where a piece starts, and a hanging piece is measured from its top.
     */
    private static final ThreadLocal<ArrayDeque<PlacingTemplate>> PLACING = ThreadLocal.withInitial(ArrayDeque::new);
    private static boolean warnedUnknownTop;

    private final int searchRange;
    private final int offset;
    /** Template columns left where the piece put them, or empty when every column follows. */
    private final List<ColumnRect> keptColumns;
    /** Follows the ceiling above instead of the floor below ({@code follow_ceiling_processor}). */
    private final boolean ceiling;

    public FollowFloorProcessor(int searchRange, int offset) {
        this(searchRange, offset, List.of(), false);
    }

    private FollowFloorProcessor(int searchRange, int offset, List<ColumnRect> keptColumns, boolean ceiling) {
        this.searchRange = searchRange;
        this.offset = offset;
        this.keptColumns = keptColumns;
        this.ceiling = ceiling;
    }

    /** Every column of the piece follows the floor except those in {@code area} (template columns). */
    public static FollowFloorProcessor outside(List<ColumnRect> area) {
        return new FollowFloorProcessor(DEFAULT_SEARCH_RANGE, 0, List.copyOf(area), false);
    }

    /**
     * A template is starting to place ({@code StructureTemplatePlacingMixin}): if it hangs from a
     * ceiling, remember its height for as long as it places.
     */
    public static void startPlacing(StructurePlaceSettings settings, Vec3i size) {
        for (StructureProcessor processor : settings.getProcessors()) {
            if (processor instanceof FollowFloorProcessor follow && follow.ceiling) {
                PLACING.get().addLast(new PlacingTemplate(settings, size.getY()));
                return;
            }
        }
    }

    /** The template {@link #startPlacing} saw has finished placing. */
    public static void stopPlacing(StructurePlaceSettings settings) {
        ArrayDeque<PlacingTemplate> placing = PLACING.get();
        if (!placing.isEmpty() && placing.peekLast().settings == settings) {
            placing.removeLast();
            // The terrain view holds the world being generated: don't keep it on this thread once the piece is done.
            ColumnShifts shifts = COLUMN_SHIFTS.get();
            if (shifts != null && shifts.settings == settings) {
                shifts.terrain = null;
            }
        }
    }

    @Nullable
    @Override
    //? if <26.2 {
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos piecePos, BlockPos pivot, StructureTemplate.StructureBlockInfo localBlockInfo, StructureTemplate.StructureBlockInfo blockInfo, StructurePlaceSettings settings) {
    //?} else {
    /*public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos piecePos, BlockPos pivot, BlockPos templateRelativePos, StructureTemplate.StructureBlockInfo blockInfo, StructurePlaceSettings settings) {
    *///?}
        //? if <26.2 {
        BlockPos templatePos = localBlockInfo.pos();
        //?} else {
        /*BlockPos templatePos = templateRelativePos;
        *///?}
        if (this.keeps(templatePos)) {
            return blockInfo;
        }
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
        int shift = this.columnShift(levelReader, shifts, pos.getX(), pos.getZ());
        if (shift == NO_FLOOR) {
            return null;
        }
        BlockPos target = pos.offset(0, shift, 0);
        // A piece settled under (or hanging over) lava or water keeps the liquid around it: its air doesn't carve holes.
        if (blockInfo.state().isAir() && this.isLiquid(levelReader, shifts, target)) {
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
        if (this.keeps(localEntityInfo.blockPos)) {
            return globalEntityInfo;
        }
        BlockPos blockPos = globalEntityInfo.blockPos;
        ColumnShifts shifts = this.shiftsFor(structurePlaceSettings, structurePiecePos);
        if (shifts.hollow) {
            return null;
        }
        if (!isInChunk(structurePlaceSettings, blockPos)) {
            return globalEntityInfo;
        }
        int shift = this.columnShift(serverLevelAccessor, shifts, blockPos.getX(), blockPos.getZ());
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
                shifts.hollow = this.isMostlyHollow(level, settings, shifts.anchorY, shifts.footprint);
            }
            if (shifts.hollow) {
                return this.keptColumns.isEmpty() ? new ArrayList<>() : this.keptOnly(originalBlockInfos, processedBlockInfos);
            }
        }
        return processedBlockInfos;
    }

    /**
     * The blocks of an adaptation area's own columns, which stay whatever the columns around them do.
     * This processor runs first, so the two lists still pair up block for block; if another processor
     * has changed that, nothing is taken out.
     */
    private List<StructureTemplate.StructureBlockInfo> keptOnly(List<StructureTemplate.StructureBlockInfo> originalBlockInfos,
                                                                List<StructureTemplate.StructureBlockInfo> processedBlockInfos) {
        if (originalBlockInfos.size() != processedBlockInfos.size()) {
            return processedBlockInfos;
        }
        List<StructureTemplate.StructureBlockInfo> kept = new ArrayList<>();
        for (int i = 0; i < processedBlockInfos.size(); i++) {
            if (this.keeps(originalBlockInfos.get(i).pos())) {
                kept.add(processedBlockInfos.get(i));
            }
        }
        return kept;
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
     * floor or the ceiling, or 0 when it doesn't. Lets checks that work from piece boxes (like keeping
     * basalt and deltas out of tagged structures) cover where the piece really is.
     */
    public static int verticalReach(StructurePiece piece) {
        if (!(piece instanceof PoolElementStructurePiece poolPiece) || !(poolPiece.getElement() instanceof SinglePoolElement element)) {
            return 0;
        }
        int reach = 0;
        if (element instanceof VersionAwareSinglePoolElement versioned
                && versioned.moogs_structures_getAdaptationArea().map(AdaptationArea::outsideFollowsGround).orElse(false)) {
            reach = DEFAULT_SEARCH_RANGE;
        }
        for (StructureProcessor processor : ((SinglePoolElementAccessor) element).moogs_structures_getProcessors().value().list()) {
            if (processor instanceof FollowFloorProcessor follow) {
                reach = Math.max(reach, follow.searchRange + Math.abs(follow.offset));
            }
        }
        return reach;
    }

    private boolean keeps(BlockPos templatePos) {
        return !this.keptColumns.isEmpty() && ColumnRect.anyContains(this.keptColumns, templatePos.getX(), templatePos.getZ());
    }

    private static boolean isInChunk(StructurePlaceSettings settings, BlockPos pos) {
        BoundingBox box = settings.getBoundingBox();
        return box == null || (pos.getX() >= box.minX() && pos.getX() <= box.maxX() && pos.getZ() >= box.minZ() && pos.getZ() <= box.maxZ());
    }

    private ColumnShifts shiftsFor(StructurePlaceSettings settings, BlockPos piecePos) {
        ColumnShifts shifts = COLUMN_SHIFTS.get();
        if (shifts == null || !shifts.isFor(this, settings, piecePos)) {
            shifts = new ColumnShifts(this, settings, piecePos.immutable(), this.anchorY(settings, piecePos));
            COLUMN_SHIFTS.set(shifts);
        }
        return shifts;
    }

    /** The height every column is measured from: the piece's bottom row, or its top row when it hangs. */
    private int anchorY(StructurePlaceSettings settings, BlockPos piecePos) {
        if (!this.ceiling) {
            return piecePos.getY();
        }
        for (PlacingTemplate placing : PLACING.get()) {
            if (placing.settings == settings) {
                return piecePos.getY() + placing.height - 1;
            }
        }
        if (!warnedUnknownTop) {
            warnedUnknownTop = true;
            MoogsStructuresCommon.LOGGER.warn("Moog's Structure Lib: follow_ceiling_processor ran outside a structure template's placement, so it can't tell where the piece's top is. Those blocks are left where they are.");
        }
        return UNKNOWN_TOP;
    }

    private static long column(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    private int columnShift(LevelReader level, ColumnShifts shifts, int x, int z) {
        long column = column(x, z);
        int shift = shifts.byColumn.get(column);
        if (shift == NOT_MEASURED) {
            shift = this.ceiling
                    ? this.findCeilingShift(level, shifts, x, z)
                    : this.findShift(level, shifts.anchorY, x, z);
            shifts.byColumn.put(column, shift);
        }
        return shift;
    }

    private boolean isLiquid(LevelReader level, ColumnShifts shifts, BlockPos pos) {
        if (this.ceiling) {
            return shifts.terrain(level).isLiquid(pos.getX(), pos.getY(), pos.getZ());
        }
        return !level.getFluidState(pos).isEmpty();
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

    /** How far the column at x, z of a hanging piece has to move so its top row sits just under the rock. */
    private int findCeilingShift(LevelReader level, ColumnShifts shifts, int x, int z) {
        if (shifts.anchorY == UNKNOWN_TOP) {
            return 0;
        }
        Terrain terrain = shifts.terrain(level);
        return this.findCeilingShift(level, shifts.anchorY, y -> terrain.isRock(x, y, z), y -> terrain.isLiquid(x, y, z));
    }

    /**
     * {@link #findCeilingShift(LevelReader, ColumnShifts, int, int)} for any reading of rock and liquid,
     * measured from the piece's top row {@code topY}: from inside the rock it drops to the first open
     * block under it, from open space it climbs to the first rock above.
     */
    private int findCeilingShift(LevelReader level, int topY, IntPredicate rockAt, IntPredicate liquidAt) {
        //? if <1.21.2 {
        int minY = level.getMinBuildHeight();
        //?} else {
        /*int minY = level.getMinY();
        *///?}
        // Never into the top layers, which in the Nether are the bedrock roof.
        int highestTop = minY + level.dimensionType().logicalHeight() - GeneralUtils.CEILING_ROOF_MARGIN;
        if (topY > highestTop) {
            // Placed in or above the roof layers: never placed as it is, as its air could open the roof.
            return NO_FLOOR;
        }
        if (topY < minY) {
            return 0;
        }

        int target = NO_FLOOR;
        if (rockAt.test(topY)) {
            // Inside the rock: the top goes to the first open block under it, which must be air.
            for (int step = 1; step <= this.searchRange && target == NO_FLOOR; step++) {
                int y = topY - step;
                if (y < minY) {
                    return NO_FLOOR;
                }
                if (rockAt.test(y)) continue;
                if (liquidAt.test(y)) {
                    return NO_FLOOR;
                }
                target = y;
            }
        }
        else if (!liquidAt.test(topY)) {
            // In open air: the top goes just under the first rock above, with no liquid on the way.
            for (int step = 1; step <= this.searchRange && target == NO_FLOOR; step++) {
                int y = topY + step;
                if (y - 1 > highestTop) {
                    return NO_FLOOR;
                }
                if (rockAt.test(y)) {
                    target = y - 1;
                }
                else if (liquidAt.test(y)) {
                    return NO_FLOOR;
                }
            }
        }
        if (target == NO_FLOOR || target + this.offset > highestTop) {
            return NO_FLOOR;
        }
        return target - topY + this.offset;
    }

    /**
     * Whether more than half of the piece's visible columns would have no floor (or for a hanging
     * piece, no ceiling) within reach: read from the terrain before carving with the carved blocks
     * taken out, so features and structures placed since can't sway it. Only judged when the chunk
     * being placed holds part of the piece and the piece is no more than two chunks across, so every
     * chunk it covers can read every other one.
     */
    private boolean isMostlyHollow(ServerLevelAccessor level, StructurePlaceSettings settings, int anchorY, LongOpenHashSet footprint) {
        BoundingBox chunkBox = settings.getBoundingBox();
        if (footprint.isEmpty() || chunkBox == null || anchorY == UNKNOWN_TOP) {
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
            IntPredicate rock = y -> isRock(terrain.getBlock(y)) && !carved.isCarved(x, y, z);
            int shift = this.ceiling
                    ? this.findCeilingShift(level, anchorY, rock, y -> isLiquid(terrain.getBlock(y)) || (carved.isCarved(x, y, z) && carvesToLava(generator, y)))
                    : this.findShift(level, anchorY, rock);
            if (shift == NO_FLOOR) {
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

    private static boolean isLiquid(BlockState state) {
        return !state.getFluidState().isEmpty();
    }

    /**
     * Whether a cave carved at this height holds lava rather than air: the Nether's carvers fill what
     * they cut in the bottom 32 layers with lava. (Carvers never turn lava or water into air either:
     * they skip blocks they can't replace, so a liquid stays a liquid.)
     */
    private static boolean carvesToLava(ChunkGenerator generator, int y) {
        return y <= generator.getMinY() + 31;
    }

    /**
     * Floor is anything with a collision box: stone, soul sand, nylium, slabs, waterlogged blocks.
     * Air, liquids, plants, fungi, roots, vines, fire and thin snow are open space.
     */
    private static boolean isSolid(LevelReader level, BlockPos pos) {
        return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    private record PlacingTemplate(StructurePlaceSettings settings, int height) {}

    /**
     * What a hanging piece reads in the chunk being placed. While the world generates: the terrain
     * before features, which is the same whatever order chunks generate in. In a finished chunk
     * (a piece placed with {@code /place}): the blocks themselves.
     */
    private static final class Terrain {
        private final LevelReader level;
        @Nullable
        private final ChunkGenerator generator;
        @Nullable
        private final RandomState randomState;
        @Nullable
        private final CarvedTerrain.Mask carved;
        private final Long2ObjectOpenHashMap<NoiseColumn> columns = new Long2ObjectOpenHashMap<>();
        private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        /** The chunk this view was made for, so a placement reused for another chunk gets its own. */
        private final long chunk;

        private Terrain(LevelReader level, @Nullable BoundingBox chunkBox) {
            this.level = level;
            this.chunk = chunkOf(chunkBox);
            CarvedTerrain.Mask mask = null;
            if (chunkBox != null && level instanceof ServerLevelAccessor serverLevel) {
                ChunkAccess chunk = level.getChunk(chunkBox.minX() >> 4, chunkBox.minZ() >> 4);
                if (chunk instanceof ProtoChunk && !(chunk instanceof ImposterProtoChunk)) {
                    mask = CarvedTerrain.of(serverLevel, chunk);
                }
            }
            this.carved = mask;
            if (mask != null) {
                ServerLevelAccessor serverLevel = (ServerLevelAccessor) level;
                this.generator = serverLevel.getLevel().getChunkSource().getGenerator();
                this.randomState = serverLevel.getLevel().getChunkSource().randomState();
            }
            else {
                this.generator = null;
                this.randomState = null;
            }
        }

        private boolean isRock(int x, int y, int z) {
            if (this.carved == null) {
                return isSolid(this.level, this.pos.set(x, y, z));
            }
            return FollowFloorProcessor.isRock(this.column(x, z).getBlock(y)) && !this.carved.isCarved(x, y, z);
        }

        private boolean isLiquid(int x, int y, int z) {
            if (this.carved == null) {
                return !this.level.getFluidState(this.pos.set(x, y, z)).isEmpty();
            }
            return FollowFloorProcessor.isLiquid(this.column(x, z).getBlock(y))
                    || (this.carved.isCarved(x, y, z) && carvesToLava(this.generator, y));
        }

        private boolean isFor(@Nullable BoundingBox chunkBox) {
            return this.chunk == chunkOf(chunkBox);
        }

        private static long chunkOf(@Nullable BoundingBox chunkBox) {
            return chunkBox == null ? Long.MIN_VALUE : FollowFloorProcessor.column(chunkBox.minX() >> 4, chunkBox.minZ() >> 4);
        }

        private NoiseColumn column(int x, int z) {
            long key = FollowFloorProcessor.column(x, z);
            NoiseColumn noiseColumn = this.columns.get(key);
            if (noiseColumn == null) {
                noiseColumn = this.generator.getBaseColumn(x, z, this.level, this.randomState);
                this.columns.put(key, noiseColumn);
            }
            return noiseColumn;
        }
    }

    private static final class ColumnShifts {
        private final FollowFloorProcessor processor;
        private final StructurePlaceSettings settings;
        private final BlockPos piecePos;
        /** The height columns are measured from: the piece's bottom row, or its top row when it hangs. */
        private final int anchorY;
        private final Long2IntOpenHashMap byColumn = new Long2IntOpenHashMap();
        /** Every column the piece has a visible (non-air) block in, inside the chunk being placed or not. */
        private final LongOpenHashSet footprint = new LongOpenHashSet();
        private boolean judged;
        /** Set once the piece is found to be mostly over a hollow: nothing of it is placed. */
        private boolean hollow;
        @Nullable
        private Terrain terrain;

        private ColumnShifts(FollowFloorProcessor processor, StructurePlaceSettings settings, BlockPos piecePos, int anchorY) {
            this.processor = processor;
            this.settings = settings;
            this.piecePos = piecePos;
            this.anchorY = anchorY;
            this.byColumn.defaultReturnValue(NOT_MEASURED);
        }

        private boolean isFor(FollowFloorProcessor processor, StructurePlaceSettings settings, BlockPos piecePos) {
            return this.processor == processor && this.settings == settings && this.piecePos.equals(piecePos);
        }

        private Terrain terrain(LevelReader level) {
            BoundingBox chunkBox = this.settings.getBoundingBox();
            if (this.terrain == null || !this.terrain.isFor(chunkBox)) {
                this.terrain = new Terrain(level, chunkBox);
            }
            return this.terrain;
        }
    }

    //? if <26.2 {
    @Override
    protected StructureProcessorType<?> getType() {
        return this.ceiling ? MoogsStructuresProcessors.FOLLOW_CEILING_PROCESSOR.get() : MoogsStructuresProcessors.FOLLOW_FLOOR_PROCESSOR.get();
    //?} else {
    /*@Override
    public MapCodec<FollowFloorProcessor> codec() {
        return this.ceiling ? CEILING_MAP_CODEC : MAP_CODEC;
    *///?}
    }
}
