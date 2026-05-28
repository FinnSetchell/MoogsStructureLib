package com.finndog.moogs_structures.world.processors;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Codec wrapper that accepts the pre-1.21.5 wrapped enchantments schema
 * ({@code "minecraft:enchantments": {"levels": {<id>: <lvl>}}}) and normalises
 * it to the 1.21.5+ flat schema ({@code "minecraft:enchantments": {<id>: <lvl>}})
 * before delegating to a vanilla {@link net.minecraft.world.item.ItemStack} codec.
 *
 * <p>Applies the same unwrap to {@code minecraft:stored_enchantments}, which was
 * flattened in the same MC release.
 *
 * <p>Used by {@link EquipArmorStandProcessor} so MNS's single 1.21-datapack jar
 * (authored against the wrapped schema) keeps loading on MC 1.21.5+, where the
 * vanilla item codec would otherwise read {@code "levels"} as an enchantment id
 * and fail with {@code Unbound values in registry minecraft:enchantment: [minecraft:levels]}.
 *
 * <p>Pass-through on encode; vanilla {@code ItemStack#save} already emits the
 * current MC version's schema, so we never persist the legacy shape.
 *
 * <p>Pass-through on decode if the input doesn't carry the legacy shape — so any
 * unexpected structure produces the normal vanilla error rather than a silent rewrite.
 */
public final class EnchantmentsSchemaCompatCodec {

    private static final String COMPONENTS_FIELD = "components";
    private static final String ENCHANTMENTS_KEY = "minecraft:enchantments";
    private static final String STORED_ENCHANTMENTS_KEY = "minecraft:stored_enchantments";
    private static final String LEVELS_FIELD = "levels";

    private EnchantmentsSchemaCompatCodec() {}

    /**
     * Wraps the given inner codec so legacy enchantments-schema inputs are
     * normalised before decode.
     */
    public static <A> Codec<A> wrap(Codec<A> inner) {
        return new Codec<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                T normalised = normalise(ops, input);
                return inner.decode(ops, normalised);
            }

            @Override
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
                return inner.encode(input, ops, prefix);
            }

            @Override
            public String toString() {
                return "EnchantmentsSchemaCompat[" + inner + "]";
            }
        };
    }

    /**
     * If {@code input} is a map with a {@code components} sub-map whose
     * {@code minecraft:enchantments} or {@code minecraft:stored_enchantments}
     * entries use the wrapped {@code {"levels": {...}}} shape, return a copy
     * with those entries unwrapped. Otherwise return {@code input} unchanged.
     */
    private static <T> T normalise(DynamicOps<T> ops, T input) {
        Optional<MapLike<T>> rootOpt = ops.getMap(input).result();
        if (rootOpt.isEmpty()) {
            return input;
        }
        MapLike<T> root = rootOpt.get();

        T components = root.get(COMPONENTS_FIELD);
        if (components == null) {
            return input;
        }

        Optional<MapLike<T>> compMapOpt = ops.getMap(components).result();
        if (compMapOpt.isEmpty()) {
            return input;
        }
        MapLike<T> compMap = compMapOpt.get();

        Map<T, T> rewrittenComponents = new LinkedHashMap<>();
        boolean[] changed = { false };
        compMap.entries().forEach(pair -> {
            T key = pair.getFirst();
            T value = pair.getSecond();
            String keyStr = ops.getStringValue(key).result().orElse(null);
            if (ENCHANTMENTS_KEY.equals(keyStr) || STORED_ENCHANTMENTS_KEY.equals(keyStr)) {
                Optional<MapLike<T>> valueMap = ops.getMap(value).result();
                if (valueMap.isPresent()) {
                    T levels = valueMap.get().get(LEVELS_FIELD);
                    if (levels != null) {
                        rewrittenComponents.put(key, levels);
                        changed[0] = true;
                        return;
                    }
                }
            }
            rewrittenComponents.put(key, value);
        });

        if (!changed[0]) {
            return input;
        }

        T newComponents = ops.createMap(rewrittenComponents);

        Map<T, T> rewrittenRoot = new LinkedHashMap<>();
        root.entries().forEach(pair -> {
            T key = pair.getFirst();
            T value = pair.getSecond();
            String keyStr = ops.getStringValue(key).result().orElse(null);
            if (COMPONENTS_FIELD.equals(keyStr)) {
                rewrittenRoot.put(key, newComponents);
            } else {
                rewrittenRoot.put(key, value);
            }
        });
        return ops.createMap(rewrittenRoot);
    }
}
