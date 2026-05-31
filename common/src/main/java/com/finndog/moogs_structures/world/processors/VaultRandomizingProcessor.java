package com.finndog.moogs_structures.world.processors;

import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

/**
 * Configures a {@code minecraft:vault} block entity at placement time. Strips runtime state
 * (cooldowns, rewarded-player lists) and rebuilds {@code config.loot_table} /
 * {@code config.key_item} from this processor's fields, choosing the normal vs ominous variant
 * based on the existing blockstate's {@code ominous} property.
 *
 * <p>The blockstate is preserved; only the block entity NBT is rewritten.
 */
public class VaultRandomizingProcessor extends StructureProcessor {

    private static final ResourceLocation DEFAULT_KEY = ResourceLocation.parse("minecraft:trial_key");
    private static final ResourceLocation DEFAULT_OMINOUS_KEY = ResourceLocation.parse("minecraft:ominous_trial_key");

    public static final MapCodec<VaultRandomizingProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("loot_table").forGetter(p -> p.lootTable),
            ResourceLocation.CODEC.optionalFieldOf("ominous_loot_table").forGetter(p -> p.ominousLootTable),
            ResourceLocation.CODEC.optionalFieldOf("key_item", DEFAULT_KEY).forGetter(p -> p.keyItem),
            ResourceLocation.CODEC.optionalFieldOf("ominous_key_item", DEFAULT_OMINOUS_KEY).forGetter(p -> p.ominousKeyItem)
    ).apply(instance, instance.stable(VaultRandomizingProcessor::new)));

    public final ResourceLocation lootTable;
    public final Optional<ResourceLocation> ominousLootTable;
    public final ResourceLocation keyItem;
    public final ResourceLocation ominousKeyItem;

    private VaultRandomizingProcessor(ResourceLocation lootTable, Optional<ResourceLocation> ominousLootTable, ResourceLocation keyItem, ResourceLocation ominousKeyItem) {
        this.lootTable = lootTable;
        this.ominousLootTable = ominousLootTable;
        this.keyItem = keyItem;
        this.ominousKeyItem = ominousKeyItem;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldView, BlockPos pos, BlockPos blockPos, StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings structurePlacementData) {
        BlockState state = structureBlockInfoWorld.state();
        if (!(state.getBlock() instanceof VaultBlock)) {
            return structureBlockInfoWorld;
        }

        boolean ominous = state.getValue(BlockStateProperties.OMINOUS);
        ResourceLocation chosenLoot = ominous ? ominousLootTable.orElse(lootTable) : lootTable;
        ResourceLocation chosenKey = ominous ? ominousKeyItem : keyItem;

        CompoundTag existing = structureBlockInfoWorld.nbt();
        CompoundTag newNbt = existing != null ? existing.copy() : new CompoundTag();

        newNbt.remove("server_data");
        newNbt.remove("shared_data");

        CompoundTag config = newNbt.contains("config") ? newNbt.getCompound("config").copy() : new CompoundTag();
        config.putString("loot_table", chosenLoot.toString());

        CompoundTag keyItemTag = new CompoundTag();
        keyItemTag.putString("id", chosenKey.toString());
        keyItemTag.putInt("count", 1);
        config.put("key_item", keyItemTag);

        newNbt.put("config", config);

        return new StructureTemplate.StructureBlockInfo(structureBlockInfoWorld.pos(), state, newNbt);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.VAULT_RANDOMIZING_PROCESSOR.get();
    }
}
