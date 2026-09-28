package com.finndog.moogs_structures.misc.structurepiececounter;

import com.finndog.moogs_structures.MoogsStructuresCommon;
//? if <1.21.5 {
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
//?} else {
/*import com.google.gson.*;
*///?}
import com.google.gson.reflect.TypeToken;
//? if >=1.21.5 {
/*import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Dynamic;
*///?}
//? if <1.21.11 {
import net.minecraft.MethodsReturnNonnullByDefault;
//?}
//? if >=1.21.11 <26.2 {
/*import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
*///?}
//? if >=1.21.4 {
/*import net.minecraft.resources.FileToIdConverter;
*///?}
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
//? if >=1.21.2 <1.21.5 {
/*import net.minecraft.util.ExtraCodecs;
*///?}
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

//? if <1.21.5 {
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//?} else {
/*import java.util.*;
*///?}
import java.util.concurrent.ConcurrentHashMap;

//? if <1.21.2 {
public class StructurePieceCountsManager extends SimpleJsonResourceReloadListener {
//?} else {
/*public class StructurePieceCountsManager extends SimpleJsonResourceReloadListener<JsonElement> {
*///?}
    //? if <1.21.5 {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().setLenient().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().create();
    public final static StructurePieceCountsManager STRUCTURE_PIECE_COUNTS_MANAGER = new StructurePieceCountsManager();
    //?} else {
    /*private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting().setLenient().disableHtmlEscaping()
            .excludeFieldsWithoutExposeAnnotation().create();

    private static final Codec<JsonElement> JSON_ELEMENT_CODEC = Codec.PASSTHROUGH.xmap(
            dyn -> dyn.convert(JsonOps.INSTANCE).getValue(),
            je  -> new Dynamic<>(JsonOps.INSTANCE, je)
    );

    private static final FileToIdConverter FILES = new FileToIdConverter("msl_pieces_spawn_counts", ".json");

    public static final StructurePieceCountsManager STRUCTURE_PIECE_COUNTS_MANAGER = new StructurePieceCountsManager();
    *///?}

    // Worldgen reads these off-thread while a reload swaps them out; volatile publishes the swap safely.
    //? if <1.21.5 {
    private volatile Map<ResourceLocation, List<StructurePieceCountsObj>> StructureToPieceCountsObjs = new HashMap<>();
    //?} else {
    /*private volatile Map<ResourceLocation, List<StructurePieceCountsObj>> structureToPieceCountsObjs = new HashMap<>();
    *///?}
    // Memoized lazily from parallel worldgen threads, so concurrent maps rather than plain HashMaps.
    private volatile Map<ResourceLocation, Map<ResourceLocation, RequiredPieceNeeds>> cachedRequirePiecesMap = new ConcurrentHashMap<>();
    private volatile Map<ResourceLocation, Map<ResourceLocation, Integer>> cachedMaxCountPiecesMap = new ConcurrentHashMap<>();

    public StructurePieceCountsManager() {
        //? if <1.21.2 {
        super(GSON, "msl_pieces_spawn_counts");
        //?}
        //? if >=1.21.2 <1.21.4 {
        /*super(ExtraCodecs.JSON, "msl_pieces_spawn_counts");
        *///?}
        //? if >=1.21.4 <1.21.5 {
        /*super(ExtraCodecs.JSON, FileToIdConverter.json("msl_pieces_spawn_counts"));
        *///?}
        //? if >=1.21.5 {
        /*super(JSON_ELEMENT_CODEC, FILES);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> prepared,
                         ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        Map<ResourceLocation, List<StructurePieceCountsObj>> mapBuilder = new HashMap<>();

        prepared.forEach((fileId, jsonElement) -> {
            try {
                mapBuilder.put(fileId, getStructurePieceCountsObjs(fileId, jsonElement));
            } catch (Exception e) {
                MoogsStructuresCommon.LOGGER.error(
                        "Moog's Structure Lib Error: Couldn't parse msl_pieces_spawn_counts file {} - JSON: {}",
                        fileId, jsonElement, e
                );
            }
        });

        this.structureToPieceCountsObjs = mapBuilder;
        this.cachedRequirePiecesMap = new ConcurrentHashMap<>();
        this.cachedMaxCountPiecesMap = new ConcurrentHashMap<>();

        StructurePieceCountsAdditionsMerger.performCountsAdditionsDetectionAndMerger(resourceManager);
        *///?}
    }

