package com.finndog.moogs_structures.client;

import com.finndog.moogs_structures.config.MslConfig;
import me.shedaniel.clothconfig2.gui.entries.IntegerSliderEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A Cloth integer-slider row with "Preview" and "Disable" buttons on the same row. The slider and
 * reset button are laid out into a narrowed area so the two buttons fit on the far right.
 * Client-only; only instantiated by the config screen when Cloth Config is present.
 */
public class SpacingPreviewSliderEntry extends IntegerSliderEntry {
    private static final int GAP = 2;

    private final Button previewButton;
    private final Button disableButton;
    private final String structureId;
    private boolean savedDisabled;
    private boolean pendingDisabled;

    public SpacingPreviewSliderEntry(Component fieldName, int min, int max, int value, int defaultValue,
                                     Function<Integer, Component> textGetter, Consumer<Integer> saveConsumer,
                                     String previewUrl, String structureId) {
        super(fieldName, min, max, value, Component.translatable("text.cloth-config.reset_value"), () -> defaultValue, saveConsumer);
        setTextGetter(textGetter);
        this.structureId = structureId;
        this.savedDisabled = MslConfig.get().isDisabledForScreen(structureId);
        this.pendingDisabled = savedDisabled;
        this.previewButton = ConfigButtons.preview(previewUrl);
        this.disableButton = ConfigButtons.disable(savedDisabled, v -> this.pendingDisabled = v);
    }

    @Override
    public void save() {
        super.save();
        if (pendingDisabled != savedDisabled) {
            MslConfig.get().setStructureDisabledAndSave(structureId, pendingDisabled);
            savedDisabled = pendingDisabled;
        }
    }

    @Override
    public boolean isEdited() {
        return super.isEdited() || pendingDisabled != savedDisabled;
    }

    // 26.1: cloth's immediate-mode render(GuiGraphics, ...) was replaced by the extract-render-state
    // model - extractRenderState(GuiGraphicsExtractor, ...). Child widgets register their own state via
    // AbstractWidget.extractRenderState(extractor, mouseX, mouseY, delta).
    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
        int reserved = ConfigButtons.PREVIEW_WIDTH + ConfigButtons.DISABLE_WIDTH + GAP * 2;
        super.extractRenderState(extractor, index, y, x, Math.max(0, entryWidth - reserved), entryHeight, mouseX, mouseY, hovered, delta);

        int stripX = x + entryWidth - reserved + GAP;
        this.previewButton.setX(stripX);
        this.previewButton.setY(y);
        this.previewButton.extractRenderState(extractor, mouseX, mouseY, delta);
        this.disableButton.setX(stripX + ConfigButtons.PREVIEW_WIDTH + GAP);
        this.disableButton.setY(y);
        this.disableButton.extractRenderState(extractor, mouseX, mouseY, delta);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        List<GuiEventListener> list = new ArrayList<>(super.children());
        list.add(this.previewButton);
        list.add(this.disableButton);
        return list;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        List<NarratableEntry> list = new ArrayList<>(super.narratables());
        list.add(this.previewButton);
        list.add(this.disableButton);
        return list;
    }
}
