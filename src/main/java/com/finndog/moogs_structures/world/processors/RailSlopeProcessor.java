package com.finndog.moogs_structures.world.processors;

//? if <26.2 {
import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
//?}
//? if >=1.20.6 {
import com.mojang.serialization.MapCodec;
//?} else {
/*import com.mojang.serialization.Codec;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
//? if <26.2 {
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
//?}
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

/**
 * Slopes straight rails on terrain_matching pieces so the track follows the ground.
 * <p>
 * Rails keep the shape saved in the template: world generation never runs a rail's own
 * shaping. Terrain matching moves every column to the ground on its own, so a rail one
 * block below the next one would stay flat and run into the bank. This processor reads
 * the same heightmap terrain matching uses, for the rail's column and the column at each
 * end, and turns the rail into a slope towards an end that is exactly one block higher.
 * A step of two or more stays flat, which leaves a break in the track.
 * <p>
 * Its rails also come out pointing the right way where the game's own rail rotation is wrong (see
 * {@link RailRotationProcessor}).
 * <p>
 * The next rail's height comes from the same heightmap rule, so this works across piece
 * joins and chunk borders without seeing the neighbouring piece, as long as every track
 * piece keeps its rails at the same height in the template.
 */
//? if <26.2 {
public class RailSlopeProcessor extends StructureProcessor {
//?} else {
/*public class RailSlopeProcessor implements StructureProcessor {
*///?}
    public static final RailSlopeProcessor INSTANCE = new RailSlopeProcessor();

    //? if >=1.20.6 <26.2 {
    public static final MapCodec<RailSlopeProcessor> CODEC = MapCodec.unit(() -> INSTANCE);
    //?}
    //? if <1.20.6 {
    /*public static final Codec<RailSlopeProcessor> CODEC = Codec.unit(() -> INSTANCE);
    *///?}
    //? if >=26.2 {
    /*public static final MapCodec<RailSlopeProcessor> MAP_CODEC = MapCodec.unit(() -> INSTANCE);
    *///?}

    private RailSlopeProcessor() {}

    @Nullable
    @Override
    //? if <26.2 {
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos templateOffset, BlockPos pivot, StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings placeSettings) {
    //?} else {
    /*public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos targetPosition, BlockPos referencePos, BlockPos templateRelativePos, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings placeSettings) {
    *///?}
        BlockState state = structureBlockInfoWorld.state();
        if (!(state.getBlock() instanceof BaseRailBlock rail)) {
            return structureBlockInfoWorld;
        }

        // The state is still in the template's frame: placement mirrors and rotates it afterwards.
        // firstEnd is the template's north (or east) end; ties go to it.
        Property<RailShape> shapeProperty = rail.getShapeProperty();
        Direction firstEnd;
        switch (state.getValue(shapeProperty)) {
            case NORTH_SOUTH, ASCENDING_NORTH, ASCENDING_SOUTH -> firstEnd = Direction.NORTH;
            case EAST_WEST, ASCENDING_EAST, ASCENDING_WEST -> firstEnd = Direction.EAST;
            default -> {
                return structureBlockInfoWorld; // curves can't slope
            }
        }

        BlockPos pos = structureBlockInfoWorld.pos();
        // Blocks outside the chunk being generated are thrown away at placement, so skip their height reads.
        BoundingBox chunkBox = placeSettings.getBoundingBox();
        if (chunkBox != null && !chunkBox.isInside(pos)) {
            return structureBlockInfoWorld;
        }

        // Same heightmap as terrain matching's gravity processor, which swaps to the live one under /place.
        Heightmap.Types heightmap = levelReader instanceof ServerLevel ? Heightmap.Types.WORLD_SURFACE : Heightmap.Types.WORLD_SURFACE_WG;
        Mirror mirror = placeSettings.getMirror();
        Rotation rotation = placeSettings.getRotation();
        Direction worldFirstEnd = rotation.rotate(mirror.mirror(firstEnd));
        int here = levelReader.getHeight(heightmap, pos.getX(), pos.getZ());
        int atFirstEnd = levelReader.getHeight(heightmap, pos.getX() + worldFirstEnd.getStepX(), pos.getZ() + worldFirstEnd.getStepZ());
        int atSecondEnd = levelReader.getHeight(heightmap, pos.getX() - worldFirstEnd.getStepX(), pos.getZ() - worldFirstEnd.getStepZ());

        RailShape shape;
        if (atFirstEnd - here == 1) {
            shape = RailRotationProcessor.slopeTowards(firstEnd);
        }
        else if (atSecondEnd - here == 1) {
            shape = RailRotationProcessor.slopeTowards(firstEnd.getOpposite());
        }
        else {
            shape = firstEnd == Direction.NORTH ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST;
        }

        BlockState sloped = state.setValue(shapeProperty, shape);
        // MSL's own structures get the rail rotation processor last, which turns every rail the right way.
        // Anywhere else, do it here, or a flat rail in a piece turned 180 degrees can come out sideways.
        if (!placeSettings.getProcessors().contains(RailRotationProcessor.INSTANCE)) {
            sloped = RailRotationProcessor.placeCorrectly(sloped, shapeProperty, mirror, rotation);
        }
        return new StructureTemplate.StructureBlockInfo(pos, sloped, structureBlockInfoWorld.nbt());
    }

    //? if <26.2 {
    @Override
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.RAIL_SLOPE_PROCESSOR.get();
    //?} else {
    /*@Override
    public MapCodec<RailSlopeProcessor> codec() {
        return MAP_CODEC;
    *///?}
    }
}
