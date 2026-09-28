package com.finndog.moogs_structures.client;

import com.finndog.moogs_structures.config.MslConfig;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A config-screen row with just a label plus "Preview" and "Disable" buttons - for structures that
 * have no spacing slider (e.g. concentric-rings strongholds). Client-only.
 */
public class StructureActionsEntry extends TooltipListEntry<Object> {
    private static final int GAP = 2;

    private final Button previewButton;
    private final Button disableButton;
    private final String structureId;
    private boolean savedDisabled;
    private boolean pendingDisabled;

    public StructureActionsEntry(Component fieldName, String previewUrl, String structureId) {
        super(fieldName, () -> Optional.empty());
        this.structureId = structureId;
        this.savedDisabled = MslConfig.get().isDisabledForScreen(structureId);
        this.pendingDisabled = savedDisabled;
        this.previewButton = previewUrl != null ? ConfigButtons.preview(previewUrl) : null;
        this.disableButton = ConfigButtons.disable(savedDisabled, v -> this.pendingDisabled = v);
    }

    @Override
    public void save() {
        if (pendingDisabled != savedDisabled) {
            MslConfig.get().setStructureDisabledAndSave(structureId, pendingDisabled);
            savedDisabled = pendingDisabled;
        }
    }

    @Override
    public boolean isEdited() {
        return pendingDisabled != savedDisabled;
    }

    @Override
    public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
        super.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, delta);
        Minecraft mc = Minecraft.getInstance();
        graphics.drawString(mc.font, getFieldName(), x, y + entryHeight / 2 - mc.font.lineHeight / 2, getPreferredTextColor());

        int reserved = ConfigButtons.DISABLE_WIDTH + (previewButton != null ? ConfigButtons.PREVIEW_WIDTH + GAP : 0);
        int stripX = x + entryWidth - reserved;
        if (previewButton != null) {
            previewButton.setX(stripX);
            previewButton.setY(y);
            previewButton.render(graphics, mouseX, mouseY, delta);
            stripX += ConfigButtons.PREVIEW_WIDTH + GAP;
        }
        disableButton.setX(stripX);
        disableButton.setY(y);
        disableButton.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public Optional<Object> getDefaultValue() {
        return Optional.empty();
    }

    @Override
    public List<? extends GuiEventListener> children() {
        List<GuiEventListener> list = new ArrayList<>();
        if (previewButton != null) list.add(previewButton);
        list.add(disableButton);
        return list;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        List<NarratableEntry> list = new ArrayList<>();
        if (previewButton != null) list.add(previewButton);
        list.add(disableButton);
        return list;
    }
}
