package com.finndog.moogs_structures.world.processors;

import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Set;

/**
 * Rewrites the anchor ({@code block_pos}) of block-attached entities to their placed world position.
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
    public static final MapCodec<HangingEntityAnchorProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    private static final Set<String> ANCHORED_ENTITY_IDS = Set.of(
            "minecraft:item_frame",
            "minecraft:glow_item_frame",
            "minecraft:painting",
            "minecraft:leash_knot"
    );

    private static final String ANCHOR_KEY = "block_pos";

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
        String id = nbt.getString("id").orElse("");
        // The id list covers vanilla; the key check picks up modded entities that carry an anchor.
        if (!ANCHORED_ENTITY_IDS.contains(id) && !nbt.contains(ANCHOR_KEY)) {
            return globalEntityInfo;
        }

        BlockPos anchor = globalEntityInfo.blockPos;
        CompoundTag newNbt = nbt.copy();
        BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, anchor)
                .result()
                .ifPresent(encoded -> newNbt.put(ANCHOR_KEY, encoded));
        return new StructureTemplate.StructureEntityInfo(globalEntityInfo.pos, globalEntityInfo.blockPos, newNbt);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.HANGING_ENTITY_ANCHOR_PROCESSOR.get();
    }
}
