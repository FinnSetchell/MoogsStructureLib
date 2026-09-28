package com.finndog.moogs_structures.world.structures.pieces;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.misc.structurepiececounter.StructurePieceCountsManager;
import com.finndog.moogs_structures.mixins.structures.SinglePoolElementAccessor;
import com.finndog.moogs_structures.mixins.structures.StructurePoolAccessor;
import com.finndog.moogs_structures.utils.BoxOctree;
import com.finndog.moogs_structures.utils.GeneralUtils;
import com.finndog.moogs_structures.world.structures.GenericJigsawStructure;
import com.google.common.collect.Queues;
import com.mojang.datafixers.util.Pair;
//? if <1.21.5 {
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
//?} else {
/*import net.minecraft.core.*;
*///?}
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Pools;
//? if >=1.21.5 {
/*import net.minecraft.resources.ResourceKey;
*///?}
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.mutable.MutableObject;

//? if <1.21.5 {
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
//?} else {
/*import java.util.*;
*///?}
import java.util.function.BiConsumer;

/**
 * Some changes were done to make it more usable by multiple structures.
 * Source: https://github.com/yungnickyoung/YUNGs-Better-Strongholds/blob/fabric-1.16/src/main/java/com/yungnickyoung/minecraft/betterstrongholds/world/jigsaw/JigsawManager.java
 */
public class PieceLimitedJigsawManager {

    // Record for entries
    public record Entry(PoolElementStructurePiece piece, MutableObject<BoxOctree> boxOctreeMutableObject, int topYLimit, int depth) { }

