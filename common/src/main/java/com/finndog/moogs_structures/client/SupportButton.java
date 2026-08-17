package com.finndog.moogs_structures.client;

import com.finndog.moogs_structures.config.MslConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * An icon button that opens a support URL, with a small close (x) in its top-right corner that
 * permanently dismisses it (persisted per-button in {@link MslConfig}). Client-only. Drawn with
 * blitSprite in immediate mode; a click routes to close vs open by cursor region.
 */
public class SupportButton extends AbstractWidget {
    private static final int CLOSE = 9;

    private final ResourceLocation icon;
    private final String url;
    private final String configId;

    public SupportButton(int x, int y, int w, int h, ResourceLocation icon, String url, Component tooltip, String configId) {
        super(x, y, w, h, tooltip);
        this.icon = icon;
        this.url = url;
        this.configId = configId;
        setTooltip(Tooltip.create(tooltip));
    }

    private boolean inClose(double mouseX, double mouseY) {
        int cx = getX() + width - CLOSE;
        int cy = getY();
        return mouseX >= cx && mouseX < cx + CLOSE && mouseY >= cy && mouseY < cy + CLOSE;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible || button != 0) return false;
        if (mouseX < getX() || mouseX >= getX() + width || mouseY < getY() || mouseY >= getY() + height) return false;
        if (inClose(mouseX, mouseY)) {
            MslConfig.get().setButtonHiddenAndSave(configId, true);
            this.visible = false;
            this.active = false;
        } else {
            ConfigButtons.openLink(url);
        }
        return true;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.blitSprite(RenderType::guiTextured, icon, getX(), getY(), width, height);
        if (isHoveredOrFocused()) {
            guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, 0x33FFFFFF);
        }
        int cx = getX() + width - CLOSE;
        int cy = getY();
        boolean closeHover = inClose(mouseX, mouseY);
        guiGraphics.fill(cx, cy, cx + CLOSE, cy + CLOSE, closeHover ? 0xD0000000 : 0x80000000);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, "×", cx + CLOSE / 2, cy + 1, closeHover ? 0xFFFF5555 : 0xFFFFFFFF);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
