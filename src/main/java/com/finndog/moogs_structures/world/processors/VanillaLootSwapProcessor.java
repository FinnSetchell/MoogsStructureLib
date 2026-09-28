package com.finndog.moogs_structures.world.processors;

import com.finndog.moogs_structures.config.ReplaceVanillaManager;
//? if <26.2 {
import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
//?}
import com.mojang.serialization.Codec;
//? if >=1.20.6 {
import com.mojang.serialization.MapCodec;
//?}
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
//? if <1.21.5 {
import net.minecraft.nbt.Tag;
//?}
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
//? if <26.2 {
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
//?}
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Map;
//? if >=26.2 {
/*import java.util.Optional;
*///?}
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;

/**
 * Rewrites a container's LootTable to the vanilla equivalent when the replacement for
 * (modid, vanilla_key) is enabled, so mods that inject into the vanilla loot table still
 * fill the replacing mod's chests. When the toggle is off the mod's own loot table stays.
 */
public class VanillaLootSwapProcessor extends StructureEntityProcessor {

    //? if >=1.20.6 <26.2 {
    public static final MapCodec<VanillaLootSwapProcessor> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
    //?}
    //? if <1.20.6 {
    /*public static final Codec<VanillaLootSwapProcessor> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
    *///?}
    //? if >=26.2 {
    /*public static final MapCodec<VanillaLootSwapProcessor> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
    *///?}
            Codec.STRING.fieldOf("modid").forGetter(p -> p.modid),
            Codec.STRING.fieldOf("vanilla_key").forGetter(p -> p.vanillaKey),
            Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC).fieldOf("loot_table_mapping").forGetter(p -> p.lootTableMapping),
            Codec.STRING.optionalFieldOf("seed_strategy", "preserve").forGetter(p -> p.seedStrategy)
    ).apply(instance, instance.stable(VanillaLootSwapProcessor::new)));

    private final String modid;
    private final String vanillaKey;
    private final Map<ResourceLocation, ResourceLocation> lootTableMapping;
    private final String seedStrategy;

    private VanillaLootSwapProcessor(String modid, String vanillaKey, Map<ResourceLocation, ResourceLocation> lootTableMapping, String seedStrategy) {
        this.modid = modid;
        this.vanillaKey = vanillaKey;
        this.lootTableMapping = lootTableMapping;
        this.seedStrategy = seedStrategy;
    }

    @Override
    //? if <26.2 {
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldReader, BlockPos pos, BlockPos blockPos, StructureTemplate.StructureBlockInfo localInfo, StructureTemplate.StructureBlockInfo worldInfo, StructurePlaceSettings settings) {
        CompoundTag nbt = worldInfo.nbt();
    //?} else {
    /*public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldReader, BlockPos targetPosition, BlockPos referencePos, BlockPos templateRelativePos, StructureTemplate.StructureBlockInfo processedBlockInfo, StructurePlaceSettings settings) {
        CompoundTag nbt = processedBlockInfo.nbt();
    *///?}
        //? if <1.21.5 {
        if (nbt == null || !nbt.contains("LootTable", Tag.TAG_STRING)) {
        //?} else {
        /*if (nbt == null) {
        *///?}
            //? if >=1.21.5 <26.2 {
            /*return worldInfo;
            *///?}
            //? if >=26.2 {
            /*return processedBlockInfo;
            *///?}
        //? if >=1.21.5 {
        /*}
        // 1.21.5: CompoundTag.getString returns Optional<String> (empty when absent or non-string);
        // the old contains(key, TAG_STRING) overload was removed.
        *///?}
        //? if >=1.21.5 <26.2 {
        /*java.util.Optional<String> lootTable = nbt.getString("LootTable");
        *///?}
        //? if >=26.2 {
        /*Optional<String> lootTable = nbt.getString("LootTable");
        *///?}
        //? if >=1.21.5 {
        /*if (lootTable.isEmpty()) {
        *///?}
            //? if <26.2 {
            return worldInfo;
            //?} else {
            /*return processedBlockInfo;
            *///?}
        }

        //? if <1.21.5 {
        ResourceLocation current = ResourceLocation.tryParse(nbt.getString("LootTable"));
        //?} else {
        /*ResourceLocation current = ResourceLocation.tryParse(lootTable.get());
        *///?}
        ResourceLocation target = current == null ? null : lootTableMapping.get(current);
        if (target == null || !ReplaceVanillaManager.isEnabled(modid, vanillaKey)) {
            //? if <26.2 {
            return worldInfo;
            //?} else {
            /*return processedBlockInfo;
            *///?}
        }

        CompoundTag newNbt = nbt.copy();
        newNbt.putString("LootTable", target.toString());
        switch (seedStrategy) {
            //? if <26.2 {
            case "randomize" -> newNbt.putLong("LootTableSeed", settings.getRandom(worldInfo.pos()).nextLong());
            //?} else {
            /*case "randomize" -> newNbt.putLong("LootTableSeed", settings.getRandom(processedBlockInfo.pos()).nextLong());
            *///?}
            case "clear" -> newNbt.remove("LootTableSeed");
            default -> { }
        }
        //? if <26.2 {
        return new StructureTemplate.StructureBlockInfo(worldInfo.pos(), worldInfo.state(), newNbt);
        //?} else {
        /*return new StructureTemplate.StructureBlockInfo(processedBlockInfo.pos(), processedBlockInfo.state(), newNbt);
        *///?}
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
                //? if <26.2 {
                structurePieceBottomCenterPos, asBlock, asBlock, structurePlaceSettings);
                //?} else {
                /*structurePieceBottomCenterPos, globalEntityInfo.blockPos, asBlock, structurePlaceSettings);
                *///?}
        if (swapped == asBlock || swapped == null) {
            return globalEntityInfo;
        }
        return new StructureTemplate.StructureEntityInfo(globalEntityInfo.pos, globalEntityInfo.blockPos, swapped.nbt());
    }

    @Override
    //? if <26.2 {
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.VANILLA_LOOT_SWAP_PROCESSOR.get();
    //?} else {
    /*public MapCodec<VanillaLootSwapProcessor> codec() {
        return MAP_CODEC;
    *///?}
    }
}
