package com.finndog.moogs_structures.world.processors;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
import com.finndog.moogs_structures.utils.GeneralUtils;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Replace blocks randomly but preserve the properties of the block.
 * Ported from RepurposedStructures (TelepathicGrunt) to MSL.
 */
public class RandomReplaceWithPropertiesProcessor extends StructureProcessor {

    public static final MapCodec<RandomReplaceWithPropertiesProcessor> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("input_block").forGetter(config -> config.inputBlock),
            BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("output_block").forGetter(config -> config.outputBlock),
            BuiltInRegistries.BLOCK.byNameCodec().listOf().optionalFieldOf("output_blocks", ImmutableList.of()).forGetter(config -> config.outputBlocks),
            Codec.floatRange(0, 1).fieldOf("probability").forGetter(config -> config.probability)
    ).apply(instance, instance.stable(RandomReplaceWithPropertiesProcessor::new)));

    private final Block inputBlock;
    private final Optional<Block> outputBlock;
    private final List<Block> outputBlocks;
    private final float probability;

    public RandomReplaceWithPropertiesProcessor(Block inputBlock, Optional<Block> outputBlock, List<Block> outputBlocks, float probability) {
        this.inputBlock = inputBlock;
        this.outputBlock = outputBlock;
        this.outputBlocks = outputBlocks;
        this.probability = probability;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldReader, BlockPos pos, BlockPos pos2, StructureTemplate.StructureBlockInfo infoIn1, StructureTemplate.StructureBlockInfo infoIn2, StructurePlaceSettings settings) {
        if(infoIn2.state().getBlock() == inputBlock) {
            BlockPos worldPos = infoIn2.pos();
            RandomSource random = RandomSource.create();
            int offSet = settings.getProcessors().indexOf(this) + 1;
            random.setSeed(worldPos.asLong() * worldPos.asLong() * offSet);
            if (random.nextFloat() < probability) {
                if (outputBlock.isPresent()) {
                    BlockState newBlockState = outputBlock.get().defaultBlockState();
                    newBlockState = GeneralUtils.copyBlockProperties(infoIn2.state(), newBlockState);
                    return new StructureTemplate.StructureBlockInfo(infoIn2.pos(), newBlockState, infoIn2.nbt());
                }
                else if (!outputBlocks.isEmpty()) {
                    BlockState newBlockState = outputBlocks.get(random.nextInt(outputBlocks.size())).defaultBlockState();
                    newBlockState = GeneralUtils.copyBlockProperties(infoIn2.state(), newBlockState);
                    return new StructureTemplate.StructureBlockInfo(infoIn2.pos(), newBlockState, infoIn2.nbt());
                }
                else {
                    MoogsStructuresCommon.LOGGER.warn("Moogs Structures: moogs_structures:random_replace_with_properties_processor in a processor file has no replacement block of any kind.");
                }
            }
        }
        return infoIn2;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.RANDOM_REPLACE_WITH_PROPERTIES_PROCESSOR.get();
    }
}
