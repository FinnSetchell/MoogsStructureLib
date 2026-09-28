package com.finndog.moogs_structures.world.processors;

//? if <26.2 {
import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
//?}
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
//? if >=1.21.5 {
/*import net.minecraft.world.level.block.Block;
*///?}
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
//? if <26.2 {
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
//?}
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * For removing stuff like floating tall grass or kelp left over after generation/adaptation.
 * Ported from RepurposedStructures (TelepathicGrunt) to MSL.
 */
//? if <26.2 {
public class RemoveFloatingBlocksProcessor extends StructureProcessor {
//?} else {
/*public class RemoveFloatingBlocksProcessor implements StructureProcessor {
*///?}

    //? if <26.2 {
    public static final MapCodec<RemoveFloatingBlocksProcessor> CODEC = MapCodec.unit(RemoveFloatingBlocksProcessor::new);
    //?} else {
    /*public static final MapCodec<RemoveFloatingBlocksProcessor> MAP_CODEC = MapCodec.unit(RemoveFloatingBlocksProcessor::new);
    *///?}
    private RemoveFloatingBlocksProcessor() { }

    @Override
    //? if <26.2 {
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos pos, BlockPos blockPos, StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings structurePlacementData) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(structureBlockInfoWorld.pos());
    //?} else {
    /*public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos targetPosition, BlockPos referencePos, BlockPos templateRelativePos, StructureTemplate.StructureBlockInfo processedBlockInfo, StructurePlaceSettings structurePlacementData) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(processedBlockInfo.pos());
    *///?}
        //? if <26.1.2 {
        if(levelReader instanceof WorldGenRegion worldGenRegion && !worldGenRegion.getCenter().equals(new ChunkPos(mutable))) {
        //?} else {
        /*if(levelReader instanceof WorldGenRegion worldGenRegion && !worldGenRegion.getCenter().equals(new ChunkPos(mutable.getX() >> 4, mutable.getZ() >> 4))) {
        *///?}
            //? if <26.2 {
            return structureBlockInfoWorld;
            //?} else {
            /*return processedBlockInfo;
            *///?}
        }

        ChunkAccess cachedChunk = levelReader.getChunk(mutable);
        //? if <26.2 {
        if(structureBlockInfoWorld.state().isAir() || !structureBlockInfoWorld.state().getFluidState().isEmpty()) {
        //?} else {
        /*if(processedBlockInfo.state().isAir() || !processedBlockInfo.state().getFluidState().isEmpty()) {
        *///?}

            //? if <1.21.5 {
            cachedChunk.setBlockState(mutable, structureBlockInfoWorld.state(), false);
            //?}
            //? if >=1.21.5 <26.2 {
            /*cachedChunk.setBlockState(mutable, structureBlockInfoWorld.state(), Block.UPDATE_CLIENTS);
            *///?}
            //? if >=26.2 {
            /*cachedChunk.setBlockState(mutable, processedBlockInfo.state(), Block.UPDATE_CLIENTS);
            *///?}
            BlockState aboveWorldState = levelReader.getBlockState(mutable.move(Direction.UP));

            while(mutable.getY() < levelReader.getHeight() && !aboveWorldState.canSurvive(levelReader, mutable)) {
                //? if <1.21.5 {
                cachedChunk.setBlockState(mutable, structureBlockInfoWorld.state(), false);
                //?}
                //? if >=1.21.5 <26.2 {
                /*cachedChunk.setBlockState(mutable, structureBlockInfoWorld.state(), Block.UPDATE_CLIENTS);
                *///?}
                //? if >=26.2 {
                /*cachedChunk.setBlockState(mutable, processedBlockInfo.state(), Block.UPDATE_CLIENTS);
                *///?}
                aboveWorldState = levelReader.getBlockState(mutable.move(Direction.UP));
            }

            for (Direction direction : Direction.Plane.HORIZONTAL) {
                //? if <26.2 {
                mutable.set(structureBlockInfoWorld.pos());
                //?} else {
                /*mutable.set(processedBlockInfo.pos());
                *///?}
                mutable.move(direction);
                //? if <26.1.2 {
                ChunkPos chunkPos = new ChunkPos(mutable);
                //?} else {
                /*ChunkPos chunkPos = new ChunkPos(mutable.getX() >> 4, mutable.getZ() >> 4);
                *///?}
                ChunkAccess chunkAccess2 = cachedChunk;
                if (!chunkPos.equals(cachedChunk.getPos())) {
                    chunkAccess2 = levelReader.getChunk(mutable);
                }
                BlockState sideBlock = chunkAccess2.getBlockState(mutable);
                if (!sideBlock.canSurvive(levelReader, mutable)) {
                    //? if <1.21.5 {
                    chunkAccess2.setBlockState(mutable, structureBlockInfoWorld.state(), false);
                    //?}
                    //? if >=1.21.5 <26.2 {
                    /*chunkAccess2.setBlockState(mutable, structureBlockInfoWorld.state(), Block.UPDATE_CLIENTS);
                    *///?}
                    //? if >=26.2 {
                    /*chunkAccess2.setBlockState(mutable, processedBlockInfo.state(), Block.UPDATE_CLIENTS);
                    *///?}
                }
            }
        }

        //? if <26.2 {
        return structureBlockInfoWorld;
        //?} else {
        /*return processedBlockInfo;
        *///?}
    }

    @Override
    //? if <26.2 {
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.REMOVE_FLOATING_BLOCKS_PROCESSOR.get();
    //?} else {
    /*public MapCodec<RemoveFloatingBlocksProcessor> codec() {
        return MAP_CODEC;
    *///?}
    }
}
