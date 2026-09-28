package com.finndog.moogs_structures.world.structures;

import com.finndog.moogs_structures.modinit.MoogsStructuresStructures;
import com.finndog.moogs_structures.utils.GeneralUtils;
//? if >=1.21.1 {
import com.finndog.moogs_structures.world.structures.codecs.YRangeAllowance;
//?}
import com.finndog.moogs_structures.world.structures.pieces.PieceLimitedJigsawManager;
import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptation;
import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptationStructure;
import com.google.common.collect.Maps;
//? if <1.21.1 {
/*import com.mojang.datafixers.util.Pair;
*///?}
import com.mojang.serialization.Codec;
//? if >=1.20.6 {
import com.mojang.serialization.MapCodec;
//?}
import com.mojang.serialization.codecs.RecordCodecBuilder;
//? if <1.21.11 {
import net.minecraft.Util;
//?} else {
/*import net.minecraft.util.Util;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource;
//? if >=1.21.1 <1.21.4 {
import net.minecraft.world.level.block.Blocks;
//?}
//? if >=1.21.5 <26.1.2 {
/*import net.minecraft.world.level.block.Blocks;
*///?}
import net.minecraft.world.level.block.state.BlockState;
//? if >=1.21.1 <1.21.4 {
import net.minecraft.world.level.chunk.ChunkGenerator;
//?}
//? if >=1.21.5 <26.1.2 {
/*import net.minecraft.world.level.chunk.ChunkGenerator;
*///?}
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
//? if >=1.21.1 {
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
//?}

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

public class GenericJigsawStructure extends Structure implements EnhancedTerrainAdaptationStructure {

