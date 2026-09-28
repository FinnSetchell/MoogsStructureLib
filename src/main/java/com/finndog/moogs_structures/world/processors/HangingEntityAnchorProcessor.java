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
import net.minecraft.nbt.CompoundTag;
//? if >=1.21.5 {
/*import net.minecraft.nbt.NbtOps;
*///?}
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
//? if <26.2 {
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
//?}
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Set;

/**
 * Rewrites the anchor ({@code TileX/TileY/TileZ}) of block-attached entities to their placed world position.
 *
 * <p>Item frames, paintings and leash knots store the block they hang on as absolute world
 * coordinates. Template placement rewrites an entity's {@code Pos} but never that anchor, so the
 * value baked into a template is stale as soon as the structure is placed somewhere else. Vanilla
 * notices, logs {@code invalid position} once per entity, and falls back to the entity's own block
 * position. Writing the anchor here produces that same position up front, so placement is unchanged
 * and the error stops.
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

        BlockPos anchor = globalEntityInfo.blockPos;
        CompoundTag newNbt = nbt.copy();
        //? if <1.21.5 {
        newNbt.putInt("TileX", anchor.getX());
        newNbt.putInt("TileY", anchor.getY());
        newNbt.putInt("TileZ", anchor.getZ());
        //?} else {
        /*BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, anchor)
                .result()
                .ifPresent(encoded -> newNbt.put(ANCHOR_KEY, encoded));
        *///?}
        return new StructureTemplate.StructureEntityInfo(globalEntityInfo.pos, globalEntityInfo.blockPos, newNbt);
    }

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