    public static Optional<Structure.GenerationStub> assembleJigsawStructure(
            Structure.GenerationContext context,
            Holder<StructureTemplatePool> startPoolHolder,
            int size,
            ResourceLocation structureID,
            BlockPos startPos,
            boolean doBoundaryAdjustments,
            Optional<Heightmap.Types> heightmapType,
            int maxY,
            int minY,
            Set<ResourceLocation> poolsThatIgnoreBounds,
            Optional<Integer> maxDistanceFromCenter,
            Optional<GenericJigsawStructure.BURYING_TYPE> buryingType,
            LiquidSettings liquidSettings,
            BiConsumer<StructurePiecesBuilder, List<PoolElementStructurePiece>> structureBoundsAdjuster
    ) {
        // Get jigsaw pool registry
        //? if <1.21.2 {
        Registry<StructureTemplatePool> jigsawPoolRegistry = context.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
        //?}
        //? if >=1.21.2 <1.21.5 {
        /*Registry<StructureTemplatePool> jigsawPoolRegistry = context.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
        *///?}
        // 1.21.5: resolve pools through HolderGetter (reload-scoped)
        //? if >=1.21.5 {
        /*HolderGetter<StructureTemplatePool> poolLookup =
                context.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
        *///?}

        // Get a random orientation for the starting piece
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        //? if <26.1.2 {
        random.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
        //?} else {
        /*random.setLargeFeatureSeed(context.seed(), context.chunkPos().x(), context.chunkPos().z());
        *///?}
        Rotation rotation = Rotation.getRandom(random);

        // Get starting pool
        StructureTemplatePool startPool = startPoolHolder.value();
        //? if <1.21.5 {
        if(startPool.size() == 0) {
            MoogsStructuresCommon.LOGGER.warn("Moog's Structure Lib: Empty or nonexistent start pool in structure: {}  Crash is imminent", structureID);
            throw new RuntimeException("Moog's Structure Lib: Empty or nonexistent start pool in structure: " + structureID + " Crash is imminent");
        //?} else {
        /*if (startPool.size() == 0) {
        *///?}
            //? if >=1.21.5 <1.21.11 {
            /*ResourceLocation startKey = startPoolHolder.unwrapKey().map(ResourceKey::location).orElse(structureID);
            *///?}
            //? if >=1.21.11 {
            /*ResourceLocation startKey = startPoolHolder.unwrapKey().map(ResourceKey::identifier).orElse(structureID);
            *///?}
            //? if >=1.21.5 {
            /*MoogsStructuresCommon.LOGGER.warn("Moog's Structure Lib: Empty or nonexistent start pool in structure: {}  Crash is imminent", startKey);
            throw new RuntimeException("Moog's Structure Lib: Empty or nonexistent start pool in structure: " + startKey + " Crash is imminent");
            *///?}
        }

        // Grab a random starting piece from the start pool. This is just the piece design itself, without rotation or position information.
        // Think of it as a blueprint.
        StructurePoolElement startPieceBlueprint = startPool.getRandomTemplate(random);
        if (startPieceBlueprint == EmptyPoolElement.INSTANCE) {
            return Optional.empty();
        }

        // Instantiate a piece using the "blueprint" we just got.
        PoolElementStructurePiece startPiece = new PoolElementStructurePiece(
                context.structureTemplateManager(),
                startPieceBlueprint,
                startPos,
                startPieceBlueprint.getGroundLevelDelta(),
                rotation,
                startPieceBlueprint.getBoundingBox(context.structureTemplateManager(), startPos, rotation),
                liquidSettings
        );

        // Store center position of starting piece's bounding box
        BoundingBox pieceBoundingBox = startPiece.getBoundingBox();
        int pieceCenterX = (pieceBoundingBox.maxX() + pieceBoundingBox.minX()) / 2;
        int pieceCenterZ = (pieceBoundingBox.maxZ() + pieceBoundingBox.minZ()) / 2;
        int pieceCenterY = heightmapType
                //? if <26.1.2 {
                .map(types -> startPos.getY() + GeneralUtils.getCachedFreeHeight(context.chunkGenerator(), pieceCenterX, pieceCenterZ, types, context.heightAccessor(), context.randomState()))
                //?} else {
                /*.map(types -> startPos.getY() + GeneralUtils.getCachedFreeHeight(
                        context.chunkGenerator(), pieceCenterX, pieceCenterZ, types, context.heightAccessor(), context.randomState()))
                *///?}
                .orElseGet(startPos::getY);

        if (heightmapType.isPresent() && (pieceCenterY > maxY || pieceCenterY < minY)) {
            return Optional.empty();
        }

        int yAdjustment = pieceBoundingBox.minY() + startPiece.getGroundLevelDelta();
        startPiece.move(0, pieceCenterY - yAdjustment, 0);
        //? if <1.21.5 {
        if (!context.validBiome().test(context.chunkGenerator().getBiomeSource().getNoiseBiome(QuartPos.fromBlock(pieceCenterX), QuartPos.fromBlock(pieceCenterY), QuartPos.fromBlock(pieceCenterZ), context.randomState().sampler()))) {
        //?}
        //? if >=1.21.5 <26.3 {
        /*if (!context.validBiome().test(context.chunkGenerator().getBiomeSource().getNoiseBiome(
        *///?}
        //? if >=26.3 {
        /*if (!context.validBiome().test(context.biomeResolver().getNoiseBiome(
        *///?}
                //? if >=1.21.5 {
                /*QuartPos.fromBlock(pieceCenterX),
                QuartPos.fromBlock(pieceCenterY),
                *///?}
                //? if >=1.21.5 <26.3 {
                /*QuartPos.fromBlock(pieceCenterZ),
                context.randomState().sampler()))) {
                *///?}
                //? if >=26.3 {
                /*QuartPos.fromBlock(pieceCenterZ)))) {
                *///?}
            return Optional.empty();
        }

        //? if <1.21.5 {
        return Optional.of(new Structure.GenerationStub(new BlockPos(pieceCenterX, pieceCenterY, pieceCenterZ), (structurePiecesBuilder) -> {
            List<PoolElementStructurePiece> components = new ArrayList<>();
            components.add(startPiece);
            Map<ResourceLocation, StructurePieceCountsManager.RequiredPieceNeeds> requiredPieces = StructurePieceCountsManager.STRUCTURE_PIECE_COUNTS_MANAGER.getRequirePieces(structureID);
            boolean runOnce = requiredPieces == null || requiredPieces.isEmpty();
            Map<ResourceLocation, Integer> currentPieceCounter = new HashMap<>();
            for (int attempts = 0; runOnce || doesNotHaveAllRequiredPieces(components, requiredPieces, currentPieceCounter); attempts++) {
        //?}
                //? if <1.21.4 {
                if (attempts == 40) {
                //?}
                //? if >=1.21.4 <1.21.5 {
                /*if (attempts == 100) {
                *///?}
                    //? if <1.21.5 {
                    MoogsStructuresCommon.LOGGER.error(
                            """
                                            
                                    -------------------------------------------------------------------
                                    Moog's Structure Lib: Failed to create valid structure with all required pieces starting from this pool file: {}. Required pieces failed to generate the required amount are: {}
                                      This can happen if a structure has a required piece but the structure size is set too low.
                                      However, this is most likely caused by a structure unable to spawn properly due to hitting the world's min y or max y build thresholds or a broken MoogsStructures datapack.
                                      Try teleporting to: {} and see if the structure generated fine with the required structure piece or if it is indeed missing it.
                                      Please report the issue to Moog's Structure Lib's dev with latest.log file if the structure is not cut off by world min/max y build thresholds.
                                            
                                    """,
                            jigsawPoolRegistry.getKey(startPool), Arrays.toString(currentPieceCounter.entrySet().stream().filter(entry -> entry.getValue() > 0).toArray()), new BlockPos(pieceCenterX, pieceCenterY, pieceCenterZ));
                    break;
                }
                    //?} else {
        /*return Optional.of(new Structure.GenerationStub(
                new BlockPos(pieceCenterX, pieceCenterY, pieceCenterZ),
                (structurePiecesBuilder) -> {
                    List<PoolElementStructurePiece> components = new ArrayList<>();
                    components.add(startPiece);
                    *///?}

                //reroll start piece
                //? if <1.21.5 {
                PoolElementStructurePiece startPieceToUse = startPiece;
                if (attempts > 0) {
                    StructurePoolElement startPieceBlueprintNew = startPool.getRandomTemplate(random);
                    startPieceToUse = new PoolElementStructurePiece(
                            context.structureTemplateManager(),
                            startPieceBlueprintNew,
                            startPiece.getPosition(),
                            startPieceBlueprintNew.getGroundLevelDelta(),
                            startPiece.getRotation(),
                            startPieceBlueprintNew.getBoundingBox(context.structureTemplateManager(), startPiece.getPosition(), startPiece.getRotation()),
                            liquidSettings
                    );
                }
                //?} else {
                    /*Map<ResourceLocation, StructurePieceCountsManager.RequiredPieceNeeds> requiredPieces =
                            StructurePieceCountsManager.STRUCTURE_PIECE_COUNTS_MANAGER.getRequirePieces(structureID);
                    boolean runOnce = requiredPieces == null || requiredPieces.isEmpty();
                    Map<ResourceLocation, Integer> currentPieceCounter = new HashMap<>();
                *///?}

                //? if <1.21.5 {
                components.clear();
                //?}
                //? if <1.21.4 {
                components.add(startPieceToUse); // Add start piece to list of pieces
                //?}
                //? if >=1.21.4 <1.21.5 {
                /*components.add(startPiece); // Add start piece to list of pieces
                *///?}
                    //? if >=1.21.5 {
                    /*for (int attempts = 0; runOnce || doesNotHaveAllRequiredPieces(components, requiredPieces, currentPieceCounter); attempts++) {
                    *///?}
                        //? if >=1.21.5 <1.21.11 {
                        /*if (attempts == 100) {
                            ResourceLocation startKey = startPoolHolder.unwrapKey().map(ResourceKey::location).orElse(structureID);
                        *///?}
                        //? if >=1.21.11 <26.1.2 {
                        /*if (attempts == 40) {
                        *///?}
                        //? if >=26.1.2 {
                        /*if (attempts == 100) {
                        *///?}
                            //? if >=1.21.11 {
                            /*ResourceLocation startKey = startPoolHolder.unwrapKey().map(ResourceKey::identifier).orElse(structureID);
                            *///?}
                            //? if >=1.21.5 <26.1.2 {
                            /*MoogsStructuresCommon.LOGGER.error("""

                                    -------------------------------------------------------------------
                                    Moog's Structure Lib: Failed to create valid structure with all required pieces starting from this pool file: {}. Required pieces failed to generate the required amount are: {}
                                      This can happen if a structure has a required piece but the structure size is set too low.
                                      However, this is most likely caused by a structure unable to spawn properly due to hitting the world's min y or max y build thresholds or a broken MoogsStructures datapack.
                                      Try teleporting to: {} and see if the structure generated fine with the required structure piece or if it is indeed missing it.
                                      Please report the issue to Moog's Structure Lib's dev with latest.log file if the structure is not cut off by world min/max y build thresholds.
                                    
                                    """,
                            *///?}
                            //? if >=26.1.2 {
                            /*MoogsStructuresCommon.LOGGER.error("""
                                    
                                    -------------------------------------------------------------------
                                    Moog's Structure Lib: Failed to create valid structure with all required pieces starting from this pool file: {}. Required pieces failed to generate the required amount are: {}
                                      This can happen if a structure has a required piece but the structure size is set too low.
                                      However, this is most likely caused by a structure unable to spawn properly due to hitting the world's min y or max y build thresholds or a broken MoogsStructures datapack.
                                      Try teleporting to: {} and see if the structure generated fine with the required structure piece or if it is indeed missing it.
                                      Please report the issue to Moog's Structure Lib's dev with latest.log file if the structure is not cut off by world min/max y build thresholds.
                                    
                                    """,
                            *///?}
                                    //? if >=1.21.5 {
                                    /*startKey,
                                    Arrays.toString(currentPieceCounter.entrySet().stream().filter(e -> e.getValue() > 0).toArray()),
                                    new BlockPos(pieceCenterX, pieceCenterY, pieceCenterZ));
                            break;
                        }
                                    *///?}

                //? if <1.21.5 {
                if (size > 0) {
                    int boxRange = maxDistanceFromCenter.orElse(80);
                    AABB axisAlignedBB = new AABB(pieceCenterX - boxRange, pieceCenterY - 120, pieceCenterZ - boxRange, pieceCenterX + boxRange + 1, pieceCenterY + 180 + 1, pieceCenterZ + boxRange + 1);
                    BoxOctree boxOctree = new BoxOctree(axisAlignedBB); // The maximum boundary of the entire structure
                    boxOctree.addBox(AABB.of(pieceBoundingBox));
                //?}
                    //? if <1.21.4 {
                    Entry startPieceEntry = new Entry(startPieceToUse, new MutableObject<>(boxOctree), pieceCenterY + 80, 0);
                    //?}
                    //? if >=1.21.4 <1.21.5 {
                    /*Entry startPieceEntry = new Entry(startPiece, new MutableObject<>(boxOctree), pieceCenterY + 80, 0);
                    *///?}
                        // Reroll start piece if needed
                        //? if >=1.21.5 {
                        /*PoolElementStructurePiece startPieceToUse = startPiece;
                        if (attempts > 0) {
                            StructurePoolElement startPieceBlueprintNew = startPool.getRandomTemplate(random);
                            startPieceToUse = new PoolElementStructurePiece(
                                    context.structureTemplateManager(),
                                    startPieceBlueprintNew,
                                    startPiece.getPosition(),
                                    startPieceBlueprintNew.getGroundLevelDelta(),
                                    startPiece.getRotation(),
                                    startPieceBlueprintNew.getBoundingBox(context.structureTemplateManager(), startPiece.getPosition(), startPiece.getRotation()),
                                    liquidSettings
                            );
                        }
                        *///?}

                    //? if <1.21.5 {
                    Assembler assembler = new Assembler(
                            structureID,
                            jigsawPoolRegistry,
                            size,
                            context,
                            components,
                            random,
                            requiredPieces,
                            buryingType.isEmpty() ? maxY : Integer.MAX_VALUE,
                            buryingType.isEmpty() ? minY : Integer.MIN_VALUE,
                            poolsThatIgnoreBounds,
                            liquidSettings);
                    assembler.availablePieces.addLast(startPieceEntry);
                    //?} else {
                        /*components.clear();
                    *///?}
                        //? if >=1.21.5 <26.1.2 {
                        /*components.add(startPieceToUse); // Add start piece to list of pieces
                        *///?}
                        //? if >=26.1.2 {
                        /*components.add(startPiece); // start stays
                        *///?}

                    //? if <1.21.5 {
                    while (!assembler.availablePieces.isEmpty()) {
                        Entry entry = assembler.availablePieces.removeFirst();
                        assembler.generatePiece(entry.piece, entry.boxOctreeMutableObject, entry.topYLimit, entry.depth, doBoundaryAdjustments, context.heightAccessor());
                    //?} else {
                        /*if (size > 0) {
                            int boxRange = maxDistanceFromCenter.orElse(80);
                            AABB axisAlignedBB = new AABB(
                                    pieceCenterX - boxRange, pieceCenterY - 120, pieceCenterZ - boxRange,
                                    pieceCenterX + boxRange + 1, pieceCenterY + 180 + 1, pieceCenterZ + boxRange + 1
                            );
                            BoxOctree boxOctree = new BoxOctree(axisAlignedBB);
                            boxOctree.addBox(AABB.of(pieceBoundingBox));
                            Entry startPieceEntry = new Entry(startPieceToUse, new MutableObject<>(boxOctree), pieceCenterY + 80, 0);

                            Assembler assembler = new Assembler(
                                    structureID,
                                    poolLookup, // 1.21.5 holder lookup
                                    size,
                                    context,
                                    components,
                                    random,
                                    requiredPieces,
                                    buryingType.isEmpty() ? maxY : Integer.MAX_VALUE,
                                    buryingType.isEmpty() ? minY : Integer.MIN_VALUE,
                                    poolsThatIgnoreBounds,
                                    liquidSettings
                            );
                            assembler.availablePieces.addLast(startPieceEntry);

                            while (!assembler.availablePieces.isEmpty()) {
                                Entry entry = assembler.availablePieces.removeFirst();
                                assembler.generatePiece(entry.piece, entry.boxOctreeMutableObject, entry.topYLimit, entry.depth, doBoundaryAdjustments, context.heightAccessor());
                            }
                        }

                        if (runOnce) break;
                    }

                    components.forEach(structurePiecesBuilder::addPiece);
                    structureBoundsAdjuster.accept(structurePiecesBuilder, components);

                    // Do not generate if out of bounds
                    if (structurePiecesBuilder.getBoundingBox().maxY() > context.heightAccessor().getMaxY()) {
                        structurePiecesBuilder.clear();
                    *///?}
                    }
                }

                //? if <1.21.5 {
                if (runOnce) break;
            }

            components.forEach(structurePiecesBuilder::addPiece);
            structureBoundsAdjuster.accept(structurePiecesBuilder, components);

            // Do not generate if out of bounds
                //?}
            //? if <1.21.2 {
            if(structurePiecesBuilder.getBoundingBox().maxY() > context.heightAccessor().getMaxBuildHeight()) {
            //?}
            //? if >=1.21.2 <1.21.5 {
            /*if(structurePiecesBuilder.getBoundingBox().maxY() > context.heightAccessor().getMaxY()) {
            *///?}
                //? if <1.21.5 {
                structurePiecesBuilder.clear();
            }
        }));
                //?} else {
        /*));
                *///?}
    }

