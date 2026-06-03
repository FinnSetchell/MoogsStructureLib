package com.finndog.moogs_structures.world.processors;

import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

/**
 * Configures a {@code minecraft:trial_spawner} block entity at placement time. Strips runtime
 * state (so every placement starts fresh) and writes {@code normal_config} / {@code ominous_config}
 * as ResourceLocation strings. On 1.21.5+ vanilla resolves these from its own
 * {@code minecraft:trial_spawner} registry at block-entity load time — no inline NBT needed.
 */
public class TrialSpawnerRandomizingProcessor extends StructureProcessor {

    public static final MapCodec<TrialSpawnerRandomizingProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("normal_config").forGetter(p -> p.normalConfig),
            Identifier.CODEC.optionalFieldOf("ominous_config").forGetter(p -> p.ominousConfig)
    ).apply(instance, instance.stable(TrialSpawnerRandomizingProcessor::new)));

    public final Identifier normalConfig;
    public final Optional<Identifier> ominousConfig;

    private TrialSpawnerRandomizingProcessor(Identifier normalConfig, Optional<Identifier> ominousConfig) {
        this.normalConfig = normalConfig;
        this.ominousConfig = ominousConfig;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldView, BlockPos pos, BlockPos blockPos, StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings structurePlacementData) {
        if (!(structureBlockInfoWorld.state().getBlock() instanceof TrialSpawnerBlock)) {
            return structureBlockInfoWorld;
        }

        CompoundTag existing = structureBlockInfoWorld.nbt();
        CompoundTag newNbt = existing != null ? existing.copy() : new CompoundTag();

        newNbt.remove("server_data");
        newNbt.remove("shared_data");
        newNbt.remove("spawn_data");
        newNbt.remove("cooldown_end_at_tick");
        newNbt.remove("next_mob_spawns_at");

        newNbt.putString("normal_config", normalConfig.toString());

        if (ominousConfig.isPresent()) {
            newNbt.putString("ominous_config", ominousConfig.get().toString());
        }

        return new StructureTemplate.StructureBlockInfo(structureBlockInfoWorld.pos(), structureBlockInfoWorld.state(), newNbt);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.TRIAL_SPAWNER_RANDOMIZING_PROCESSOR.get();
    }
}