    //? if <26.2 {
    @MethodsReturnNonnullByDefault
    //?}
    private List<StructurePieceCountsObj> getStructurePieceCountsObjs(ResourceLocation fileKey, JsonElement jsonElement) throws Exception {
        //? if <1.21.5 {
        List<StructurePieceCountsObj> piecesSpawnCounts = GSON.fromJson(jsonElement.getAsJsonObject().get("pieces_spawn_counts"), new TypeToken<List<StructurePieceCountsObj>>() {}.getType());
        for(int i = piecesSpawnCounts.size() - 1; i >= 0; i--) {
        //?} else {
        /*List<StructurePieceCountsObj> piecesSpawnCounts =
                GSON.fromJson(jsonElement.getAsJsonObject().get("pieces_spawn_counts"),
                        new TypeToken<List<StructurePieceCountsObj>>() {}.getType());

        for (int i = piecesSpawnCounts.size() - 1; i >= 0; i--) {
        *///?}
            StructurePieceCountsObj entry = piecesSpawnCounts.get(i);
            //? if <1.21.5 {
            if(entry.alwaysSpawnThisMany != null && entry.neverSpawnMoreThanThisMany != null && entry.alwaysSpawnThisMany > entry.neverSpawnMoreThanThisMany) {
                throw new Exception("Moog's Structure Lib Error: Found " + entry.nbtPieceName + " entry has alwaysSpawnThisMany greater than neverSpawnMoreThanThisMany which is invalid.");
            //?} else {
            /*if (entry.alwaysSpawnThisMany != null &&
                    entry.neverSpawnMoreThanThisMany != null &&
                    entry.alwaysSpawnThisMany > entry.neverSpawnMoreThanThisMany) {
                throw new Exception("Moog's Structure Lib Error: Found " + entry.nbtPieceName +
                        " entry has alwaysSpawnThisMany greater than neverSpawnMoreThanThisMany which is invalid.");
            *///?}
            }
        }
        return piecesSpawnCounts;
    }

