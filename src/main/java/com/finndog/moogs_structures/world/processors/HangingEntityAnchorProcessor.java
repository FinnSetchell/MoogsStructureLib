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
import net.minecraft.nbt.CompoundTag;
//? if >=1.21.5 {
/*import net.minecraft.nbt.NbtOps;
*///?}
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
//? if <26.2 {
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
//?}
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Puts block-attached entities (item frames, paintings, leash knots) on the right block when a template is placed.
 *
 * <p>These entities store the block they hang in (their anchor, {@code TileX/TileY/TileZ} or {@code block_pos})
 * as absolute world coordinates, so the value baked into a template is stale wherever the structure lands. And
 * placement moves each entity to its placed centre, which makes the block holding that centre its new anchor.
 * A painting an even number of blocks tall or wide is centred on the line between two blocks, so that put it
 * one block too high, or one block to the side. This works out the right anchor from the placed centre, writes
 * it, and hands placement the anchor's own centre, which always lands back in it.
 *
 * <p>MSL's pool elements attach this automatically, so structures need no datapack changes; other
 * pieces can opt in with {@code moogs_structures:hanging_entity_anchor_processor}.
 */
public class HangingEntityAnchorProcessor extends StructureEntityProcessor {

    public static final HangingEntityAnchorProcessor INSTANCE = new HangingEntityAnchorProcessor();
    //? if >=1.20.6 <26.2 {
    public static final MapCodec<HangingEntityAnchorProcessor> CODEC = MapCodec.unit(() -> INSTANCE);
    //?}
    //? if <1.20.6 {
    /*public static final Codec<HangingEntityAnchorProcessor> CODEC = Codec.unit(() -> INSTANCE);
    *///?}
    //? if >=26.2 {
    /*public static final MapCodec<HangingEntityAnchorProcessor> MAP_CODEC = MapCodec.unit(() -> INSTANCE);
    *///?}

    private static final Set<String> ANCHORED_ENTITY_IDS = Set.of(
            "minecraft:item_frame",
            "minecraft:glow_item_frame",
            "minecraft:painting",
            "minecraft:leash_knot"
    );

    //? if >=1.21.5 {
    /*private static final String ANCHOR_KEY = "block_pos";

    *///?}
    private HangingEntityAnchorProcessor() { }

    @Override
    public StructureTemplate.StructureEntityInfo processEntity(ServerLevelAccessor serverLevelAccessor,
                                                               BlockPos structurePiecePos,
                                                               BlockPos structurePieceBottomCenterPos,
                                                               StructureTemplate.StructureEntityInfo localEntityInfo,
                                                               StructureTemplate.StructureEntityInfo globalEntityInfo,
                                                               StructurePlaceSettings structurePlaceSettings) {
        CompoundTag nbt = globalEntityInfo.nbt;
        if (nbt == null) {
            return globalEntityInfo;
        }
        //? if <1.21.5 {
        String id = nbt.getString("id");
        //?} else {
        /*String id = nbt.getString("id").orElse("");
        *///?}
        // The id list covers vanilla; the key check picks up modded entities that carry an anchor.
        //? if <1.21.5 {
        if (!ANCHORED_ENTITY_IDS.contains(id) && !nbt.contains("TileX")) {
        //?} else {
        /*if (!ANCHORED_ENTITY_IDS.contains(id) && !nbt.contains(ANCHOR_KEY)) {
        *///?}
            return globalEntityInfo;
        }

        CompoundTag newNbt = nbt.copy();
        Mirror mirror = structurePlaceSettings.getMirror();
        Rotation rotation = structurePlaceSettings.getRotation();
        Direction stored = readFacing(newNbt);
        // Where it faces once placed, turned the way its wall is: mirrored first, then rotated.
        Direction facing = stored == null ? null : rotation.rotate(mirror.mirror(stored));
        //? if forge || neoforge {
        /*// Forge and NeoForge place entities with the game's own code, which rotates a hanging entity before
        // mirroring it. With a mirror and a quarter turn that faces it into its wall, so store the facing
        // that rotating then mirroring turns into the right one.
        if (facing != null && facing.getAxis() != Direction.Axis.Y) {
            writeFacing(newNbt, inverse(rotation).rotate(mirror.mirror(facing)));
        }
        *///?}

        BlockPos anchor = anchorOf(globalEntityInfo.pos, facing);
        //? if <1.21.5 {
        newNbt.putInt("TileX", anchor.getX());
        newNbt.putInt("TileY", anchor.getY());
        newNbt.putInt("TileZ", anchor.getZ());
        //?} else {
        /*BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, anchor)
                .result()
                .ifPresent(encoded -> newNbt.put(ANCHOR_KEY, encoded));
        *///?}
        // Placement makes the block holding this position the entity's anchor, and a block's centre is in it.
        return new StructureTemplate.StructureEntityInfo(Vec3.atCenterOf(anchor), anchor, newNbt);
    }

    /**
     * The block a block-attached entity hangs in, from its placed centre. A painting an even number of blocks
     * wide or tall is centred on the line between two blocks, and its anchor is the lower one and the one on
     * the clockwise side of its facing. A quarter block down and clockwise from the centre lands in that block,
     * and never leaves it for odd sizes, item frames or knots. Without a sideways facing (a knot, or an item
     * frame on a floor or ceiling) the entity is one block, so its centre's block is the anchor.
     */
    private static BlockPos anchorOf(Vec3 centre, @Nullable Direction facing) {
        if (facing == null || facing.getAxis() == Direction.Axis.Y) {
            return BlockPos.containing(centre);
        }
        Direction clockwise = facing.getClockWise();
        return BlockPos.containing(
                centre.x + 0.25 * clockwise.getStepX(),
                centre.y - 0.25,
                centre.z + 0.25 * clockwise.getStepZ());
    }

    /** Paintings store their facing as a 2D value under {@code facing}, item frames as a 3D value under {@code Facing}. */
    @Nullable
    private static Direction readFacing(CompoundTag nbt) {
        //? if <1.21.5 {
        if (nbt.contains("facing")) return Direction.from2DDataValue(nbt.getByte("facing"));
        if (nbt.contains("Facing")) return Direction.from3DDataValue(nbt.getByte("Facing"));
        //?} else {
        /*if (nbt.contains("facing")) return Direction.from2DDataValue(nbt.getByteOr("facing", (byte) 0));
        if (nbt.contains("Facing")) return Direction.from3DDataValue(nbt.getByteOr("Facing", (byte) 0));
        *///?}
        return null;
    }

    //? if forge || neoforge {
    /*private static void writeFacing(CompoundTag nbt, Direction facing) {
        if (nbt.contains("facing")) {
            nbt.putByte("facing", (byte) facing.get2DDataValue());
        } else {
            nbt.putByte("Facing", (byte) facing.get3DDataValue());
        }
    }

    private static Rotation inverse(Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
            case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            default -> rotation;
        };
    }
    *///?}

    @Override
    //? if <26.2 {
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.HANGING_ENTITY_ANCHOR_PROCESSOR.get();
    //?} else {
    /*public MapCodec<HangingEntityAnchorProcessor> codec() {
        return MAP_CODEC;
    *///?}
    }
}
