package com.finndog.moogs_structures.config;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * On server-data reload (world load or /reload) re-scans every data pack - mod jars AND datapacks -
 * for data/&lt;namespace&gt;/moogs_structures/replace_vanilla.json and rebuilds the config screen's
 * structure list. This lets datapacks declare structures for the Disable / spacing / preview screen,
 * not just bundled mods. The mod-init scan remains the pre-world baseline.
 */
public class StructureManifestReloadListener implements PreparableReloadListener {
    private static final String DIR = "moogs_structures";
    private static final String FILE = "replace_vanilla.json";
    private static final String SET_DIR = "worldgen/structure_set";

    private record Prepared(Map<String, String> manifests, Map<String, Map<String, String>> setJsons) {}

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager,
                                          ProfilerFiller prepProfiler, ProfilerFiller applyProfiler,
                                          Executor prepExecutor, Executor applyExecutor) {
        return CompletableFuture.supplyAsync(() -> {
                    Map<String, String> manifests = readManifests(manager);
                    Map<String, Map<String, String>> setJsons = readStructureSets(manager);
                    return new Prepared(manifests, setJsons);
                }, prepExecutor)
                .thenCompose(barrier::wait)
                .thenAcceptAsync(prepared -> {
                    StructureListManager.reload(prepared.manifests(), prepared.setJsons());
                    // Re-read config on datapack reload so /reload applies preset/disable/spacing
                    // changes to newly generated chunks without a full world reload.
                    ReplaceVanillaManager.reloadConfig();
                }, applyExecutor);
    }

    private static Map<String, String> readManifests(ResourceManager manager) {
        Map<String, String> out = new HashMap<>();
        for (Map.Entry<ResourceLocation, Resource> e :
                manager.listResources(DIR, loc -> loc.getPath().endsWith(FILE)).entrySet()) {
            try (InputStream is = e.getValue().open()) {
                out.put(e.getKey().getNamespace(), new String(is.readAllBytes(), StandardCharsets.UTF_8));
            } catch (IOException ex) {
                MoogsStructuresCommon.LOGGER.warn("Moogs Structures: could not read {} ({})", e.getKey(), ex.getMessage());
            }
        }
        return out;
    }

    /**
     * namespace -&gt; (set id "ns:name" -&gt; raw JSON) for every structure_set placed by an MSL placement
     * type, across all data packs. This is what lets any mod's MSL structures appear in the config
     * screen with no opt-in file. A cheap substring guard skips the bulk of unrelated vanilla/mod sets
     * before the authoritative placement-type check.
     */
    private static Map<String, Map<String, String>> readStructureSets(ResourceManager manager) {
        Map<String, Map<String, String>> out = new HashMap<>();
        for (Map.Entry<ResourceLocation, Resource> e :
                manager.listResources(SET_DIR, loc -> loc.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation loc = e.getKey();
            String ns = loc.getNamespace();
            String path = loc.getPath(); // worldgen/structure_set/<name>.json
            String name = path.substring(SET_DIR.length() + 1, path.length() - ".json".length());
            try (InputStream is = e.getValue().open()) {
                String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                if (!json.contains("moogs_structures:")) continue;
                if (!StructureListManager.isMslStructureSet(json)) continue;
                out.computeIfAbsent(ns, k -> new HashMap<>()).put(ns + ":" + name, json);
            } catch (IOException ex) {
                MoogsStructuresCommon.LOGGER.warn("Moogs Structures: could not read structure_set {} ({})", loc, ex.getMessage());
            }
        }
        return out;
    }
}
