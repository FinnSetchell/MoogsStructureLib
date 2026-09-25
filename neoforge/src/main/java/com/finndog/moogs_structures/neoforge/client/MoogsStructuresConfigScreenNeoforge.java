package com.finndog.moogs_structures.neoforge.client;

import com.finndog.moogs_structures.client.SpacingPreviewSliderEntry;
import com.finndog.moogs_structures.client.StructureActionsEntry;
import com.finndog.moogs_structures.client.SupportLinks;
import com.finndog.moogs_structures.config.MslConfig;
import com.finndog.moogs_structures.config.ReplaceVanillaManager;
import com.finndog.moogs_structures.config.StructureListManager;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the Cloth Config screen from the presets and structures discovered across all mods.
 * Only loaded on the client when Cloth Config is present (see {@link MoogsStructuresNeoforgeClient}).
 */
public final class MoogsStructuresConfigScreenNeoforge {
    private MoogsStructuresConfigScreenNeoforge() {}

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("moogs_structures.config.title"))
                .setAfterInitConsumer(SupportLinks::addTo);
        ConfigEntryBuilder eb = builder.entryBuilder();

        buildPresets(builder, eb);
        buildStructures(builder, eb);

        return builder.build();
    }

    private static void reloadNotice(ConfigCategory category, ConfigEntryBuilder eb) {
        category.addEntry(eb.startTextDescription(Component.translatable("moogs_structures.config.reload_notice")
                .withStyle(ChatFormatting.YELLOW)).build());
    }

    private static void buildPresets(ConfigBuilder builder, ConfigEntryBuilder eb) {
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("moogs_structures.config.category.replace_vanilla"));
        reloadNotice(category, eb);
        for (ReplaceVanillaManager.PresetInfo preset : ReplaceVanillaManager.getPresets()) {
            String tooltip = preset.description().isEmpty() ? preset.modid() : preset.description();
            category.addEntry(eb.startBooleanToggle(Component.literal(preset.name()), ReplaceVanillaManager.isPresetEnabled(preset))
                    .setDefaultValue(preset.defaultEnabled())
                    .setTooltip(Component.literal(tooltip))
                    .setSaveConsumer(value -> ReplaceVanillaManager.setPresetEnabled(preset, value))
                    .build());
        }
    }

    private static void buildStructures(ConfigBuilder builder, ConfigEntryBuilder eb) {
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("moogs_structures.config.category.structures"));
        category.addEntry(eb.startIntSlider(Component.translatable("moogs_structures.config.universal_frequency"), toPercent(MslConfig.get().getUniversalFrequency()), 25, 400)
                .setDefaultValue(100)
                .setTextGetter(MoogsStructuresConfigScreenNeoforge::multiplierLabel)
                .setTooltip(Component.translatable("moogs_structures.config.universal_frequency.tooltip"))
                .setSaveConsumer(v -> { if (v != toPercent(MslConfig.get().getUniversalFrequency())) MslConfig.get().setUniversalFrequencyAndSave(v / 100.0); })
                .build());

        for (StructureListManager.ModGroup group : StructureListManager.getGroups()) {
            List<AbstractConfigListEntry> entries = new ArrayList<>();
            entries.add(eb.startIntSlider(Component.translatable("moogs_structures.config.all_of_mod", group.modName()), toPercent(MslConfig.get().getModFrequency(group.modid())), 25, 400)
                    .setDefaultValue(100)
                    .setTextGetter(MoogsStructuresConfigScreenNeoforge::multiplierLabel)
                    .setTooltip(Component.translatable("moogs_structures.config.mod_frequency.tooltip", group.modName()))
                    .setSaveConsumer(v -> { if (v != toPercent(MslConfig.get().getModFrequency(group.modid()))) MslConfig.get().setModFrequencyAndSave(group.modid(), v / 100.0); })
                    .build());
            for (StructureListManager.StructureEntry s : group.structures()) {
                entries.add(structureRow(s));
            }
            category.addEntry(eb.startSubCategory(Component.literal(group.modName()), entries).setExpanded(false).build());
        }
    }

    private static AbstractConfigListEntry structureRow(StructureListManager.StructureEntry s) {
        if (s.spacingKey() != null) {
            int value = toPercent(MslConfig.get().getStructureFrequency(s.spacingKey()));
            return new SpacingPreviewSliderEntry(Component.literal(s.name()), 25, 400, value, 100,
                    MoogsStructuresConfigScreenNeoforge::multiplierLabel,
                    v -> { if (v != value) MslConfig.get().setStructureFrequencyAndSave(s.spacingKey(), v / 100.0); },
                    s.previewUrl(), s.structureId());
        }
        return new StructureActionsEntry(Component.literal(s.name()), s.previewUrl(), s.structureId());
    }

    private static int toPercent(double multiplier) {
        return Math.max(25, Math.min(400, (int) Math.round(multiplier * 100.0)));
    }

    private static Component multiplierLabel(int percent) {
        return Component.literal(String.format("%.2fx", percent / 100.0));
    }
}
