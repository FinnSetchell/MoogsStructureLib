//? if <1.21.5 {
package com.finndog.moogs_structures.utils;

import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

/**
 * Mob drop chances written in a datapack's JSON, as Minecraft before 1.21.5 reads them.
 *
 * <p>JSON has no float type, so NBT made from it picks a type per number: 0.02 becomes a double, 0.0 and
 * 1.0 become bytes (a list of only those becomes a byte array), and a list mixing kinds comes out as a list
 * of wrapped values. Before 1.21.5 a mob reads {@code ArmorDropChances} and {@code HandDropChances} only as
 * lists of floats; anything else leaves the 8.5% default (0% on 1.21.4 for a byte array). From 1.21.5 a mob
 * reads {@code drop_chances} through a codec that takes any number, so nothing needs rewriting there.
 */
public final class LegacyDropChances {
    private static final String[] KEYS = {"ArmorDropChances", "HandDropChances"};

    private LegacyDropChances() {}

    /** Rewrites this entity's drop chance lists, and its passengers', as lists of floats. */
    public static void asFloatLists(CompoundTag entity) {
        for (String key : KEYS) {
            ListTag floats = floats(entity.get(key));
            if (floats != null) {
                entity.put(key, floats);
            }
        }
        if (entity.get("Passengers") instanceof ListTag passengers) {
            for (Tag passenger : passengers) {
                if (passenger instanceof CompoundTag compound) {
                    asFloatLists(compound);
                }
            }
        }
    }

    /** The list's numbers as floats, or null when it is not a list of numbers. */
    @Nullable
    private static ListTag floats(@Nullable Tag tag) {
        if (!(tag instanceof CollectionTag<?> list)) {
            return null;
        }
        ListTag floats = new ListTag();
        for (Tag element : list) {
            // A list mixing kinds of number holds each one wrapped as {"": number}.
            if (element instanceof CompoundTag wrapper && wrapper.size() == 1 && wrapper.contains("")) {
                element = wrapper.get("");
            }
            if (!(element instanceof NumericTag number)) {
                return null;
            }
            floats.add(FloatTag.valueOf(number.getAsFloat()));
        }
        return floats;
    }
}
//?}
