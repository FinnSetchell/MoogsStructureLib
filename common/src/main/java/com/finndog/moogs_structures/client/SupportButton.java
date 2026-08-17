package com.finndog.moogs_structures.client;

import com.finndog.moogs_structures.config.MslConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * An icon button that opens a support URL, with a small close (x) in its top-right corner that
 * permanently dismisses it (persisted per-button in {@link MslConfig}). Client-only. A click routes
 * to close vs open by cursor region. 1.20.1 has no GUI sprite atlas, so the icon is drawn as a plain
 * texture scaled from its full size to the widget bounds.
 */
public class SupportButton extends AbstractWidget {
    private static final int CLOSE = 9;

    private final ResourceLocation icon;
    private final int texWidth;
    private final int texHeight;
    private final String url;
    private final String configId;

    public SupportButton(int x, int y, int w, int h, ResourceLocation icon, int texWidth, int texHeight, String url, Component tooltip, String configId) {
        super(x, y, w, h, tooltip);
        this.icon = icon;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
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
    public void onClick(double mouseX, double mouseY) {
        if (inClose(mouseX, mouseY)) {
            MslConfig.get().setButtonHiddenAndSave(configId, true);
            this.visible = false;
            this.active = false;
        } else {
            ConfigButtons.openLink(url);
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.blit(icon, getX(), getY(), width, height, 0.0F, 0.0F, texWidth, texHeight, texWidth, texHeight);
        if (isHoveredOrFocused()) {
            graphics.fill(getX(), getY(), getX() + width, getY() + height, 0x33FFFFFF);
        }
        int cx = getX() + width - CLOSE;
        int cy = getY();
        boolean closeHover = inClose(mouseX, mouseY);
        graphics.fill(cx, cy, cx + CLOSE, cy + CLOSE, closeHover ? 0xD0000000 : 0x80000000);
        graphics.drawCenteredString(Minecraft.getInstance().font, "×", cx + CLOSE / 2, cy + 1, closeHover ? 0xFFFF5555 : 0xFFFFFFFF);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
