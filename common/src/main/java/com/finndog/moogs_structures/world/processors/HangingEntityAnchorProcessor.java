package com.finndog.moogs_structures.world.processors;

import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Set;

/**
 * Rewrites hanging-entity anchors ({@code TileX/TileY/TileZ}) to the entity's placed world
 * position as a structure is placed.
 *
 * <p>Pre-1.21 hanging entities (item frames, paintings, leash knots) store their anchor as
 * absolute world coordinates in NBT, but template placement only rewrites {@code Pos} — so any
 * template-authored anchor is stale at every placement site, fails the ~16-block sanity check in
 * {@code HangingEntity#readAdditionalSaveData}, and logs
 * {@code Hanging entity at invalid position} once per placed entity (the entity itself survives:
 * its anchor is re-derived from {@code Pos}). Even templates saved by a vanilla structure block
 * carry the authoring world's coordinates and log this. 1.21+ removed the fields and derives the
 * anchor from the entity position, which is the behavior this processor gives the pre-1.21 path.
 *
 * <p>{@code VersionAwareSinglePoolElement} appends this processor to every placement it drives, so
 * versioned structures get it with no datapack changes; other pieces can opt in via
 * {@code moogs_structures:hanging_entity_anchor_processor} in a processor list.
 */
public class HangingEntityAnchorProcessor extends StructureEntityProcessor {

    public static final HangingEntityAnchorProcessor INSTANCE = new HangingEntityAnchorProcessor();
    public static final Codec<HangingEntityAnchorProcessor> CODEC = Codec.unit(() -> INSTANCE);

    private static final Set<String> HANGING_ENTITY_IDS = Set.of(
            "minecraft:item_frame",
            "minecraft:glow_item_frame",
            "minecraft:painting",
            "minecraft:leash_knot"
    );

    private HangingEntityAnchorProcessor() { }

    @Override
    public StructureTemplate.StructureEntityInfo processEntity(ServerLevelAccessor serverLevelAccessor,
                                                               BlockPos structurePiecePos,
                                                               BlockPos structurePieceBottomCenterPos,
                                                               StructureTemplate.StructureEntityInfo localEntityInfo,
                                                               StructureTemplate.StructureEntityInfo globalEntityInfo,
                                                               StructurePlaceSettings structurePlaceSettings) {
        CompoundTag nbt = globalEntityInfo.nbt;
        if (nbt == null || !HANGING_ENTITY_IDS.contains(nbt.getString("id"))) {
            return globalEntityInfo;
        }
        BlockPos anchor = globalEntityInfo.blockPos;
        CompoundTag newNbt = nbt.copy();
        newNbt.putInt("TileX", anchor.getX());
        newNbt.putInt("TileY", anchor.getY());
        newNbt.putInt("TileZ", anchor.getZ());
        return new StructureTemplate.StructureEntityInfo(globalEntityInfo.pos, globalEntityInfo.blockPos, newNbt);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.HANGING_ENTITY_ANCHOR_PROCESSOR.get();
    }
}
