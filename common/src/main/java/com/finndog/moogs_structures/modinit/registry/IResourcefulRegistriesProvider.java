package com.finndog.moogs_structures.modinit.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Supplier;

public interface IResourcefulRegistriesProvider {

    <T> ResourcefulRegistry<T> create(Registry<T> registry, String id);

    <T, K extends Registry<T>> Pair<Supplier<CustomRegistryLookup<T>>, ResourcefulRegistry<T>> createCustomRegistryInternal(String modId, ResourceKey<K> key, boolean save, boolean sync, boolean allowModification);
}