    //? if <1.21.5 {
    private static boolean doesNotHaveAllRequiredPieces(List<? extends StructurePiece> components, 
    //?} else {
    /*private static boolean doesNotHaveAllRequiredPieces(List<? extends StructurePiece> components,
    *///?}
                                                        Map<ResourceLocation, StructurePieceCountsManager.RequiredPieceNeeds> requiredPieces,
                                                        //? if <1.21.5 {
                                                        Map<ResourceLocation, Integer> counter
    ) {
                                                        //?} else {
                                                        /*Map<ResourceLocation, Integer> counter) {
                                                        *///?}
        counter.clear();
        requiredPieces.forEach((key, value) -> counter.put(key, value.getRequiredAmount()));
        //? if <1.21.5 {
        for(Object piece : components) {
            if(piece instanceof PoolElementStructurePiece) {
                StructurePoolElement poolElement = ((PoolElementStructurePiece)piece).getElement();
                if(poolElement instanceof SinglePoolElement) {
        //?} else {
        /*for (Object piece : components) {
            if (piece instanceof PoolElementStructurePiece p) {
                StructurePoolElement poolElement = p.getElement();
                if (poolElement instanceof SinglePoolElement spe) {
        *///?}
                    ResourceLocation pieceID = ((SinglePoolElementAccessor) poolElement).moogs_structures_getTemplate().left().orElse(null);
                    //? if <1.21.5 {
                    if(counter.containsKey(pieceID)) {
                    //?} else {
                    /*if (pieceID != null && counter.containsKey(pieceID)) {
                    *///?}
                        counter.put(pieceID, counter.get(pieceID) - 1);
                    }
                }
            }
        }

        return counter.values().stream().anyMatch(count -> count > 0);
    }


    public static final class Assembler {
        // 1.21.5: use HolderGetter instead of Registry for pools
        //? if <1.21.5 {
        private final Registry<StructureTemplatePool> poolRegistry;
        //?} else {
        /*private final HolderGetter<StructureTemplatePool> poolLookup;
        *///?}
        private final int maxDepth;
        private final Structure.GenerationContext context;
        private final List<? super PoolElementStructurePiece> structurePieces;
        private final RandomSource random;
        public final Deque<Entry> availablePieces = Queues.newArrayDeque();
        private final Map<ResourceLocation, Integer> currentPieceCounts;
        private final Map<ResourceLocation, Integer> maximumPieceCounts;
        private final Map<ResourceLocation, StructurePieceCountsManager.RequiredPieceNeeds> requiredPieces;
        private final int maxY;
        private final int minY;
        private final Set<ResourceLocation> poolsThatIgnoreBounds;
        private final LiquidSettings liquidSettings;


        public Assembler(ResourceLocation structureID,
                         //? if <1.21.5 {
                         Registry<StructureTemplatePool> poolRegistry,
                         //?} else {
                         /*HolderGetter<StructureTemplatePool> poolLookup,
                         *///?}
                         int maxDepth,
                         Structure.GenerationContext context,
                         List<? super PoolElementStructurePiece> structurePieces,
                         RandomSource random,
                         Map<ResourceLocation, StructurePieceCountsManager.RequiredPieceNeeds> requiredPieces,
                         int maxY,
                         int minY,
                         Set<ResourceLocation> poolsThatIgnoreBounds,
                         //? if <1.21.5 {
                         LiquidSettings liquidSettings
        ) {
            this.poolRegistry = poolRegistry;
                         //?} else {
                         /*LiquidSettings liquidSettings) {
            this.poolLookup = poolLookup;
                         *///?}
            this.maxDepth = maxDepth;
            this.context = context;
            this.structurePieces = structurePieces;
            this.random = random;
            this.maxY = maxY;
            this.minY = minY;

            // Create map clone so we do not modify the original map.
            this.requiredPieces = requiredPieces == null ? new HashMap<>() : new HashMap<>(requiredPieces);
            this.maximumPieceCounts = new HashMap<>(StructurePieceCountsManager.STRUCTURE_PIECE_COUNTS_MANAGER.getMaximumCountForPieces(structureID));
            this.poolsThatIgnoreBounds = poolsThatIgnoreBounds;
            this.liquidSettings = liquidSettings;

            // pieceCounts will keep track of how many of the pieces we are checking were spawned
            this.currentPieceCounts = new HashMap<>();
            this.requiredPieces.forEach((key, value) -> this.currentPieceCounts.putIfAbsent(key, 0));
            this.maximumPieceCounts.forEach((key, value) -> this.currentPieceCounts.putIfAbsent(key, 0));
        }

