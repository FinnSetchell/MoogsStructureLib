package com.finndog.moogs_structures.world.processors;

//? if <26.2 {
import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
//?}
import com.finndog.moogs_structures.world.structures.GenericJigsawStructure;
//? if >=1.20.6 {
import com.mojang.serialization.MapCodec;
//?} else {
/*import com.mojang.serialization.Codec;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
//? if <26.2 {
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
//?}
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

/**
 * Makes rails come out of a rotated or mirrored piece pointing the way they were built.
 * <p>
 * Placement mirrors and then rotates each rail with the rail's own methods. Up to 1.21.5 those turn a flat
 * straight rail sideways when the piece is turned 180 degrees (MC-196102): regular rails on 1.20-1.20.1 and
 * powered, activator and detector rails on 1.20-1.21.5, with Forge and NeoForge still getting detector rails
 * wrong. This processor works out where each rail should point in the world and sets whichever template
 * shape those same methods turn into it. Where the game already gets it right, nothing changes.
 * <p>
 * MSL adds it, last, to every piece of its own structure types (see the StructureStart and StructureTemplate
 * mixins), and only to those, so vanilla's and other mods' structures keep the game's behaviour.
 */
//? if <26.2 {
public class RailRotationProcessor extends StructureProcessor {
//?} else {
/*public class RailRotationProcessor implements StructureProcessor {
*///?}
    public static final RailRotationProcessor INSTANCE = new RailRotationProcessor();

    //? if >=1.20.6 <26.2 {
    public static final MapCodec<RailRotationProcessor> CODEC = MapCodec.unit(() -> INSTANCE);
    //?}
    //? if <1.20.6 {
    /*public static final Codec<RailRotationProcessor> CODEC = Codec.unit(() -> INSTANCE);
    *///?}
    //? if >=26.2 {
    /*public static final MapCodec<RailRotationProcessor> MAP_CODEC = MapCodec.unit(() -> INSTANCE);
    *///?}

    private static final ThreadLocal<Boolean> PLACING_MSL_STRUCTURE = ThreadLocal.withInitial(() -> false);

    private RailRotationProcessor() {}

    /** Called as a structure starts placing its pieces in a chunk. */
    public static void startPlacing(Structure structure) {
        PLACING_MSL_STRUCTURE.set(structure instanceof GenericJigsawStructure);
    }

    /** Called when that structure has finished placing in the chunk. */
    public static void stopPlacing() {
        PLACING_MSL_STRUCTURE.set(false);
    }

    /** Adds this processor, last, to a template being placed for one of MSL's own structures. */
    public static void addWhilePlacingMslStructure(StructurePlaceSettings placeSettings) {
        if (PLACING_MSL_STRUCTURE.get() && !placeSettings.getProcessors().contains(INSTANCE)) {
            placeSettings.addProcessor(INSTANCE);
        }
    }

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
        BlockState placed = placeCorrectly(state, rail.getShapeProperty(), placeSettings.getMirror(), placeSettings.getRotation());
        return placed == state ? structureBlockInfoWorld : new StructureTemplate.StructureBlockInfo(structureBlockInfoWorld.pos(), placed, structureBlockInfoWorld.nbt());
    }

    /**
     * Returns the template state that placement (the rail's own mirror, then rotate) turns into the shape this
     * rail should have in the world. That is the state itself unless the game's rotation gets this rail wrong.
     */
    public static BlockState placeCorrectly(BlockState state, Property<RailShape> shapeProperty, Mirror mirror, Rotation rotation) {
        RailShape wanted = placedShape(state.getValue(shapeProperty), mirror, rotation);
        if (state.mirror(mirror).rotate(rotation).getValue(shapeProperty) == wanted) {
            return state;
        }
        for (RailShape candidate : shapeProperty.getPossibleValues()) {
            BlockState candidateState = state.setValue(shapeProperty, candidate);
            if (candidateState.mirror(mirror).rotate(rotation).getValue(shapeProperty) == wanted) {
                return candidateState;
            }
        }
        return state;
    }

    /** Where a rail shape should point after a piece is mirrored, then rotated. */
    public static RailShape placedShape(RailShape shape, Mirror mirror, Rotation rotation) {
        return switch (shape) {
            case NORTH_SOUTH, EAST_WEST -> turn(shape == RailShape.NORTH_SOUTH ? Direction.NORTH : Direction.EAST, mirror, rotation).getAxis() == Direction.Axis.Z ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST;
            case ASCENDING_NORTH -> slopeTowards(turn(Direction.NORTH, mirror, rotation));
            case ASCENDING_SOUTH -> slopeTowards(turn(Direction.SOUTH, mirror, rotation));
            case ASCENDING_EAST -> slopeTowards(turn(Direction.EAST, mirror, rotation));
            case ASCENDING_WEST -> slopeTowards(turn(Direction.WEST, mirror, rotation));
            case SOUTH_EAST -> curve(turn(Direction.SOUTH, mirror, rotation), turn(Direction.EAST, mirror, rotation));
            case SOUTH_WEST -> curve(turn(Direction.SOUTH, mirror, rotation), turn(Direction.WEST, mirror, rotation));
            case NORTH_WEST -> curve(turn(Direction.NORTH, mirror, rotation), turn(Direction.WEST, mirror, rotation));
            case NORTH_EAST -> curve(turn(Direction.NORTH, mirror, rotation), turn(Direction.EAST, mirror, rotation));
        };
    }

    public static RailShape slopeTowards(Direction direction) {
        return switch (direction) {
            case NORTH -> RailShape.ASCENDING_NORTH;
            case SOUTH -> RailShape.ASCENDING_SOUTH;
            case EAST -> RailShape.ASCENDING_EAST;
            default -> RailShape.ASCENDING_WEST;
        };
    }

    private static Direction turn(Direction direction, Mirror mirror, Rotation rotation) {
        return rotation.rotate(mirror.mirror(direction));
    }

    private static RailShape curve(Direction one, Direction other) {
        boolean north = one == Direction.NORTH || other == Direction.NORTH;
        boolean east = one == Direction.EAST || other == Direction.EAST;
        return north ? (east ? RailShape.NORTH_EAST : RailShape.NORTH_WEST) : (east ? RailShape.SOUTH_EAST : RailShape.SOUTH_WEST);
    }

    //? if <26.2 {
    @Override
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.RAIL_ROTATION_PROCESSOR.get();
    //?} else {
    /*@Override
    public MapCodec<RailRotationProcessor> codec() {
        return MAP_CODEC;
    *///?}
    }
}
