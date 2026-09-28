package com.finndog.moogs_structures.client;

import com.finndog.moogs_structures.config.MslConfig;
import net.minecraft.client.Minecraft;
//? if <26.1.2 {
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?}
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
//? if >=1.21.2 <1.21.10 {
/*import net.minecraft.client.renderer.RenderType;
*///?}
//? if >=1.21.10 {
/*import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
*///?}
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * An icon button that opens a support URL, with a small close (x) in its top-right corner that
 * permanently dismisses it (persisted per-button in {@link MslConfig}). Client-only. A click routes
 * to close vs open by cursor region.
 */
public class SupportButton extends AbstractWidget {
    private static final int CLOSE = 9;

    private final ResourceLocation icon;
    //? if <1.20.6 {
    /*private final int texWidth;
    private final int texHeight;
    *///?}
    private final String url;
    private final String configId;

    //? if >=1.20.6 {
    public SupportButton(int x, int y, int w, int h, ResourceLocation icon, String url, Component tooltip, String configId) {
    //?} else {
    /*public SupportButton(int x, int y, int w, int h, ResourceLocation icon, int texWidth, int texHeight, String url, Component tooltip, String configId) {
    *///?}
        super(x, y, w, h, tooltip);
        this.icon = icon;
        //? if <1.20.6 {
        /*this.texWidth = texWidth;
        this.texHeight = texHeight;
        *///?}
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
    //? if <1.21.2 {
    public void onClick(double mouseX, double mouseY) {
    //?}
    //? if >=1.21.2 <1.21.10 {
    /*public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible || button != 0) return false;
        if (mouseX < getX() || mouseX >= getX() + width || mouseY < getY() || mouseY >= getY() + height) return false;
    *///?}
        //? if <1.21.10 {
        if (inClose(mouseX, mouseY)) {
        //?} else {
    /*public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (inClose(event.x(), event.y())) {
        *///?}
            MslConfig.get().setButtonHiddenAndSave(configId, true);
            this.visible = false;
            this.active = false;
        } else {
            ConfigButtons.openLink(url);
        }
        //? if >=1.21.2 <1.21.10 {
        /*return true;
        *///?}
    }

    @Override
    //? if >=1.20.6 <1.21.2 {
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.blitSprite(icon, getX(), getY(), width, height);
    //?}
    //? if <1.20.6 {
    /*public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.blit(icon, getX(), getY(), width, height, 0.0F, 0.0F, texWidth, texHeight, texWidth, texHeight);
    *///?}
    //? if >=1.21.2 <1.21.10 {
    /*protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.blitSprite(RenderType::guiTextured, icon, getX(), getY(), width, height);
    *///?}
    //? if >=1.21.10 <26.1.2 {
    /*protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, getX(), getY(), width, height);
    *///?}
    //? if >=26.1.2 {
    /*protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        extractor.blitSprite(RenderPipelines.GUI_TEXTURED, icon, getX(), getY(), width, height);
    *///?}
        if (isHoveredOrFocused()) {
            //? if <1.21.2 {
            graphics.fill(getX(), getY(), getX() + width, getY() + height, 0x33FFFFFF);
            //?}
            //? if >=1.21.2 <1.21.10 {
            /*guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, 0x33FFFFFF);
            *///?}
            //? if >=1.21.10 <26.1.2 {
            /*graphics.fill(getX(), getY(), getX() + width, getY() + height, 0x33FFFFFF);
            *///?}
            //? if >=26.1.2 {
            /*extractor.fill(getX(), getY(), getX() + width, getY() + height, 0x33FFFFFF);
            *///?}
        }
        int cx = getX() + width - CLOSE;
        int cy = getY();
        boolean closeHover = inClose(mouseX, mouseY);
        //? if <1.21.2 {
        graphics.fill(cx, cy, cx + CLOSE, cy + CLOSE, closeHover ? 0xD0000000 : 0x80000000);
        graphics.drawCenteredString(Minecraft.getInstance().font, "×", cx + CLOSE / 2, cy + 1, closeHover ? 0xFFFF5555 : 0xFFFFFFFF);
        //?}
        //? if >=1.21.2 <1.21.10 {
        /*guiGraphics.fill(cx, cy, cx + CLOSE, cy + CLOSE, closeHover ? 0xD0000000 : 0x80000000);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, "×", cx + CLOSE / 2, cy + 1, closeHover ? 0xFFFF5555 : 0xFFFFFFFF);
        *///?}
        //? if >=1.21.10 <26.1.2 {
        /*graphics.fill(cx, cy, cx + CLOSE, cy + CLOSE, closeHover ? 0xD0000000 : 0x80000000);
        graphics.drawCenteredString(Minecraft.getInstance().font, "×", cx + CLOSE / 2, cy + 1, closeHover ? 0xFFFF5555 : 0xFFFFFFFF);
        *///?}
        //? if >=26.1.2 {
        /*extractor.fill(cx, cy, cx + CLOSE, cy + CLOSE, closeHover ? 0xD0000000 : 0x80000000);
        extractor.centeredText(Minecraft.getInstance().font, "×", cx + CLOSE / 2, cy + 1, closeHover ? 0xFFFF5555 : 0xFFFFFFFF);
        *///?}
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
