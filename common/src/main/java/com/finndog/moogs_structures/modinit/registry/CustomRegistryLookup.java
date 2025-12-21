package com.finndog.moogs_structures.modinit.registry;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface CustomRegistryLookup<T> extends Iterable<T> {

    boolean containsKey(Identifier id);

    boolean containsValue(T value);

    @Nullable
    T get(Identifier id);

    @Nullable
    Identifier getKey(T value);

    Collection<T> getValues();

    Collection<Identifier> getKeys();

}
