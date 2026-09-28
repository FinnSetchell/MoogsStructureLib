package com.finndog.moogs_structures.client;

//? if >=26.3 {
/*import com.mojang.blaze3d.Blaze3D;
*///?}
import net.minecraft.ChatFormatting;
//? if <1.21.11 {
import net.minecraft.Util;
//?}
//? if >=1.21.11 <26.3 {
/*import net.minecraft.util.Util;
*///?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

//? if >=26.3 {
/*import java.net.URI;
*///?}
import java.util.function.Consumer;

/**
 * Shared "Preview" and "Disable" row buttons for the config screen entries. Client-only.
 */
public final class ConfigButtons {
    public static final int PREVIEW_WIDTH = 55;
    public static final int DISABLE_WIDTH = 62;

    private ConfigButtons() {}

    public static Button preview(String url) {
        boolean hasUrl = url != null && !url.isBlank();
        Button.Builder builder = Button.builder(Component.translatable("moogs_structures.config.preview"), b -> { if (hasUrl) openLink(url); })
                .bounds(0, 0, PREVIEW_WIDTH, 20);
        if (!hasUrl) {
            // No preview link for this row - grey it out and say why on hover.
            builder.tooltip(Tooltip.create(Component.translatable("moogs_structures.config.preview.unavailable")));
        }
        Button button = builder.build();
        button.active = hasUrl;
        return button;
    }

    /**
     * A toggle-button showing the enabled/disabled state. Does NOT persist - it reports the pending
     * value via {@code onChange}; the owning entry writes it in its Cloth save() so it follows the
     * screen's Save/Cancel flow like the sliders.
     */
    public static Button disable(boolean initialDisabled, Consumer<Boolean> onChange) {
        boolean[] state = { initialDisabled };
        return Button.builder(label(state[0]), b -> {
            state[0] = !state[0];
            onChange.accept(state[0]);
            b.setMessage(label(state[0]));
        }).bounds(0, 0, DISABLE_WIDTH, 20).build();
    }

    private static Component label(boolean disabled) {
        return disabled
                ? Component.translatable("moogs_structures.config.disabled").withStyle(ChatFormatting.RED)
                : Component.translatable("moogs_structures.config.enabled").withStyle(ChatFormatting.GREEN);
    }

    public static void openLink(String url) {
        Minecraft mc = Minecraft.getInstance();
        // 26.2: the current screen moved off Minecraft onto its Gui (Minecraft.screen ->
        // Minecraft.gui.screen()), and setScreen was renamed to setScreenAndShow.
        //? if <26.2 {
        Screen previous = mc.screen;
        mc.setScreen(new ConfirmLinkScreen(open -> {
        //?} else {
        /*Screen previous = mc.gui.screen();
        *///?}
        // 26.3: links are URIs, and opening one moved off Util.OS onto Blaze3D with the SDL switch.
        //? if >=26.3 {
        /*URI uri = URI.create(url);
        *///?}
        //? if >=26.2 {
        /*mc.setScreenAndShow(new ConfirmLinkScreen(open -> {
        *///?}
            //? if <26.3 {
            if (open) Util.getPlatform().openUri(url);
            //?} else {
            /*if (open) Blaze3D.openUri(uri);
            *///?}
            //? if <26.2 {
            mc.setScreen(previous);
            //?} else {
            /*mc.setScreenAndShow(previous);
            *///?}
        //? if <26.3 {
        }, url, true));
        //?} else {
        /*}, uri, true));
        *///?}
    }
}
