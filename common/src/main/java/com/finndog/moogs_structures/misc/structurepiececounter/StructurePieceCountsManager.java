package com.finndog.moogs_structures.misc.structurepiececounter;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Dynamic;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StructurePieceCountsManager extends SimpleJsonResourceReloadListener<JsonElement> {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting().setLenient().disableHtmlEscaping()
            .excludeFieldsWithoutExposeAnnotation().create();

    private static final Codec<JsonElement> JSON_ELEMENT_CODEC = Codec.PASSTHROUGH.xmap(
            dyn -> dyn.convert(JsonOps.INSTANCE).getValue(),
            je  -> new Dynamic<>(JsonOps.INSTANCE, je)
    );

    private static final FileToIdConverter FILES = new FileToIdConverter("msl_pieces_spawn_counts", ".json");

    public static final StructurePieceCountsManager STRUCTURE_PIECE_COUNTS_MANAGER = new StructurePieceCountsManager();

    // Worldgen reads these off-thread while a reload swaps them out; volatile publishes the swap safely.
    private volatile Map<Identifier, List<StructurePieceCountsObj>> structureToPieceCountsObjs = new HashMap<>();
    // Memoized lazily from parallel worldgen threads, so concurrent maps rather than plain HashMaps.
    private volatile Map<Identifier, Map<Identifier, RequiredPieceNeeds>> cachedRequirePiecesMap = new ConcurrentHashMap<>();
    private volatile Map<Identifier, Map<Identifier, Integer>> cachedMaxCountPiecesMap = new ConcurrentHashMap<>();

    public StructurePieceCountsManager() {
        super(JSON_ELEMENT_CODEC, FILES);
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared,
                         ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        Map<Identifier, List<StructurePieceCountsObj>> mapBuilder = new HashMap<>();

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
    }

    @MethodsReturnNonnullByDefault
    private List<StructurePieceCountsObj> getStructurePieceCountsObjs(Identifier fileIdentifier, JsonElement jsonElement) throws Exception {
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

    public void parseAndAddCountsJSONObj(Identifier structureRL, List<JsonElement> jsonElements) {
        jsonElements.forEach(jsonElement -> {
            try {
                this.structureToPieceCountsObjs
                        .computeIfAbsent(structureRL, rl -> new ArrayList<>())
                        .addAll(getStructurePieceCountsObjs(structureRL, jsonElement));
            } catch (Exception e) {
                MoogsStructuresCommon.LOGGER.error(
                        "Moog's Structure Lib Error: Couldn't parse msl_pieces_spawn_counts file {} - JSON: {}",
                        structureRL, jsonElement, e
                );
            }
        });
    }

    @Nullable
    public Map<Identifier, RequiredPieceNeeds> getRequirePieces(Identifier structureRL) {
        Map<Identifier, List<StructurePieceCountsObj>> counts = this.structureToPieceCountsObjs;
        if (!counts.containsKey(structureRL)) return null;
        return cachedRequirePiecesMap.computeIfAbsent(structureRL, rl -> {
            Map<Identifier, RequiredPieceNeeds> requirePiecesMap = new HashMap<>();
            List<StructurePieceCountsObj> list = counts.get(rl);
            if (list != null) {
                for (StructurePieceCountsObj entry : list) {
                    if (entry.alwaysSpawnThisMany != null) {
                        requirePiecesMap.put(
                                Identifier.tryParse(entry.nbtPieceName),
                                new RequiredPieceNeeds(entry.alwaysSpawnThisMany,
                                        entry.minimumDistanceFromCenterPiece != null ? entry.minimumDistanceFromCenterPiece : 0)
                        );
                    }
                }
            }
            return requirePiecesMap;
        });
    }

    @MethodsReturnNonnullByDefault
    public Map<Identifier, Integer> getMaximumCountForPieces(Identifier structureRL) {
        Map<Identifier, List<StructurePieceCountsObj>> counts = this.structureToPieceCountsObjs;
        return cachedMaxCountPiecesMap.computeIfAbsent(structureRL, rl -> {
            Map<Identifier, Integer> maxCountPiecesMap = new HashMap<>();
            List<StructurePieceCountsObj> list = counts.get(rl);
            if (list != null) {
                for (StructurePieceCountsObj entry : list) {
                    if (entry.neverSpawnMoreThanThisMany != null) {
                        maxCountPiecesMap.put(Identifier.tryParse(entry.nbtPieceName), entry.neverSpawnMoreThanThisMany);
                    }
                }
            }
            return maxCountPiecesMap;
        });
    }

    public record RequiredPieceNeeds(int maxLimit, int minDistanceFromCenter) {
        public int getRequiredAmount() { return maxLimit; }
        public int getMinDistanceFromCenter() { return minDistanceFromCenter; }
    }
}
