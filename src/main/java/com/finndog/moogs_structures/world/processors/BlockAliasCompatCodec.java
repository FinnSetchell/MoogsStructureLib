package com.finndog.moogs_structures.world.processors;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Codec wrapper that translates legacy vanilla block ids to their renamed modern
 * counterparts before delegating to a vanilla block-resolving codec (typically
 * {@link net.minecraft.world.level.block.state.BlockState#CODEC}).
 *
 * <p>Specifically intended to bridge datapacks authored against an older MC where a
 * block had its original name (e.g. {@code minecraft:chain}) onto a newer MC where
 * that block was renamed (e.g. {@code minecraft:iron_chain} starting around MC 1.21.9).
 * Without the translation the vanilla block registry codec throws
 * {@code Unknown registry key in ResourceKey[minecraft:root / minecraft:block]: minecraft:chain}
 * and the entire processor list fails to load.
 *
 * <p><b>Runtime-aware:</b> the translation only fires if the rename target actually
 * exists in the live block registry. On an older MC version that still has the legacy
 * name, the probe finds no modern target and the wrapper passes the input through
 * unchanged — so the same MSL jar works on both sides of a rename boundary.
 *
 * <p>Handles both input shapes:
 * <ul>
 *   <li>Map with a {@code Name} field — the shape consumed by {@code BlockState.CODEC}.</li>
 *   <li>Raw string — the shape consumed by {@code BuiltInRegistries.BLOCK.byNameCodec()}.</li>
 * </ul>
 *
 * <p>Encode is pass-through; vanilla codecs already emit the current MC version's id.
 *
 * <p>To add a new alias, extend {@link #RENAMES}. Order of entries doesn't matter; the
 * map is consulted by legacy-name lookup only.
 */
public final class BlockAliasCompatCodec {

    /** Legacy vanilla block id -> modern vanilla block id. */
    private static final Map<String, String> RENAMES = Map.of(
            "minecraft:chain", "minecraft:iron_chain"
    );

    private static final String NAME_FIELD = "Name";

    private BlockAliasCompatCodec() {}

    /**
     * Wraps the given inner codec so legacy block ids are normalised to their modern
     * registered names before decode.
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
                return "BlockAliasCompat[" + inner + "]";
            }
        };
    }

    /**
     * If {@code input} carries a legacy block id (either as a map with a {@code Name}
     * field, or as a raw string) AND the corresponding modern block exists in the
     * runtime block registry, return a copy with the id rewritten. Otherwise return
     * {@code input} unchanged.
     */
    private static <T> T normalise(DynamicOps<T> ops, T input) {
        // Shape 1: {Name: "minecraft:chain", Properties: {...}} (BlockState codec)
        Optional<MapLike<T>> rootOpt = ops.getMap(input).result();
        if (rootOpt.isPresent()) {
            MapLike<T> root = rootOpt.get();
            T nameValue = root.get(NAME_FIELD);
            if (nameValue == null) {
                return input;
            }
            String name = ops.getStringValue(nameValue).result().orElse(null);
            if (name == null) {
                return input;
            }
            String renamed = aliasIfRegistered(name);
            if (renamed.equals(name)) {
                return input;
            }
            Map<T, T> rewritten = new LinkedHashMap<>();
            root.entries().forEach(pair -> {
                String key = ops.getStringValue(pair.getFirst()).result().orElse(null);
                if (NAME_FIELD.equals(key)) {
                    rewritten.put(pair.getFirst(), ops.createString(renamed));
                } else {
                    rewritten.put(pair.getFirst(), pair.getSecond());
                }
            });
            return ops.createMap(rewritten);
        }

        // Shape 2: "minecraft:chain" (raw string — byNameCodec)
        Optional<String> strOpt = ops.getStringValue(input).result();
        if (strOpt.isPresent()) {
            String renamed = aliasIfRegistered(strOpt.get());
            if (!renamed.equals(strOpt.get())) {
                return ops.createString(renamed);
            }
        }

        return input;
    }

    /**
     * Returns the modern alias for {@code name} if a rename is defined AND the
     * target block exists in the runtime registry; otherwise returns {@code name}.
     */
    private static String aliasIfRegistered(String name) {
        String target = RENAMES.get(name);
        if (target == null) {
            return name;
        }
        ResourceLocation targetId = ResourceLocation.tryParse(target);
        if (targetId == null) {
            return name;
        }
        if (BuiltInRegistries.BLOCK.containsKey(targetId)) {
            return target;
        }
        return name;
    }
}