    //? if <1.21.5 {
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> loader, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, List<StructurePieceCountsObj>> mapBuilder = new HashMap<>();
        loader.forEach((fileKey, jsonElement) -> {
            try {
                mapBuilder.put(fileKey, getStructurePieceCountsObjs(fileKey, jsonElement));
            }
            catch (Exception e) {
                MoogsStructuresCommon.LOGGER.error("Moog's Structure Lib Error: Couldn't parse msl_pieces_spawn_counts file {} - JSON looks like: {}", fileKey, jsonElement, e);
            }
        });
        this.StructureToPieceCountsObjs = mapBuilder;
        this.cachedRequirePiecesMap = new ConcurrentHashMap<>();
        this.cachedMaxCountPiecesMap = new ConcurrentHashMap<>();
        StructurePieceCountsAdditionsMerger.performCountsAdditionsDetectionAndMerger(manager);
    }

    //?}
    public void parseAndAddCountsJSONObj(ResourceLocation structureRL, List<JsonElement> jsonElements) {
        jsonElements.forEach(jsonElement -> {
            try {
                //? if <1.21.5 {
                this.StructureToPieceCountsObjs.computeIfAbsent(structureRL, rl -> new ArrayList<>()).addAll(getStructurePieceCountsObjs(structureRL, jsonElement));
            }
            catch (Exception e) {
                MoogsStructuresCommon.LOGGER.error("Moog's Structure Lib Error: Couldn't parse msl_pieces_spawn_counts file {} - JSON looks like: {}", structureRL, jsonElement, e);
                //?} else {
                /*this.structureToPieceCountsObjs
                        .computeIfAbsent(structureRL, rl -> new ArrayList<>())
                        .addAll(getStructurePieceCountsObjs(structureRL, jsonElement));
            } catch (Exception e) {
                MoogsStructuresCommon.LOGGER.error(
                        "Moog's Structure Lib Error: Couldn't parse msl_pieces_spawn_counts file {} - JSON: {}",
                        structureRL, jsonElement, e
                );
                *///?}
            }
        });
    }

    @Nullable
    public Map<ResourceLocation, RequiredPieceNeeds> getRequirePieces(ResourceLocation structureRL) {
        if (structureRL == null) return null;
        //? if <1.21.5 {
        Map<ResourceLocation, List<StructurePieceCountsObj>> counts = this.StructureToPieceCountsObjs;
        // check to make sure we do have entries for this structure
        if(!counts.containsKey(structureRL))
            return null;

        //?} else {
        /*Map<ResourceLocation, List<StructurePieceCountsObj>> counts = this.structureToPieceCountsObjs;
        if (!counts.containsKey(structureRL)) return null;
        *///?}
        return cachedRequirePiecesMap.computeIfAbsent(structureRL, rl -> {
            Map<ResourceLocation, RequiredPieceNeeds> requirePiecesMap = new HashMap<>();
            //? if <1.21.5 {
            List<StructurePieceCountsObj> structurePieceCountsObjs = counts.get(rl);
            if(structurePieceCountsObjs != null) {
                structurePieceCountsObjs.forEach(entry -> {
                    if (entry.alwaysSpawnThisMany != null)
            //?}
                        //? if >=1.21.1 <1.21.5 {
                        requirePiecesMap.put(ResourceLocation.tryParse(entry.nbtPieceName), new RequiredPieceNeeds(entry.alwaysSpawnThisMany, entry.minimumDistanceFromCenterPiece != null ? entry.minimumDistanceFromCenterPiece : 0));
                        //?}
                        //? if <1.21.1 {
                        /*requirePiecesMap.put(new ResourceLocation(entry.nbtPieceName), new RequiredPieceNeeds(entry.alwaysSpawnThisMany, entry.minimumDistanceFromCenterPiece != null ? entry.minimumDistanceFromCenterPiece : 0));
                        *///?}
                //? if <1.21.5 {
                });
                //?} else {
            /*List<StructurePieceCountsObj> list = counts.get(rl);
            if (list != null) {
                for (StructurePieceCountsObj entry : list) {
                    if (entry.alwaysSpawnThisMany != null) {
                        requirePiecesMap.put(
                                ResourceLocation.tryParse(entry.nbtPieceName),
                                new RequiredPieceNeeds(entry.alwaysSpawnThisMany,
                                        entry.minimumDistanceFromCenterPiece != null ? entry.minimumDistanceFromCenterPiece : 0)
                        );
                    }
                }
                *///?}
            }
            return requirePiecesMap;
        });
    }

    //? if <26.2 {
    @MethodsReturnNonnullByDefault
    //?}
    public Map<ResourceLocation, Integer> getMaximumCountForPieces(ResourceLocation structureRL) {
        // A structure nested inside another mod's wrapper (e.g. Lithostitched's delegating structure) has no
        // registry id of its own. The concurrent cache rejects a null key, so such a structure gets no limits.
        if (structureRL == null) return Map.of();
        //? if <1.21.5 {
        Map<ResourceLocation, List<StructurePieceCountsObj>> counts = this.StructureToPieceCountsObjs;
        //?} else {
        /*Map<ResourceLocation, List<StructurePieceCountsObj>> counts = this.structureToPieceCountsObjs;
        *///?}
        return cachedMaxCountPiecesMap.computeIfAbsent(structureRL, rl -> {
            Map<ResourceLocation, Integer> maxCountPiecesMap = new HashMap<>();
            //? if <1.21.5 {
            List<StructurePieceCountsObj> structurePieceCountsObjs = counts.get(rl);
            if(structurePieceCountsObjs != null) {
                structurePieceCountsObjs.forEach(entry -> {
                    if(entry.neverSpawnMoreThanThisMany != null)
            //?} else {
            /*List<StructurePieceCountsObj> list = counts.get(rl);
            if (list != null) {
                for (StructurePieceCountsObj entry : list) {
                    if (entry.neverSpawnMoreThanThisMany != null) {
            *///?}
                        //? if >=1.21.1 {
                        maxCountPiecesMap.put(ResourceLocation.tryParse(entry.nbtPieceName), entry.neverSpawnMoreThanThisMany);
                        //?} else {
                        /*maxCountPiecesMap.put(new ResourceLocation(entry.nbtPieceName), entry.neverSpawnMoreThanThisMany);
                        *///?}
                //? if <1.21.5 {
                });
                //?} else {
                    /*}
                }
                *///?}
            }
            return maxCountPiecesMap;
        });
    }

    public record RequiredPieceNeeds(int maxLimit, int minDistanceFromCenter) {
        //? if <1.21.5 {
        public int getRequiredAmount() {
            return maxLimit;
        }

        public int getMinDistanceFromCenter() {
            return minDistanceFromCenter;
        }
        //?} else {
        /*public int getRequiredAmount() { return maxLimit; }
        public int getMinDistanceFromCenter() { return minDistanceFromCenter; }
        *///?}
    }
}
