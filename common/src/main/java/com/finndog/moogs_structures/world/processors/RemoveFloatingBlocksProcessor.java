package com.finndog.moogs_structures.world.processors;

import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * For removing stuff like floating tall grass or kelp left over after generation/adaptation.
 * Ported from RepurposedStructures (TelepathicGrunt) to MSL.
 */
public class RemoveFloatingBlocksProcessor implements StructureProcessor {

    public static final MapCodec<RemoveFloatingBlocksProcessor> MAP_CODEC = MapCodec.unit(RemoveFloatingBlocksProcessor::new);
    private RemoveFloatingBlocksProcessor() { }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos targetPosition, BlockPos referencePos, BlockPos templateRelativePos, StructureTemplate.StructureBlockInfo processedBlockInfo, StructurePlaceSettings structurePlacementData) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(processedBlockInfo.pos());
        if(levelReader instanceof WorldGenRegion worldGenRegion && !worldGenRegion.getCenter().equals(new ChunkPos(mutable.getX() >> 4, mutable.getZ() >> 4))) {
            return processedBlockInfo;
        }

        ChunkAccess cachedChunk = levelReader.getChunk(mutable);
        if(processedBlockInfo.state().isAir() || !processedBlockInfo.state().getFluidState().isEmpty()) {

            cachedChunk.setBlockState(mutable, processedBlockInfo.state(), Block.UPDATE_CLIENTS);
            BlockState aboveWorldState = levelReader.getBlockState(mutable.move(Direction.UP));

            while(mutable.getY() < levelReader.getHeight() && !aboveWorldState.canSurvive(levelReader, mutable)) {
                cachedChunk.setBlockState(mutable, processedBlockInfo.state(), Block.UPDATE_CLIENTS);
                aboveWorldState = levelReader.getBlockState(mutable.move(Direction.UP));
            }

            for (Direction direction : Direction.Plane.HORIZONTAL) {
                mutable.set(processedBlockInfo.pos());
                mutable.move(direction);
                ChunkPos chunkPos = new ChunkPos(mutable.getX() >> 4, mutable.getZ() >> 4);
                ChunkAccess chunkAccess2 = cachedChunk;
                if (!chunkPos.equals(cachedChunk.getPos())) {
                    chunkAccess2 = levelReader.getChunk(mutable);
                }
                BlockState sideBlock = chunkAccess2.getBlockState(mutable);
                if (!sideBlock.canSurvive(levelReader, mutable)) {
                    chunkAccess2.setBlockState(mutable, processedBlockInfo.state(), Block.UPDATE_CLIENTS);
                }
            }
        }

        return processedBlockInfo;
    }

    @Override
    public MapCodec<RemoveFloatingBlocksProcessor> codec() {
        return MAP_CODEC;
    }
}
