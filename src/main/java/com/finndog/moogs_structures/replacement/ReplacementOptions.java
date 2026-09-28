package com.finndog.moogs_structures.replacement;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.google.gson.JsonObject;

import java.util.Set;

/**
 * The optional per-replacement "options" object from a mod's replace_vanilla.json. Every flag
 * defaults to true, so a manifest that omits the block gets the full fidelity treatment.
 */
public record ReplacementOptions(boolean aliasLookups, boolean inheritSpawnOverrides, boolean redirectLocate, boolean mirrorTags) {
    public static final ReplacementOptions DEFAULTS = new ReplacementOptions(true, true, true, true);

    private static final String ALIAS_LOOKUPS = "alias_lookups";
    private static final String INHERIT_SPAWN_OVERRIDES = "inherit_spawn_overrides";
    private static final String REDIRECT_LOCATE = "redirect_locate";
    private static final String MIRROR_TAGS = "mirror_tags";
    private static final Set<String> KNOWN_KEYS = Set.of(ALIAS_LOOKUPS, INHERIT_SPAWN_OVERRIDES, REDIRECT_LOCATE, MIRROR_TAGS);

    public static ReplacementOptions parse(JsonObject options, String modid, String presetId) {
        if (options == null) return DEFAULTS;

        for (String key : options.keySet()) {
            if (!KNOWN_KEYS.contains(key)) {
                MoogsStructuresCommon.LOGGER.warn("Moogs Structures: unknown replacement option '{}' in preset '{}' of '{}', ignoring it",
                        key, presetId, modid);
            }
        }

        return new ReplacementOptions(
                flag(options, ALIAS_LOOKUPS),
                flag(options, INHERIT_SPAWN_OVERRIDES),
                flag(options, REDIRECT_LOCATE),
                flag(options, MIRROR_TAGS));
    }

    private static boolean flag(JsonObject options, String key) {
        if (!options.has(key) || !options.get(key).isJsonPrimitive()) return true;
        return options.get(key).getAsBoolean();
    }
}