    //? if >=1.20.6 {
    public static final MapCodec<GenericJigsawStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    //?} else {
    /*public static final Codec<GenericJigsawStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    *///?}
            GenericJigsawStructure.settingsCodec(instance),
            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
            Codec.intRange(0, 128).fieldOf("size").forGetter(structure -> structure.size),
            //? if >=1.21.1 {
            YRangeAllowance.CODEC.optionalFieldOf("y_allowance").forGetter(structure -> structure.yAllowance),
            //?} else {
            /*Codec.INT.optionalFieldOf("min_y_allowed").forGetter(structure -> structure.minYAllowed),
            Codec.INT.optionalFieldOf("max_y_allowed").forGetter(structure -> structure.maxYAllowed),
            Codec.intRange(1, 1000).optionalFieldOf("allowed_y_range_from_start").forGetter(structure -> structure.allowedYRangeFromStart),
            *///?}
            HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
            Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
            Codec.BOOL.fieldOf("cannot_spawn_in_liquid").orElse(false).forGetter(structure -> structure.cannotSpawnInLiquid),
            Codec.intRange(1, 100).optionalFieldOf("terrain_height_radius_check").forGetter(structure -> structure.terrainHeightCheckRadius),
            Codec.intRange(1, 1000).optionalFieldOf("allowed_terrain_height_range").forGetter(structure -> structure.allowedTerrainHeightRange),
            Codec.intRange(1, 100).optionalFieldOf("valid_biome_radius_check").forGetter(structure -> structure.biomeRadius),
            ResourceLocation.CODEC.listOf().fieldOf("pools_that_ignore_boundaries").orElse(new ArrayList<>()).xmap(HashSet::new, ArrayList::new).forGetter(structure -> structure.poolsThatIgnoreBoundaries),
            Codec.intRange(1, 128).optionalFieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter),
            StringRepresentable.fromEnum(BURYING_TYPE::values).optionalFieldOf("burying_type").forGetter(structure -> structure.buryingType),
            // DFU groups cap at 16 slots, so these two share one; the JSON stays flat.
            //? if >=1.21.1 {
            Codec.BOOL.fieldOf("use_bounding_box_hack").orElse(false).forGetter(structure -> structure.useBoundingBoxHack),
            LiquidSettings.CODEC.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(structure -> structure.liquidSettings),
            EnhancedTerrainAdaptation.CODEC.optionalFieldOf("enhanced_terrain_adaptation", EnhancedTerrainAdaptation.NONE).forGetter(structure -> structure.enhancedTerrainAdaptation)
    ).apply(instance, GenericJigsawStructure::new));
            //?} else {
            /*Codec.mapPair(Codec.BOOL.fieldOf("use_bounding_box_hack").orElse(false),
                    EnhancedTerrainAdaptation.CODEC.optionalFieldOf("enhanced_terrain_adaptation", EnhancedTerrainAdaptation.NONE))
                    .forGetter(structure -> Pair.of(structure.useBoundingBoxHack, structure.enhancedTerrainAdaptation))
    ).apply(instance, (config, startPool, size, minYAllowed, maxYAllowed, allowedYRangeFromStart, startHeight, projectStartToHeightmap, cannotSpawnInLiquid, terrainHeightCheckRadius, allowedTerrainHeightRange, biomeRadius, poolsThatIgnoreBoundaries, maxDistanceFromCenter, buryingType, hackAndAdaptation) -> new GenericJigsawStructure(
            config, startPool, size, minYAllowed, maxYAllowed, allowedYRangeFromStart, startHeight, projectStartToHeightmap, cannotSpawnInLiquid, terrainHeightCheckRadius, allowedTerrainHeightRange, biomeRadius, poolsThatIgnoreBoundaries, maxDistanceFromCenter, buryingType,
            hackAndAdaptation.getFirst(), hackAndAdaptation.getSecond())));
            *///?}

    public final Holder<StructureTemplatePool> startPool;
    public final int size;
    //? if >=1.21.1 {
    public final Optional<YRangeAllowance> yAllowance;
    //?} else {
    /*public final Optional<Integer> minYAllowed;
    public final Optional<Integer> maxYAllowed;
    public final Optional<Integer> allowedYRangeFromStart;
    *///?}
    public final HeightProvider startHeight;
    public final Optional<Heightmap.Types> projectStartToHeightmap;
    public final boolean cannotSpawnInLiquid;
    public final Optional<Integer> terrainHeightCheckRadius;
    public final Optional<Integer> allowedTerrainHeightRange;
    public final Optional<Integer> biomeRadius;
    public final HashSet<ResourceLocation> poolsThatIgnoreBoundaries;
    public final Optional<Integer> maxDistanceFromCenter;
    public final Optional<BURYING_TYPE> buryingType;
    public final boolean useBoundingBoxHack;
    //? if >=1.21.1 {
    public final LiquidSettings liquidSettings;
    //?}
    public final EnhancedTerrainAdaptation enhancedTerrainAdaptation;


    public GenericJigsawStructure(StructureSettings config,
                                  Holder<StructureTemplatePool> startPool,
                                  int size,
                                  //? if >=1.21.1 {
                                  Optional<YRangeAllowance> yAllowance,
                                  //?} else {
                                  /*Optional<Integer> minYAllowed,
                                  Optional<Integer> maxYAllowed,
                                  Optional<Integer> allowedYRangeFromStart,
                                  *///?}
                                  HeightProvider startHeight,
                                  Optional<Heightmap.Types> projectStartToHeightmap,
                                  boolean cannotSpawnInLiquid,
                                  Optional<Integer> terrainHeightCheckRadius,
                                  Optional<Integer> allowedTerrainHeightRange,
                                  Optional<Integer> biomeRadius,
                                  HashSet<ResourceLocation> poolsThatIgnoreBoundaries,
                                  Optional<Integer> maxDistanceFromCenter,
                                  Optional<BURYING_TYPE> buryingType,
                                  boolean useBoundingBoxHack,
                                  //? if >=1.21.1 {
                                  LiquidSettings liquidSettings,
                                  //?}
                                  EnhancedTerrainAdaptation enhancedTerrainAdaptation)
    {
        super(config);
        this.enhancedTerrainAdaptation = enhancedTerrainAdaptation;
        this.startPool = startPool;
        this.size = size;
        //? if >=1.21.1 {
        this.yAllowance = yAllowance;
        //?} else {
        /*this.minYAllowed = minYAllowed;
        this.maxYAllowed = maxYAllowed;
        this.allowedYRangeFromStart = allowedYRangeFromStart;
        *///?}
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.cannotSpawnInLiquid = cannotSpawnInLiquid;
        this.terrainHeightCheckRadius = terrainHeightCheckRadius;
        this.allowedTerrainHeightRange = allowedTerrainHeightRange;
        this.biomeRadius = biomeRadius;
        this.poolsThatIgnoreBoundaries = poolsThatIgnoreBoundaries;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.buryingType = buryingType;
        this.useBoundingBoxHack = useBoundingBoxHack;
        //? if >=1.21.1 {
        this.liquidSettings = liquidSettings;
        //?}


        //? if >=1.21.1 {
        if (yAllowance.isPresent() && yAllowance.get().maxYAllowed.isPresent() && yAllowance.get().minYAllowed.isPresent() && yAllowance.get().maxYAllowed.get() < yAllowance.get().minYAllowed.get()) {
        //?} else {
        /*if (maxYAllowed.isPresent() && minYAllowed.isPresent() && maxYAllowed.get() < minYAllowed.get()) {
        *///?}
            throw new RuntimeException("""
                Moog's Structure Lib: maxYAllowed cannot be less than minYAllowed.
                Please correct this error as there's no way to spawn this structure properly
                    Structure pool of problematic structure: %s
            """.formatted(startPool.value()));
        }
    }

    @Override
    public EnhancedTerrainAdaptation getEnhancedTerrainAdaptation() {
        return this.enhancedTerrainAdaptation;
    }

    protected boolean extraSpawningChecks(GenerationContext context, BlockPos blockPos) {
        ChunkPos chunkPos = context.chunkPos();

        if (this.biomeRadius.isPresent() && !(context.biomeSource() instanceof CheckerboardColumnBiomeSource)) {
            int validBiomeRange = this.biomeRadius.get();
            int sectionY = blockPos.getY();
            if (projectStartToHeightmap.isPresent()) {
                //? if >=1.21.1 <1.21.4 {
                sectionY += GeneralUtils.getCachedFreeHeight(context.chunkGenerator(), blockPos.getX() + 7, blockPos.getZ() + 7, projectStartToHeightmap.get(), context.heightAccessor(), context.randomState()) - 1;
                //?}
                //? if <1.21.1 {
                /*sectionY += context.chunkGenerator().getFirstOccupiedHeight(blockPos.getX(), blockPos.getZ(), projectStartToHeightmap.get(), context.heightAccessor(), context.randomState());
                *///?}
                //? if >=1.21.4 <1.21.5 {
                /*sectionY += context.chunkGenerator().getFirstOccupiedHeight(blockPos.getX(), blockPos.getZ(), projectStartToHeightmap.get(), context.heightAccessor(), context.randomState());
                *///?}
                //? if >=1.21.5 <26.1.2 {
                /*sectionY += GeneralUtils.getCachedFreeHeight(context.chunkGenerator(), blockPos.getX() + 7, blockPos.getZ() + 7, projectStartToHeightmap.get(), context.heightAccessor(), context.randomState()) - 1;
                *///?}
                //? if >=26.1.2 {
                /*sectionY += context.chunkGenerator().getFirstOccupiedHeight(blockPos.getX(), blockPos.getZ(), projectStartToHeightmap.get(), context.heightAccessor(), context.randomState());
                *///?}
            }
            sectionY = QuartPos.fromBlock(sectionY);

            //? if <26.1.2 {
            for (int curChunkX = chunkPos.x - validBiomeRange; curChunkX <= chunkPos.x + validBiomeRange; curChunkX++) {
                for (int curChunkZ = chunkPos.z - validBiomeRange; curChunkZ <= chunkPos.z + validBiomeRange; curChunkZ++) {
            //?} else {
            /*for (int curChunkX = chunkPos.x() - validBiomeRange; curChunkX <= chunkPos.x() + validBiomeRange; curChunkX++) {
                for (int curChunkZ = chunkPos.z() - validBiomeRange; curChunkZ <= chunkPos.z() + validBiomeRange; curChunkZ++) {
            *///?}
                    //? if <26.3 {
                    Holder<Biome> biome = context.biomeSource().getNoiseBiome(QuartPos.fromSection(curChunkX), sectionY, QuartPos.fromSection(curChunkZ), context.randomState().sampler());
                    //?} else {
                    /*Holder<Biome> biome = context.biomeResolver().getNoiseBiome(QuartPos.fromSection(curChunkX), sectionY, QuartPos.fromSection(curChunkZ));
                    *///?}
                    if (!context.validBiome().test(biome)) {
                        return false;
                    }
                }
            }
        }

        if (this.cannotSpawnInLiquid) {
            BlockPos centerOfChunk = chunkPos.getMiddleBlockPosition(0);
            //? if >=1.21.1 <1.21.4 {
            ChunkGenerator chunkGenerator = context.chunkGenerator();
            NoiseColumn columnOfBlocks = chunkGenerator.getBaseColumn(centerOfChunk.getX(), centerOfChunk.getZ(), context.heightAccessor(), context.randomState());
            BlockState topBlock = Blocks.AIR.defaultBlockState();
            for (int i = chunkGenerator.getMinY() + chunkGenerator.getGenDepth(); i > chunkGenerator.getMinY(); i--) {
                BlockState block = columnOfBlocks.getBlock(i);
                if (!block.isAir()) {
                    topBlock = block;
                    break;
                }
            }
            //?}
            //? if <1.21.1 {
            /*int landHeight = context.chunkGenerator().getFirstOccupiedHeight(centerOfChunk.getX(), centerOfChunk.getZ(), Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            NoiseColumn columnOfBlocks = context.chunkGenerator().getBaseColumn(centerOfChunk.getX(), centerOfChunk.getZ(), context.heightAccessor(), context.randomState());
            BlockState topBlock = columnOfBlocks.getBlock(centerOfChunk.getY() + landHeight);
            *///?}
            //? if >=1.21.4 <1.21.5 {
            /*int landHeight = context.chunkGenerator().getFirstOccupiedHeight(centerOfChunk.getX(), centerOfChunk.getZ(), Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            NoiseColumn columnOfBlocks = context.chunkGenerator().getBaseColumn(centerOfChunk.getX(), centerOfChunk.getZ(), context.heightAccessor(), context.randomState());
            BlockState topBlock = columnOfBlocks.getBlock(centerOfChunk.getY() + landHeight);
            *///?}
            //? if >=1.21.5 <26.1.2 {
            /*ChunkGenerator chunkGenerator = context.chunkGenerator();
            NoiseColumn columnOfBlocks = chunkGenerator.getBaseColumn(centerOfChunk.getX(), centerOfChunk.getZ(), context.heightAccessor(), context.randomState());
            BlockState topBlock = Blocks.AIR.defaultBlockState();
            for (int i = chunkGenerator.getMinY() + chunkGenerator.getGenDepth(); i > chunkGenerator.getMinY(); i--) {
                BlockState block = columnOfBlocks.getBlock(i);
                if (!block.isAir()) {
                    topBlock = block;
                    break;
                }
            }
            *///?}
            //? if >=26.1.2 {
            /*int landHeight = context.chunkGenerator().getFirstOccupiedHeight(centerOfChunk.getX(), centerOfChunk.getZ(), Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            NoiseColumn columnOfBlocks = context.chunkGenerator().getBaseColumn(centerOfChunk.getX(), centerOfChunk.getZ(), context.heightAccessor(), context.randomState());
            BlockState topBlock = columnOfBlocks.getBlock(centerOfChunk.getY() + landHeight);
            *///?}

            if(!topBlock.getFluidState().isEmpty()) {
                return false;
            }
        }

        if (this.terrainHeightCheckRadius.isPresent() &&
                //? if >=1.21.1 {
                (this.allowedTerrainHeightRange.isPresent() || (this.yAllowance.isPresent() && this.yAllowance.get().minYAllowed.isPresent())))
                //?} else {
            /*(this.allowedTerrainHeightRange.isPresent() || this.minYAllowed.isPresent()))
                *///?}
        {
            int maxTerrainHeight = Integer.MIN_VALUE;
            int minTerrainHeight = Integer.MAX_VALUE;
            int terrainCheckRange = this.terrainHeightCheckRadius.get();

            //? if <26.1.2 {
            for (int curChunkX = chunkPos.x - terrainCheckRange; curChunkX <= chunkPos.x + terrainCheckRange; curChunkX++) {
                for (int curChunkZ = chunkPos.z - terrainCheckRange; curChunkZ <= chunkPos.z + terrainCheckRange; curChunkZ++) {
            //?}
                    //? if >=1.21.1 <1.21.4 {
                    int height = GeneralUtils.getCachedFreeHeight(context.chunkGenerator(), (curChunkX << 4) + 7, (curChunkZ << 4) + 7, this.projectStartToHeightmap.orElse(Heightmap.Types.WORLD_SURFACE_WG), context.heightAccessor(), context.randomState());
                    //?}
                    //? if <1.21.1 {
                    /*int height = context.chunkGenerator().getBaseHeight((curChunkX << 4) + 7, (curChunkZ << 4) + 7, this.projectStartToHeightmap.orElse(Heightmap.Types.WORLD_SURFACE_WG), context.heightAccessor(), context.randomState());
                    *///?}
                    //? if >=1.21.4 <1.21.5 {
                    /*int height = context.chunkGenerator().getBaseHeight((curChunkX << 4) + 7, (curChunkZ << 4) + 7, this.projectStartToHeightmap.orElse(Heightmap.Types.WORLD_SURFACE_WG), context.heightAccessor(), context.randomState());
                    *///?}
                    //? if >=1.21.5 <26.1.2 {
                    /*int height = GeneralUtils.getCachedFreeHeight(context.chunkGenerator(), (curChunkX << 4) + 7, (curChunkZ << 4) + 7, this.projectStartToHeightmap.orElse(Heightmap.Types.WORLD_SURFACE_WG), context.heightAccessor(), context.randomState());
                    *///?}
            //? if >=26.1.2 {
            /*for (int curChunkX = chunkPos.x() - terrainCheckRange; curChunkX <= chunkPos.x() + terrainCheckRange; curChunkX++) {
                for (int curChunkZ = chunkPos.z() - terrainCheckRange; curChunkZ <= chunkPos.z() + terrainCheckRange; curChunkZ++) {
                    int height = context.chunkGenerator().getBaseHeight((curChunkX << 4) + 7, (curChunkZ << 4) + 7, this.projectStartToHeightmap.orElse(Heightmap.Types.WORLD_SURFACE_WG), context.heightAccessor(), context.randomState());
            *///?}
                    maxTerrainHeight = Math.max(maxTerrainHeight, height);
                    minTerrainHeight = Math.min(minTerrainHeight, height);

                    //? if >=1.21.1 {
                    if (this.yAllowance.isPresent() && this.yAllowance.get().minYAllowed.isPresent() && minTerrainHeight < this.yAllowance.get().minYAllowed.get()) {
                    //?} else {
                    /*if (this.minYAllowed.isPresent() && minTerrainHeight < this.minYAllowed.get()) {
                    *///?}
                        return false;
                    }

                    //? if >=1.21.1 {
                    if (this.yAllowance.isPresent() && this.yAllowance.get().maxYAllowed.isPresent() && minTerrainHeight > this.yAllowance.get().maxYAllowed.get()) {
                    //?} else {
                    /*if (this.maxYAllowed.isPresent() && minTerrainHeight > this.maxYAllowed.get()) {
                    *///?}
                        return false;
                    }
                }
            }

            if(this.allowedTerrainHeightRange.isPresent() &&
                maxTerrainHeight - minTerrainHeight > this.allowedTerrainHeightRange.get())
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int offsetY = this.startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
        BlockPos blockpos = new BlockPos(context.chunkPos().getMinBlockX(), offsetY, context.chunkPos().getMinBlockZ());

        if (!extraSpawningChecks(context, blockpos)) {
            return Optional.empty();
        }

        int topClipOff = Integer.MAX_VALUE;
        int bottomClipOff = Integer.MIN_VALUE;
        //? if <1.21.1 {
        /*if(this.allowedYRangeFromStart.isPresent()) {
            topClipOff = blockpos.getY() + this.allowedYRangeFromStart.get();
            bottomClipOff = blockpos.getY() - this.allowedYRangeFromStart.get();
        }
        *///?}

        //? if >=1.21.1 {
        if (this.yAllowance.isPresent()) {
            if(this.yAllowance.get().maxYAllowed.isPresent()) {
                topClipOff = Math.min(topClipOff, this.yAllowance.get().maxYAllowed.get());
            }
        //?} else {
        /*if(this.maxYAllowed.isPresent()) {
            topClipOff = Math.min(topClipOff, this.maxYAllowed.get());
        }
        *///?}

            //? if >=1.21.1 {
            if(this.yAllowance.get().minYAllowed.isPresent()) {
                bottomClipOff = Math.max(bottomClipOff, this.yAllowance.get().minYAllowed.get());
            }
            //?} else {
        /*if(this.minYAllowed.isPresent()) {
            bottomClipOff = Math.max(bottomClipOff, this.minYAllowed.get());
            *///?}
        }

        int finalTopClipOff = topClipOff;
        int finalBottomClipOff = bottomClipOff;
        return PieceLimitedJigsawManager.assembleJigsawStructure(
                context,
                this.startPool,
                this.size,
                //? if <1.21.2 {
                context.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(this),
                //?} else {
                /*context.registryAccess().lookupOrThrow(Registries.STRUCTURE).getKey(this),
                *///?}
                blockpos,
                this.useBoundingBoxHack,
                this.projectStartToHeightmap,
                topClipOff,
                bottomClipOff,
                this.poolsThatIgnoreBoundaries,
                this.maxDistanceFromCenter,
                this.buryingType,
                //? if >=1.21.1 {
                this.liquidSettings,
                //?}
                (structurePiecesBuilder, pieces) -> postLayoutAdjustments(structurePiecesBuilder, context, offsetY, blockpos, finalTopClipOff, finalBottomClipOff, pieces));
    }

    protected void postLayoutAdjustments(StructurePiecesBuilder structurePiecesBuilder, GenerationContext context, int offsetY, BlockPos blockpos, int topClipOff, int bottomClipOff, List<PoolElementStructurePiece> pieces) {
        GeneralUtils.centerAllPieces(blockpos, pieces);

        if(this.buryingType.isEmpty()) {
            return;
        }

        if(this.buryingType.get() == BURYING_TYPE.LOWEST_CORNER) {
            Heightmap.Types heightMapToUse = this.projectStartToHeightmap.orElse(Heightmap.Types.WORLD_SURFACE_WG);

            BoundingBox box = pieces.get(0).getBoundingBox();
            //? if >=1.21.1 <1.21.4 {
            int highestLandPos = GeneralUtils.getCachedFreeHeight(context.chunkGenerator(), box.minX(), box.minZ(), heightMapToUse, context.heightAccessor(), context.randomState()) - 1;
            highestLandPos = Math.min(highestLandPos, GeneralUtils.getCachedFreeHeight(context.chunkGenerator(), box.minX(), box.maxZ(), heightMapToUse, context.heightAccessor(), context.randomState()) - 1);
            highestLandPos = Math.min(highestLandPos, GeneralUtils.getCachedFreeHeight(context.chunkGenerator(), box.maxX(), box.minZ(), heightMapToUse, context.heightAccessor(), context.randomState()) - 1);
            highestLandPos = Math.min(highestLandPos, GeneralUtils.getCachedFreeHeight(context.chunkGenerator(), box.maxX(), box.maxZ(), heightMapToUse, context.heightAccessor(), context.randomState()) - 1);
            //?}
            //? if <1.21.1 {
            /*int highestLandPos = context.chunkGenerator().getFirstOccupiedHeight(box.minX(), box.minZ(), heightMapToUse, context.heightAccessor(), context.randomState());
            highestLandPos = Math.min(highestLandPos, context.chunkGenerator().getFirstOccupiedHeight(box.minX(), box.maxZ(), heightMapToUse, context.heightAccessor(), context.randomState()));
            highestLandPos = Math.min(highestLandPos, context.chunkGenerator().getFirstOccupiedHeight(box.maxX(), box.minZ(), heightMapToUse, context.heightAccessor(), context.randomState()));
            highestLandPos = Math.min(highestLandPos, context.chunkGenerator().getFirstOccupiedHeight(box.maxX(), box.maxZ(), heightMapToUse, context.heightAccessor(), context.randomState()));
            *///?}
            //? if >=1.21.4 <1.21.5 {
            /*int highestLandPos = context.chunkGenerator().getFirstOccupiedHeight(box.minX(), box.minZ(), heightMapToUse, context.heightAccessor(), context.randomState());
            highestLandPos = Math.min(highestLandPos, context.chunkGenerator().getFirstOccupiedHeight(box.minX(), box.maxZ(), heightMapToUse, context.heightAccessor(), context.randomState()));
            highestLandPos = Math.min(highestLandPos, context.chunkGenerator().getFirstOccupiedHeight(box.maxX(), box.minZ(), heightMapToUse, context.heightAccessor(), context.randomState()));
            highestLandPos = Math.min(highestLandPos, context.chunkGenerator().getFirstOccupiedHeight(box.maxX(), box.maxZ(), heightMapToUse, context.heightAccessor(), context.randomState()));
            *///?}
            //? if >=1.21.5 <26.1.2 {
            /*int highestLandPos = GeneralUtils.getCachedFreeHeight(context.chunkGenerator(), box.minX(), box.minZ(), heightMapToUse, context.heightAccessor(), context.randomState()) - 1;
            highestLandPos = Math.min(highestLandPos, GeneralUtils.getCachedFreeHeight(context.chunkGenerator(), box.minX(), box.maxZ(), heightMapToUse, context.heightAccessor(), context.randomState()) - 1);
            highestLandPos = Math.min(highestLandPos, GeneralUtils.getCachedFreeHeight(context.chunkGenerator(), box.maxX(), box.minZ(), heightMapToUse, context.heightAccessor(), context.randomState()) - 1);
            highestLandPos = Math.min(highestLandPos, GeneralUtils.getCachedFreeHeight(context.chunkGenerator(), box.maxX(), box.maxZ(), heightMapToUse, context.heightAccessor(), context.randomState()) - 1);
            *///?}
            //? if >=26.1.2 {
            /*int highestLandPos = context.chunkGenerator().getFirstOccupiedHeight(box.minX(), box.minZ(), heightMapToUse, context.heightAccessor(), context.randomState());
            highestLandPos = Math.min(highestLandPos, context.chunkGenerator().getFirstOccupiedHeight(box.minX(), box.maxZ(), heightMapToUse, context.heightAccessor(), context.randomState()));
            highestLandPos = Math.min(highestLandPos, context.chunkGenerator().getFirstOccupiedHeight(box.maxX(), box.minZ(), heightMapToUse, context.heightAccessor(), context.randomState()));
            highestLandPos = Math.min(highestLandPos, context.chunkGenerator().getFirstOccupiedHeight(box.maxX(), box.maxZ(), heightMapToUse, context.heightAccessor(), context.randomState()));
            *///?}

            if(!this.cannotSpawnInLiquid && (heightMapToUse == Heightmap.Types.OCEAN_FLOOR_WG || heightMapToUse == Heightmap.Types.OCEAN_FLOOR)) {
                int maxHeightForSubmerging = context.chunkGenerator().getSeaLevel() - box.getYSpan();
                highestLandPos = Math.min(highestLandPos, maxHeightForSubmerging);
            }
            else {
                highestLandPos = Math.max(highestLandPos, context.chunkGenerator().getSeaLevel());
            }

            offsetToNewHeight(context, offsetY, pieces, box, highestLandPos);
        }
        else if(this.buryingType.get() == BURYING_TYPE.AVERAGE_LAND) {
            BoundingBox box = pieces.get(0).getBoundingBox();
            //? if <26.3 {
            BlockPos centerPos = new BlockPos(box.getCenter());
            //?} else {
            /*BlockPos centerPos = box.getCenter();
            *///?}
            int radius = (int) Math.sqrt((box.getLength().getX() * box.getLength().getX()) + (box.getLength().getZ() * box.getLength().getZ())) / 2;

            Heightmap.Types heightMapToUse = this.projectStartToHeightmap.orElse(Heightmap.Types.WORLD_SURFACE_WG);
            List<Integer> landHeights = new ArrayList<>();
            for(int xOffset = -radius; xOffset <= radius; xOffset += (radius/2)) {
                for(int zOffset = -radius; zOffset <= radius; zOffset += (radius/2)) {
                    //? if >=1.21.1 <1.21.4 {
                    int landHeight = GeneralUtils.getCachedFreeHeight(context.chunkGenerator(), centerPos.getX() + xOffset, centerPos.getZ() + zOffset, heightMapToUse, context.heightAccessor(), context.randomState()) - 1;
                    //?}
                    //? if <1.21.1 {
                    /*int landHeight = context.chunkGenerator().getFirstOccupiedHeight(centerPos.getX() + xOffset, centerPos.getZ() + zOffset, heightMapToUse, context.heightAccessor(), context.randomState());
                    *///?}
                    //? if >=1.21.4 <1.21.5 {
                    /*int landHeight = context.chunkGenerator().getFirstOccupiedHeight(centerPos.getX() + xOffset, centerPos.getZ() + zOffset, heightMapToUse, context.heightAccessor(), context.randomState());
                    *///?}
                    //? if >=1.21.5 <26.1.2 {
                    /*int landHeight = GeneralUtils.getCachedFreeHeight(context.chunkGenerator(), centerPos.getX() + xOffset, centerPos.getZ() + zOffset, heightMapToUse, context.heightAccessor(), context.randomState()) - 1;
                    *///?}
                    //? if >=26.1.2 {
                    /*int landHeight = context.chunkGenerator().getFirstOccupiedHeight(centerPos.getX() + xOffset, centerPos.getZ() + zOffset, heightMapToUse, context.heightAccessor(), context.randomState());
                    *///?}
                    landHeights.add(landHeight);
                }
            }

            // Offset structure to average land around it
            //? if >=1.21.1 {
            int minYAllowed;
            int maxYAllowed;
            //?} else {
            /*OptionalDouble avgHeightOptional = landHeights.stream()
                    .filter(height -> height > this.minYAllowed.orElse(Integer.MIN_VALUE) && height < this.maxYAllowed.orElse(Integer.MAX_VALUE))
                    .mapToInt(Integer::intValue).average();
            *///?}

            //? if >=1.21.1 {
            if (this.yAllowance.isPresent()) {
                minYAllowed = this.yAllowance.get().minYAllowed.orElse(Integer.MIN_VALUE);
                maxYAllowed = this.yAllowance.get().maxYAllowed.orElse(Integer.MAX_VALUE);
            }
            else {
                maxYAllowed = Integer.MAX_VALUE;
                minYAllowed = Integer.MIN_VALUE;
            //?} else {
            /*if(this.maxYAllowed.isPresent() && avgHeightOptional.isEmpty()) {
                avgHeightOptional = OptionalDouble.of(this.maxYAllowed.get());
            *///?}
            }

            //? if >=1.21.1 {
            OptionalDouble avgHeightOptional = landHeights.stream()
                    .filter(height -> height > minYAllowed && height < maxYAllowed)
                    .mapToInt(Integer::intValue).average();

            if (this.yAllowance.isPresent()) {
                if (this.yAllowance.get().maxYAllowed.isPresent() && avgHeightOptional.isEmpty()) {
                    avgHeightOptional = OptionalDouble.of(this.yAllowance.get().maxYAllowed.get());
                }

                if (this.yAllowance.get().minYAllowed.isPresent() && avgHeightOptional.isEmpty()) {
                    avgHeightOptional = OptionalDouble.of(this.yAllowance.get().minYAllowed.get());
                }
            //?} else {
            /*if(this.minYAllowed.isPresent() && avgHeightOptional.isEmpty()) {
                avgHeightOptional = OptionalDouble.of(this.minYAllowed.get());
            *///?}
            }

            if (avgHeightOptional.isPresent()) {
                double avgHeight = avgHeightOptional.getAsDouble();
                if(this.cannotSpawnInLiquid && heightMapToUse != Heightmap.Types.OCEAN_FLOOR_WG && heightMapToUse != Heightmap.Types.OCEAN_FLOOR) {
                    avgHeight = Math.max(avgHeight, context.chunkGenerator().getSeaLevel());
                    //? if >=1.21.1 {
                    if(this.yAllowance.isPresent() && this.yAllowance.get().maxYAllowed.isPresent()) {
                        avgHeight = Math.max(avgHeight, this.yAllowance.get().maxYAllowed.get());
                    //?} else {
                    /*if(this.maxYAllowed.isPresent()) {
                        avgHeight = Math.max(avgHeight, this.maxYAllowed.get());
                    *///?}
                    }
                }

                int parentHeight = pieces.get(0).getBoundingBox().minY();
                int offsetAmount = (((int)avgHeight) - parentHeight) + offsetY;
                pieces.forEach(child -> child.move(0, offsetAmount, 0));
            }
            else {
                pieces.clear();
            }
        }
        else if(this.buryingType.get() == BURYING_TYPE.LOWEST_SIDE) {
            Heightmap.Types heightMapToUse = this.projectStartToHeightmap.orElse(Heightmap.Types.WORLD_SURFACE_WG);

            BoundingBox box = pieces.get(0).getBoundingBox();
            BlockPos centerPos = box.getCenter();
            int highestLandPos = Integer.MAX_VALUE;
            //? if >=1.21.1 {
            Optional<Integer> minYAllowed = Optional.empty();
            if (this.yAllowance.isPresent()) {
                minYAllowed = this.yAllowance.get().minYAllowed;
            }
            //?}
            highestLandPos = terrainHeight(context, heightMapToUse, box.minX(), centerPos.getZ(), minYAllowed, highestLandPos);
            highestLandPos = terrainHeight(context, heightMapToUse, centerPos.getX(), box.maxZ(), minYAllowed, highestLandPos);
            highestLandPos = terrainHeight(context, heightMapToUse, centerPos.getX(), box.minZ(), minYAllowed, highestLandPos);
            highestLandPos = terrainHeight(context, heightMapToUse, box.maxX(), centerPos.getZ(), minYAllowed, highestLandPos);
            if (minYAllowed.isPresent() && highestLandPos == Integer.MAX_VALUE) {
                highestLandPos = minYAllowed.get();
            }

            if(!this.cannotSpawnInLiquid && (heightMapToUse == Heightmap.Types.OCEAN_FLOOR_WG || heightMapToUse == Heightmap.Types.OCEAN_FLOOR)) {
                int maxHeightForSubmerging = context.chunkGenerator().getSeaLevel() - box.getYSpan();
                highestLandPos = Math.min(highestLandPos, maxHeightForSubmerging);
            }
            else {
                highestLandPos = Math.max(highestLandPos, context.chunkGenerator().getSeaLevel());
            }

            offsetToNewHeight(context, offsetY, pieces, box, highestLandPos);
        }
    }

    private int terrainHeight(GenerationContext context, Heightmap.Types heightMapToUse, int x, int z, Optional<Integer> minYAllowed, int highestLandPos) {
        int landPos = context.chunkGenerator().getFirstOccupiedHeight(x, z, heightMapToUse, context.heightAccessor(), context.randomState());
        if (minYAllowed.isPresent()) {
            if(landPos >= minYAllowed.get()) {
                highestLandPos = landPos;
            }
        }
        else {
            highestLandPos = Math.min(highestLandPos, landPos);
        }
        return highestLandPos;
    }

    private void offsetToNewHeight(GenerationContext context, int offsetY, List<PoolElementStructurePiece> pieces, BoundingBox box, int highestLandPos) {
        //? if >=1.21.1 {
        if (this.yAllowance.isPresent()) {
            if(this.yAllowance.get().maxYAllowed.isPresent() && this.yAllowance.get().minYAllowed.isPresent()
                    && (box.maxY() + offsetY) < this.yAllowance.get().minYAllowed.get()) {
                highestLandPos = Math.max(highestLandPos, this.yAllowance.get().maxYAllowed.get());
            }
        //?} else {
        /*if(this.maxYAllowed.isPresent() && this.minYAllowed.isPresent()
                && (box.maxY() + offsetY) < this.minYAllowed.get()) {
            highestLandPos = Math.max(highestLandPos, this.maxYAllowed.get());
        }
        *///?}

            //? if >=1.21.1 {
            if(this.yAllowance.get().minYAllowed.isPresent() && (box.minY() + offsetY) < this.yAllowance.get().minYAllowed.get()) {
                highestLandPos = Math.min(highestLandPos, this.yAllowance.get().minYAllowed.get());
            }
            //?} else {
        /*if(this.minYAllowed.isPresent() && (box.minY() + offsetY) < this.minYAllowed.get()) {
            highestLandPos = Math.min(highestLandPos, this.minYAllowed.get());
            *///?}
        }

        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        //? if <26.1.2 {
        random.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
        //?} else {
        /*random.setLargeFeatureSeed(context.seed(), context.chunkPos().x(), context.chunkPos().z());
        *///?}
        int heightDiff = highestLandPos - box.minY();
        for(StructurePiece structurePiece : pieces) {
            structurePiece.move(0, heightDiff + offsetY, 0);
        }
    }

    @Override
    public StructureType<?> type() {
        return MoogsStructuresStructures.GENERIC_JIGSAW_STRUCTURE.get();
    }

    public enum BURYING_TYPE implements StringRepresentable {
        LOWEST_CORNER("LOWEST_CORNER"),
        AVERAGE_LAND("AVERAGE_LAND"),
        LOWEST_SIDE("LOWEST_SIDE");

        private final String name;

        BURYING_TYPE(String name) {
            this.name = name;
        }

        private static final Map<String, BURYING_TYPE> BY_NAME = Util.make(Maps.newHashMap(), (hashMap) -> {
            BURYING_TYPE[] var1 = values();
            for (BURYING_TYPE type : var1) {
                hashMap.put(type.name, type);
            }
        });

        public static BURYING_TYPE byName(String name) {
            return BY_NAME.get(name.toUpperCase(Locale.ROOT));
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}