package com.finndog.moogs_structures.world.structures;

import com.finndog.moogs_structures.modinit.MoogsStructuresStructures;
import com.finndog.moogs_structures.utils.GeneralUtils;
import com.finndog.moogs_structures.utils.StrictOptionalField;
import com.finndog.moogs_structures.utils.VersionResolver;
//? if >=1.21.1 {
import com.finndog.moogs_structures.world.structures.codecs.YRangeAllowance;
//?}
//? if >=1.20.6 {
import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptation;
//?}
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
//? if >=1.21.1 {
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
//?} else {
/*import com.finndog.moogs_structures.world.structures.codecs.LiquidSettings;
*///?}

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

//? if <1.20.6 {
/*import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptation;

*///?}
//? if <1.21.1 {
/*import com.finndog.moogs_structures.world.structures.terrainadaptation.EncapsulateBackport;

*///?}
public class GenericNetherJigsawStructure extends GenericJigsawStructure {

    /**
     * {@code land_search_direction}, and {@code burying_type}, which the Nether type takes only as LOWEST_CORNER and
     * only with a land search: the land is searched for under each corner of the start piece and the lowest is used.
     * One slot (DFU groups cap at 16 slots); the JSON stays flat. Declared before CODEC, which reads it while it is built.
     */
    private static final MapCodec<Pair<LAND_SEARCH_DIRECTION, Optional<BURYING_TYPE>>> LAND_SEARCH = Codec.mapPair(
                    StringRepresentable.fromEnum(LAND_SEARCH_DIRECTION::values).fieldOf("land_search_direction"),
                    StrictOptionalField.of(StringRepresentable.fromEnum(BURYING_TYPE::values), "burying_type"))
            .flatXmap(GenericNetherJigsawStructure::checkBurying, DataResult::success);

