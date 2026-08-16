package com.finndog.moogs_structures.config;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Aggregates the replacement presets declared by every consumer mod, resolves whether a preset
 * is currently enabled (via {@link MslConfig}), and answers the questions the processor, the
 * disable mixin, and the locate command need: is a (modid, vanilla_key) replacement active,
 * should a vanilla structure be cancelled, and what replaced it.
 *
 * A preset is a named bundle of replacements defined in
 * data/&lt;modid&gt;/moogs_structures/replace_vanilla.json. Runs at mod init.
 */
public final class ReplaceVanillaManager {
    private ReplaceVanillaManager() {}

    public record Replacement(String modid, String presetId, boolean defaultEnabled,
                              Identifier vanillaStructure, Identifier replacementStructure) {}

    /** A preset as shown in the in-game config screen. */
    public record PresetInfo(String modid, String presetId, String name, String description, boolean defaultEnabled) {}

    // modid -> presetId -> default, handed to MslConfig so the file lists every known preset.
    private static final Map<String, Map<String, Boolean>> PRESET_DEFAULTS = new TreeMap<>();
    // Ordered list of every discovered preset, for the config screen.
    private static final List<PresetInfo> PRESETS = new ArrayList<>();
    // "modid/vanilla_key" -> owning presetId, so the loot processor can resolve enablement.
    private static final Map<String, Replacement> BY_VANILLA_KEY = new HashMap<>();
    // vanilla structure id -> replacement, for the disable mixin and locate command.
    private static final Map<Identifier, Replacement> BY_VANILLA_STRUCTURE = new HashMap<>();

    /** Scans mod manifests once at mod init, then does an initial config read. */
    public static void init() {
        PRESET_DEFAULTS.clear();
        PRESETS.clear();
        BY_VANILLA_KEY.clear();
        BY_VANILLA_STRUCTURE.clear();

        Map<String, String> manifests = PlatformConfig.INSTANCE.getOptionalPackManifests();
        for (Map.Entry<String, String> entry : manifests.entrySet()) {
            try {
                parseManifest(entry.getKey(), entry.getValue());
            } catch (RuntimeException e) {
                MoogsStructuresCommon.LOGGER.warn("Moogs Structures: could not parse replace_vanilla.json for '{}' ({}: {})",
                        entry.getKey(), e.getClass().getSimpleName(), e.getMessage());
            }
        }

        reloadConfig();
    }

    /** Re-reads the config file. Called on every world load so edits apply without a full restart. */
    public static void reloadConfig() {
        MslConfig.get().loadAndSync(PlatformConfig.INSTANCE.getConfigDir(), PRESET_DEFAULTS);
    }

    private static void parseManifest(String modid, String json) {
        JsonElement rootEl = JsonParser.parseString(json);
        if (!rootEl.isJsonObject()) return;
        JsonObject root = rootEl.getAsJsonObject();
        if (!root.has("presets") || !root.get("presets").isJsonArray()) return;

        for (JsonElement presetEl : root.getAsJsonArray("presets")) {
            if (!presetEl.isJsonObject()) continue;
            JsonObject preset = presetEl.getAsJsonObject();
            String presetId = preset.has("id") ? preset.get("id").getAsString() : null;
            if (presetId == null) {
                MoogsStructuresCommon.LOGGER.warn("Moogs Structures: skipping preset with no id in '{}' replace_vanilla.json", modid);
                continue;
            }
            boolean defaultEnabled = preset.has("default_enabled") && preset.get("default_enabled").getAsBoolean();
            PRESET_DEFAULTS.computeIfAbsent(modid, k -> new TreeMap<>()).put(presetId, defaultEnabled);

            String name = preset.has("name") ? preset.get("name").getAsString() : presetId;
            String description = preset.has("description") ? preset.get("description").getAsString() : "";
            PRESETS.add(new PresetInfo(modid, presetId, name, description, defaultEnabled));

            if (!preset.has("replacements") || !preset.get("replacements").isJsonArray()) continue;
            for (JsonElement el : preset.getAsJsonArray("replacements")) {
                if (!el.isJsonObject()) continue;
                JsonObject obj = el.getAsJsonObject();
                String vanillaKey = obj.has("vanilla_key") ? obj.get("vanilla_key").getAsString() : null;
                Identifier vanillaStructure = obj.has("vanilla_structure") ? Identifier.tryParse(obj.get("vanilla_structure").getAsString()) : null;
                Identifier replacementStructure = obj.has("replacement_structure") ? Identifier.tryParse(obj.get("replacement_structure").getAsString()) : null;
                if (vanillaKey == null || vanillaStructure == null) {
                    MoogsStructuresCommon.LOGGER.warn("Moogs Structures: skipping malformed replacement in preset '{}' of '{}'", presetId, modid);
                    continue;
                }
                Replacement r = new Replacement(modid, presetId, defaultEnabled, vanillaStructure, replacementStructure);
                BY_VANILLA_KEY.put(modid + "/" + vanillaKey, r);
                BY_VANILLA_STRUCTURE.put(vanillaStructure, r);
            }
        }
    }

    public static boolean isEnabled(String modid, String vanillaKey) {
        Replacement r = BY_VANILLA_KEY.get(modid + "/" + vanillaKey);
        return r != null && MslConfig.get().presetEnabled(r.modid(), r.presetId(), r.defaultEnabled());
    }

    /** True when a replacement targeting this vanilla structure is currently enabled. */
    public static boolean shouldCancelVanilla(Identifier vanillaStructure) {
        return getActiveReplacement(vanillaStructure).isPresent();
    }

    /** The replacement for this vanilla structure, only if its preset is currently enabled. */
    public static Optional<Replacement> getActiveReplacement(Identifier vanillaStructure) {
        Replacement r = BY_VANILLA_STRUCTURE.get(vanillaStructure);
        if (r != null && MslConfig.get().presetEnabled(r.modid(), r.presetId(), r.defaultEnabled())) {
            return Optional.of(r);
        }
        return Optional.empty();
    }

    /** True when any replacement binding exists, letting the mixin skip work entirely when unused. */
    public static boolean hasAnyBindings() {
        return !BY_VANILLA_STRUCTURE.isEmpty();
    }

    /** Every discovered preset, for the in-game config screen. */
    public static List<PresetInfo> getPresets() {
        return List.copyOf(PRESETS);
    }

    public static boolean isPresetEnabled(PresetInfo preset) {
        return MslConfig.get().presetEnabled(preset.modid(), preset.presetId(), preset.defaultEnabled());
    }

    public static void setPresetEnabled(PresetInfo preset, boolean value) {
        MslConfig.get().setAndSave(preset.modid(), preset.presetId(), value);
    }
}
