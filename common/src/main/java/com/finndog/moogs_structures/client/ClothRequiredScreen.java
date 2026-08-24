package com.finndog.moogs_structures.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Shown when the mod-list config button is used without Cloth Config installed. A plain vanilla
 * screen (no Cloth dependency) that tells the player the config GUI needs Cloth Config, so the
 * button guides them instead of doing nothing.
 */
public class ClothRequiredScreen extends Screen {
    private static final Component MESSAGE = Component.translatable("moogs_structures.cloth_required.message");

    private final Screen parent;

    public ClothRequiredScreen(Screen parent) {
        super(Component.translatable("moogs_structures.cloth_required.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        MultiLineTextWidget text = new MultiLineTextWidget(cx - 155, this.height / 2 - 40, MESSAGE, this.font);
        text.setMaxWidth(310);
        text.setCentered(true);
        addRenderableWidget(text);
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                .bounds(cx - 100, this.height / 2 + 30, 200, 20).build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
