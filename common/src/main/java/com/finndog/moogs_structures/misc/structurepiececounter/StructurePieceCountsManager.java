package com.finndog.moogs_structures.misc.structurepiececounter;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StructurePieceCountsManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().setLenient().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().create();
    public final static StructurePieceCountsManager STRUCTURE_PIECE_COUNTS_MANAGER = new StructurePieceCountsManager();

    // Worldgen reads these off-thread while a reload swaps them out; volatile publishes the swap safely.
    private volatile Map<ResourceLocation, List<StructurePieceCountsObj>> StructureToPieceCountsObjs = new HashMap<>();
    // Memoized lazily from parallel worldgen threads, so concurrent maps rather than plain HashMaps.
    private volatile Map<ResourceLocation, Map<ResourceLocation, RequiredPieceNeeds>> cachedRequirePiecesMap = new ConcurrentHashMap<>();
    private volatile Map<ResourceLocation, Map<ResourceLocation, Integer>> cachedMaxCountPiecesMap = new ConcurrentHashMap<>();

    public StructurePieceCountsManager() {
        super(GSON, "msl_pieces_spawn_counts");
    }

    @MethodsReturnNonnullByDefault
    private List<StructurePieceCountsObj> getStructurePieceCountsObjs(ResourceLocation fileIdentifier, JsonElement jsonElement) throws Exception {
        List<StructurePieceCountsObj> piecesSpawnCounts = GSON.fromJson(jsonElement.getAsJsonObject().get("pieces_spawn_counts"), new TypeToken<List<StructurePieceCountsObj>>() {}.getType());
        for(int i = piecesSpawnCounts.size() - 1; i >= 0; i--) {
            StructurePieceCountsObj entry = piecesSpawnCounts.get(i);
            if(entry.alwaysSpawnThisMany != null && entry.neverSpawnMoreThanThisMany != null && entry.alwaysSpawnThisMany > entry.neverSpawnMoreThanThisMany) {
                throw new Exception("Moog's Structure Lib Error: Found " + entry.nbtPieceName + " entry has alwaysSpawnThisMany greater than neverSpawnMoreThanThisMany which is invalid.");
            }
        }
        return piecesSpawnCounts;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> loader, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, List<StructurePieceCountsObj>> mapBuilder = new HashMap<>();
        loader.forEach((fileIdentifier, jsonElement) -> {
            try {
                mapBuilder.put(fileIdentifier, getStructurePieceCountsObjs(fileIdentifier, jsonElement));
            }
            catch (Exception e) {
                MoogsStructuresCommon.LOGGER.error("Moog's Structure Lib Error: Couldn't parse msl_pieces_spawn_counts file {} - JSON looks like: {}", fileIdentifier, jsonElement, e);
            }
        });
        this.StructureToPieceCountsObjs = mapBuilder;
        this.cachedRequirePiecesMap = new ConcurrentHashMap<>();
        this.cachedMaxCountPiecesMap = new ConcurrentHashMap<>();
        StructurePieceCountsAdditionsMerger.performCountsAdditionsDetectionAndMerger(manager);
    }

    public void parseAndAddCountsJSONObj(ResourceLocation structureRL, List<JsonElement> jsonElements) {
        jsonElements.forEach(jsonElement -> {
            try {
                this.StructureToPieceCountsObjs.computeIfAbsent(structureRL, rl -> new ArrayList<>()).addAll(getStructurePieceCountsObjs(structureRL, jsonElement));
            }
            catch (Exception e) {
                MoogsStructuresCommon.LOGGER.error("Moog's Structure Lib Error: Couldn't parse msl_pieces_spawn_counts file {} - JSON looks like: {}", structureRL, jsonElement, e);
            }
        });
    }

    @Nullable
    public Map<ResourceLocation, RequiredPieceNeeds> getRequirePieces(ResourceLocation structureRL) {
        Map<ResourceLocation, List<StructurePieceCountsObj>> counts = this.StructureToPieceCountsObjs;
        // check to make sure we do have entries for this structure
        if(!counts.containsKey(structureRL))
            return null;

        return cachedRequirePiecesMap.computeIfAbsent(structureRL, rl -> {
            Map<ResourceLocation, RequiredPieceNeeds> requirePiecesMap = new HashMap<>();
            List<StructurePieceCountsObj> structurePieceCountsObjs = counts.get(rl);
            if(structurePieceCountsObjs != null) {
                structurePieceCountsObjs.forEach(entry -> {
                    if (entry.alwaysSpawnThisMany != null)
                        requirePiecesMap.put(new ResourceLocation(entry.nbtPieceName), new RequiredPieceNeeds(entry.alwaysSpawnThisMany, entry.minimumDistanceFromCenterPiece != null ? entry.minimumDistanceFromCenterPiece : 0));
                });
            }
            return requirePiecesMap;
        });
    }

    @MethodsReturnNonnullByDefault
    public Map<ResourceLocation, Integer> getMaximumCountForPieces(ResourceLocation structureRL) {
        Map<ResourceLocation, List<StructurePieceCountsObj>> counts = this.StructureToPieceCountsObjs;
        return cachedMaxCountPiecesMap.computeIfAbsent(structureRL, rl -> {
            Map<ResourceLocation, Integer> maxCountPiecesMap = new HashMap<>();
            List<StructurePieceCountsObj> structurePieceCountsObjs = counts.get(rl);
            if(structurePieceCountsObjs != null) {
                structurePieceCountsObjs.forEach(entry -> {
                    if(entry.neverSpawnMoreThanThisMany != null)
                        maxCountPiecesMap.put(new ResourceLocation(entry.nbtPieceName), entry.neverSpawnMoreThanThisMany);
                });
            }
            return maxCountPiecesMap;
        });
    }

    public record RequiredPieceNeeds(int maxLimit, int minDistanceFromCenter) {
        public int getRequiredAmount() {
            return maxLimit;
        }

        public int getMinDistanceFromCenter() {
            return minDistanceFromCenter;
        }
    }
}
