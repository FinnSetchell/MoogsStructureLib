package com.finndog.moogs_structures.world.processors;

import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Settles blocks down onto the terrain heightmap.
 * Ported from RepurposedStructures (TelepathicGrunt) to MSL.
 */
public class SuperGravityProcessor implements StructureProcessor {

    public static final MapCodec<SuperGravityProcessor> MAP_CODEC = RecordCodecBuilder.mapCodec(
            (instance) -> instance.group(
                    Heightmap.Types.CODEC.fieldOf("heightmap").orElse(Heightmap.Types.WORLD_SURFACE_WG).forGetter((p) -> p.heightmap),
                    Codec.INT.fieldOf("offset").orElse(0).forGetter((p) -> p.offset),
                    BuiltInRegistries.BLOCK.byNameCodec().listOf().fieldOf("ignore_block").orElse(new ArrayList<>()).xmap(HashSet::new, ArrayList::new).forGetter(p -> p.blocksToIgnore),
                    Codec.BOOL.fieldOf("require_water_surface").orElse(false).forGetter((p) -> p.requireWaterSurface)
            ).apply(instance, SuperGravityProcessor::new));

    private final Heightmap.Types heightmap;
    private final int offset;
    private final HashSet<Block> blocksToIgnore;
    private final boolean requireWaterSurface;

    public SuperGravityProcessor(Heightmap.Types types, int offset, HashSet<Block> blocksToIgnore, boolean requireWaterSurface) {
        this.heightmap = types;
        this.offset = offset;
        this.blocksToIgnore = blocksToIgnore;
        this.requireWaterSurface = requireWaterSurface;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos targetPosition, BlockPos referencePos, BlockPos templateRelativePos, StructureTemplate.StructureBlockInfo processedBlockInfo, StructurePlaceSettings placeSettings) {
        Heightmap.Types heightmap$types;
        if (levelReader instanceof ServerLevel) {
            if (this.heightmap == Heightmap.Types.WORLD_SURFACE_WG) {
                heightmap$types = Heightmap.Types.WORLD_SURFACE;
            }
            else if (this.heightmap == Heightmap.Types.OCEAN_FLOOR_WG) {
                heightmap$types = Heightmap.Types.OCEAN_FLOOR;
            }
            else {
                heightmap$types = this.heightmap;
            }
        }
        else {
            heightmap$types = this.heightmap;
        }

        int heightmapY = levelReader.getHeight(heightmap$types, processedBlockInfo.pos().getX(), processedBlockInfo.pos().getZ());
        int localY = templateRelativePos.getY();

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        mutable.set(processedBlockInfo.pos().getX(), heightmapY, processedBlockInfo.pos().getZ());
        BlockState aboveState = levelReader.getBlockState(mutable);
        mutable.move(Direction.DOWN);
        BlockState currentState = levelReader.getBlockState(mutable);
        while (blocksToIgnore.contains(currentState.getBlock()) || (requireWaterSurface && currentState.getFluidState().is(FluidTags.WATER))) {
            aboveState = currentState;
            mutable.move(Direction.DOWN);
            currentState = levelReader.getBlockState(mutable);
        }

        if (requireWaterSurface ? aboveState.getFluidState().is(FluidTags.WATER) : aboveState.isAir()) {
            return new StructureTemplate.StructureBlockInfo(new BlockPos(processedBlockInfo.pos().getX(), mutable.getY() + localY + this.offset, processedBlockInfo.pos().getZ()), processedBlockInfo.state(), processedBlockInfo.nbt());
        }

        return null;
    }

    @Override
    public MapCodec<SuperGravityProcessor> codec() {
        return MAP_CODEC;
    }
}
