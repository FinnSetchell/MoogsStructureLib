package com.finndog.moogs_structures.config;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Reads/writes config/moogs_structures.json. Schema:
 * <pre>{@code
 * {
 *   "presets": { "<modid>": { "<presetId>": true } },
 *   "spacing": {
 *     "universal_multiplier": 1.0,
 *     "per_mod": { "<modid>": 1.0 },
 *     "per_structure": { "<structure_set_id>": 1.0 }
 *   }
 * }
 * }</pre>
 * Presets are booleans discovered from mod manifests (synced on load). Spacing multipliers scale a
 * structure's spacing/separation; the effective multiplier for a set is
 * universal x per_mod[namespace] x per_structure[id]. Both sections share one atomic writer so
 * neither clobbers the other. A generation counter is bumped on {@link #loadAndSync} (world load)
 * so placements can memoize effective values and only recompute per world session, not per edit.
 */
public final class MslConfig {
    private static final String FILE_NAME = "moogs_structures.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final MslConfig INSTANCE = new MslConfig();

    public static MslConfig get() { return INSTANCE; }

    private Map<String, Map<String, Boolean>> presets = new TreeMap<>();
    private double universalSpacing = 1.0;
    private Map<String, Double> perModSpacing = new TreeMap<>();
    private Map<String, Double> perStructureSpacing = new TreeMap<>();
    private Set<String> disabledStructures = new TreeSet<>();
    private Set<String> hiddenButtons = new TreeSet<>();
    // Immutable snapshot read by the worldgen mixin off multiple threads; swapped only at world load.
    private volatile Set<ResourceLocation> disabledSnapshot = Set.of();
    private int generation = 0;
    private Path file;

    private MslConfig() {}

    public synchronized void loadAndSync(Path configDir, Map<String, Map<String, Boolean>> discoveredPresets) {
        this.file = configDir.resolve(FILE_NAME);
        Stored stored = readStored(file);

        Map<String, Map<String, Boolean>> mergedPresets = new TreeMap<>();
        for (Map.Entry<String, Map<String, Boolean>> mod : discoveredPresets.entrySet()) {
            Map<String, Boolean> storedForMod = stored.presets.getOrDefault(mod.getKey(), Map.of());
            Map<String, Boolean> out = new TreeMap<>();
            for (Map.Entry<String, Boolean> preset : mod.getValue().entrySet()) {
                out.put(preset.getKey(), storedForMod.getOrDefault(preset.getKey(), preset.getValue()));
            }
            if (!out.isEmpty()) mergedPresets.put(mod.getKey(), out);
        }

        this.presets = mergedPresets;
        this.universalSpacing = stored.universalSpacing;
        this.perModSpacing = stored.perModSpacing;
        this.perStructureSpacing = stored.perStructureSpacing;
        this.disabledStructures = stored.disabledStructures;
        this.hiddenButtons = stored.hiddenButtons;
        this.disabledSnapshot = buildSnapshot(stored.disabledStructures);
        this.generation++;
        writeFile();
    }

    private static Set<ResourceLocation> buildSnapshot(Set<String> ids) {
        Set<ResourceLocation> out = new HashSet<>();
        for (String id : ids) {
            ResourceLocation rl = ResourceLocation.tryParse(id);
            if (rl != null) out.add(rl);
        }
        return Set.copyOf(out);
    }

    // --- presets ---

    public boolean presetEnabled(String modid, String presetId, boolean defaultValue) {
        Map<String, Boolean> forMod = presets.get(modid);
        if (forMod == null) return defaultValue;
        return forMod.getOrDefault(presetId, defaultValue);
    }

    public synchronized void setAndSave(String modid, String presetId, boolean value) {
        if (file == null) return;
        presets.computeIfAbsent(modid, k -> new TreeMap<>()).put(presetId, value);
        writeFile();
    }

    // --- spacing ---

    /** Bumped each world load; placements memoize effective spacing against this so an in-world edit never desyncs an active session. */
    public int spacingGeneration() { return generation; }

    public double getUniversalSpacingMultiplier() { return universalSpacing; }

    public double getModSpacingMultiplier(String modid) { return perModSpacing.getOrDefault(modid, 1.0); }

    public double getStructureSpacingMultiplier(String structureSetId) { return perStructureSpacing.getOrDefault(structureSetId, 1.0); }

    /** universal x per_mod[namespace] x per_structure[id]. Null id -> universal only. */
    public double getEffectiveSpacingMultiplier(String structureSetId) {
        double m = universalSpacing;
        if (structureSetId != null) {
            int colon = structureSetId.indexOf(':');
            String namespace = colon > 0 ? structureSetId.substring(0, colon) : structureSetId;
            m *= perModSpacing.getOrDefault(namespace, 1.0);
            m *= perStructureSpacing.getOrDefault(structureSetId, 1.0);
        }
        return m;
    }

    public synchronized void setUniversalSpacingAndSave(double value) {
        if (file == null) return;
        this.universalSpacing = value;
        writeFile();
    }

    public synchronized void setModSpacingAndSave(String modid, double value) {
        if (file == null) return;
        perModSpacing.put(modid, value);
        writeFile();
    }

    public synchronized void setStructureSpacingAndSave(String structureSetId, double value) {
        if (file == null) return;
        perStructureSpacing.put(structureSetId, value);
        writeFile();
    }

    // --- disabled structures ---

    /** Read by the worldgen mixin (thread-safe immutable snapshot; changes take effect on world reload). */
    public boolean isStructureDisabled(ResourceLocation structureId) {
        return disabledSnapshot.contains(structureId);
    }

    public boolean hasAnyDisabled() {
        return !disabledSnapshot.isEmpty();
    }

    /** Current config value for the screen toggle (reflects unsaved-this-session edits). */
    public boolean isDisabledForScreen(String structureId) {
        return disabledStructures.contains(structureId);
    }

    public synchronized void setStructureDisabledAndSave(String structureId, boolean disabled) {
        if (file == null) return;
        if (disabled) disabledStructures.add(structureId);
        else disabledStructures.remove(structureId);
        writeFile();
    }

    // --- dismissible support buttons (Discord/Ko-fi on the config screen) ---

    /** Whether the user has permanently dismissed a support-link button. Instance-global, so it sticks across worlds. */
    public boolean isButtonHidden(String buttonId) {
        return hiddenButtons.contains(buttonId);
    }

    public synchronized void setButtonHiddenAndSave(String buttonId, boolean hidden) {
        if (file == null) return;
        if (hidden) hiddenButtons.add(buttonId);
        else hiddenButtons.remove(buttonId);
        writeFile();
    }

    // --- io ---

    private record Stored(Map<String, Map<String, Boolean>> presets, double universalSpacing,
                          Map<String, Double> perModSpacing, Map<String, Double> perStructureSpacing,
                          Set<String> disabledStructures, Set<String> hiddenButtons) {}

    private static Stored readStored(Path file) {
        Map<String, Map<String, Boolean>> presets = new HashMap<>();
        double universal = 1.0;
        Map<String, Double> perMod = new TreeMap<>();
        Map<String, Double> perStructure = new TreeMap<>();
        Set<String> disabled = new TreeSet<>();
        Set<String> hiddenButtons = new TreeSet<>();
        if (Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
                if (root.has("disabled_structures") && root.get("disabled_structures").isJsonArray()) {
                    JsonArray arr = root.getAsJsonArray("disabled_structures");
                    for (int i = 0; i < arr.size(); i++) {
                        if (arr.get(i).isJsonPrimitive()) disabled.add(arr.get(i).getAsString());
                    }
                }
                if (root.has("hidden_buttons") && root.get("hidden_buttons").isJsonArray()) {
                    JsonArray arr = root.getAsJsonArray("hidden_buttons");
                    for (int i = 0; i < arr.size(); i++) {
                        if (arr.get(i).isJsonPrimitive()) hiddenButtons.add(arr.get(i).getAsString());
                    }
                }
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
                        presets.put(modid, forMod);
                    }
                }
                if (root.has("spacing") && root.get("spacing").isJsonObject()) {
                    JsonObject spacing = root.getAsJsonObject("spacing");
                    if (spacing.has("universal_multiplier")) universal = spacing.get("universal_multiplier").getAsDouble();
                    readMultiplierMap(spacing, "per_mod", perMod);
                    readMultiplierMap(spacing, "per_structure", perStructure);
                }
            } catch (IOException | RuntimeException e) {
                MoogsStructuresCommon.LOGGER.warn("Moogs Structures: failed to read {} - defaults will be used ({}: {})",
                        file, e.getClass().getSimpleName(), e.getMessage());
            }
        }
        return new Stored(presets, universal, perMod, perStructure, disabled, hiddenButtons);
    }

    private static void readMultiplierMap(JsonObject parent, String key, Map<String, Double> out) {
        if (!parent.has(key) || !parent.get(key).isJsonObject()) return;
        JsonObject obj = parent.getAsJsonObject(key);
        for (String k : obj.keySet()) {
            if (obj.get(k).isJsonPrimitive() && obj.get(k).getAsJsonPrimitive().isNumber()) {
                out.put(k, obj.get(k).getAsDouble());
            }
        }
    }

    private synchronized void writeFile() {
        try {
            Files.createDirectories(file.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("_comment", "Presets replace vanilla structures with Moogs ones. Spacing multipliers make structures rarer (higher) or denser (lower); effective = universal x per_mod x per_structure. Changes apply on world reload and only affect newly generated chunks.");

            JsonObject presetsObj = new JsonObject();
            for (Map.Entry<String, Map<String, Boolean>> mod : presets.entrySet()) {
                JsonObject byPreset = new JsonObject();
                for (Map.Entry<String, Boolean> preset : mod.getValue().entrySet()) {
                    byPreset.addProperty(preset.getKey(), preset.getValue());
                }
                presetsObj.add(mod.getKey(), byPreset);
            }
            root.add("presets", presetsObj);

            JsonObject spacing = new JsonObject();
            spacing.addProperty("universal_multiplier", universalSpacing);
            JsonObject perMod = new JsonObject();
            perModSpacing.forEach(perMod::addProperty);
            spacing.add("per_mod", perMod);
            JsonObject perStructure = new JsonObject();
            perStructureSpacing.forEach(perStructure::addProperty);
            spacing.add("per_structure", perStructure);
            root.add("spacing", spacing);

            JsonArray disabled = new JsonArray();
            disabledStructures.forEach(disabled::add);
            root.add("disabled_structures", disabled);

            JsonArray hidden = new JsonArray();
            hiddenButtons.forEach(hidden::add);
            root.add("hidden_buttons", hidden);

            try (Writer w = Files.newBufferedWriter(file)) {
                GSON.toJson(root, w);
            }
        } catch (IOException | RuntimeException e) {
            MoogsStructuresCommon.LOGGER.warn("Moogs Structures: failed to write {} ({}: {})",
                    file, e.getClass().getSimpleName(), e.getMessage());
        }
    }
}
