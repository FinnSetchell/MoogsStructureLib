package com.finndog.moogs_structures.modinit.registry;

import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public interface RegistryEntry<T> extends Supplier<T> {

    @Override
    T get();

    Identifier getId();

}