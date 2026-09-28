package com.finndog.moogs_structures.world.processors;

//? if <26.2 {
import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
//?}
import com.finndog.moogs_structures.utils.GeneralUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
//? if >=1.20.6 {
import com.mojang.serialization.MapCodec;
//?} else {
/*import com.mojang.serialization.Codec;
*///?}
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
//? if <26.2 {
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
//?}
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.FluidState;

import java.util.List;

/**
 * Will help enclose the structure in solid blocks rather than allow fluid source blocks to be floating.
 * Best for Nether Structures with Cave Air marking the insides that should never be exposed to lava.
 * Ported from RepurposedStructures (TelepathicGrunt) to MSL.
 */
//? if <26.2 {
public class CloseOffFluidSourcesProcessor extends StructureProcessor {
//?} else {
/*public class CloseOffFluidSourcesProcessor implements StructureProcessor {
*///?}

    //? if >=1.20.6 <26.2 {
    public static final MapCodec<CloseOffFluidSourcesProcessor> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
    //?}
    //? if <1.20.6 {
    /*public static final Codec<CloseOffFluidSourcesProcessor> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
    *///?}
    //? if >=26.2 {
    /*public static final MapCodec<CloseOffFluidSourcesProcessor> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
    *///?}
            Codec.mapPair(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block"), Codec.intRange(1, Integer.MAX_VALUE).fieldOf("weight"))
                    .codec().listOf().fieldOf("weighted_list_of_replacement_blocks")
                    .forGetter(processor -> processor.weightedReplacementBlocks),
            Codec.BOOL.fieldOf("ignore_down").orElse(false).forGetter(processor -> processor.ignoreDown),
            Codec.BOOL.fieldOf("if_air_in_world").orElse(false).forGetter(processor -> processor.ifAirInWorld)
    ).apply(instance, instance.stable(CloseOffFluidSourcesProcessor::new)));

    private final List<Pair<Block, Integer>> weightedReplacementBlocks;
    private final boolean ignoreDown;
    private final boolean ifAirInWorld;

    public CloseOffFluidSourcesProcessor(List<Pair<Block, Integer>> weightedReplacementBlocks, boolean ignoreDown, boolean ifAirInWorld) {
        this.weightedReplacementBlocks = weightedReplacementBlocks;
        this.ignoreDown = ignoreDown;
        this.ifAirInWorld = ifAirInWorld;
    }

    @Override
    //? if <26.2 {
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos pos, BlockPos pos2, StructureTemplate.StructureBlockInfo infoIn1, StructureTemplate.StructureBlockInfo infoIn2, StructurePlaceSettings settings) {
    //?} else {
    /*public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos targetPosition, BlockPos referencePos, BlockPos templateRelativePos, StructureTemplate.StructureBlockInfo processedBlockInfo, StructurePlaceSettings settings) {
    *///?}

        //? if <26.1.2 {
        ChunkPos currentChunkPos = new ChunkPos(infoIn2.pos());
        //?}
        //? if >=26.1.2 <26.2 {
        /*ChunkPos currentChunkPos = new ChunkPos(infoIn2.pos().getX() >> 4, infoIn2.pos().getZ() >> 4);
        *///?}
        //? if <26.2 {
        if(infoIn2.state().is(Blocks.STRUCTURE_VOID) || !infoIn2.state().getFluidState().isEmpty()) {
            return infoIn2;
        //?} else {
        /*ChunkPos currentChunkPos = new ChunkPos(processedBlockInfo.pos().getX() >> 4, processedBlockInfo.pos().getZ() >> 4);
        if(processedBlockInfo.state().is(Blocks.STRUCTURE_VOID) || !processedBlockInfo.state().getFluidState().isEmpty()) {
            return processedBlockInfo;
        *///?}
        }

        if(levelReader instanceof WorldGenRegion worldGenRegion && !worldGenRegion.getCenter().equals(currentChunkPos)) {
            //? if <26.2 {
            return infoIn2;
            //?} else {
            /*return processedBlockInfo;
            *///?}
        }

        //? if <1.21.2 {
        if(!GeneralUtils.isFullCube(levelReader, infoIn2.pos(), infoIn2.state()) || !infoIn2.state().blocksMotion()) {
        //?}
        //? if >=1.21.2 <1.21.5 {
        /*if(!GeneralUtils.isFullCube(infoIn2.state()) || !infoIn2.state().blocksMotion()) {
        *///?}
        //? if >=1.21.5 <26.2 {
        /*if(!GeneralUtils.isFullCube(levelReader, infoIn2.pos(), infoIn2.state()) || !infoIn2.state().blocksMotion()) {
        *///?}
        //? if >=26.2 <26.3 {
        /*if(!GeneralUtils.isFullCube(levelReader, processedBlockInfo.pos(), processedBlockInfo.state()) || !processedBlockInfo.state().blocksMotion()) {
        *///?}
        //? if >=26.3 {
        /*if(!GeneralUtils.isFullCube(levelReader, processedBlockInfo.pos(), processedBlockInfo.state()) || !processedBlockInfo.state().isSolid()) {
        *///?}
            //? if <26.1.2 {
            ChunkAccess currentChunk = levelReader.getChunk(currentChunkPos.x, currentChunkPos.z);
            //?} else {
            /*ChunkAccess currentChunk = levelReader.getChunk(currentChunkPos.x(), currentChunkPos.z());
            *///?}

            //? if <26.2 {
            if(ifAirInWorld && !currentChunk.getBlockState(infoIn2.pos()).isAir()) return infoIn2;
            //?} else {
            /*if(ifAirInWorld && !currentChunk.getBlockState(processedBlockInfo.pos()).isAir()) return processedBlockInfo;
            *///?}

            // Remove fluid sources in adjacent horizontal blocks across chunk boundaries and above as well
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            for (Direction direction : Direction.values()) {
                if(ignoreDown && direction == Direction.DOWN) continue;

                //? if <26.2 {
                mutable.set(infoIn2.pos()).move(direction);
                //?} else {
                /*mutable.set(processedBlockInfo.pos()).move(direction);
                *///?}
                //? if <1.21.2 {
                if (mutable.getY() < currentChunk.getMinBuildHeight() || mutable.getY() >= currentChunk.getMaxBuildHeight()) {
                //?} else {
                /*if (mutable.getY() < currentChunk.getMinY() || mutable.getY() >= currentChunk.getMaxY()) {
                *///?}
                    continue;
                }

                //? if <26.1.2 {
                if (currentChunkPos.x != mutable.getX() >> 4 || currentChunkPos.z != mutable.getZ() >> 4) {
                //?} else {
                /*if (currentChunkPos.x() != mutable.getX() >> 4 || currentChunkPos.z() != mutable.getZ() >> 4) {
                *///?}
                    currentChunk = levelReader.getChunk(mutable);
                    //? if <26.1.2 {
                    currentChunkPos = new ChunkPos(mutable);
                    //?} else {
                    /*currentChunkPos = new ChunkPos(mutable.getX() >> 4, mutable.getZ() >> 4);
                    *///?}
                }

                LevelHeightAccessor levelHeightAccessor = currentChunk.getHeightAccessorForGeneration();
                //? if <1.21.2 {
                if(levelReader instanceof WorldGenLevel && mutable.getY() >= levelHeightAccessor.getMinBuildHeight() && mutable.getY() < levelHeightAccessor.getMaxBuildHeight()) {
                //?} else {
                /*if(levelReader instanceof WorldGenLevel && mutable.getY() >= levelHeightAccessor.getMinY() && mutable.getY() < levelHeightAccessor.getMaxY()) {
                *///?}
                    int sectionYIndex = currentChunk.getSectionIndex(mutable.getY());
                    LevelChunkSection levelChunkSection = currentChunk.getSection(sectionYIndex);
                    if (levelChunkSection == null) continue;

                    FluidState fluidState = levelChunkSection.getFluidState(
                            SectionPos.sectionRelative(mutable.getX()),
                            SectionPos.sectionRelative(mutable.getY()),
                            SectionPos.sectionRelative(mutable.getZ()));

                    if (fluidState.isSource()) {
                        //? if <26.2 {
                        RandomSource random = settings.getRandom(infoIn2.pos());
                        //?} else {
                        /*RandomSource random = settings.getRandom(processedBlockInfo.pos());
                        *///?}
                        Block replacementBlock = GeneralUtils.getRandomEntry(weightedReplacementBlocks, random);
                        levelChunkSection.setBlockState(
                                SectionPos.sectionRelative(mutable.getX()),
                                SectionPos.sectionRelative(mutable.getY()),
                                SectionPos.sectionRelative(mutable.getZ()),
                                replacementBlock.defaultBlockState(),
                                false);
                    }
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
        return MoogsStructuresProcessors.CLOSE_OFF_FLUID_SOURCES_PROCESSOR.get();
    //?} else {
    /*public MapCodec<CloseOffFluidSourcesProcessor> codec() {
        return MAP_CODEC;
    *///?}
    }
}
