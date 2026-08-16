package com.finndog.moogs_structures.config;

import com.finndog.moogs_structures.platform.Services;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Loader-abstraction SPI for reading loader metadata and bundled data files. Each loader ships a
 * {@code config.<loader>.PlatformConfigImpl} registered via META-INF/services, loaded here through the
 * same ServiceLoader mechanism the rest of the mod uses (see {@link Services}).
 */
public interface PlatformConfig {

    PlatformConfig INSTANCE = Services.load(PlatformConfig.class);

    Path getConfigDir();

    /** modid -&gt; raw contents of that mod's data/&lt;modid&gt;/moogs_structures/optional_packs.json, for mods that ship one. */
    Map<String, String> getOptionalPackManifests();

    /**
     * structure_set id ("modid:name", including any subfolders) -&gt; raw JSON, for every
     * data/&lt;modid&gt;/worldgen/structure_set/*.json bundled in that mod's jar. Used to auto-derive the
     * config screen's structure list so a mod need not restate its structures in the manifest.
     */
    Map<String, String> getStructureSetJsons(String modid);

    /** Every loaded mod id, so the config screen can auto-discover which ones ship MSL structures. */
    List<String> getAllModIds();

    /** A mod's human-readable name from loader metadata, for the config screen group header. Null if unknown. */
    String getModName(String modid);
}
