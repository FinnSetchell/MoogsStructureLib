package com.finndog.moogs_structures.forge.client;

import com.finndog.moogs_structures.client.ClothRequiredScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Client-only. Registers the config-screen extension point so the mod list shows a config
 * button. Loaded only via DistExecutor on the client. The button always registers: with Cloth
 * Config present it opens the config screen, without it a short screen telling the player Cloth
 * Config is required (the screen classes are compiled against Cloth but Cloth is not required).
 */
public final class MoogsStructuresForgeClient {
    private MoogsStructuresForgeClient() {}

    public static void registerConfigScreen() {
        boolean clothPresent = ModList.get().isLoaded("cloth_config");
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> clothPresent
                                ? MoogsStructuresConfigScreenForge.create(parent)
                                : new ClothRequiredScreen(parent)));
    }
}