        //? if <1.21.5 {
        public void generatePiece(PoolElementStructurePiece piece, 
                                  MutableObject<BoxOctree> boxOctree, 
                                  int minY, 
        //?} else {
        /*public void generatePiece(PoolElementStructurePiece piece,
                                  MutableObject<BoxOctree> boxOctree,
                                  int minY,
        *///?}
                                  int depth,
                                  boolean doBoundaryAdjustments,
                                  //? if <1.21.5 {
                                  LevelHeightAccessor heightLimitView
        ) {
                                  //?}
            // Collect data from params regarding piece to process
                                  //? if >=1.21.5 {
                                  /*LevelHeightAccessor heightLimitView) {

                                  *///?}
            StructurePoolElement pieceBlueprint = piece.getElement();
            BlockPos piecePos = piece.getPosition();
            Rotation pieceRotation = piece.getRotation();
            BoundingBox pieceBoundingBox = piece.getBoundingBox();
            int pieceMinY = pieceBoundingBox.minY();
            MutableObject<BoxOctree> parentOctree = new MutableObject<>();

            // Get list of all jigsaw blocks in this piece
            //? if <1.21.2 {
            List<StructureTemplate.StructureBlockInfo> pieceJigsawBlocks = pieceBlueprint.getShuffledJigsawBlocks(context.structureTemplateManager(), piecePos, pieceRotation, this.random);
            //?}
            //? if >=1.21.2 <1.21.5 {
            /*List<StructureTemplate.JigsawBlockInfo> pieceJigsawBlocks = pieceBlueprint.getShuffledJigsawBlocks(context.structureTemplateManager(), piecePos, pieceRotation, this.random);
            *///?}
            //? if >=1.21.5 {
            /*List<StructureTemplate.JigsawBlockInfo> pieceJigsawBlocks =
                    pieceBlueprint.getShuffledJigsawBlocks(context.structureTemplateManager(), piecePos, pieceRotation, this.random);
            *///?}

            //? if <1.21.2 {
            for (StructureTemplate.StructureBlockInfo jigsawBlock : pieceJigsawBlocks) {
            //?} else {
            /*for (StructureTemplate.JigsawBlockInfo jigsawBlock : pieceJigsawBlocks) {
            *///?}
                // Gather jigsaw block information
                //? if <1.21.2 {
                Direction direction = JigsawBlock.getFrontFacing(jigsawBlock.state());
                BlockPos jigsawBlockPos = jigsawBlock.pos();
                //?}
                //? if >=1.21.2 <26.3 {
                /*Direction direction = JigsawBlock.getFrontFacing(jigsawBlock.info().state());
                BlockPos jigsawBlockPos = jigsawBlock.info().pos();
                *///?}
                //? if >=26.3 {
                /*Direction direction = JigsawBlock.getFrontFacing(jigsawBlock.state());
                BlockPos jigsawBlockPos = jigsawBlock.pos();
                *///?}
                BlockPos jigsawBlockTargetPos = jigsawBlockPos.relative(direction);

                // Get the jigsaw block's piece pool
                //? if <1.21.2 {
                ResourceLocation jigsawBlockPool = ResourceLocation.tryParse(jigsawBlock.nbt().getString("pool"));
                //?}
                //? if >=1.21.2 <1.21.5 {
                /*ResourceLocation jigsawBlockPool = ResourceLocation.tryParse(jigsawBlock.info().nbt().getString("pool"));
                *///?}
                //? if <1.21.5 {
                Optional<StructureTemplatePool> poolOptional = this.poolRegistry.getOptional(jigsawBlockPool);
                //?}
                // Resolve pool from NBT via HolderGetter
                //? if >=1.21.5 <1.21.11 {
                /*String poolStr = jigsawBlock.info().nbt().getString("pool").orElse("");
                *///?}
                //? if >=1.21.11 <26.3 {
                /*String poolStr = jigsawBlock.info().nbt().getString("pool").get();
                *///?}
                //? if >=26.3 {
                /*String poolStr = jigsawBlock.pool().identifier().toString();
                *///?}
                //? if >=1.21.5 {
                /*ResourceLocation poolId = ResourceLocation.tryParse(poolStr);
                if (poolId == null) {
                    MoogsStructuresCommon.LOGGER.warn("Invalid pool id in jigsaw NBT: '{}'", poolStr);
                    continue;
                }
                ResourceKey<StructureTemplatePool> poolKey =
                        ResourceKey.create(Registries.TEMPLATE_POOL, poolId);

                Optional<Holder.Reference<StructureTemplatePool>> optPoolRef = this.poolLookup.get(poolKey);
                if (optPoolRef.isEmpty()) {
                    MoogsStructuresCommon.LOGGER.warn(
                            "Moog's Structure Lib: Missing pool {} called from {}",
                            poolId,
                            (pieceBlueprint instanceof SinglePoolElement spe)
                                    ? ((SinglePoolElementAccessor) pieceBlueprint).moogs_structures_getTemplate().left().orElse(null)
                                    : "not SinglePoolElement");
                    continue;
                }
                *///?}

                // Only continue if we are using the jigsaw pattern registry and if it is not empty
                //? if <1.21.5 {
                if (!(poolOptional.isPresent() && (poolOptional.get().size() != 0 || Objects.equals(jigsawBlockPool, Pools.EMPTY.location())))) {
                    MoogsStructuresCommon.LOGGER.warn("Moog's Structure Lib: Empty or nonexistent pool: {} which is being called from {}", jigsawBlockPool, pieceBlueprint instanceof SinglePoolElement ? ((SinglePoolElementAccessor) pieceBlueprint).moogs_structures_getTemplate().left().get() : "not a SinglePoolElement class");
                //?} else {
                /*StructureTemplatePool pool = optPoolRef.get().value();

                boolean isEmptyPool = pool.size() == 0;
                *///?}
                //? if >=1.21.5 <1.21.11 {
                /*boolean isExplicitEmpty = poolKey.location().equals(Pools.EMPTY.location());
                *///?}
                //? if >=1.21.11 {
                /*boolean isExplicitEmpty = poolKey.identifier().equals(Pools.EMPTY.identifier());
                *///?}
                //? if >=1.21.5 {
                /*if (isEmptyPool && !isExplicitEmpty) {
                    MoogsStructuresCommon.LOGGER.warn(
                            "Moog's Structure Lib: Empty pool {} called from {}",
                            poolId,
                            (pieceBlueprint instanceof SinglePoolElement spe)
                                    ? ((SinglePoolElementAccessor) pieceBlueprint).moogs_structures_getTemplate().left().orElse(null)
                                    : "not SinglePoolElement");
                *///?}
                    continue;
                }

                // Get the jigsaw block's fallback pool (which is a part of the pool's JSON)
                // Fallback is a Holder now
                //? if <1.21.5 {
                Holder<StructureTemplatePool> jigsawBlockFallback = poolOptional.get().getFallback();
                //?} else {
                /*Holder<StructureTemplatePool> jigsawBlockFallback = pool.getFallback();
                *///?}

                // Adjustments for if the target block position is inside the current piece
                boolean isTargetInsideCurrentPiece = pieceBoundingBox.isInside(jigsawBlockTargetPos);
                int targetPieceBoundsTop;
                MutableObject<BoxOctree> octreeToUse;
                if (isTargetInsideCurrentPiece) {
                    octreeToUse = parentOctree;
                    targetPieceBoundsTop = pieceMinY;
                    if (parentOctree.getValue() == null) {
                        parentOctree.setValue(new BoxOctree(AABB.of(pieceBoundingBox)));
                    }
                //? if <1.21.5 {
                }
                else {
                //?} else {
                /*} else {
                *///?}
                    octreeToUse = boxOctree;
                    targetPieceBoundsTop = minY;
                }

                // Process the pool pieces, randomly choosing different pieces from the pool to spawn
                if (depth != this.maxDepth) {
                    //? if <1.21.5 {
                    StructurePoolElement generatedPiece = this.processList(new ArrayList<>(((StructurePoolAccessor)poolOptional.get()).moogs_structures_getRawTemplates()), doBoundaryAdjustments, jigsawBlock, jigsawBlockTargetPos, pieceMinY, jigsawBlockPos, octreeToUse, piece, depth, targetPieceBoundsTop, heightLimitView, false);
                    if (generatedPiece != null) continue; // Stop here since we've already generated the piece
                    //?} else {
                    /*List<Pair<StructurePoolElement, Integer>> elements =
                            new ArrayList<>(((StructurePoolAccessor) pool).moogs_structures_getRawTemplates());

                    StructurePoolElement generatedPiece =
                            this.processList(
                                    elements,
                                    doBoundaryAdjustments,
                                    jigsawBlock,
                                    jigsawBlockTargetPos,
                                    pieceMinY,
                                    jigsawBlockPos,
                                    octreeToUse,
                                    piece,
                                    depth,
                                    targetPieceBoundsTop,
                                    heightLimitView,
                                    false
                            );
                    if (generatedPiece != null) continue;
                    *///?}
                }

                // Process the fallback pieces in the event none of the pool pieces work
                boolean ignoreBounds = false;
                //? if <1.21.5 {
                if(poolsThatIgnoreBounds != null) {
                    ResourceLocation fallBackPoolRL = poolRegistry.getKey(jigsawBlockFallback.value());
                    ignoreBounds = poolsThatIgnoreBounds.contains(fallBackPoolRL);
                //?} else {
                /*if (poolsThatIgnoreBounds != null) {
                    ResourceLocation fallbackPoolId = jigsawBlockFallback.unwrapKey()
                *///?}
                            //? if >=1.21.5 <1.21.11 {
                            /*.map(ResourceKey::location)
                            *///?}
                            //? if >=1.21.11 {
                            /*.map(ResourceKey::identifier)
                            *///?}
                            //? if >=1.21.5 {
                            /*.orElse(null);
                    if (fallbackPoolId != null) {
                        ignoreBounds = poolsThatIgnoreBounds.contains(fallbackPoolId);
                    }
                            *///?}
                }

                //? if <1.21.5 {
                this.processList(new ArrayList<>(((StructurePoolAccessor)jigsawBlockFallback.value()).moogs_structures_getRawTemplates()), doBoundaryAdjustments, jigsawBlock, jigsawBlockTargetPos, pieceMinY, jigsawBlockPos, octreeToUse, piece, depth, targetPieceBoundsTop, heightLimitView, ignoreBounds);
                //?} else {
                /*StructureTemplatePool fallbackPool = jigsawBlockFallback.value();
                List<Pair<StructurePoolElement, Integer>> fallbackElements =
                        new ArrayList<>(((StructurePoolAccessor) fallbackPool).moogs_structures_getRawTemplates());

                this.processList(
                        fallbackElements,
                        doBoundaryAdjustments,
                        jigsawBlock,
                        jigsawBlockTargetPos,
                        pieceMinY,
                        jigsawBlockPos,
                        octreeToUse,
                        piece,
                        depth,
                        targetPieceBoundsTop,
                        heightLimitView,
                        ignoreBounds
                );
                *///?}
            }
        }

