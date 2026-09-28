package com.finndog.moogs_structures.config.neoforge;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.config.PlatformConfig;
import net.neoforged.fml.ModList;
//? if >=1.21.10 {
/*import net.neoforged.fml.jarcontents.JarContents;
*///?}
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.IModInfo;

//? if <1.21.10 {
import java.nio.file.Files;
//?} else {
/*import java.nio.charset.StandardCharsets;
*///?}
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
//? if <1.21.10 {
import java.util.stream.Stream;
//?}

public class PlatformConfigImpl implements PlatformConfig {

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public Map<String, String> getOptionalPackManifests() {
        Map<String, String> out = new LinkedHashMap<>();
        for (IModInfo mod : ModList.get().getMods()) {
            String modid = mod.getModId();
            //? if <1.21.10 {
            Path path = mod.getOwningFile().getFile().findResource("data", modid, "moogs_structures", "replace_vanilla.json");
            if (path == null || !Files.exists(path)) continue;
            //?} else {
            /*JarContents contents = mod.getOwningFile().getFile().getContents();
            String rel = "data/" + modid + "/moogs_structures/replace_vanilla.json";
            if (!contents.containsFile(rel)) continue;
            *///?}
            try {
                //? if <1.21.10 {
                out.put(modid, Files.readString(path));
                //?} else {
                /*out.put(modid, new String(contents.readFile(rel), StandardCharsets.UTF_8));
                *///?}
            } catch (Exception e) {
                MoogsStructuresCommon.LOGGER.warn("Moogs Structures: could not read optional_packs.json for '{}' ({})", modid, e.getMessage());
            }
        }
        return out;
    }

    @Override
    public Map<String, String> getStructureSetJsons(String modid) {
        Map<String, String> out = new LinkedHashMap<>();
        for (IModInfo mod : ModList.get().getMods()) {
            if (!mod.getModId().equals(modid)) continue;
            //? if <1.21.10 {
            Path dir = mod.getOwningFile().getFile().findResource("data", modid, "worldgen", "structure_set");
            if (dir == null || !Files.exists(dir)) return out;
            try (Stream<Path> walk = Files.walk(dir)) {
                walk.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                    String rel = dir.relativize(p).toString().replace('\\', '/');
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
            //?} else {
            /*JarContents contents = mod.getOwningFile().getFile().getContents();
            String prefix = "data/" + modid + "/worldgen/structure_set/";
            contents.visitContent(prefix, (relPath, resource) -> {
                if (!relPath.endsWith(".json")) return;
                String name = relPath;
                int idx = name.indexOf(prefix);
                if (idx >= 0) name = name.substring(idx + prefix.length());
                name = name.substring(0, name.length() - ".json".length());
                try {
                    out.put(modid + ":" + name, new String(resource.readAllBytes(), StandardCharsets.UTF_8));
                } catch (Exception e) {
                    MoogsStructuresCommon.LOGGER.warn("Moogs Structures: could not read structure_set '{}' ({})", relPath, e.getMessage());
                }
            });
            *///?}
            return out;
        }
        return out;
    }

    @Override
    public List<String> getAllModIds() {
        List<String> out = new ArrayList<>();
        for (IModInfo mod : ModList.get().getMods()) {
            out.add(mod.getModId());
        }
        return out;
    }

    @Override
    public String getModName(String modid) {
        return ModList.get().getModContainerById(modid)
                .map(c -> c.getModInfo().getDisplayName())
                .orElse(null);
    }
}
