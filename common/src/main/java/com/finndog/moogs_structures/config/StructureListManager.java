package com.finndog.moogs_structures.config;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Aggregates the per-mod "structures" manifest blocks so the in-game config screen can list every
 * structure grouped by mod, each with a Disable toggle, an optional spacing slider, and an online
 * preview link. A mod declares, in data/&lt;modid&gt;/moogs_structures/replace_vanilla.json:
 * <pre>{@code
 * "structures": {
 *   "mod_name": "Moog's Temples Reimagined",
 *   "preview_url_template": "https://previews.moogsmods.com/temples-reimagined/1.21.1/{structure}",
 *   "entries": [
 *     { "structure": "mtr:desert_temple", "name": "Desert Temple", "spacing_key": "mtr:desert_temple" },
 *     { "structure": "mtr:stronghold", "name": "Stronghold" }
 *   ]
 * }
 * }</pre>
 * The structure id drives disable + preview; spacing_key (when present) drives the spacing slider and
 * must match the structure_set placement's spacing_key. These are distinct identifiers - a
 * structure_set id need not equal the structure id it contains - so both are declared explicitly.
 */
public final class StructureListManager {
    private StructureListManager() {}

    public record StructureEntry(String structureId, String name, String previewUrl, String spacingKey) {}
    public record ModGroup(String modid, String modName, List<StructureEntry> structures) {}

    private static final List<ModGroup> GROUPS = new ArrayList<>();

    /** Baseline scan of mod-jar manifests at mod init (available before any world loads). */
    public static void init() {
        populate(PlatformConfig.getOptionalPackManifests());
    }

    /**
     * Re-scan from the server data resource manager on datapack (re)load, so structures declared by
     * datapacks - not just mod jars - show up in the config screen. Called by the reload listener.
     */
    public static void reload(Map<String, String> manifests) {
        populate(manifests);
    }

    private static void populate(Map<String, String> manifests) {
        GROUPS.clear();
        for (Map.Entry<String, String> entry : manifests.entrySet()) {
            try {
                parseManifest(entry.getKey(), entry.getValue());
            } catch (RuntimeException e) {
                MoogsStructuresCommon.LOGGER.warn("Moogs Structures: could not parse structures block for '{}' ({}: {})",
                        entry.getKey(), e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    private static void parseManifest(String modid, String json) {
        JsonElement rootEl = JsonParser.parseString(json);
        if (!rootEl.isJsonObject()) return;
        JsonObject root = rootEl.getAsJsonObject();
        if (!root.has("structures") || !root.get("structures").isJsonObject()) return;
        JsonObject structures = root.getAsJsonObject("structures");
        if (!structures.has("entries") || !structures.get("entries").isJsonArray()) return;

        String modName = structures.has("mod_name") ? structures.get("mod_name").getAsString() : modid;
        String template = structures.has("preview_url_template") ? structures.get("preview_url_template").getAsString() : null;

        List<StructureEntry> entries = new ArrayList<>();
        for (JsonElement el : structures.getAsJsonArray("entries")) {
            if (!el.isJsonObject()) continue;
            JsonObject obj = el.getAsJsonObject();
            if (!obj.has("structure")) continue;
            String structureId = obj.get("structure").getAsString();
            String name = obj.has("name") ? obj.get("name").getAsString() : structureId;
            String spacingKey = obj.has("spacing_key") ? obj.get("spacing_key").getAsString() : null;
            String previewUrl = obj.has("preview_url") ? obj.get("preview_url").getAsString() : buildUrl(template, structureId);
            entries.add(new StructureEntry(structureId, name, previewUrl, spacingKey));
        }
        if (!entries.isEmpty()) {
            GROUPS.add(new ModGroup(modid, modName, entries));
        }
    }

    private static String buildUrl(String template, String structureId) {
        if (template == null) return null;
        int colon = structureId.indexOf(':');
        String path = colon >= 0 ? structureId.substring(colon + 1) : structureId;
        return template.replace("{structure}", path);
    }

    /** Structures grouped by mod, for the config screen. */
    public static List<ModGroup> getGroups() {
        return List.copyOf(GROUPS);
    }
}
