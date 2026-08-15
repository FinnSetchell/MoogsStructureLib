package com.finndog.moogs_structures.neoforge.client;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-only mod entrypoint. Registers the config-screen extension point so the mod list shows a
 * config button. Loaded only on the client (dist = CLIENT), and only registers the screen when
 * Cloth Config is present - the screen classes are compiled against Cloth but Cloth is not required.
 */
@Mod(value = MoogsStructuresCommon.MODID, dist = Dist.CLIENT)
public class MoogsStructuresNeoforgeClient {

    public MoogsStructuresNeoforgeClient(ModContainer modContainer) {
        if (!ModList.get().isLoaded("cloth_config")) return;
        modContainer.registerExtensionPoint(
                IConfigScreenFactory.class,
                (minecraft, parent) -> MoogsStructuresConfigScreenNeoforge.create(parent));
    }
}
