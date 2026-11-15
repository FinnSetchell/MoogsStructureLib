package com.finndog.moogs_structures.world.structures;

import com.finndog.moogs_structures.modinit.MoogsStructuresStructures;
import com.finndog.moogs_structures.utils.GeneralUtils;
//? if >=1.21.1 {
import com.finndog.moogs_structures.world.structures.codecs.YRangeAllowance;
//?}
//? if >=1.20.6 {
import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptation;
//?}
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
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

//? if <1.20.6 {
/*import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptation;

*///?}
public class GenericNetherJigsawStructure extends GenericJigsawStructure {

    //? if >=1.20.6 {
    public static final MapCodec<GenericNetherJigsawStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    //?} else {
    /*public static final Codec<GenericNetherJigsawStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    *///?}
            GenericNetherJigsawStructure.settingsCodec(instance),
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
            Codec.BOOL.fieldOf("cannot_spawn_in_liquid").orElse(false).forGetter(structure -> structure.cannotSpawnInLiquid),
            Codec.intRange(1, 100).optionalFieldOf("valid_biome_radius_check").forGetter(structure -> structure.biomeRadius),
            ResourceLocation.CODEC.listOf().fieldOf("pools_that_ignore_boundaries").orElse(new ArrayList<>()).xmap(HashSet::new, ArrayList::new).forGetter(structure -> structure.poolsThatIgnoreBoundaries),
            Codec.intRange(1, 128).optionalFieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter),
            Codec.intRange(0, 100).optionalFieldOf("ledge_offset_y").forGetter(structure -> structure.ledgeOffsetY),
            StringRepresentable.fromEnum(LAND_SEARCH_DIRECTION::values).fieldOf("land_search_direction").forGetter(structure -> structure.searchDirection),
            Codec.BOOL.fieldOf("use_bounding_box_hack").orElse(false).forGetter(structure -> structure.useBoundingBoxHack),
            //? if >=1.21.1 {
            LiquidSettings.CODEC.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(structure -> structure.liquidSettings),
            //?}
            EnhancedTerrainAdaptation.CODEC.optionalFieldOf("enhanced_terrain_adaptation", EnhancedTerrainAdaptation.NONE).forGetter(structure -> structure.enhancedTerrainAdaptation)
    ).apply(instance, GenericNetherJigsawStructure::new));

    public final Optional<Integer> ledgeOffsetY;
    public final LAND_SEARCH_DIRECTION searchDirection;

    public GenericNetherJigsawStructure(StructureSettings config,
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
                                        boolean cannotSpawnInLiquid,
                                        Optional<Integer> biomeRadius,
                                        HashSet<ResourceLocation> poolsThatIgnoreBoundaries,
                                        Optional<Integer> maxDistanceFromCenter,
                                        Optional<Integer> ledgeOffsetY,
                                        LAND_SEARCH_DIRECTION searchDirection,
                                        boolean useBoundingBoxHack,
                                        //? if >=1.21.1 {
                                        LiquidSettings liquidSettings,
                                        //?}
                                        EnhancedTerrainAdaptation enhancedTerrainAdaptation) {
        super(config,
            startPool,
            size,
            //? if >=1.21.1 {
            yAllowance,
            //?} else {
            /*minYAllowed,
            maxYAllowed,
            allowedYRangeFromStart,
            *///?}
            startHeight,
            Optional.empty(),
            cannotSpawnInLiquid,
            Optional.empty(),
            Optional.empty(),
            biomeRadius,
            poolsThatIgnoreBoundaries,
            maxDistanceFromCenter,
            Optional.empty(),
            useBoundingBoxHack,
            //? if >=1.21.1 {
            liquidSettings,
            //?}
            enhancedTerrainAdaptation);

        this.ledgeOffsetY = ledgeOffsetY;
        this.searchDirection = searchDirection;
    }

    /**
     * The top of a Nether column is the bedrock roof, so for {@code cannot_spawn_in_liquid} look where
     * the pieces will actually go: the land {@link #postLayoutAdjustments} searches for at the start
     * piece's column. When it finds none, the structure would drop to the lava sea, so it doesn't
     * spawn there at all and {@code /locate} moves on to a spot it can use.
     */
    @Override
    protected boolean isInLiquid(GenerationContext context, BlockPos blockPos) {
        if (this.searchDirection == LAND_SEARCH_DIRECTION.CEILING) {
            // Decided with the start piece in hand, see startPieceFits.
            return false;
        }
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        if (this.searchDirection == LAND_SEARCH_DIRECTION.FIXED_HEIGHT) {
            NoiseColumn column = chunkGenerator.getBaseColumn(blockPos.getX(), blockPos.getZ(), context.heightAccessor(), context.randomState());
            return !column.getBlock(blockPos.getY() + this.ledgeOffsetY.orElse(0)).getFluidState().isEmpty();
        }
        return noLand(context, this.findLand(context, new BoundingBox(blockPos)));
    }

    /** The land the placement searches for, at the middle column of {@code around}. */
    private BlockPos findLand(GenerationContext context, BoundingBox around) {
        if (this.searchDirection == LAND_SEARCH_DIRECTION.HIGHEST_LAND) {
            return GeneralUtils.getHighestLand(context.chunkGenerator(), context.randomState(), around, context.heightAccessor(), !this.cannotSpawnInLiquid);
        }
        return GeneralUtils.getLowestLand(context.chunkGenerator(), context.randomState(), around, context.heightAccessor(), !this.cannotSpawnInLiquid);
    }

    /** The land search found nothing usable, so placement falls back to sea level (in the Nether, on the lava sea). */
    private static boolean noLand(GenerationContext context, BlockPos land) {
        return land.getY() >= GeneralUtils.getMaxTerrainLimit(context.chunkGenerator()) || land.getY() <= context.chunkGenerator().getSeaLevel() + 1;
    }

    /**
     * A structure hanging from a ceiling only spawns where its start piece fits under one, with nothing but air
     * below the rock for the whole piece. There is no fallback height: anywhere else it would float or sit in
     * the lava sea.
     */
    @Override
    protected boolean startPieceFits(GenerationContext context, int offsetY, BlockPos blockpos, int topClipOff, int bottomClipOff, BoundingBox startPieceBox) {
        if (this.searchDirection != LAND_SEARCH_DIRECTION.CEILING) {
            return true;
        }
        // Where postLayoutAdjustments will put it: centred on blockpos, as centerAllPieces does.
        BlockPos center = startPieceBox.getCenter();
        BoundingBox placed = startPieceBox.moved(blockpos.getX() - center.getX(), 0, blockpos.getZ() - center.getZ());
        return this.ceilingTop(context, placed, offsetY, topClipOff, bottomClipOff) != GeneralUtils.NO_CEILING;
    }

    /**
     * A hanging structure is laid out as soon as its spot is picked, because whether every piece hangs clear
     * (checked in {@link #postLayoutAdjustments}) decides whether the spot is used at all, and {@code /locate}
     * has to get the same answer. The laid-out pieces are kept, so they are not laid out twice. Everything
     * it reads is the terrain before caves are carved, so the answer only depends on the seed.
     */
    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        Optional<GenerationStub> stub = super.findGenerationPoint(context);
        if (this.searchDirection != LAND_SEARCH_DIRECTION.CEILING || stub.isEmpty()) {
            return stub;
        }
        StructurePiecesBuilder pieces = stub.get().getPiecesBuilder();
        if (pieces.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new GenerationStub(stub.get().position(), Either.right(pieces)));
    }

    /**
     * Jigsaw children aren't held to y_allowance while a hanging structure is laid out at its starting height:
     * it only reaches its real height when it is hung from the ceiling, where the whole structure is checked.
     */
    @Override
    protected boolean clipsPiecesWhileAssembling() {
        return this.searchDirection != LAND_SEARCH_DIRECTION.CEILING;
    }

    /** The top row for the start piece in {@code startPieceBox} (at its final x and z) hanging from a ceiling. */
    private int ceilingTop(GenerationContext context, BoundingBox startPieceBox, int offsetY, int topClipOff, int bottomClipOff) {
        // ledge_offset_y and start_height move the structure up, as for the land searches: here, into the rock.
        return GeneralUtils.getCeilingTop(context.chunkGenerator(), context.randomState(), context.heightAccessor(), startPieceBox,
                this.ledgeOffsetY.orElse(0) + offsetY, bottomClipOff, topClipOff, this.cannotSpawnInLiquid);
    }

    @Override
    protected void postLayoutAdjustments(StructurePiecesBuilder structurePiecesBuilder, GenerationContext context, int offsetY, BlockPos blockpos, int topClipOff, int bottomClipOff, List<PoolElementStructurePiece> pieces) {
        GeneralUtils.centerAllPieces(blockpos, pieces);

        if (this.searchDirection == LAND_SEARCH_DIRECTION.CEILING) {
            BoundingBox startPieceBox = pieces.get(0).getBoundingBox();
            int top = this.ceilingTop(context, startPieceBox, offsetY, topClipOff, bottomClipOff);
            if (top == GeneralUtils.NO_CEILING) {
                // Only when a structure with required pieces swapped its start piece for one that doesn't fit.
                structurePiecesBuilder.clear();
                return;
            }
            // Never slid to fit y_allowance like below: that would pull it off the ceiling. The start piece
            // was checked against it; a jigsaw child reaching into the bedrock roof or out of y_allowance
            // means the structure isn't placed here at all.
            int yDiff = top - startPieceBox.maxY();
            BoundingBox whole = structurePiecesBuilder.getBoundingBox();
            int highestTop = Math.min(GeneralUtils.getMaxTerrainLimit(context.chunkGenerator()) - GeneralUtils.CEILING_ROOF_MARGIN, topClipOff);
            if (whole.maxY() + yDiff > highestTop || whole.minY() + yDiff < bottomClipOff) {
                structurePiecesBuilder.clear();
                return;
            }
            pieces.forEach(piece -> piece.move(0, yDiff, 0));
            // Every jigsaw child must hang clear too: a spire tip below the start piece mustn't end in the
            // lava sea or the cave floor.
            int startBottom = pieces.get(0).getBoundingBox().minY();
            for (int i = 1; i < pieces.size(); i++) {
                if (!GeneralUtils.hangsClear(context.chunkGenerator(), context.randomState(), context.heightAccessor(), pieces.get(i).getBoundingBox(), startBottom, this.cannotSpawnInLiquid)) {
                    structurePiecesBuilder.clear();
                    return;
                }
            }
            return;
        }

        // Decide the target base (Y of the lowest point of the start piece).
        int targetBaseY;
        if (this.searchDirection == LAND_SEARCH_DIRECTION.FIXED_HEIGHT) {
            // Spawn at a fixed height: place the structure base at the sampled start_height and skip the terrain search.
            targetBaseY = offsetY + this.ledgeOffsetY.orElse(0);
        }
        else {
            WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
            //? if <26.1.2 {
            random.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
            //?} else {
            /*random.setLargeFeatureSeed(context.seed(), context.chunkPos().x(), context.chunkPos().z());
            *///?}
            BlockPos placementPos = this.findLand(context, structurePiecesBuilder.getBoundingBox());
            // With cannot_spawn_in_liquid the structure only spawned because the start piece's column has
            // land (isInLiquid). If the middle of the whole structure has none, use that land instead of
            // dropping to the lava sea.
            if (this.cannotSpawnInLiquid && noLand(context, placementPos)) {
                placementPos = this.findLand(context, new BoundingBox(blockpos));
            }

            if (noLand(context, placementPos)) {
                targetBaseY = context.chunkGenerator().getSeaLevel() + this.ledgeOffsetY.orElse(0);
            }
            else {
                targetBaseY = placementPos.getY() + this.ledgeOffsetY.orElse(0);
            }

            // Fold in the start_height offset so the y allowance is evaluated against the final world position.
            targetBaseY += offsetY;
        }

        // Enforce the structure's y_allowance on the FINAL placement. topClipOff/bottomClipOff are derived from
        // max_y_allowed / min_y_allowed (Integer.MAX_VALUE / Integer.MIN_VALUE when unset). For nether structures the
        // vertical position is decided here (not during assembly), so without this clamp max_y_allowed has no effect.
        // We clamp the structure's bounding box so no part generates outside the allowed range.
        BoundingBox fullBox = structurePiecesBuilder.getBoundingBox();
        int currentBaseY = pieces.get(0).getBoundingBox().minY();
        int structureHeight = fullBox.maxY() - fullBox.minY();
        int baseToBoxMin = fullBox.minY() - currentBaseY; // usually <= 0 (other pieces may sit below the start piece)

        if (topClipOff != Integer.MAX_VALUE) {
            // Ensure the highest block of the structure does not exceed the cap.
            int maxAllowedBaseY = topClipOff - structureHeight - baseToBoxMin;
            if (targetBaseY > maxAllowedBaseY) {
                targetBaseY = maxAllowedBaseY;
            }
        }
        if (bottomClipOff != Integer.MIN_VALUE) {
            // Ensure the lowest block of the structure does not drop below the floor of the allowed range.
            int minAllowedBaseY = bottomClipOff - baseToBoxMin;
            if (targetBaseY < minAllowedBaseY) {
                targetBaseY = minAllowedBaseY;
            }
        }

        int yDiff = targetBaseY - currentBaseY;
        pieces.forEach(piece -> piece.move(0, yDiff, 0));
    }

    public enum LAND_SEARCH_DIRECTION implements StringRepresentable {
        HIGHEST_LAND("HIGHEST_LAND"),
        LOWEST_LAND("LOWEST_LAND"),
        FIXED_HEIGHT("FIXED_HEIGHT"),
        /** Hangs the structure from the highest ceiling it fits under, top first. */
        CEILING("CEILING");

        private final String name;

        LAND_SEARCH_DIRECTION(String name) {
            this.name = name;
        }

        private static final Map<String, LAND_SEARCH_DIRECTION> BY_NAME = Util.make(Maps.newHashMap(), (hashMap) -> {
            LAND_SEARCH_DIRECTION[] var1 = values();
            for (LAND_SEARCH_DIRECTION type : var1) {
                hashMap.put(type.name, type);
            }
        });

        public static LAND_SEARCH_DIRECTION byName(String name) {
            return BY_NAME.get(name.toUpperCase(Locale.ROOT));
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    @Override
    public StructureType<?> type() {
        return MoogsStructuresStructures.GENERIC_NETHER_JIGSAW_STRUCTURE.get();
    }
}