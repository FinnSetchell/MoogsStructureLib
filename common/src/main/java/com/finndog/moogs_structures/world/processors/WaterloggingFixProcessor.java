package com.finndog.moogs_structures.world.processors;

import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class WaterloggingFixProcessor extends StructureProcessor {

    public static final MapCodec<WaterloggingFixProcessor> CODEC = MapCodec.unit(WaterloggingFixProcessor::new);

    private WaterloggingFixProcessor() {}

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos pos, BlockPos pos2, StructureTemplate.StructureBlockInfo infoIn1, StructureTemplate.StructureBlockInfo infoIn2, StructurePlaceSettings settings) {
        if (!infoIn2.state().hasProperty(BlockStateProperties.WATERLOGGED)) {
            return infoIn2;
        }

        if (levelReader instanceof WorldGenRegion worldGenRegion && !worldGenRegion.getCenter().equals(new ChunkPos(infoIn2.pos()))) {
            return infoIn2;
        }

        BlockState blockState = levelReader.getChunk(infoIn2.pos()).getBlockState(infoIn2.pos());
        boolean isWater = blockState.getFluidState().is(FluidTags.WATER);

        if (isWater) {
            ChunkAccess chunk = levelReader.getChunk(infoIn2.pos());
            int currentY = infoIn2.pos().getY();
            if (currentY >= chunk.getMinBuildHeight() && currentY <= chunk.getMaxBuildHeight()) {
                ((LevelAccessor) levelReader).scheduleTick(infoIn2.pos(), infoIn2.state().getBlock(), 0);
            }
        }

        return new StructureTemplate.StructureBlockInfo(
                infoIn2.pos(),
                infoIn2.state().setValue(BlockStateProperties.WATERLOGGED, isWater),
                infoIn2.nbt());
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.WATERLOGGING_FIX_PROCESSOR.get();
    }
}
