package com.finndog.moogs_structures.world.processors;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import net.minecraft.SharedConstants;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;

/**
 * Codec wrapper that accepts the pre-1.20.5 item shape ({@code {id, Count, tag}}) and upgrades it
 * to the 1.20.5+ data-components shape ({@code {id, count, components}}) with vanilla's own item
 * DataFixer before handing off to a vanilla {@link net.minecraft.world.item.ItemStack} codec. This
 * lets one processor JSON authored in the 1.20.4 item shape load both on the 1.20-1.20.4 branch
 * (native) and here, the same way {@link EnchantmentsSchemaCompatCodec} bridges the 1.21.5 shift.
 *
 * <p>Encode is pass-through (vanilla item save already emits the current schema). Decode only
 * upgrades when the input carries the legacy shape ({@code tag}/{@code Count}); a component-shape or
 * malformed item is left for the inner codec to handle as usual.
 */
public final class ItemComponentsCompatCodec {

    // 1.20.4's data version, just below the 1.20.5 data-components rework (24w09a, data version 3819).
    private static final int PRE_COMPONENTS_DATA_VERSION = 3700;

    private ItemComponentsCompatCodec() {}

    public static <A> Codec<A> wrap(Codec<A> inner) {
        return new Codec<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                return inner.decode(ops, upgradeIfLegacy(ops, input));
            }

            @Override
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
                return inner.encode(input, ops, prefix);
            }

            @Override
            public String toString() {
                return "ItemComponentsCompat[" + inner + "]";
            }
        };
    }

    // Run vanilla's ITEM_STACK datafixer from the pre-components version up to the current one, but
    // only when the input carries the legacy shape (a tag or Count field). Malformed input is left
    // untouched so the inner codec reports it normally.
    private static <T> T upgradeIfLegacy(DynamicOps<T> ops, T input) {
        MapLike<T> root = ops.getMap(input).result().orElse(null);
        if (root == null || (root.get("tag") == null && root.get("Count") == null)) {
            return input;
        }
        try {
            int current = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
            return DataFixers.getDataFixer()
                    .update(References.ITEM_STACK, new Dynamic<>(ops, input), PRE_COMPONENTS_DATA_VERSION, current)
                    .getValue();
        } catch (Exception e) {
            return input;
        }
    }
}
