package com.finndog.moogs_structures.client;

import com.finndog.moogs_structures.config.MslConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * An icon button that opens a support URL, with a small close (x) in its top-right corner that
 * permanently dismisses it (persisted per-button in {@link MslConfig}). Client-only. Drawn via
 * blitSprite in the immediate-mode render model; a click routes to close vs open by cursor region.
 */
public class SupportButton extends AbstractWidget {
    private static final int CLOSE = 9;

    private final Identifier icon;
    private final String url;
    private final String configId;

    public SupportButton(int x, int y, int w, int h, Identifier icon, String url, Component tooltip, String configId) {
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
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (inClose(event.x(), event.y())) {
            MslConfig.get().setButtonHiddenAndSave(configId, true);
            this.visible = false;
            this.active = false;
        } else {
            ConfigButtons.openLink(url);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, getX(), getY(), width, height);
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
