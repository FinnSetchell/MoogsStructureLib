package com.finndog.moogs_structures.forge.client;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Client-only. Registers the config-screen extension point so the mod list shows a config
 * button. Loaded only via DistExecutor on the client, and only registers when Cloth Config
 * is present.
 */
public final class MoogsStructuresForgeClient {
    private MoogsStructuresForgeClient() {}

    public static void registerConfigScreen() {
        if (!ModList.get().isLoaded("cloth_config")) return;
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> MoogsStructuresConfigScreenForge.create(parent)));
    }
}
