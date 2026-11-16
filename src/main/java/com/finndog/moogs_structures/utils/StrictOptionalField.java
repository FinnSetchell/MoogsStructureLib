package com.finndog.moogs_structures.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * An optional field that is an error when it is present but doesn't parse, on every Minecraft version.
 * <p>
 * Up to 1.20.4 the game's DataFixerUpper turns a field that fails to parse into "absent" inside
 * {@code optionalFieldOf}, silently; 1.20.5 made that an error. Without this, the same broken datapack
 * loads with the field ignored on 1.20.x and stops the world loading on 1.21 and later.
 */
public final class StrictOptionalField<A> extends MapCodec<Optional<A>> {
    private final String name;
    private final Codec<A> codec;

    private StrictOptionalField(String name, Codec<A> codec) {
        this.name = name;
        this.codec = codec;
    }

    public static <A> MapCodec<Optional<A>> of(Codec<A> codec, String name) {
        return new StrictOptionalField<>(name, codec);
    }

    public static <A> MapCodec<A> of(Codec<A> codec, String name, A defaultValue) {
        return new StrictOptionalField<>(name, codec).xmap(value -> value.orElse(defaultValue), Optional::of);
    }

    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.of(ops.createString(this.name));
    }

    @Override
    public <T> DataResult<Optional<A>> decode(DynamicOps<T> ops, MapLike<T> input) {
        T value = input.get(this.name);
        return value == null ? DataResult.success(Optional.empty()) : this.codec.parse(ops, value).map(Optional::of);
    }

    @Override
    public <T> RecordBuilder<T> encode(Optional<A> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        return input.isPresent() ? prefix.add(this.name, this.codec.encodeStart(ops, input.get())) : prefix;
    }

    @Override
    public String toString() {
        return "StrictOptionalField[" + this.name + ": " + this.codec + "]";
    }
}
