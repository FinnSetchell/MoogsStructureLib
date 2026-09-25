package com.finndog.moogs_structures.config;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
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
 *   "frequency": {
 *     "universal_multiplier": 1.0,
 *     "per_mod": { "<modid>": 1.0 },
 *     "per_structure": { "<structure_set_id>": 1.0 }
 *   }
 * }
 * }</pre>
 * Presets are booleans discovered from mod manifests (synced on load). Frequency multipliers say how
 * common a structure is: 2.0 means about twice as many. The effective frequency for a set is
 * universal x per_mod[namespace] x per_structure[id], and placements scale spacing/separation by
 * 1 / sqrt(frequency), because the number of structures in an area goes with 1 / spacing^2.
 * A pre-3.4 file stores spacing multipliers instead; they are converted on read (f = 1 / s^2),
 * which reproduces the old effective spacing exactly. Both sections share one atomic writer so
 * neither clobbers the other. A generation counter is bumped on {@link #loadAndSync} (world load)
 * so placements can memoize effective values and only recompute per world session, not per edit.
 */
public final class MslConfig {
    private static final String FILE_NAME = "moogs_structures.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final MslConfig INSTANCE = new MslConfig();

    public static MslConfig get() { return INSTANCE; }

    private Map<String, Map<String, Boolean>> presets = new TreeMap<>();
    // Bounds for a stored frequency. Wide enough to hold every legacy spacing value (0.25x..4x
    // spacing = 16x..1/16 frequency); only guards against zero, negative or absurd hand edits.
    private static final double MIN_FREQUENCY = 0.01;
    private static final double MAX_FREQUENCY = 100.0;

    private double universalFrequency = 1.0;
    private Map<String, Double> perModFrequency = new TreeMap<>();
    private Map<String, Double> perStructureFrequency = new TreeMap<>();
    private Set<String> disabledStructures = new TreeSet<>();
    private Set<String> hiddenButtons = new TreeSet<>();
    // Immutable snapshot read by the worldgen mixin off multiple threads; swapped only at world load.
    private volatile Set<Identifier> disabledSnapshot = Set.of();
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
        this.universalFrequency = stored.universalFrequency;
        this.perModFrequency = stored.perModFrequency;
        this.perStructureFrequency = stored.perStructureFrequency;
        this.disabledStructures = stored.disabledStructures;
        this.hiddenButtons = stored.hiddenButtons;
        this.disabledSnapshot = buildSnapshot(stored.disabledStructures);
        this.generation++;
        writeFile();
    }

    private static Set<Identifier> buildSnapshot(Set<String> ids) {
        Set<Identifier> out = new HashSet<>();
        for (String id : ids) {
            Identifier rl = Identifier.tryParse(id);
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

    // --- frequency ---

    /** Bumped each world load; placements memoize effective spacing against this so an in-world edit never desyncs an active session. */
    public int spacingGeneration() { return generation; }

    public double getUniversalFrequency() { return universalFrequency; }

    public double getModFrequency(String modid) { return perModFrequency.getOrDefault(modid, 1.0); }

    public double getStructureFrequency(String structureSetId) { return perStructureFrequency.getOrDefault(structureSetId, 1.0); }

    /** universal x per_mod[namespace] x per_structure[id]. Null id -> universal only. */
    public double getEffectiveFrequency(String structureSetId) {
        double f = universalFrequency;
        if (structureSetId != null) {
            int colon = structureSetId.indexOf(':');
            String namespace = colon > 0 ? structureSetId.substring(0, colon) : structureSetId;
            f *= perModFrequency.getOrDefault(namespace, 1.0);
            f *= perStructureFrequency.getOrDefault(structureSetId, 1.0);
        }
        return f;
    }

    /**
     * What placements multiply spacing and separation by: 1 / sqrt(effective frequency). Twice as
     * common means spacing shrinks by sqrt(2), since structure count goes with 1 / spacing^2.
     */
    public double getEffectiveSpacingMultiplier(String structureSetId) {
        return 1.0 / Math.sqrt(getEffectiveFrequency(structureSetId));
    }

    public synchronized void setUniversalFrequencyAndSave(double value) {
        if (file == null) return;
        this.universalFrequency = clampFrequency(value);
        writeFile();
    }

    public synchronized void setModFrequencyAndSave(String modid, double value) {
        if (file == null) return;
        perModFrequency.put(modid, clampFrequency(value));
        writeFile();
    }

    public synchronized void setStructureFrequencyAndSave(String structureSetId, double value) {
        if (file == null) return;
        perStructureFrequency.put(structureSetId, clampFrequency(value));
        writeFile();
    }

    private static double clampFrequency(double value) {
        return Math.max(MIN_FREQUENCY, Math.min(MAX_FREQUENCY, value));
    }

    // --- disabled structures ---

    /** Read by the worldgen mixin (thread-safe immutable snapshot; changes take effect on world reload). */
    public boolean isStructureDisabled(Identifier structureId) {
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

    private record Stored(Map<String, Map<String, Boolean>> presets, double universalFrequency,
                          Map<String, Double> perModFrequency, Map<String, Double> perStructureFrequency,
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
                if (root.has("frequency") && root.get("frequency").isJsonObject()) {
                    JsonObject frequency = root.getAsJsonObject("frequency");
                    universal = readMultiplier(frequency, "universal_multiplier", false, universal);
                    readMultiplierMap(frequency, "per_mod", false, perMod);
                    readMultiplierMap(frequency, "per_structure", false, perStructure);
                } else if (root.has("spacing") && root.get("spacing").isJsonObject()) {
                    // Pre-3.4 file: spacing multipliers. Frequency = 1 / spacing^2 keeps every
                    // structure exactly where it was; the next write stores the frequency section.
                    JsonObject spacing = root.getAsJsonObject("spacing");
                    universal = readMultiplier(spacing, "universal_multiplier", true, universal);
                    readMultiplierMap(spacing, "per_mod", true, perMod);
                    readMultiplierMap(spacing, "per_structure", true, perStructure);
                    MoogsStructuresCommon.LOGGER.info("Moogs Structures: converted spacing multipliers in {} to frequency multipliers", file);
                }
            } catch (IOException | RuntimeException e) {
                MoogsStructuresCommon.LOGGER.warn("Moogs Structures: failed to read {} - defaults will be used ({}: {})",
                        file, e.getClass().getSimpleName(), e.getMessage());
            }
        }
        return new Stored(presets, universal, perMod, perStructure, disabled, hiddenButtons);
    }

    private static double readMultiplier(JsonObject parent, String key, boolean legacySpacing, double fallback) {
        if (!parent.has(key) || !parent.get(key).isJsonPrimitive() || !parent.get(key).getAsJsonPrimitive().isNumber()) return fallback;
        Double f = toFrequency(parent.get(key).getAsDouble(), legacySpacing, key);
        return f != null ? f : fallback;
    }

    private static void readMultiplierMap(JsonObject parent, String key, boolean legacySpacing, Map<String, Double> out) {
        if (!parent.has(key) || !parent.get(key).isJsonObject()) return;
        JsonObject obj = parent.getAsJsonObject(key);
        for (String k : obj.keySet()) {
            if (obj.get(k).isJsonPrimitive() && obj.get(k).getAsJsonPrimitive().isNumber()) {
                Double f = toFrequency(obj.get(k).getAsDouble(), legacySpacing, k);
                if (f != null) out.put(k, f);
            }
        }
    }

    /** A stored value as a frequency, or null (ignored, with a warning) if it is not a positive number. */
    private static Double toFrequency(double value, boolean legacySpacing, String key) {
        if (!Double.isFinite(value) || value <= 0) {
            MoogsStructuresCommon.LOGGER.warn("Moogs Structures: ignoring {} multiplier {} for '{}' - it must be a positive number",
                    legacySpacing ? "spacing" : "frequency", value, key);
            return null;
        }
        return clampFrequency(legacySpacing ? 1.0 / (value * value) : value);
    }

    /** Rounded for the file so a converted legacy value reads 0.111111 rather than 0.1111111111111111. */
    private static double forFile(double value) {
        return Double.parseDouble(String.format(Locale.ROOT, "%.6f", value));
    }

    private synchronized void writeFile() {
        try {
            Files.createDirectories(file.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("_comment", "Presets replace vanilla structures with Moogs ones. Frequency multipliers say how common structures are: 2.0 means about twice as many, 0.5 about half as many; effective = universal x per_mod x per_structure. Changes apply on world reload and only affect newly generated chunks.");

            JsonObject presetsObj = new JsonObject();
            for (Map.Entry<String, Map<String, Boolean>> mod : presets.entrySet()) {
                JsonObject byPreset = new JsonObject();
                for (Map.Entry<String, Boolean> preset : mod.getValue().entrySet()) {
                    byPreset.addProperty(preset.getKey(), preset.getValue());
                }
                presetsObj.add(mod.getKey(), byPreset);
            }
            root.add("presets", presetsObj);

            JsonObject frequency = new JsonObject();
            frequency.addProperty("universal_multiplier", forFile(universalFrequency));
            JsonObject perMod = new JsonObject();
            perModFrequency.forEach((k, v) -> perMod.addProperty(k, forFile(v)));
            frequency.add("per_mod", perMod);
            JsonObject perStructure = new JsonObject();
            perStructureFrequency.forEach((k, v) -> perStructure.addProperty(k, forFile(v)));
            frequency.add("per_structure", perStructure);
            root.add("frequency", frequency);

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
