package com.finndog.moogs_structures.config.fabric;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.config.PlatformConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class PlatformConfigImpl implements PlatformConfig {

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public Map<String, String> getOptionalPackManifests() {
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

    @Override
    public Map<String, String> getStructureSetJsons(String modid) {
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

    @Override
    public List<String> getAllModIds() {
        List<String> out = new ArrayList<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            out.add(mod.getMetadata().getId());
        }
        return out;
    }

    @Override
    public String getModName(String modid) {
        return FabricLoader.getInstance().getModContainer(modid)
                .map(m -> m.getMetadata().getName())
                .orElse(null);
    }
}
