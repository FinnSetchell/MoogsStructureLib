package com.finndog.moogs_structures.neoforge.client;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.client.ClothRequiredScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-only mod entrypoint. Registers the config-screen extension point so the mod list shows a
 * config button. Loaded only on the client (dist = CLIENT). The button always registers: with Cloth
 * Config present it opens the config screen, without it a short screen telling the player Cloth
 * Config is required (the screen classes are compiled against Cloth but Cloth is not required).
 */
@Mod(value = MoogsStructuresCommon.MODID, dist = Dist.CLIENT)
public class MoogsStructuresNeoforgeClient {

    public MoogsStructuresNeoforgeClient(ModContainer modContainer) {
        modContainer.registerExtensionPoint(
                IConfigScreenFactory.class,
                (minecraft, parent) -> ModList.get().isLoaded("cloth_config")
                        ? MoogsStructuresConfigScreenNeoforge.create(parent)
                        : new ClothRequiredScreen(parent));
    }
}
