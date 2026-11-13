package com.finndog.moogs_structures.world.processors;

import com.finndog.moogs_structures.mixins.structures.SinglePoolElementAccessor;
//? if <26.2 {
import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
//?}
import com.mojang.serialization.Codec;
//? if >=1.20.6 {
import com.mojang.serialization.MapCodec;
//?}
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
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
                    Codec.intRange(1, 64).fieldOf("search_range").orElse(16).forGetter((p) -> p.searchRange),
                    Codec.INT.fieldOf("offset").orElse(0).forGetter((p) -> p.offset)
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
        // Blocks outside the chunk being placed are thrown away after processing, and their columns
        // may not be generated yet, so leave them alone. Their own chunk's pass moves them.
        if (!isInChunk(settings, pos)) {
            return blockInfo;
        }
        int shift = this.columnShift(levelReader, settings, piecePos, pos.getX(), pos.getZ());
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
        if (!isInChunk(structurePlaceSettings, blockPos)) {
            return globalEntityInfo;
        }
        int shift = this.columnShift(serverLevelAccessor, structurePlaceSettings, structurePiecePos, blockPos.getX(), blockPos.getZ());
        if (shift == NO_FLOOR) {
            return null;
        }
        if (shift == 0) {
            return globalEntityInfo;
        }
        return new StructureTemplate.StructureEntityInfo(globalEntityInfo.pos.add(0, shift, 0), blockPos.offset(0, shift, 0), globalEntityInfo.nbt);
    }

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

    private int columnShift(LevelReader level, StructurePlaceSettings settings, BlockPos piecePos, int x, int z) {
        ColumnShifts shifts = COLUMN_SHIFTS.get();
        if (shifts == null || !shifts.isFor(this, settings, piecePos)) {
            shifts = new ColumnShifts(this, settings, piecePos.immutable());
            COLUMN_SHIFTS.set(shifts);
        }
        long column = ((long) x << 32) | (z & 0xFFFFFFFFL);
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

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, baseY, z);
        boolean climb = isSolid(level, pos);
        for (int step = 1; step <= this.searchRange; step++) {
            int y = climb ? baseY + step : baseY - step;
            if (y < minY || y > maxY) {
                return NO_FLOOR;
            }
            boolean solid = isSolid(level, pos.setY(y));
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
