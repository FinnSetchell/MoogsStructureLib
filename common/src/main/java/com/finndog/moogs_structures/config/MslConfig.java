package com.finndog.moogs_structures.config;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reads config/moogs_structures.json. Schema:
 * { "presets": { "&lt;modid&gt;": { "&lt;presetId&gt;": true } } }
 * Presets are defined by the consumer mods, not here; this only stores the on/off state.
 * On load the file is synced with every preset discovered across mods so new presets show up
 * with their default while existing user choices are preserved.
 */
public final class MslConfig {
    private static final String FILE_NAME = "moogs_structures.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final MslConfig INSTANCE = new MslConfig();

    public static MslConfig get() { return INSTANCE; }

    private Map<String, Map<String, Boolean>> presets = Collections.emptyMap();

    private MslConfig() {}

    /**
     * @param discoveredPresets modid -&gt; (presetId -&gt; default) gathered from all mod manifests,
     *                          used to seed and top up the config file.
     */
    public synchronized void loadAndSync(Path configDir, Map<String, Map<String, Boolean>> discoveredPresets) {
        Path file = configDir.resolve(FILE_NAME);
        Map<String, Map<String, Boolean>> stored = readStored(file);

        Map<String, Map<String, Boolean>> merged = new TreeMap<>();
        for (Map.Entry<String, Map<String, Boolean>> mod : discoveredPresets.entrySet()) {
            Map<String, Boolean> storedForMod = stored.getOrDefault(mod.getKey(), Collections.emptyMap());
            Map<String, Boolean> out = new TreeMap<>();
            for (Map.Entry<String, Boolean> preset : mod.getValue().entrySet()) {
                out.put(preset.getKey(), storedForMod.getOrDefault(preset.getKey(), preset.getValue()));
            }
            if (!out.isEmpty()) merged.put(mod.getKey(), out);
        }

        this.presets = merged;
        writeFile(file, merged);
    }

    public boolean presetEnabled(String modid, String presetId, boolean defaultValue) {
        Map<String, Boolean> forMod = presets.get(modid);
        if (forMod == null) return defaultValue;
        return forMod.getOrDefault(presetId, defaultValue);
    }

    private static Map<String, Map<String, Boolean>> readStored(Path file) {
        Map<String, Map<String, Boolean>> stored = new HashMap<>();
        if (!Files.exists(file)) return stored;
        try (Reader r = Files.newBufferedReader(file)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            if (root.has("presets") && root.get("presets").isJsonObject()) {
                JsonObject presetsObj = root.getAsJsonObject("presets");
                for (String modid : presetsObj.keySet()) {
                    if (!presetsObj.get(modid).isJsonObject()) continue;
                    JsonObject byPreset = presetsObj.getAsJsonObject(modid);
                    Map<String, Boolean> forMod = new HashMap<>();
                    for (String presetId : byPreset.keySet()) {
                        if (byPreset.get(presetId).isJsonPrimitive() && byPreset.get(presetId).getAsJsonPrimitive().isBoolean()) {
                            forMod.put(presetId, byPreset.get(presetId).getAsBoolean());
                        }
                    }
                    stored.put(modid, forMod);
                }
            }
        } catch (IOException | RuntimeException e) {
            MoogsStructuresCommon.LOGGER.warn("Moogs Structures: failed to read {} - defaults will be used ({}: {})",
                    file, e.getClass().getSimpleName(), e.getMessage());
        }
        return stored;
    }

    private static void writeFile(Path file, Map<String, Map<String, Boolean>> presets) {
        try {
            Files.createDirectories(file.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("_comment", "Enable a preset to replace a vanilla structure with the Moogs equivalent. Presets are defined by each Moogs mod. Changes apply when you reload the world and only affect newly generated chunks.");
            JsonObject presetsObj = new JsonObject();
            for (Map.Entry<String, Map<String, Boolean>> mod : presets.entrySet()) {
                JsonObject byPreset = new JsonObject();
                for (Map.Entry<String, Boolean> preset : mod.getValue().entrySet()) {
                    byPreset.addProperty(preset.getKey(), preset.getValue());
                }
                presetsObj.add(mod.getKey(), byPreset);
            }
            root.add("presets", presetsObj);
            try (Writer w = Files.newBufferedWriter(file)) {
                GSON.toJson(root, w);
            }
        } catch (IOException | RuntimeException e) {
            MoogsStructuresCommon.LOGGER.warn("Moogs Structures: failed to write {} ({}: {})",
                    file, e.getClass().getSimpleName(), e.getMessage());
        }
    }
}
