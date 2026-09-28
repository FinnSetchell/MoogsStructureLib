package com.finndog.moogs_structures.world.processors;

//? if <26.2 {
import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
//?}
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
//? if >=1.21.5 {
/*import net.minecraft.world.level.block.Block;
*///?}
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
//? if <26.2 {
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
//?}
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Fluids;

/**
 * Floods a structure's interior with water up to a given flood level.
 * Ported from RepurposedStructures (TelepathicGrunt) to MSL.
 */
//? if <26.2 {
public class FloodWithWaterProcessor extends StructureProcessor {
//?} else {
/*public class FloodWithWaterProcessor implements StructureProcessor {
*///?}

    //? if <26.2 {
    public static final MapCodec<FloodWithWaterProcessor> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
    //?} else {
    /*public static final MapCodec<FloodWithWaterProcessor> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
    *///?}
            Codec.INT.fieldOf("flood_level").forGetter(config -> config.floodLevel)
    ).apply(instance, instance.stable(FloodWithWaterProcessor::new)));

    private final int floodLevel;

    private FloodWithWaterProcessor(int floodLevel) {
        this.floodLevel = floodLevel;
    }

    @Override
    //? if <26.2 {
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos pos, BlockPos blockPos, StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings structurePlacementData) {
        if(structureBlockInfoWorld.state().getFluidState().is(FluidTags.WATER)) {
            tickWaterFluid(levelReader, structureBlockInfoWorld);
            return structureBlockInfoWorld;
    //?} else {
    /*public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos targetPosition, BlockPos referencePos, BlockPos templateRelativePos, StructureTemplate.StructureBlockInfo processedBlockInfo, StructurePlaceSettings structurePlacementData) {
        if(processedBlockInfo.state().getFluidState().is(FluidTags.WATER)) {
            tickWaterFluid(levelReader, processedBlockInfo);
            return processedBlockInfo;
    *///?}
        }

        //? if <26.1.2 {
        if(levelReader instanceof WorldGenRegion worldGenRegion && !worldGenRegion.getCenter().equals(new ChunkPos(structureBlockInfoWorld.pos()))) {
        //?}
        //? if >=26.1.2 <26.2 {
        /*if(levelReader instanceof WorldGenRegion worldGenRegion && !worldGenRegion.getCenter().equals(new ChunkPos(structureBlockInfoWorld.pos().getX() >> 4, structureBlockInfoWorld.pos().getZ() >> 4))) {
        *///?}
            //? if <26.2 {
            return structureBlockInfoWorld;
            //?} else {
        /*if(levelReader instanceof WorldGenRegion worldGenRegion && !worldGenRegion.getCenter().equals(new ChunkPos(processedBlockInfo.pos().getX() >> 4, processedBlockInfo.pos().getZ() >> 4))) {
            return processedBlockInfo;
            *///?}
        }

        //? if <26.2 {
        if (structureBlockInfoWorld.pos().getY() <= floodLevel) {
        //?} else {
        /*if (processedBlockInfo.pos().getY() <= floodLevel) {
        *///?}
            boolean flooded = false;
            //? if <26.2 {
            if(structureBlockInfoWorld.state().isAir() || structureBlockInfoWorld.state().is(BlockTags.FLOWER_POTS) || structureBlockInfoWorld.state().is(BlockTags.BUTTONS) || structureBlockInfoWorld.state().canBeReplaced(Fluids.WATER)) {
                structureBlockInfoWorld = new StructureTemplate.StructureBlockInfo(structureBlockInfoWorld.pos(), Blocks.WATER.defaultBlockState(), null);
                tickWaterFluid(levelReader, structureBlockInfoWorld);
            //?} else {
            /*if(processedBlockInfo.state().isAir() || processedBlockInfo.state().is(BlockTags.FLOWER_POTS) || processedBlockInfo.state().is(BlockTags.BUTTONS) || processedBlockInfo.state().canBeReplaced(Fluids.WATER)) {
                processedBlockInfo = new StructureTemplate.StructureBlockInfo(processedBlockInfo.pos(), Blocks.WATER.defaultBlockState(), null);
                tickWaterFluid(levelReader, processedBlockInfo);
            *///?}
                flooded = true;
            }
            //? if <26.2 {
            else if(structureBlockInfoWorld.state().hasProperty(BlockStateProperties.WATERLOGGED)) {
                structureBlockInfoWorld = new StructureTemplate.StructureBlockInfo(structureBlockInfoWorld.pos(), structureBlockInfoWorld.state().setValue(BlockStateProperties.WATERLOGGED, true), structureBlockInfoWorld.nbt());
                tickWaterFluid(levelReader, structureBlockInfoWorld);
            //?} else {
            /*else if(processedBlockInfo.state().hasProperty(BlockStateProperties.WATERLOGGED)) {
                processedBlockInfo = new StructureTemplate.StructureBlockInfo(processedBlockInfo.pos(), processedBlockInfo.state().setValue(BlockStateProperties.WATERLOGGED, true), processedBlockInfo.nbt());
                tickWaterFluid(levelReader, processedBlockInfo);
            *///?}
                flooded = true;
            }
            //? if <26.2 {
            else if(structureBlockInfoWorld.state().getBlock() instanceof BushBlock) {
                structureBlockInfoWorld = new StructureTemplate.StructureBlockInfo(structureBlockInfoWorld.pos(), Blocks.WATER.defaultBlockState(), null);
                tickWaterFluid(levelReader, structureBlockInfoWorld);
            //?} else {
            /*else if(processedBlockInfo.state().getBlock() instanceof BushBlock) {
                processedBlockInfo = new StructureTemplate.StructureBlockInfo(processedBlockInfo.pos(), Blocks.WATER.defaultBlockState(), null);
                tickWaterFluid(levelReader, processedBlockInfo);
            *///?}
                flooded = true;
            }

            if(flooded) {
                //? if <26.1.2 {
                ChunkPos currentChunkPos = new ChunkPos(structureBlockInfoWorld.pos());
                ChunkAccess currentChunk = levelReader.getChunk(currentChunkPos.x, currentChunkPos.z);
                //?}
                //? if >=26.1.2 <26.2 {
                /*ChunkPos currentChunkPos = new ChunkPos(structureBlockInfoWorld.pos().getX() >> 4, structureBlockInfoWorld.pos().getZ() >> 4);
                *///?}
                //? if >=26.2 {
                /*ChunkPos currentChunkPos = new ChunkPos(processedBlockInfo.pos().getX() >> 4, processedBlockInfo.pos().getZ() >> 4);
                *///?}
                //? if >=26.1.2 {
                /*ChunkAccess currentChunk = levelReader.getChunk(currentChunkPos.x(), currentChunkPos.z());
                *///?}
                BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
                for (Direction direction : Direction.values()) {
                    if(direction == Direction.UP) continue;

                    //? if <26.2 {
                    mutable.set(structureBlockInfoWorld.pos()).move(direction);
                    //?} else {
                    /*mutable.set(processedBlockInfo.pos()).move(direction);
                    *///?}
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

                    BlockState neighboringBlock = currentChunk.getBlockState(mutable);
                    // Seal only open air. Anything else non-solid beside the water is part of a structure,
                    // often one already placed in the neighbouring chunk (a spawner, chest or torch), and
                    // replacing it orphaned its block entity.
                    if (neighboringBlock.isAir()) {
                        //? if <1.21.5 {
                        currentChunk.setBlockState(mutable, Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), false);
                        //?} else {
                        /*currentChunk.setBlockState(mutable, Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), Block.UPDATE_CLIENTS);
                        *///?}
                    }
                }
            }
        }
        //? if <26.2 {
        return structureBlockInfoWorld;
        //?} else {
        /*return processedBlockInfo;
        *///?}
    }

    //? if <26.2 {
    private void tickWaterFluid(LevelReader worldView, StructureTemplate.StructureBlockInfo structureBlockInfoWorld) {
        ((LevelAccessor)worldView).scheduleTick(structureBlockInfoWorld.pos(), Fluids.WATER, 1);
    //?} else {
    /*private void tickWaterFluid(LevelReader worldView, StructureTemplate.StructureBlockInfo blockInfo) {
        ((LevelAccessor)worldView).scheduleTick(blockInfo.pos(), Fluids.WATER, 1);
    *///?}
    }

    @Override
    //? if <26.2 {
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.FLOOD_WITH_WATER_PROCESSOR.get();
    //?} else {
    /*public MapCodec<FloodWithWaterProcessor> codec() {
        return MAP_CODEC;
    *///?}
    }
}
