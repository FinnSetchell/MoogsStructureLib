package com.finndog.moogs_structures.world.processors;

import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
import com.finndog.moogs_structures.utils.GeneralUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;

/**
 * Randomizes a mob spawner's mob from an inline weighted list of entity types.
 * Adapted from RepurposedStructures' SpawnerRandomizingProcessor, but takes the mob list
 * directly in the codec instead of depending on RS's datapack-driven MobSpawnerManager.
 */
public class SpawnerRandomizingProcessor extends StructureProcessor {

    public static final Codec<SpawnerRandomizingProcessor> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.mapPair(BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity"), Codec.intRange(1, Integer.MAX_VALUE).fieldOf("weight"))
                    .codec().listOf().fieldOf("weighted_entities").forGetter(p -> p.weightedEntities),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("delay").orElse(20).forGetter(p -> p.delay),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("max_nearby_entities").orElse(6).forGetter(p -> p.maxNearbyEntities),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("max_spawn_delay").orElse(800).forGetter(p -> p.maxSpawnDelay),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("min_spawn_delay").orElse(200).forGetter(p -> p.minSpawnDelay),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("required_player_range").orElse(16).forGetter(p -> p.requiredPlayerRange),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("spawn_count").orElse(4).forGetter(p -> p.spawnCount),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("spawn_range").orElse(4).forGetter(p -> p.spawnRange),
            BlockState.CODEC.fieldOf("spawner_replacement_block").orElse(Blocks.AIR.defaultBlockState()).forGetter(p -> p.replacementState)
    ).apply(instance, instance.stable(SpawnerRandomizingProcessor::new)));

    public final List<Pair<EntityType<?>, Integer>> weightedEntities;
    public final int delay;
    public final int maxNearbyEntities;
    public final int maxSpawnDelay;
    public final int minSpawnDelay;
    public final int requiredPlayerRange;
    public final int spawnCount;
    public final int spawnRange;
    public final BlockState replacementState;

    private SpawnerRandomizingProcessor(List<Pair<EntityType<?>, Integer>> weightedEntities,
                                        int delay, int maxNearbyEntities, int maxSpawnDelay, int minSpawnDelay,
                                        int requiredPlayerRange, int spawnCount, int spawnRange, BlockState replacementState) {
        this.weightedEntities = weightedEntities;
        this.delay = delay;
        this.maxNearbyEntities = maxNearbyEntities;
        this.maxSpawnDelay = maxSpawnDelay;
        this.minSpawnDelay = minSpawnDelay;
        this.requiredPlayerRange = requiredPlayerRange;
        this.spawnCount = spawnCount;
        this.spawnRange = spawnRange;
        this.replacementState = replacementState;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldView, BlockPos pos, BlockPos blockPos, StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings structurePlacementData) {
        if (structureBlockInfoWorld.state().getBlock() instanceof SpawnerBlock) {
            BlockPos worldPos = structureBlockInfoWorld.pos();
            RandomSource random = structurePlacementData.getRandom(structureBlockInfoWorld.pos());
            CompoundTag spawnerNBT = buildSpawnerNbt(random);

            if (spawnerNBT == null) {
                return new StructureTemplate.StructureBlockInfo(worldPos, replacementState, null);
            }
            else {
                return new StructureTemplate.StructureBlockInfo(worldPos, structureBlockInfoWorld.state(), spawnerNBT);
            }
        }
        return structureBlockInfoWorld;
    }

    private CompoundTag buildSpawnerNbt(RandomSource random) {
        if (weightedEntities.isEmpty()) return null;
        EntityType<?> entity = GeneralUtils.getRandomEntry(weightedEntities, random);
        if (entity == null) return null;
        ResourceLocation entityRL = BuiltInRegistries.ENTITY_TYPE.getKey(entity);

        CompoundTag compound = new CompoundTag();
        compound.putShort("Delay", (short) delay);
        compound.putShort("MinSpawnDelay", (short) minSpawnDelay);
        compound.putShort("MaxSpawnDelay", (short) maxSpawnDelay);
        compound.putShort("SpawnCount", (short) spawnCount);
        compound.putShort("MaxNearbyEntities", (short) maxNearbyEntities);
        compound.putShort("RequiredPlayerRange", (short) requiredPlayerRange);
        compound.putShort("SpawnRange", (short) spawnRange);

        CompoundTag spawnData = new CompoundTag();
        CompoundTag spawnPotentialData = new CompoundTag();
        CompoundTag entityData = new CompoundTag();
        entityData.putString("id", entityRL.toString());
        spawnPotentialData.put("entity", entityData);

        CompoundTag listEntry = new CompoundTag();
        listEntry.put("data", spawnPotentialData);
        listEntry.putInt("weight", 1);
        ListTag listTag = new ListTag();
        listTag.add(listEntry);
        compound.put("SpawnPotentials", listTag);

        CompoundTag entityEntry = new CompoundTag();
        entityEntry.putString("id", entityRL.toString());
        spawnData.put("entity", entityEntry);
        compound.put("SpawnData", spawnData);

        return compound;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.SPAWNER_RANDOMIZING_PROCESSOR.get();
    }
}
