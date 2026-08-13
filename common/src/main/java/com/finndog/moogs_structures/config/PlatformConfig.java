package com.finndog.moogs_structures.config;

import dev.architectury.injectables.annotations.ExpectPlatform;
import org.apache.commons.lang3.NotImplementedException;

import java.nio.file.Path;
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
}
