package com.finndog.moogs_structures.client;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.config.MslConfig;
import com.finndog.moogs_structures.mixins.client.ScreenInvoker;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Attaches the Discord and Ko-fi icon buttons to the config screen's top-right corner. Called from
 * each loader's Cloth builder via setAfterInitConsumer. Client-only. Dismissed buttons stay hidden.
 */
public final class SupportLinks {
    private static final int H = 20;          // button height; each icon's width follows its aspect ratio
    private static final int DISCORD_W = 20;   // discord.png is square (128x128)
    private static final int KOFI_W = 51;      // kofi.png is 128x50, so ~2.56x wide at height 20
    private static final int GAP = 4;
    private static final int PAD = 3;
    private static final int TOP = 2;

    private static final String DISCORD_ID = "discord";
    private static final String KOFI_ID = "kofi";
    private static final String DISCORD_URL = "https://discord.gg/S5nffJbuvA";
    private static final String KOFI_URL = "https://ko-fi.com/finndog";
    private static final ResourceLocation DISCORD_TEX = new ResourceLocation(MoogsStructuresCommon.MODID, "textures/gui/discord.png");
    private static final ResourceLocation KOFI_TEX = new ResourceLocation(MoogsStructuresCommon.MODID, "textures/gui/kofi.png");
    private static final int DISCORD_TEX_W = 128, DISCORD_TEX_H = 128;
    private static final int KOFI_TEX_W = 128, KOFI_TEX_H = 50;

    private SupportLinks() {}

    public static void addTo(Screen screen) {
        MslConfig cfg = MslConfig.get();
        int right = screen.width - PAD;
        right = maybeAdd(screen, cfg, right, DISCORD_W, DISCORD_ID, DISCORD_TEX, DISCORD_TEX_W, DISCORD_TEX_H, DISCORD_URL, "Join the Discord");
        maybeAdd(screen, cfg, right, KOFI_W, KOFI_ID, KOFI_TEX, KOFI_TEX_W, KOFI_TEX_H, KOFI_URL, "Support on Ko-fi");
    }

    private static int maybeAdd(Screen screen, MslConfig cfg, int right, int w, String id, ResourceLocation tex, int texW, int texH, String url, String tooltip) {
        if (cfg.isButtonHidden(id)) return right;
        int x = right - w;
        ((ScreenInvoker) screen).msl$addRenderableWidget(new SupportButton(x, TOP, w, H, tex, texW, texH, url, Component.literal(tooltip), id));
        return x - GAP;
    }
}
