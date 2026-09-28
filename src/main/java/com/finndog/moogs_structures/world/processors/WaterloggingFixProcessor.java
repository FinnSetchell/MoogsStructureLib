//? if <1.21.1 {
/*package com.finndog.moogs_structures.world.processors;

import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
//? if >=1.20.6 {
import com.mojang.serialization.MapCodec;
//?} else {
/^import com.mojang.serialization.Codec;
^///?}
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
//? if >=1.20.6 {
import net.minecraft.tags.FluidTags;
//?}
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
//? if >=1.20.6 {
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
//?}
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class WaterloggingFixProcessor extends StructureProcessor {

    //? if >=1.20.6 {
    public static final MapCodec<WaterloggingFixProcessor> CODEC = MapCodec.unit(WaterloggingFixProcessor::new);
    //?} else {
    /^public static final Codec<WaterloggingFixProcessor> CODEC = Codec.unit(WaterloggingFixProcessor::new);
    ^///?}

    //? if >=1.20.6 {
    private WaterloggingFixProcessor() {}
    //?} else {
    /^private WaterloggingFixProcessor() { }
    ^///?}

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos pos, BlockPos pos2, StructureTemplate.StructureBlockInfo infoIn1, StructureTemplate.StructureBlockInfo infoIn2, StructurePlaceSettings settings) {
        //? if >=1.20.6 {
        if (!infoIn2.state().hasProperty(BlockStateProperties.WATERLOGGED)) {
            return infoIn2;
        }
        //?} else {
        /^if(!infoIn2.state().getFluidState().isEmpty()) {
            if(levelReader instanceof WorldGenRegion worldGenRegion && !worldGenRegion.getCenter().equals(new ChunkPos(infoIn2.pos()))) {
                return infoIn2;
            }
        ^///?}

        //? if >=1.20.6 {
        if (levelReader instanceof WorldGenRegion worldGenRegion && !worldGenRegion.getCenter().equals(new ChunkPos(infoIn2.pos()))) {
            return infoIn2;
        }

        BlockState blockState = levelReader.getChunk(infoIn2.pos()).getBlockState(infoIn2.pos());
        boolean isWater = blockState.getFluidState().is(FluidTags.WATER);

        if (isWater) {
        //?}
            ChunkAccess chunk = levelReader.getChunk(infoIn2.pos());
            //? if <1.20.6 {
            /^int minY = chunk.getMinBuildHeight();
            int maxY = chunk.getMaxBuildHeight();
            ^///?}
            int currentY = infoIn2.pos().getY();
            //? if >=1.20.6 {
            if (currentY >= chunk.getMinBuildHeight() && currentY <= chunk.getMaxBuildHeight()) {
            //?} else {
            /^if(currentY >= minY && currentY <= maxY) {
            ^///?}
                ((LevelAccessor) levelReader).scheduleTick(infoIn2.pos(), infoIn2.state().getBlock(), 0);
            }
        }

        //? if >=1.20.6 {
        return new StructureTemplate.StructureBlockInfo(
                infoIn2.pos(),
                infoIn2.state().setValue(BlockStateProperties.WATERLOGGED, isWater),
                infoIn2.nbt());
        //?} else {
        /^return infoIn2;
        ^///?}
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.WATERLOGGING_FIX_PROCESSOR.get();
    }
}

*///?}