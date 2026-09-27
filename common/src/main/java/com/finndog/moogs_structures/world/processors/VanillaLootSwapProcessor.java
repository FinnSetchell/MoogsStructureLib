package com.finndog.moogs_structures.world.processors;

import com.finndog.moogs_structures.config.ReplaceVanillaManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Map;
import java.util.Optional;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;

/**
 * Rewrites a container's LootTable to the vanilla equivalent when the replacement for
 * (modid, vanilla_key) is enabled, so mods that inject into the vanilla loot table still
 * fill the replacing mod's chests. When the toggle is off the mod's own loot table stays.
 */
public class VanillaLootSwapProcessor extends StructureEntityProcessor {

    public static final MapCodec<VanillaLootSwapProcessor> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            Codec.STRING.fieldOf("modid").forGetter(p -> p.modid),
            Codec.STRING.fieldOf("vanilla_key").forGetter(p -> p.vanillaKey),
            Codec.unboundedMap(Identifier.CODEC, Identifier.CODEC).fieldOf("loot_table_mapping").forGetter(p -> p.lootTableMapping),
            Codec.STRING.optionalFieldOf("seed_strategy", "preserve").forGetter(p -> p.seedStrategy)
    ).apply(instance, instance.stable(VanillaLootSwapProcessor::new)));

    private final String modid;
    private final String vanillaKey;
    private final Map<Identifier, Identifier> lootTableMapping;
    private final String seedStrategy;

    private VanillaLootSwapProcessor(String modid, String vanillaKey, Map<Identifier, Identifier> lootTableMapping, String seedStrategy) {
        this.modid = modid;
        this.vanillaKey = vanillaKey;
        this.lootTableMapping = lootTableMapping;
        this.seedStrategy = seedStrategy;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldReader, BlockPos targetPosition, BlockPos referencePos, BlockPos templateRelativePos, StructureTemplate.StructureBlockInfo processedBlockInfo, StructurePlaceSettings settings) {
        CompoundTag nbt = processedBlockInfo.nbt();
        if (nbt == null) {
            return processedBlockInfo;
        }
        // CompoundTag.getString returns Optional<String> (empty when absent or non-string);
        // the old contains(key, TAG_STRING) overload was removed.
        Optional<String> lootTable = nbt.getString("LootTable");
        if (lootTable.isEmpty()) {
            return processedBlockInfo;
        }

        Identifier current = Identifier.tryParse(lootTable.get());
        Identifier target = current == null ? null : lootTableMapping.get(current);
        if (target == null || !ReplaceVanillaManager.isEnabled(modid, vanillaKey)) {
            return processedBlockInfo;
        }

        CompoundTag newNbt = nbt.copy();
        newNbt.putString("LootTable", target.toString());
        switch (seedStrategy) {
            case "randomize" -> newNbt.putLong("LootTableSeed", settings.getRandom(processedBlockInfo.pos()).nextLong());
            case "clear" -> newNbt.remove("LootTableSeed");
            default -> { }
        }
        return new StructureTemplate.StructureBlockInfo(processedBlockInfo.pos(), processedBlockInfo.state(), newNbt);
    }

    // Chest and hopper minecarts carry the same LootTable/LootTableSeed keys as containers, so run the
    // entity's NBT through processBlock as a stand-in block at the entity's position.
    @Override
    public StructureTemplate.StructureEntityInfo processEntity(ServerLevelAccessor serverLevelAccessor,
                                                               BlockPos structurePiecePos,
                                                               BlockPos structurePieceBottomCenterPos,
                                                               StructureTemplate.StructureEntityInfo localEntityInfo,
                                                               StructureTemplate.StructureEntityInfo globalEntityInfo,
                                                               StructurePlaceSettings structurePlaceSettings) {
        if (globalEntityInfo.nbt == null) {
            return globalEntityInfo;
        }
        StructureTemplate.StructureBlockInfo asBlock = new StructureTemplate.StructureBlockInfo(
                globalEntityInfo.blockPos, Blocks.AIR.defaultBlockState(), globalEntityInfo.nbt);
        StructureTemplate.StructureBlockInfo swapped = processBlock(serverLevelAccessor, structurePiecePos,
                structurePieceBottomCenterPos, globalEntityInfo.blockPos, asBlock, structurePlaceSettings);
        if (swapped == asBlock || swapped == null) {
            return globalEntityInfo;
        }
        return new StructureTemplate.StructureEntityInfo(globalEntityInfo.pos, globalEntityInfo.blockPos, swapped.nbt());
    }

    @Override
    public MapCodec<VanillaLootSwapProcessor> codec() {
        return MAP_CODEC;
    }
}
