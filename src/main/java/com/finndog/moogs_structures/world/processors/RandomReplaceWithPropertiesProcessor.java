package com.finndog.moogs_structures.world.processors;

import com.finndog.moogs_structures.MoogsStructuresCommon;
//? if <26.2 {
import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
//?}
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
//? if <26.2 {
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
//?}
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Replace blocks randomly but preserve the properties of the block.
 * Ported from RepurposedStructures (TelepathicGrunt) to MSL.
 */
//? if <26.2 {
public class RandomReplaceWithPropertiesProcessor extends StructureProcessor {
//?} else {
/*public class RandomReplaceWithPropertiesProcessor implements StructureProcessor {
*///?}

    //? if <26.2 {
    public static final MapCodec<RandomReplaceWithPropertiesProcessor> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
    //?} else {
    /*public static final MapCodec<RandomReplaceWithPropertiesProcessor> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
    *///?}
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
    //? if <26.2 {
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldReader, BlockPos pos, BlockPos pos2, StructureTemplate.StructureBlockInfo infoIn1, StructureTemplate.StructureBlockInfo infoIn2, StructurePlaceSettings settings) {
        if(infoIn2.state().getBlock() == inputBlock) {
            BlockPos worldPos = infoIn2.pos();
    //?} else {
    /*public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldReader, BlockPos targetPosition, BlockPos referencePos, BlockPos templateRelativePos, StructureTemplate.StructureBlockInfo processedBlockInfo, StructurePlaceSettings settings) {
        if(processedBlockInfo.state().getBlock() == inputBlock) {
            BlockPos worldPos = processedBlockInfo.pos();
    *///?}
            RandomSource random = RandomSource.create();
            int offSet = settings.getProcessors().indexOf(this) + 1;
            random.setSeed(worldPos.asLong() * worldPos.asLong() * offSet);
            if (random.nextFloat() < probability) {
                if (outputBlock.isPresent()) {
                    BlockState newBlockState = outputBlock.get().defaultBlockState();
                    //? if <26.2 {
                    newBlockState = GeneralUtils.copyBlockProperties(infoIn2.state(), newBlockState);
                    return new StructureTemplate.StructureBlockInfo(infoIn2.pos(), newBlockState, infoIn2.nbt());
                    //?} else {
                    /*newBlockState = GeneralUtils.copyBlockProperties(processedBlockInfo.state(), newBlockState);
                    return new StructureTemplate.StructureBlockInfo(processedBlockInfo.pos(), newBlockState, processedBlockInfo.nbt());
                    *///?}
                }
                else if (!outputBlocks.isEmpty()) {
                    BlockState newBlockState = outputBlocks.get(random.nextInt(outputBlocks.size())).defaultBlockState();
                    //? if <26.2 {
                    newBlockState = GeneralUtils.copyBlockProperties(infoIn2.state(), newBlockState);
                    return new StructureTemplate.StructureBlockInfo(infoIn2.pos(), newBlockState, infoIn2.nbt());
                    //?} else {
                    /*newBlockState = GeneralUtils.copyBlockProperties(processedBlockInfo.state(), newBlockState);
                    return new StructureTemplate.StructureBlockInfo(processedBlockInfo.pos(), newBlockState, processedBlockInfo.nbt());
                    *///?}
                }
                else {
                    MoogsStructuresCommon.LOGGER.warn("Moogs Structures: moogs_structures:random_replace_with_properties_processor in a processor file has no replacement block of any kind.");
                }
            }
        }
        //? if <26.2 {
        return infoIn2;
        //?} else {
        /*return processedBlockInfo;
        *///?}
    }

    @Override
    //? if <26.2 {
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.RANDOM_REPLACE_WITH_PROPERTIES_PROCESSOR.get();
    //?} else {
    /*public MapCodec<RandomReplaceWithPropertiesProcessor> codec() {
        return MAP_CODEC;
    *///?}
    }
}
