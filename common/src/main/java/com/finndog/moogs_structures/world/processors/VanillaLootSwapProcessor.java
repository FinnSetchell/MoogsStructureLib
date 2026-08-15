package com.finndog.moogs_structures.world.processors;

import com.finndog.moogs_structures.config.ReplaceVanillaManager;
import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Map;

/**
 * Rewrites a container's LootTable to the vanilla equivalent when the replacement for
 * (modid, vanilla_key) is enabled, so mods that inject into the vanilla loot table still
 * fill the replacing mod's chests. When the toggle is off the mod's own loot table stays.
 */
public class VanillaLootSwapProcessor extends StructureProcessor {

    public static final MapCodec<VanillaLootSwapProcessor> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
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
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldReader, BlockPos pos, BlockPos blockPos, StructureTemplate.StructureBlockInfo localInfo, StructureTemplate.StructureBlockInfo worldInfo, StructurePlaceSettings settings) {
        CompoundTag nbt = worldInfo.nbt();
        if (nbt == null || !nbt.contains("LootTable", Tag.TAG_STRING)) {
            return worldInfo;
        }

        ResourceLocation current = ResourceLocation.tryParse(nbt.getString("LootTable"));
        ResourceLocation target = current == null ? null : lootTableMapping.get(current);
        if (target == null || !ReplaceVanillaManager.isEnabled(modid, vanillaKey)) {
            return worldInfo;
        }

        CompoundTag newNbt = nbt.copy();
        newNbt.putString("LootTable", target.toString());
        switch (seedStrategy) {
            case "randomize" -> newNbt.putLong("LootTableSeed", settings.getRandom(worldInfo.pos()).nextLong());
            case "clear" -> newNbt.remove("LootTableSeed");
            default -> { }
        }
        return new StructureTemplate.StructureBlockInfo(worldInfo.pos(), worldInfo.state(), newNbt);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.VANILLA_LOOT_SWAP_PROCESSOR.get();
    }
}
