//? if <1.21.1 {
/*package com.finndog.moogs_structures.world.structures.codecs;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/^*
 * The game's LiquidSettings, which only arrived in 1.21, so {@code liquid_settings} means the same on 1.20.
 * {@code ignore_waterlogging} turns off the template's keepLiquids while an MSL structure places its pieces
 * (StructureTemplateLiquidSettingsMixin), which is what the game's own setting does from 1.21 on.
 ^/
public enum LiquidSettings implements StringRepresentable {
    IGNORE_WATERLOGGING("ignore_waterlogging"),
    APPLY_WATERLOGGING("apply_waterlogging");

    public static final Codec<LiquidSettings> CODEC = StringRepresentable.fromEnum(LiquidSettings::values);

    private final String name;

    LiquidSettings(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
*///?}
