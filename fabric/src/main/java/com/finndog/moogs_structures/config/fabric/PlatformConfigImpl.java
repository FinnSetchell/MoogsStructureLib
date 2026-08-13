package com.finndog.moogs_structures.config.fabric;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class PlatformConfigImpl {

    public static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static Map<String, String> getOptionalPackManifests() {
        Map<String, String> out = new LinkedHashMap<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            String modid = mod.getMetadata().getId();
            Optional<Path> path = mod.findPath("data/" + modid + "/moogs_structures/replace_vanilla.json");
            if (path.isEmpty()) continue;
            try {
                out.put(modid, Files.readString(path.get()));
            } catch (Exception e) {
                MoogsStructuresCommon.LOGGER.warn("Moogs Structures: could not read optional_packs.json for '{}' ({})", modid, e.getMessage());
            }
        }
        return out;
    }
}
