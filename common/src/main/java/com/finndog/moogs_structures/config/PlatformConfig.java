package com.finndog.moogs_structures.config;

import dev.architectury.injectables.annotations.ExpectPlatform;
import org.apache.commons.lang3.NotImplementedException;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class PlatformConfig {

    @ExpectPlatform
    public static Path getConfigDir() {
        throw new NotImplementedException();
    }

    /** modid -&gt; raw contents of that mod's data/&lt;modid&gt;/moogs_structures/optional_packs.json, for mods that ship one. */
    @ExpectPlatform
    public static Map<String, String> getOptionalPackManifests() {
        throw new NotImplementedException();
    }

    /**
     * structure_set id ("modid:name", including any subfolders) -&gt; raw JSON, for every
     * data/&lt;modid&gt;/worldgen/structure_set/*.json bundled in that mod's jar. Used to auto-derive the
     * config screen's structure list so a mod need not restate its structures in the manifest.
     */
    @ExpectPlatform
    public static Map<String, String> getStructureSetJsons(String modid) {
        throw new NotImplementedException();
    }

    /** Every loaded mod id, so the config screen can auto-discover which ones ship MSL structures. */
    @ExpectPlatform
    public static List<String> getAllModIds() {
        throw new NotImplementedException();
    }

    /** A mod's human-readable name from loader metadata, for the config screen group header. Null if unknown. */
    @ExpectPlatform
    public static String getModName(String modid) {
        throw new NotImplementedException();
    }
}