    private static DataResult<Pair<LAND_SEARCH_DIRECTION, Optional<BURYING_TYPE>>> checkBurying(Pair<LAND_SEARCH_DIRECTION, Optional<BURYING_TYPE>> landSearch) {
        Optional<BURYING_TYPE> burying = landSearch.getSecond();
        if (burying.isPresent() && burying.get() != BURYING_TYPE.LOWEST_CORNER) {
            return DataResult.error(() -> "Nether structures only take burying_type LOWEST_CORNER, not " + burying.get().getSerializedName());
        }
        if (burying.isPresent() && (landSearch.getFirst() == LAND_SEARCH_DIRECTION.FIXED_HEIGHT || landSearch.getFirst() == LAND_SEARCH_DIRECTION.CEILING)) {
            return DataResult.error(() -> "burying_type needs a land search: a " + landSearch.getFirst().getSerializedName() + " structure doesn't use the land");
        }
        return DataResult.success(landSearch);
    }

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
            LAND_SEARCH.forGetter(structure -> Pair.of(structure.searchDirection, structure.buryingType)),
            Codec.BOOL.fieldOf("use_bounding_box_hack").orElse(false).forGetter(structure -> structure.useBoundingBoxHack),
            // DFU groups cap at 16 slots, so before 1.21.1 the last fields share one; the JSON stays flat.
            //? if >=1.21.1 {
            LiquidSettings.CODEC.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(structure -> structure.liquidSettings),
            EnhancedTerrainAdaptation.CODEC.optionalFieldOf("enhanced_terrain_adaptation", EnhancedTerrainAdaptation.NONE).forGetter(structure -> structure.enhancedTerrainAdaptation),
            GenericJigsawStructure.MC_VERSIONS.forGetter(structure -> structure.mcVersions)
    ).apply(instance, (config, startPool, size, yAllowance, startHeight, cannotSpawnInLiquid, biomeRadius, poolsThatIgnoreBoundaries, maxDistanceFromCenter, ledgeOffsetY, landSearch, useBoundingBoxHack, liquidSettings, enhancedTerrainAdaptation, mcVersions) -> new GenericNetherJigsawStructure(
            config, startPool, size, yAllowance, startHeight, cannotSpawnInLiquid, biomeRadius, poolsThatIgnoreBoundaries, maxDistanceFromCenter, ledgeOffsetY, landSearch.getFirst(), landSearch.getSecond(), useBoundingBoxHack,
            liquidSettings, enhancedTerrainAdaptation, mcVersions)));
            //?} else {
            /*Codec.mapPair(StrictOptionalField.of(LiquidSettings.CODEC, "liquid_settings", LiquidSettings.APPLY_WATERLOGGING),
                    Codec.mapPair(EncapsulateBackport.FIELD,
                            EnhancedTerrainAdaptation.CODEC.optionalFieldOf("enhanced_terrain_adaptation", EnhancedTerrainAdaptation.NONE)))
                    .forGetter(structure -> Pair.of(structure.liquidSettings, Pair.of(structure.encapsulateBackport, structure.enhancedTerrainAdaptation))),
            GenericJigsawStructure.MC_VERSIONS.forGetter(structure -> structure.mcVersions)
    ).apply(instance, (config, startPool, size, minYAllowed, maxYAllowed, allowedYRangeFromStart, startHeight, cannotSpawnInLiquid, biomeRadius, poolsThatIgnoreBoundaries, maxDistanceFromCenter, ledgeOffsetY, landSearch, useBoundingBoxHack, liquidEncapsulateAndAdaptation, mcVersions) -> new GenericNetherJigsawStructure(
            config, startPool, size, minYAllowed, maxYAllowed, allowedYRangeFromStart, startHeight, cannotSpawnInLiquid, biomeRadius, poolsThatIgnoreBoundaries, maxDistanceFromCenter, ledgeOffsetY, landSearch.getFirst(), landSearch.getSecond(), useBoundingBoxHack,
            liquidEncapsulateAndAdaptation.getFirst(), liquidEncapsulateAndAdaptation.getSecond().getFirst(), liquidEncapsulateAndAdaptation.getSecond().getSecond(), mcVersions)));
            *///?}

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
                                        Optional<BURYING_TYPE> buryingType,
                                        boolean useBoundingBoxHack,
                                        LiquidSettings liquidSettings,
                                        //? if <1.21.1 {
                                        /*boolean encapsulateBackport,
                                        *///?}
                                        EnhancedTerrainAdaptation enhancedTerrainAdaptation,
                                        Optional<VersionResolver.VersionRange> mcVersions) {
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
            false,
            // max_footprint_in_liquid reads the top of each terrain column, which in the Nether is the bedrock roof.
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            biomeRadius,
            poolsThatIgnoreBoundaries,
            maxDistanceFromCenter,
            buryingType,
            useBoundingBoxHack,
            liquidSettings,
            //? if <1.21.1 {
            /*encapsulateBackport,
            *///?}
            enhancedTerrainAdaptation,
            mcVersions);

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
        return noLand(context, this.findLand(context, new BoundingBox(blockPos), Integer.MAX_VALUE));
    }

    /** The land the placement searches for, at the middle column of {@code around}, at or under {@code highestY}. */
    private BlockPos findLand(GenerationContext context, BoundingBox around, int highestY) {
        if (this.searchDirection == LAND_SEARCH_DIRECTION.HIGHEST_LAND) {
            return GeneralUtils.getHighestLand(context.chunkGenerator(), context.randomState(), around, context.heightAccessor(), !this.cannotSpawnInLiquid, highestY);
        }
        return GeneralUtils.getLowestLand(context.chunkGenerator(), context.randomState(), around, context.heightAccessor(), !this.cannotSpawnInLiquid, highestY);
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

    /** Whether this structure hangs from a ceiling, so the carvers leave the rock above its pieces (see {@link HangingRockGuard}). */
    public boolean hangsFromCeiling() {
        return this.searchDirection == LAND_SEARCH_DIRECTION.CEILING;
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

        BoundingBox fullBox = structurePiecesBuilder.getBoundingBox();
        BoundingBox startBox = pieces.get(0).getBoundingBox();
        int currentBaseY = startBox.minY();
        int baseToBoxMin = fullBox.minY() - currentBaseY; // usually <= 0 (other pieces may sit below the start piece)
        int baseToBoxMax = fullBox.maxY() - currentBaseY;

        // Decide the target base (Y of the lowest point of the start piece).
        int targetBaseY = this.allowedBaseY(this.baseY(context, fullBox, startBox, blockpos, offsetY, Integer.MAX_VALUE), topClipOff, bottomClipOff, baseToBoxMin, baseToBoxMax);

        // Under a roof (the Nether's bedrock, y 123 to 127) no part of the structure, every jigsaw piece included, goes
        // higher than y 122. Only a structure that would is moved: one on land goes on the highest land in its column
        // low enough for it, or is lowered into its land when there's none. A fixed-height structure set to start
        // above y 122 was put in or on the roof on purpose and stays.
        int highestTopUnderRoof = GeneralUtils.getMaxTerrainLimit(context.chunkGenerator()) - GeneralUtils.ROOF_MARGIN;
        int highestBaseUnderRoof = highestTopUnderRoof - baseToBoxMax;
        boolean startsInRoof = this.searchDirection == LAND_SEARCH_DIRECTION.FIXED_HEIGHT && targetBaseY > highestTopUnderRoof;
        if (targetBaseY > highestBaseUnderRoof && !startsInRoof
                && GeneralUtils.hasRoof(context.chunkGenerator(), context.randomState(), context.heightAccessor(), blockpos.getX(), blockpos.getZ())) {
            if (this.searchDirection != LAND_SEARCH_DIRECTION.FIXED_HEIGHT) {
                int highestLandY = highestBaseUnderRoof - this.ledgeOffsetY.orElse(0) - offsetY;
                targetBaseY = this.allowedBaseY(this.baseY(context, fullBox, startBox, blockpos, offsetY, highestLandY), topClipOff, bottomClipOff, baseToBoxMin, baseToBoxMax);
            }
            // Last, so not even min_y_allowed takes it into the roof.
            targetBaseY = Math.min(targetBaseY, highestBaseUnderRoof);
        }

        int yDiff = targetBaseY - currentBaseY;
        pieces.forEach(piece -> piece.move(0, yDiff, 0));
    }

    /**
     * Where the start piece's base goes before the y allowance: at start_height for FIXED_HEIGHT, otherwise
     * ledge_offset_y and start_height above the land found under {@code box}'s middle, or with LOWEST_CORNER the
     * lowest land under {@code startBox}'s corners. Only land at or under {@code highestLandY} counts while a column
     * has some; otherwise the land as found without that limit.
     */
    private int baseY(GenerationContext context, BoundingBox box, BoundingBox startBox, BlockPos blockpos, int offsetY, int highestLandY) {
        if (this.searchDirection == LAND_SEARCH_DIRECTION.FIXED_HEIGHT) {
            // Spawn at a fixed height: place the structure base at the sampled start_height and skip the terrain search.
            return offsetY + this.ledgeOffsetY.orElse(0);
        }
        // The codec only lets LOWEST_CORNER through for this type.
        BlockPos placementPos = this.buryingType.isPresent()
                ? this.lowestCornerLand(context, box, startBox, highestLandY)
                : this.findLandAtOrUnder(context, box, highestLandY);
        // With cannot_spawn_in_liquid the structure only spawned because the start piece's column has
        // land (isInLiquid). If the middle of the whole structure has none, use that land instead of
        // dropping to the lava sea.
        if (this.cannotSpawnInLiquid && noLand(context, placementPos)) {
            placementPos = this.findLandAtOrUnder(context, new BoundingBox(blockpos), highestLandY);
        }
        int landY = noLand(context, placementPos) ? context.chunkGenerator().getSeaLevel() : placementPos.getY();
        // Fold in the start_height offset so the y allowance is evaluated against the final world position.
        return landY + this.ledgeOffsetY.orElse(0) + offsetY;
    }

    /**
     * LOWEST_CORNER: the lowest of the land found under the start piece's four corners, so no corner of it is left
     * over the air. A corner with no land is left out; with none at any corner, the land under {@code box}'s middle.
     */
    private BlockPos lowestCornerLand(GenerationContext context, BoundingBox box, BoundingBox startBox, int highestLandY) {
        BlockPos lowest = null;
        for (int x : new int[]{startBox.minX(), startBox.maxX()}) {
            for (int z : new int[]{startBox.minZ(), startBox.maxZ()}) {
                BlockPos land = this.findLandAtOrUnder(context, new BoundingBox(new BlockPos(x, 0, z)), highestLandY);
                if (!noLand(context, land) && (lowest == null || land.getY() < lowest.getY())) {
                    lowest = land.immutable();
                }
            }
        }
        return lowest != null ? lowest : this.findLandAtOrUnder(context, box, highestLandY);
    }

    /** The land at or under {@code highestY} at the middle column of {@code around}, or the land with no limit if there is none. */
    private BlockPos findLandAtOrUnder(GenerationContext context, BoundingBox around, int highestY) {
        BlockPos land = this.findLand(context, around, highestY);
        return highestY == Integer.MAX_VALUE || !noLand(context, land) ? land : this.findLand(context, around, Integer.MAX_VALUE);
    }

    /**
     * Enforce the structure's y_allowance on the FINAL placement. topClipOff/bottomClipOff are derived from
     * max_y_allowed / min_y_allowed (Integer.MAX_VALUE / Integer.MIN_VALUE when unset). For nether structures the
     * vertical position is decided here (not during assembly), so without this clamp max_y_allowed has no effect.
     * We clamp the structure's bounding box so no part generates outside the allowed range.
     */
    private static int allowedBaseY(int targetBaseY, int topClipOff, int bottomClipOff, int baseToBoxMin, int baseToBoxMax) {
        if (topClipOff != Integer.MAX_VALUE) {
            // Ensure the highest block of the structure does not exceed the cap.
            int maxAllowedBaseY = topClipOff - baseToBoxMax;
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
        return targetBaseY;
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