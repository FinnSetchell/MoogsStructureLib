//? if >=1.20.6 {
package com.finndog.moogs_structures.neoforge.client;

import com.finndog.moogs_structures.client.ClothRequiredScreen;
import com.finndog.moogs_structures.client.MoogsStructuresConfigScreen;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-only. Registers the config-screen extension point so the mod list shows a config button.
 * Called by {@link com.finndog.moogs_structures.neoforge.MoogsStructuresNeoforge} only when it runs
 * on the client, so a dedicated server never loads this class or the screen classes it names. The
 * button always registers: with Cloth Config present it opens the config screen, without it a short
 * screen telling the player Cloth Config is required (the screen classes are compiled against Cloth
 * but Cloth is not required).
 *
 * <p>Deliberately not a second {@code @Mod(dist = CLIENT)} class: NeoForge 1.20.5 (FML 3.0.18) has
 * no {@code dist} on {@code @Mod} and runs only one {@code @Mod} class per mod id, so a second one
 * can take the main class's place and crash dedicated servers.
 */
public final class MoogsStructuresNeoforgeClient {
    private MoogsStructuresNeoforgeClient() {}

    public static void registerConfigScreen(ModContainer modContainer) {
        modContainer.registerExtensionPoint(
                IConfigScreenFactory.class,
                (minecraft, parent) -> ModList.get().isLoaded("cloth_config")
                        ? MoogsStructuresConfigScreen.create(parent)
                        : new ClothRequiredScreen(parent));
    }
}

//?}