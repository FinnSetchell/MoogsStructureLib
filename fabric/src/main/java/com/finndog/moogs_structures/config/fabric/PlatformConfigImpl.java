package com.finndog.moogs_structures.config.fabric;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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

    public static Map<String, String> getStructureSetJsons(String modid) {
        Map<String, String> out = new LinkedHashMap<>();
        Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(modid);
        if (mod.isEmpty()) return out;
        Optional<Path> dir = mod.get().findPath("data/" + modid + "/worldgen/structure_set");
        if (dir.isEmpty()) return out;
        Path root = dir.get();
        try (Stream<Path> walk = Files.walk(root)) {
            walk.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                String rel = root.relativize(p).toString().replace('\\', '/');
                String name = rel.substring(0, rel.length() - ".json".length());
                try {
                    out.put(modid + ":" + name, Files.readString(p));
                } catch (Exception e) {
                    MoogsStructuresCommon.LOGGER.warn("Moogs Structures: could not read structure_set '{}:{}' ({})", modid, name, e.getMessage());
                }
            });
        } catch (Exception e) {
            MoogsStructuresCommon.LOGGER.warn("Moogs Structures: could not scan structure_sets for '{}' ({})", modid, e.getMessage());
        }
        return out;
    }
}
