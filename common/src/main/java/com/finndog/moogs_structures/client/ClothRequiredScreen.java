package com.finndog.moogs_structures.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * Shown when the mod-list config button is used without Cloth Config installed. A plain vanilla
 * screen (no Cloth dependency) that tells the player the config GUI needs Cloth Config, so the
 * button guides them instead of doing nothing. 1.20.1 has no MultiLineTextWidget, so the message is
 * split and drawn by hand.
 */
public class ClothRequiredScreen extends Screen {
    private static final Component MESSAGE = Component.literal(
            "Moog's Structure Lib's config screen requires Cloth Config API. Install it to configure in game.");
    private static final int MAX_WIDTH = 310;

    private final Screen parent;

    public ClothRequiredScreen(Screen parent) {
        super(Component.literal("Moog's Structure Lib"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                .bounds(cx - 100, this.height / 2 + 30, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics);
        int y = this.height / 2 - 40;
        for (FormattedCharSequence line : this.font.split(MESSAGE, MAX_WIDTH)) {
            graphics.drawCenteredString(this.font, line, this.width / 2, y, 0xFFFFFF);
            y += this.font.lineHeight;
        }
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
