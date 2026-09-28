package com.finndog.moogs_structures.fabric.client;

import com.finndog.moogs_structures.client.ClothRequiredScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        boolean clothPresent = FabricLoader.getInstance().isModLoaded("cloth-config")
                || FabricLoader.getInstance().isModLoaded("cloth-config2");
        if (!clothPresent) {
            return ClothRequiredScreen::new;
        }
        return MoogsStructuresConfigScreen::create;
    }
}
