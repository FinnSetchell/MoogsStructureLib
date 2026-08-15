package com.finndog.moogs_structures.config;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Builds the config screen's Structures list. Every mod that ships structure_sets using an MSL
 * placement type ({@code advanced_random_spread} / {@code conditional_concentric_rings}) is listed
 * automatically - no opt-in file needed. Each structure gets a Disable toggle and, for
 * {@code advanced_random_spread} sets, a spacing slider; both are keyed by the set id and enforced by
 * the stamped placement, so they work with no MSL-specific fields in the set JSON. The group header is
 * the mod's loader display name; the display name per row is the title-cased structure path.
 *
 * <p>A mod's {@code "structures"} block in data/&lt;modid&gt;/moogs_structures/replace_vanilla.json is
 * optional and only <em>enriches</em> its group:
 * <pre>{@code
 * "structures": { "mod_slug": "temples-reimagined" }
 * }</pre>
 * {@code mod_slug} enables the online preview link (MSL cannot guess it, so without it a row has no
 * preview); {@code mod_name} overrides the header; an explicit {@code entries} array overrides the
 * derived rows. Preview URLs resolve to
 * {@code https://previews.moogsmods.com/<mod_slug>/<mc_version>/<structure_path>} with the running
 * game version, or a full {@code preview_url_template} ({@code {structure}} / {@code {mc_version}}
 * tokens) overrides the host/layout.
 */
public final class StructureListManager {
    private StructureListManager() {}

    public record StructureEntry(String structureId, String name, String previewUrl, String spacingKey) {}
    public record ModGroup(String modid, String modName, List<StructureEntry> structures) {}

    private static final List<ModGroup> GROUPS = new ArrayList<>();

    private static final String PLACEMENT_SPREAD = "moogs_structures:advanced_random_spread";
    private static final String PLACEMENT_RINGS = "moogs_structures:conditional_concentric_rings";

    /** Baseline scan at mod init: every loaded mod's bundled MSL structure_sets (before any world loads). */
    public static void init() {
        Map<String, Map<String, String>> setsByMod = new TreeMap<>();
        for (String modid : PlatformConfig.INSTANCE.getAllModIds()) {
            Map<String, String> msl = mslSetsOnly(PlatformConfig.INSTANCE.getStructureSetJsons(modid));
            if (!msl.isEmpty()) setsByMod.put(modid, msl);
        }
        populate(PlatformConfig.INSTANCE.getOptionalPackManifests(), setsByMod);
    }

    /**
     * Re-scan on datapack (re)load. The reload listener passes every namespace's MSL structure_sets it
     * found in the resource manager, so datapack-added structures appear alongside bundled ones.
     */
    public static void reload(Map<String, String> manifests, Map<String, Map<String, String>> setsByNamespace) {
        populate(manifests, setsByNamespace);
    }

    private static void populate(Map<String, String> manifests, Map<String, Map<String, String>> setsByMod) {
        GROUPS.clear();
        Map<String, JsonObject> markers = parseMarkers(manifests);

        // Every mod with MSL structure_sets, plus any that hand-authored explicit entries.
        Set<String> modids = new TreeSet<>(setsByMod.keySet());
        markers.forEach((modid, block) -> { if (block.has("entries")) modids.add(modid); });

        for (String modid : modids) {
            try {
                JsonObject marker = markers.get(modid);
                String template = marker != null ? resolveTemplate(marker) : null;
                List<StructureEntry> entries = marker != null && marker.has("entries") && marker.get("entries").isJsonArray()
                        ? parseExplicitEntries(marker.getAsJsonArray("entries"), template)
                        : deriveEntries(setsByMod.getOrDefault(modid, Map.of()), template);
                if (entries.isEmpty()) continue;
                String modName = marker != null && marker.has("mod_name")
                        ? marker.get("mod_name").getAsString()
                        : orElse(PlatformConfig.INSTANCE.getModName(modid), modid);
                GROUPS.add(new ModGroup(modid, modName, entries));
            } catch (RuntimeException e) {
                MoogsStructuresCommon.LOGGER.warn("Moogs Structures: could not build structure list for '{}' ({}: {})",
                        modid, e.getClass().getSimpleName(), e.getMessage());
            }
        }
        GROUPS.sort(Comparator.comparing(ModGroup::modName));
    }

    /** modid -&gt; its "structures" manifest block, for the optional enrichment (slug / name / entries). */
    private static Map<String, JsonObject> parseMarkers(Map<String, String> manifests) {
        Map<String, JsonObject> out = new HashMap<>();
        for (Map.Entry<String, String> e : manifests.entrySet()) {
            try {
                JsonElement root = JsonParser.parseString(e.getValue());
                if (root.isJsonObject() && root.getAsJsonObject().has("structures")
                        && root.getAsJsonObject().get("structures").isJsonObject()) {
                    out.put(e.getKey(), root.getAsJsonObject().getAsJsonObject("structures"));
                }
            } catch (RuntimeException ignored) {
            }
        }
        return out;
    }

    private static Map<String, String> mslSetsOnly(Map<String, String> sets) {
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : sets.entrySet()) {
            if (isMslStructureSet(e.getValue())) out.put(e.getKey(), e.getValue());
        }
        return out;
    }

    /** True when a structure_set JSON is placed by an MSL placement type (spread or rings). */
    public static boolean isMslStructureSet(String rawJson) {
        try {
            JsonObject set = JsonParser.parseString(rawJson).getAsJsonObject();
            if (!set.has("placement") || !set.get("placement").isJsonObject()) return false;
            String type = optString(set.getAsJsonObject("placement"), "type");
            return PLACEMENT_SPREAD.equals(type) || PLACEMENT_RINGS.equals(type);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static String orElse(String value, String fallback) {
        return value != null ? value : fallback;
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
                boolean spread = PLACEMENT_SPREAD.equals(type);
                if (!spread && !PLACEMENT_RINGS.equals(type)) continue;   // non-MSL placement: not ours to list
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
