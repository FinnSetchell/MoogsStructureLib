package com.finndog.moogs_structures.misc.structurepiececounter;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Dynamic;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class StructurePieceCountsManager extends SimpleJsonResourceReloadListener<JsonElement> {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting().setLenient().disableHtmlEscaping()
            .excludeFieldsWithoutExposeAnnotation().create();

    private static final Codec<JsonElement> JSON_ELEMENT_CODEC = Codec.PASSTHROUGH.xmap(
            dyn -> dyn.convert(JsonOps.INSTANCE).getValue(),
            je  -> new Dynamic<>(JsonOps.INSTANCE, je)
    );

    private static final FileToIdConverter FILES = new FileToIdConverter("rs_pieces_spawn_counts", ".json");

    public static final StructurePieceCountsManager STRUCTURE_PIECE_COUNTS_MANAGER = new StructurePieceCountsManager();

    private Map<ResourceLocation, List<StructurePieceCountsObj>> structureToPieceCountsObjs = new HashMap<>();
    private final Map<ResourceLocation, Map<ResourceLocation, RequiredPieceNeeds>> cachedRequirePiecesMap = new HashMap<>();
    private final Map<ResourceLocation, Map<ResourceLocation, Integer>> cachedMaxCountPiecesMap = new HashMap<>();

    public StructurePieceCountsManager() {
        super(JSON_ELEMENT_CODEC, FILES);
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
                        "Moog's Structure Lib Error: Couldn't parse rs_pieces_spawn_counts file {} - JSON: {}",
                        fileId, jsonElement, e
                );
            }
        });

        this.structureToPieceCountsObjs = mapBuilder;
        cachedRequirePiecesMap.clear();
        cachedMaxCountPiecesMap.clear();

        StructurePieceCountsAdditionsMerger.performCountsAdditionsDetectionAndMerger(resourceManager);
    }

    @MethodsReturnNonnullByDefault
    private List<StructurePieceCountsObj> getStructurePieceCountsObjs(ResourceLocation fileIdentifier, JsonElement jsonElement) throws Exception {
        List<StructurePieceCountsObj> piecesSpawnCounts =
                GSON.fromJson(jsonElement.getAsJsonObject().get("pieces_spawn_counts"),
                        new TypeToken<List<StructurePieceCountsObj>>() {}.getType());

        for (int i = piecesSpawnCounts.size() - 1; i >= 0; i--) {
            StructurePieceCountsObj entry = piecesSpawnCounts.get(i);
            if (entry.alwaysSpawnThisMany != null &&
                    entry.neverSpawnMoreThanThisMany != null &&
                    entry.alwaysSpawnThisMany > entry.neverSpawnMoreThanThisMany) {
                throw new Exception("Moog's Structure Lib Error: Found " + entry.nbtPieceName +
                        " entry has alwaysSpawnThisMany greater than neverSpawnMoreThanThisMany which is invalid.");
            }
        }
        return piecesSpawnCounts;
    }

    public void parseAndAddCountsJSONObj(ResourceLocation structureRL, List<JsonElement> jsonElements) {
        jsonElements.forEach(jsonElement -> {
            try {
                this.structureToPieceCountsObjs
                        .computeIfAbsent(structureRL, rl -> new ArrayList<>())
                        .addAll(getStructurePieceCountsObjs(structureRL, jsonElement));
            } catch (Exception e) {
                MoogsStructuresCommon.LOGGER.error(
                        "Moog's Structure Lib Error: Couldn't parse rs_pieces_spawn_counts file {} - JSON: {}",
                        structureRL, jsonElement, e
                );
            }
        });
    }

    @Nullable
    public Map<ResourceLocation, RequiredPieceNeeds> getRequirePieces(ResourceLocation structureRL) {
        if (!this.structureToPieceCountsObjs.containsKey(structureRL)) return null;
        if (cachedRequirePiecesMap.containsKey(structureRL)) return cachedRequirePiecesMap.get(structureRL);

        Map<ResourceLocation, RequiredPieceNeeds> requirePiecesMap = new HashMap<>();
        List<StructurePieceCountsObj> list = this.structureToPieceCountsObjs.get(structureRL);
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
        }
        cachedRequirePiecesMap.put(structureRL, requirePiecesMap);
        return requirePiecesMap;
    }

    @MethodsReturnNonnullByDefault
    public Map<ResourceLocation, Integer> getMaximumCountForPieces(ResourceLocation structureRL) {
        if (cachedMaxCountPiecesMap.containsKey(structureRL)) return cachedMaxCountPiecesMap.get(structureRL);

        Map<ResourceLocation, Integer> maxCountPiecesMap = new HashMap<>();
        List<StructurePieceCountsObj> list = this.structureToPieceCountsObjs.get(structureRL);
        if (list != null) {
            for (StructurePieceCountsObj entry : list) {
                if (entry.neverSpawnMoreThanThisMany != null) {
                    maxCountPiecesMap.put(ResourceLocation.tryParse(entry.nbtPieceName), entry.neverSpawnMoreThanThisMany);
                }
            }
        }
        cachedMaxCountPiecesMap.put(structureRL, maxCountPiecesMap);
        return maxCountPiecesMap;
    }

    public record RequiredPieceNeeds(int maxLimit, int minDistanceFromCenter) {
        public int getRequiredAmount() { return maxLimit; }
        public int getMinDistanceFromCenter() { return minDistanceFromCenter; }
    }
}
