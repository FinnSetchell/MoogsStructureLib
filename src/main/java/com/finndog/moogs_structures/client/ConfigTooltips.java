package com.finndog.moogs_structures.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.Optional;

/**
 * Wraps config screen tooltips at the width vanilla wraps its own tooltips at. Cloth Config only
 * breaks tooltip lines at the screen's full width, so a long tooltip otherwise becomes a box as wide
 * as the screen. Client-only.
 */
public final class ConfigTooltips {
    /** Vanilla's tooltip width (Tooltip.MAX_WIDTH, which is private). */
    private static final int WIDTH = 170;

    private ConfigTooltips() {}

    /** The text as lines no wider than a vanilla tooltip, keeping each part's style, for Cloth's setTooltip. */
    public static Component[] wrap(Component text) {
        return Minecraft.getInstance().font.getSplitter().splitLines(text, WIDTH, Style.EMPTY).stream()
                .map(ConfigTooltips::toComponent)
                .toArray(Component[]::new);
    }

    private static Component toComponent(FormattedText line) {
        MutableComponent out = Component.empty();
        line.visit((style, part) -> {
            out.append(Component.literal(part).withStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return out;
    }
}
