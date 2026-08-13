package com.finndog.moogs_structures.forge.client;

import com.finndog.moogs_structures.config.ReplaceVanillaManager;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Builds the Cloth Config screen from the presets discovered across all mods.
 * Only loaded on the client when Cloth Config is present (see {@link MoogsStructuresForgeClient}).
 */
public final class MoogsStructuresConfigScreenForge {
    private MoogsStructuresConfigScreenForge() {}

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Moog's Structures"));
        ConfigEntryBuilder eb = builder.entryBuilder();
        ConfigCategory category = builder.getOrCreateCategory(Component.literal("Replace Vanilla Structures"));

        for (ReplaceVanillaManager.PresetInfo preset : ReplaceVanillaManager.getPresets()) {
            String tooltip = preset.description().isEmpty() ? preset.modid() : preset.description();
            category.addEntry(eb.startBooleanToggle(Component.literal(preset.name()), ReplaceVanillaManager.isPresetEnabled(preset))
                    .setDefaultValue(preset.defaultEnabled())
                    .setTooltip(Component.literal(tooltip))
                    .setSaveConsumer(value -> ReplaceVanillaManager.setPresetEnabled(preset, value))
                    .build());
        }
        return builder.build();
    }
}