        /**
         * Helper function. Searches candidatePieces for a suitable piece to spawn.
         * All other params are intended to be passed directly from {@link Assembler#generatePiece}
         * @return The piece genereated, or null if no suitable pieces were found.
         */
        private StructurePoolElement processList(
                List<Pair<StructurePoolElement, Integer>> candidatePieces,
                boolean doBoundaryAdjustments,
                //? if <1.21.2 {
                StructureTemplate.StructureBlockInfo jigsawBlock,
                //?} else {
                /*StructureTemplate.JigsawBlockInfo jigsawBlock,
                *///?}
                BlockPos jigsawBlockTargetPos,
                int pieceMinY,
                BlockPos jigsawBlockPos,
                MutableObject<BoxOctree> boxOctreeMutableObject,
                PoolElementStructurePiece piece,
                int depth,
                int targetPieceBoundsTop,
                LevelHeightAccessor heightLimitView,
                boolean ignoreBounds
        ) {
            StructureTemplatePool.Projection piecePlacementBehavior = piece.getElement().getProjection();
            boolean isPieceRigid = piecePlacementBehavior == StructureTemplatePool.Projection.RIGID;
            boolean isPieceOceanFloor = piece.getElement() instanceof LegacyOceanBottomSinglePoolElement;
            int jigsawBlockRelativeY = jigsawBlockPos.getY() - pieceMinY;
            int surfaceHeight = -1; // The y-coordinate of the surface. Only used if isPieceRigid is false.

            int totalCount = candidatePieces.stream().mapToInt(Pair::getSecond).reduce(0, Integer::sum);

            while (candidatePieces.size() > 0) {
                // Prioritize required piece if the following conditions are met:
                // 1. It's a potential candidate for this pool
                // 2. It hasn't already been placed
                // 3. We are at least certain amount of pieces away from the starting piece.
                Pair<StructurePoolElement, Integer> chosenPiecePair = null;
                // Condition 2
                Optional<ResourceLocation> pieceNeededToSpawn = this.requiredPieces.keySet().stream().filter(key -> {
                    int currentCount = this.currentPieceCounts.get(key);
                    //? if <1.21.5 {
                    StructurePieceCountsManager.RequiredPieceNeeds requiredPieceNeeds = this.requiredPieces.get(key);
                    int requireCount = requiredPieceNeeds == null ? 0 : requiredPieceNeeds.getRequiredAmount();
                    //?} else {
                    /*StructurePieceCountsManager.RequiredPieceNeeds needs = this.requiredPieces.get(key);
                    int requireCount = needs == null ? 0 : needs.getRequiredAmount();
                    *///?}
                    return currentCount < requireCount;
                }).findFirst();

                if (pieceNeededToSpawn.isPresent()) {
                    for (int i = 0; i < candidatePieces.size(); i++) {
                        Pair<StructurePoolElement, Integer> candidatePiecePair = candidatePieces.get(i);
                        StructurePoolElement candidatePiece = candidatePiecePair.getFirst();
                        //? if <1.21.5 {
                        if (candidatePiece instanceof SinglePoolElement && ((SinglePoolElementAccessor) candidatePiece).moogs_structures_getTemplate().left().get().equals(pieceNeededToSpawn.get())) { // Condition 1
                            if (depth >= Math.min(maxDepth - 1, this.requiredPieces.get(pieceNeededToSpawn.get()).getMinDistanceFromCenter())) { // Condition 3
                                // All conditions are met. Use required piece  as chosen piece.
                        //?} else {
                        /*if (candidatePiece instanceof SinglePoolElement
                                && ((SinglePoolElementAccessor) candidatePiece).moogs_structures_getTemplate().left().orElse(null) != null
                                && ((SinglePoolElementAccessor) candidatePiece).moogs_structures_getTemplate().left().get().equals(pieceNeededToSpawn.get())) {
                            if (depth >= Math.min(maxDepth - 1, this.requiredPieces.get(pieceNeededToSpawn.get()).getMinDistanceFromCenter())) {
                        *///?}
                                chosenPiecePair = candidatePiecePair;
                            //? if <1.21.5 {
                            }
                            else {
                            //?}
                                // If not far enough from starting room, remove the required piece from the list
                            //? if >=1.21.5 {
                            /*} else {
                            *///?}
                                totalCount -= candidatePiecePair.getSecond();
                                candidatePieces.remove(candidatePiecePair);
                            }
                            break;
                        }
                    }
                }

                // Choose piece if required piece wasn't selected
                if (chosenPiecePair == null) {
                    int chosenWeight = random.nextInt(totalCount) + 1;

                    for (Pair<StructurePoolElement, Integer> candidate : candidatePieces) {
                        chosenWeight -= candidate.getSecond();
                        if (chosenWeight <= 0) {
                            chosenPiecePair = candidate;
                            break;
                        }
                    }
                }

                StructurePoolElement candidatePiece = chosenPiecePair.getFirst();

                // Vanilla check. Not sure on the implications of this.
                if (candidatePiece == EmptyPoolElement.INSTANCE) {
                    return null;
                }

                // Before performing any logic, check to ensure we haven't reached the max number of instances of this piece.
                // This logic is my own additional logic - vanilla does not offer this behavior.
                ResourceLocation pieceName = null;
                //? if <1.21.5 {
                if(candidatePiece instanceof SinglePoolElement) {
                    pieceName = ((SinglePoolElementAccessor) candidatePiece).moogs_structures_getTemplate().left().get();
                    if (this.currentPieceCounts.containsKey(pieceName) && this.maximumPieceCounts.containsKey(pieceName)) {
                        if (this.currentPieceCounts.get(pieceName) >= this.maximumPieceCounts.get(pieceName)) {
                            // Remove this piece from the list of candidates and retry.
                            totalCount -= chosenPiecePair.getSecond();
                            candidatePieces.remove(chosenPiecePair);
                            continue;
                        }
                //?} else {
                /*if (candidatePiece instanceof SinglePoolElement) {
                    pieceName = ((SinglePoolElementAccessor) candidatePiece).moogs_structures_getTemplate().left().orElse(null);
                    if (pieceName != null
                            && this.currentPieceCounts.containsKey(pieceName)
                            && this.maximumPieceCounts.containsKey(pieceName)
                            && this.currentPieceCounts.get(pieceName) >= this.maximumPieceCounts.get(pieceName)) {
                        totalCount -= chosenPiecePair.getSecond();
                        candidatePieces.remove(chosenPiecePair);
                        continue;
                *///?}
                    }
                }

                // Try different rotations to see which sides of the piece are fit to be the receiving end
                for (Rotation rotation : Rotation.getShuffled(this.random)) {
                    //? if <1.21.2 {
                    List<StructureTemplate.StructureBlockInfo> candidateJigsawBlocks = candidatePiece.getShuffledJigsawBlocks(context.structureTemplateManager(), BlockPos.ZERO, rotation, this.random);
                    //?}
                    //? if >=1.21.2 <1.21.5 {
                    /*List<StructureTemplate.JigsawBlockInfo> candidateJigsawBlocks = candidatePiece.getShuffledJigsawBlocks(context.structureTemplateManager(), BlockPos.ZERO, rotation, this.random);
                    *///?}
                    //? if <1.21.5 {
                    BoundingBox tempCandidateBoundingBox = candidatePiece.getBoundingBox(context.structureTemplateManager(), BlockPos.ZERO, rotation);
                    //?} else {
                    /*List<StructureTemplate.JigsawBlockInfo> candidateJigsawBlocks =
                            candidatePiece.getShuffledJigsawBlocks(context.structureTemplateManager(), BlockPos.ZERO, rotation, this.random);
                    BoundingBox tempCandidateBoundingBox =
                            candidatePiece.getBoundingBox(context.structureTemplateManager(), BlockPos.ZERO, rotation);
                    *///?}

                    // Some sort of logic for setting the candidateHeightAdjustments var if doBoundaryAdjustments.
                    int candidateHeightAdjustments;
                    if (doBoundaryAdjustments && tempCandidateBoundingBox.getYSpan() <= 16) {
                        candidateHeightAdjustments = candidateJigsawBlocks.stream().mapToInt((pieceCandidateJigsawBlock) -> {
                            //? if <1.21.2 {
                            if (!tempCandidateBoundingBox.isInside(pieceCandidateJigsawBlock.pos().relative(JigsawBlock.getFrontFacing(pieceCandidateJigsawBlock.state())))) {
                            //?}
                            //? if >=1.21.2 <26.3 {
                            /*if (!tempCandidateBoundingBox.isInside(pieceCandidateJigsawBlock.info().pos().relative(JigsawBlock.getFrontFacing(pieceCandidateJigsawBlock.info().state())))) {
                            *///?}
                            //? if >=26.3 {
                            /*if (!tempCandidateBoundingBox.isInside(pieceCandidateJigsawBlock.pos().relative(JigsawBlock.getFrontFacing(pieceCandidateJigsawBlock.state())))) {
                            *///?}
                                return 0;
                            //? if <1.21.5 {
                            }
                            else {
                            //?}
                                //? if <1.21.2 {
                                ResourceLocation candidateTargetPool = ResourceLocation.tryParse(pieceCandidateJigsawBlock.nbt().getString("pool"));
                                //?}
                                //? if >=1.21.2 <1.21.5 {
                                /*ResourceLocation candidateTargetPool = ResourceLocation.tryParse(pieceCandidateJigsawBlock.info().nbt().getString("pool"));
                                *///?}
                                //? if <1.21.5 {
                                Optional<StructureTemplatePool> candidateTargetPoolOptional = this.poolRegistry.getOptional(candidateTargetPool);
                                if (candidateTargetPoolOptional.isEmpty()) {
                                    MoogsStructuresCommon.LOGGER.warn("Moog's Structure Lib: Non-existent child pool attempted to be spawned: {} which is being called from {}. Let Moog's Structure Lib dev (FinnDog) know about this log entry.", candidateTargetPool, candidatePiece instanceof SinglePoolElement ? ((SinglePoolElementAccessor) candidatePiece).moogs_structures_getTemplate().left().get() : "not a SinglePoolElement class");
                                //?} else {
                            /*} else {
                                *///?}
                                //? if >=1.21.5 <1.21.11 {
                                /*String tgt = pieceCandidateJigsawBlock.info().nbt().getString("pool").orElse("");
                                *///?}
                                //? if >=1.21.11 <26.3 {
                                /*String tgt = pieceCandidateJigsawBlock.info().nbt().getString("pool").get();
                                *///?}
                                //? if >=26.3 {
                                /*String tgt = pieceCandidateJigsawBlock.pool().identifier().toString();
                                *///?}
                                //? if >=1.21.5 {
                                /*ResourceLocation targetPoolId = ResourceLocation.tryParse(tgt);
                                if (targetPoolId == null) return 0;
                                ResourceKey<StructureTemplatePool> targetKey =
                                        ResourceKey.create(Registries.TEMPLATE_POOL, targetPoolId);
                                Optional<Holder.Reference<StructureTemplatePool>> candOpt = this.poolLookup.get(targetKey);
                                if (candOpt.isEmpty()) {
                                    MoogsStructuresCommon.LOGGER.warn(
                                            "Moog's Structure Lib: Non-existent child pool attempted to be spawned: {} which is being called from {}. Let Moog's Structure Lib dev (FinnDog) know about this log entry.",
                                            targetPoolId,
                                            candidatePiece instanceof SinglePoolElement spe
                                                    ? ((SinglePoolElementAccessor) candidatePiece).moogs_structures_getTemplate().left().orElse(null)
                                                    : "not SinglePoolElement");
                                    return 0;
                                *///?}
                                }
                                //? if <1.21.5 {
                                int tallestCandidateTargetFallbackPieceHeight = candidateTargetPoolOptional.map((c) -> c.getFallback().value().getMaxSize(context.structureTemplateManager())).orElse(0);
                                int tallestCandidateTargetPoolPieceHeight = candidateTargetPoolOptional.map((c) -> c.getMaxSize(context.structureTemplateManager())).orElse(0);
                                return Math.max(tallestCandidateTargetPoolPieceHeight, tallestCandidateTargetFallbackPieceHeight);
                                //?} else {
                                /*StructureTemplatePool candPool = candOpt.get().value();
                                int tallestFallback = candPool.getFallback().value().getMaxSize(context.structureTemplateManager());
                                int tallestPool = candPool.getMaxSize(context.structureTemplateManager());
                                return Math.max(tallestPool, tallestFallback);
                                *///?}
                            }
                        }).max().orElse(0);
                    //? if <1.21.5 {
                    } 
                    else {
                    //?} else {
                    /*} else {
                    *///?}
                        candidateHeightAdjustments = 0;
                    }

                    // Check for each of the candidate's jigsaw blocks for a match
                    //? if <1.21.2 {
                    for (StructureTemplate.StructureBlockInfo candidateJigsawBlock : candidateJigsawBlocks) {
                    //?} else {
                    /*for (StructureTemplate.JigsawBlockInfo candidateJigsawBlock : candidateJigsawBlocks) {
                    *///?}
                        //? if <1.21.5 {
                        if (GeneralUtils.canJigsawsAttach(jigsawBlock, candidateJigsawBlock)) {
                        //?}
                        //? if >=1.21.5 <26.3 {
                        /*if (GeneralUtils.canJigsawsAttach(jigsawBlock.info(), candidateJigsawBlock.info())) {
                        *///?}
                            //? if <1.21.2 {
                            BlockPos candidateJigsawBlockPos = candidateJigsawBlock.pos();
                            //?}
                            //? if >=1.21.2 <26.3 {
                            /*BlockPos candidateJigsawBlockPos = candidateJigsawBlock.info().pos();
                            *///?}
                        //? if >=26.3 {
                        /*if (JigsawBlock.canAttach(jigsawBlock, candidateJigsawBlock)) {
                            BlockPos candidateJigsawBlockPos = candidateJigsawBlock.pos();
                        *///?}
                            //? if <1.21.5 {
                            BlockPos candidateJigsawBlockRelativePos = new BlockPos(jigsawBlockTargetPos.getX() - candidateJigsawBlockPos.getX(), jigsawBlockTargetPos.getY() - candidateJigsawBlockPos.getY(), jigsawBlockTargetPos.getZ() - candidateJigsawBlockPos.getZ());
                            //?} else {
                            /*BlockPos candidateJigsawBlockRelativePos = new BlockPos(
                                    jigsawBlockTargetPos.getX() - candidateJigsawBlockPos.getX(),
                                    jigsawBlockTargetPos.getY() - candidateJigsawBlockPos.getY(),
                                    jigsawBlockTargetPos.getZ() - candidateJigsawBlockPos.getZ()
                            );
                            *///?}

                            // Get the bounding box for the piece, offset by the relative position difference
                            //? if <1.21.5 {
                            BoundingBox candidateBoundingBox = candidatePiece.getBoundingBox(context.structureTemplateManager(), candidateJigsawBlockRelativePos, rotation);
                            //?} else {
                            /*BoundingBox candidateBoundingBox =
                                    candidatePiece.getBoundingBox(context.structureTemplateManager(), candidateJigsawBlockRelativePos, rotation);
                            *///?}

                            // Determine if candidate is rigid
                            StructureTemplatePool.Projection candidatePlacementBehavior = candidatePiece.getProjection();
                            boolean isCandidateRigid = candidatePlacementBehavior == StructureTemplatePool.Projection.RIGID;
                            boolean isCandidatePieceOceanFloor = candidatePiece instanceof LegacyOceanBottomSinglePoolElement;

                            // Determine how much the candidate jigsaw block is off in the y direction.
                            // This will be needed to offset the candidate piece so that the jigsaw blocks line up properly.
                            int candidateJigsawBlockRelativeY = candidateJigsawBlockPos.getY();
                            //? if <1.21.2 {
                            int candidateJigsawYOffsetNeeded = jigsawBlockRelativeY - candidateJigsawBlockRelativeY + JigsawBlock.getFrontFacing(jigsawBlock.state()).getStepY();
                            //?}
                            //? if >=1.21.2 <1.21.5 {
                            /*int candidateJigsawYOffsetNeeded = jigsawBlockRelativeY - candidateJigsawBlockRelativeY + JigsawBlock.getFrontFacing(jigsawBlock.info().state()).getStepY();
                            *///?}
                            //? if >=1.21.5 {
                            /*int candidateJigsawYOffsetNeeded =
                            *///?}
                                    //? if >=1.21.5 <26.3 {
                                    /*jigsawBlockRelativeY - candidateJigsawBlockRelativeY + JigsawBlock.getFrontFacing(jigsawBlock.info().state()).getStepY();
                                    *///?}
                                    //? if >=26.3 {
                                    /*jigsawBlockRelativeY - candidateJigsawBlockRelativeY + JigsawBlock.getFrontFacing(jigsawBlock.state()).getStepY();
                                    *///?}

                            // Determine how much we need to offset the candidate piece itself in order to have the jigsaw blocks aligned.
                            // Depends on if the placement of both pieces is rigid or not
                            int adjustedCandidatePieceMinY;
                            if (isPieceRigid && !isPieceOceanFloor && isCandidateRigid && !isCandidatePieceOceanFloor) {
                                adjustedCandidatePieceMinY = pieceMinY + candidateJigsawYOffsetNeeded;
                            //? if <1.21.5 {
                            }
                            else {
                            //?} else {
                            /*} else {
                            *///?}
                                if (surfaceHeight == -1) {
                                    //? if <1.21.4 {
                                    surfaceHeight = context.chunkGenerator().getFirstFreeHeight(jigsawBlockPos.getX(), jigsawBlockPos.getZ(), isCandidatePieceOceanFloor || isPieceOceanFloor ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG, heightLimitView, context.randomState());
                                    //?}
                                    //? if >=1.21.4 <1.21.5 {
                                    /*surfaceHeight = GeneralUtils.getCachedFreeHeight(context.chunkGenerator(), jigsawBlockPos.getX(), jigsawBlockPos.getZ(), isCandidatePieceOceanFloor || isPieceOceanFloor ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG, heightLimitView, context.randomState());
                                    *///?}
                                    //? if >=1.21.5 <26.1.2 {
                                    /*surfaceHeight = context.chunkGenerator().getFirstFreeHeight(
                                            jigsawBlockPos.getX(), jigsawBlockPos.getZ(),
                                    *///?}
                                    //? if >=26.1.2 {
                                    /*surfaceHeight = GeneralUtils.getCachedFreeHeight(
                                            context.chunkGenerator(), jigsawBlockPos.getX(), jigsawBlockPos.getZ(),
                                    *///?}
                                            //? if >=1.21.5 {
                                            /*(isCandidatePieceOceanFloor || isPieceOceanFloor) ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG,
                                            heightLimitView, context.randomState());
                                            *///?}
                                }

                                adjustedCandidatePieceMinY = surfaceHeight - candidateJigsawBlockRelativeY;
                            }
                            int candidatePieceYOffsetNeeded = adjustedCandidatePieceMinY - candidateBoundingBox.minY();

                            // Offset the candidate's bounding box by the necessary amount
                            BoundingBox adjustedCandidateBoundingBox = candidateBoundingBox.moved(0, candidatePieceYOffsetNeeded, 0);

                            // Add this offset to the relative jigsaw block position as well
                            BlockPos adjustedCandidateJigsawBlockRelativePos = candidateJigsawBlockRelativePos.offset(0, candidatePieceYOffsetNeeded, 0);

                            // Final adjustments to the bounding box.
                            if (candidateHeightAdjustments > 0) {
                                //? if <1.21.5 {
                                int k2 = Math.max(candidateHeightAdjustments + 1, adjustedCandidateBoundingBox.maxY() - adjustedCandidateBoundingBox.minY());
                                //?} else {
                                /*int k2 = Math.max(candidateHeightAdjustments + 1, adjustedCandidateBoundingBox.getYSpan());
                                *///?}
                                adjustedCandidateBoundingBox.encapsulate(new BlockPos(adjustedCandidateBoundingBox.minX(), adjustedCandidateBoundingBox.minY() + k2, adjustedCandidateBoundingBox.minZ()));
                            }

                            // Prevent pieces from spawning above max Y or below min Y
                            if (adjustedCandidateBoundingBox.maxY() > this.maxY || adjustedCandidateBoundingBox.minY() < this.minY) {
                                continue;
                            }

                            AABB axisAlignedBB = AABB.of(adjustedCandidateBoundingBox);
                            AABB axisAlignedBBDeflated = axisAlignedBB.deflate(0.25D);
                            boolean validBounds = false;

                            // Make sure new piece fits within the chosen octree without intersecting any other piece.
                            //? if <1.21.4 {
                            if (ignoreBounds || (boxOctreeMutableObject.getValue().withinBoundsButNotIntersectingChildren(axisAlignedBBDeflated))) {
                            //?}
                            //? if >=1.21.4 <1.21.5 {
                            /*if (ignoreBounds || (boxOctreeMutableObject.getValue().boundaryContains(axisAlignedBBDeflated) && !boxOctreeMutableObject.getValue().intersectsAnyBox(axisAlignedBBDeflated))) {
                            *///?}
                            //? if >=1.21.5 <26.1.2 {
                            /*if (ignoreBounds || (boxOctreeMutableObject.getValue().withinBoundsButNotIntersectingChildren(axisAlignedBBDeflated))) {
                            *///?}
                            //? if >=26.1.2 {
                            /*if (ignoreBounds || (boxOctreeMutableObject.getValue().boundaryContains(axisAlignedBBDeflated)
                                    && !boxOctreeMutableObject.getValue().intersectsAnyBox(axisAlignedBBDeflated))) {
                            *///?}
                                boxOctreeMutableObject.getValue().addBox(axisAlignedBB);
                                validBounds = true;
                            }

                            if (validBounds) {

                                // Determine ground level delta for this new piece
                                int newPieceGroundLevelDelta = piece.getGroundLevelDelta();
                                int groundLevelDelta;
                                if (isCandidateRigid && !isCandidatePieceOceanFloor) {
                                    groundLevelDelta = newPieceGroundLevelDelta - candidateJigsawYOffsetNeeded;
                                //? if <1.21.5 {
                                }
                                else {
                                //?} else {
                                /*} else {
                                *///?}
                                    groundLevelDelta = candidatePiece.getGroundLevelDelta();
                                }

                                // Create new piece
                                PoolElementStructurePiece newPiece = new PoolElementStructurePiece(
                                        context.structureTemplateManager(),
                                        candidatePiece,
                                        adjustedCandidateJigsawBlockRelativePos,
                                        groundLevelDelta,
                                        rotation,
                                        adjustedCandidateBoundingBox,
                                        liquidSettings
                                );

                                // Determine actual y-value for the new jigsaw block
                                int candidateJigsawBlockY;
                                if (isPieceRigid && !isPieceOceanFloor) {
                                    candidateJigsawBlockY = pieceMinY + jigsawBlockRelativeY;
                                //? if <1.21.5 {
                                }
                                else if (isCandidateRigid && !isCandidatePieceOceanFloor) {
                                //?} else {
                                /*} else if (isCandidateRigid && !isCandidatePieceOceanFloor) {
                                *///?}
                                    candidateJigsawBlockY = adjustedCandidatePieceMinY + candidateJigsawBlockRelativeY;
                                //? if <1.21.5 {
                                }
                                else {
                                //?} else {
                                /*} else {
                                *///?}
                                    if (surfaceHeight == -1) {
                                        //? if <1.21.4 {
                                        surfaceHeight = context.chunkGenerator().getFirstFreeHeight(jigsawBlockPos.getX(), jigsawBlockPos.getZ(), isCandidatePieceOceanFloor || isPieceOceanFloor ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG, heightLimitView, context.randomState());
                                        //?}
                                        //? if >=1.21.4 <1.21.5 {
                                        /*surfaceHeight = GeneralUtils.getCachedFreeHeight(context.chunkGenerator(), jigsawBlockPos.getX(), jigsawBlockPos.getZ(), isCandidatePieceOceanFloor || isPieceOceanFloor ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG, heightLimitView, context.randomState());
                                        *///?}
                                        //? if >=1.21.5 <26.1.2 {
                                        /*surfaceHeight = context.chunkGenerator().getFirstFreeHeight(
                                                jigsawBlockPos.getX(), jigsawBlockPos.getZ(),
                                        *///?}
                                        //? if >=26.1.2 {
                                        /*surfaceHeight = GeneralUtils.getCachedFreeHeight(
                                                context.chunkGenerator(), jigsawBlockPos.getX(), jigsawBlockPos.getZ(),
                                        *///?}
                                                //? if >=1.21.5 {
                                                /*(isCandidatePieceOceanFloor || isPieceOceanFloor) ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG,
                                                heightLimitView, context.randomState());
                                                *///?}
                                    }

                                    candidateJigsawBlockY = surfaceHeight + candidateJigsawYOffsetNeeded / 2;
                                }

                                // Add the junction to the existing piece
                                //? if <1.21.5 {
                                piece.addJunction(
                                        new JigsawJunction(
                                                jigsawBlockTargetPos.getX(),
                                                candidateJigsawBlockY - jigsawBlockRelativeY + newPieceGroundLevelDelta,
                                                jigsawBlockTargetPos.getZ(),
                                                candidateJigsawYOffsetNeeded,
                                                candidatePlacementBehavior)
                                );
                                //?} else {
                                /*piece.addJunction(new JigsawJunction(
                                        jigsawBlockTargetPos.getX(),
                                        candidateJigsawBlockY - jigsawBlockRelativeY + newPieceGroundLevelDelta,
                                        jigsawBlockTargetPos.getZ(),
                                        candidateJigsawYOffsetNeeded,
                                        piecePlacementBehavior));
                                *///?}

                                // Add the junction to the new piece
                                //? if <1.21.5 {
                                newPiece.addJunction(
                                        new JigsawJunction(
                                                jigsawBlockPos.getX(),
                                                candidateJigsawBlockY - candidateJigsawBlockRelativeY + groundLevelDelta,
                                                jigsawBlockPos.getZ(),
                                                -candidateJigsawYOffsetNeeded,
                                                piecePlacementBehavior)
                                );
                                //?} else {
                                /*newPiece.addJunction(new JigsawJunction(
                                        jigsawBlockPos.getX(),
                                        candidateJigsawBlockY - candidateJigsawBlockRelativeY + groundLevelDelta,
                                        jigsawBlockPos.getZ(),
                                        -candidateJigsawYOffsetNeeded,
                                        piecePlacementBehavior));
                                *///?}

                                // Add the piece
                                this.structurePieces.add(newPiece);
                                if (depth + 1 <= this.maxDepth) {
                                    this.availablePieces.addLast(new Entry(newPiece, boxOctreeMutableObject, targetPieceBoundsTop, depth + 1));
                                }
                                // Update piece count, if an entry exists for this piece
                                if (pieceName != null && this.currentPieceCounts.containsKey(pieceName)) {
                                    this.currentPieceCounts.put(pieceName, this.currentPieceCounts.get(pieceName) + 1);
                                }
                                return candidatePiece;
                            }
                        }
                    }
                }
                totalCount -= chosenPiecePair.getSecond();
                candidatePieces.remove(chosenPiecePair);
            }
            return null;
        }
    }
}
