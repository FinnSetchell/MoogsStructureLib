package com.finndog.moogs_structures.config;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Aggregates the per-mod "structures" manifest blocks so the in-game config screen can list every
 * structure grouped by mod, each with a Disable toggle, an optional spacing slider, and an online
 * preview link. A mod declares, in data/&lt;modid&gt;/moogs_structures/replace_vanilla.json:
 * <pre>{@code
 * "structures": { "mod_slug": "temples-reimagined" }
 * }</pre>
 * That one line is usually enough: MSL auto-derives one config row per structure_set in the mod's jar
 * (see {@link #deriveEntries}). The set's contents give the structure id and preview path; the display
 * name is the title-cased path; the spacing slider is offered only for
 * {@code moogs_structures:advanced_random_spread} sets (keyed by the set id) and suppressed for other
 * placement types (e.g. concentric rings). Disable is keyed by the set id and enforced by the stamped
 * placement, so it works for every set including multi-structure ones.
 *
 * <p>A mod may instead hand-author an explicit {@code entries} array to override the derived rows:
 * <pre>{@code
 * "structures": {
 *   "mod_slug": "temples-reimagined",
 *   "entries": [ { "structure": "mtr:desert_temple", "name": "Desert Temple", "spacing_key": "mtr:desert_temple" } ]
 * }
 * }</pre>
 *
 * <p>Preview URLs are built from {@code mod_slug} as
 * {@code https://previews.moogsmods.com/<mod_slug>/<mc_version>/<structure_path>}, where
 * {@code <mc_version>} is the running game version - so the link tracks whatever version the pack is
 * played on. A mod may instead supply a full {@code preview_url_template} (with {@code {structure}}
 * and optional {@code {mc_version}} tokens) to override the default host/layout.
 */
public final class StructureListManager {
    private StructureListManager() {}

    public record StructureEntry(String structureId, String name, String previewUrl, String spacingKey) {}
    public record ModGroup(String modid, String modName, List<StructureEntry> structures) {}

    private static final List<ModGroup> GROUPS = new ArrayList<>();

    /** modid -&gt; the raw JSON of every structure_set that mod bundles, used to auto-derive rows. */
    public interface SetJsonProvider extends Function<String, Map<String, String>> {}

    /** Baseline scan of mod-jar manifests at mod init (available before any world loads). */
    public static void init() {
        populate(PlatformConfig.getOptionalPackManifests(), PlatformConfig::getStructureSetJsons);
    }

    /**
     * Re-scan from the server data resource manager on datapack (re)load, so structures declared by
     * datapacks - not just mod jars - show up in the config screen. Called by the reload listener,
     * which supplies the structure_set JSON per namespace it read from the resource manager.
     */
    public static void reload(Map<String, String> manifests, Map<String, Map<String, String>> setJsons) {
        populate(manifests, modid -> setJsons.getOrDefault(modid, Map.of()));
    }

    private static void populate(Map<String, String> manifests, SetJsonProvider setProvider) {
        GROUPS.clear();
        for (Map.Entry<String, String> entry : manifests.entrySet()) {
            try {
                parseManifest(entry.getKey(), entry.getValue(), setProvider);
            } catch (RuntimeException e) {
                MoogsStructuresCommon.LOGGER.warn("Moogs Structures: could not parse structures block for '{}' ({}: {})",
                        entry.getKey(), e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    private static void parseManifest(String modid, String json, SetJsonProvider setProvider) {
        JsonElement rootEl = JsonParser.parseString(json);
        if (!rootEl.isJsonObject()) return;
        JsonObject root = rootEl.getAsJsonObject();
        if (!root.has("structures") || !root.get("structures").isJsonObject()) return;
        JsonObject structures = root.getAsJsonObject("structures");

        String modName = structures.has("mod_name") ? structures.get("mod_name").getAsString() : modid;
        String template = resolveTemplate(structures);

        List<StructureEntry> entries = structures.has("entries") && structures.get("entries").isJsonArray()
                ? parseExplicitEntries(structures.getAsJsonArray("entries"), template)
                : deriveEntries(setProvider.apply(modid), template);

        if (!entries.isEmpty()) {
            GROUPS.add(new ModGroup(modid, modName, entries));
        }
    }

    private static List<StructureEntry> parseExplicitEntries(com.google.gson.JsonArray arr, String template) {
        List<StructureEntry> entries = new ArrayList<>();
        for (JsonElement el : arr) {
            if (!el.isJsonObject()) continue;
            JsonObject obj = el.getAsJsonObject();
            if (!obj.has("structure")) continue;
            String structureId = obj.get("structure").getAsString();
            String name = obj.has("name") ? obj.get("name").getAsString() : structureId;
            String spacingKey = obj.has("spacing_key") ? obj.get("spacing_key").getAsString() : null;
            String previewUrl = obj.has("preview_url") ? obj.get("preview_url").getAsString() : buildUrl(template, structureId);
            entries.add(new StructureEntry(structureId, name, previewUrl, spacingKey));
        }
        return entries;
    }

    /**
     * One row per structure_set. The row's id (disable + spacing key) is the set id; the spacing slider
     * is offered only for advanced_random_spread sets; the preview points at the single contained
     * structure's render (omitted for multi-structure sets, which have no single render); the name is
     * the title-cased path of the representative structure (or the set, for multi-structure sets).
     */
    private static List<StructureEntry> deriveEntries(Map<String, String> setJsons, String template) {
        List<StructureEntry> entries = new ArrayList<>();
        for (Map.Entry<String, String> e : setJsons.entrySet()) {
            String setId = e.getKey();
            try {
                JsonObject set = JsonParser.parseString(e.getValue()).getAsJsonObject();
                String type = set.has("placement") && set.get("placement").isJsonObject()
                        ? optString(set.getAsJsonObject("placement"), "type") : null;
                List<String> structs = new ArrayList<>();
                if (set.has("structures") && set.get("structures").isJsonArray()) {
                    for (JsonElement se : set.getAsJsonArray("structures")) {
                        if (se.isJsonObject() && se.getAsJsonObject().has("structure")) {
                            structs.add(se.getAsJsonObject().get("structure").getAsString());
                        }
                    }
                }
                boolean spread = "moogs_structures:advanced_random_spread".equals(type);
                boolean single = structs.size() == 1;
                String spacingKey = spread ? setId : null;
                String name = titleCase(single ? pathOf(structs.get(0)) : pathOf(setId));
                String previewUrl = single ? buildUrl(template, structs.get(0)) : null;
                entries.add(new StructureEntry(setId, name, previewUrl, spacingKey));
            } catch (RuntimeException ex) {
                MoogsStructuresCommon.LOGGER.warn("Moogs Structures: could not derive a config row from structure_set '{}' ({}: {})",
                        setId, ex.getClass().getSimpleName(), ex.getMessage());
            }
        }
        entries.sort(Comparator.comparing(StructureEntry::name));
        return entries;
    }

    private static String optString(JsonObject obj, String key) {
        return obj.has(key) && obj.get(key).isJsonPrimitive() ? obj.get(key).getAsString() : null;
    }

    private static String pathOf(String id) {
        int colon = id.indexOf(':');
        return colon >= 0 ? id.substring(colon + 1) : id;
    }

    /** "prismarine_dog_statue" -&gt; "Prismarine Dog Statue"; uses the last path segment for subfolders. */
    private static String titleCase(String path) {
        int slash = path.lastIndexOf('/');
        String seg = slash >= 0 ? path.substring(slash + 1) : path;
        StringBuilder sb = new StringBuilder();
        for (String word : seg.replace('_', ' ').trim().split("\\s+")) {
            if (word.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }

    /**
     * An explicit {@code preview_url_template} wins; otherwise a {@code mod_slug} yields the standard
     * previews.moogsmods.com layout with a dynamic {@code {mc_version}}; otherwise no preview link.
     */
    private static String resolveTemplate(JsonObject structures) {
        if (structures.has("preview_url_template")) {
            return structures.get("preview_url_template").getAsString();
        }
        if (structures.has("mod_slug")) {
            return "https://previews.moogsmods.com/" + structures.get("mod_slug").getAsString() + "/{mc_version}/{structure}";
        }
        return null;
    }

    private static String buildUrl(String template, String structureId) {
        if (template == null) return null;
        int colon = structureId.indexOf(':');
        String path = colon >= 0 ? structureId.substring(colon + 1) : structureId;
        return template.replace("{structure}", path).replace("{mc_version}", mcVersion());
    }

    private static String cachedMcVersion;

    /** Running game version name (e.g. "1.20.1") for the {@code {mc_version}} preview-URL token. */
    private static String mcVersion() {
        if (cachedMcVersion == null) {
            try {
                cachedMcVersion = SharedConstants.getCurrentVersion().getName();
            } catch (Throwable t) {
                cachedMcVersion = "";
            }
        }
        return cachedMcVersion;
    }

    /** Structures grouped by mod, for the config screen. */
    public static List<ModGroup> getGroups() {
        return List.copyOf(GROUPS);
    }
}
