package com.finndog.moogs_structures.config.forge;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class PlatformConfigImpl {

    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static Map<String, String> getOptionalPackManifests() {
        Map<String, String> out = new LinkedHashMap<>();
        for (IModInfo mod : ModList.get().getMods()) {
            String modid = mod.getModId();
            Path path = mod.getOwningFile().getFile().findResource("data", modid, "moogs_structures", "replace_vanilla.json");
            if (path == null || !Files.exists(path)) continue;
            try {
                out.put(modid, Files.readString(path));
            } catch (Exception e) {
                MoogsStructuresCommon.LOGGER.warn("Moogs Structures: could not read optional_packs.json for '{}' ({})", modid, e.getMessage());
            }
        }
        return out;
    }
}
