package com.finndog.moogs_structures.modinit.registry;

import com.finndog.moogs_structures.platform.Services;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Supplier;

public class ResourcefulRegistries {

    public static <T> ResourcefulRegistry<T> create(ResourcefulRegistry<T> parent) {
        return new ResourcefulRegistryChild<>(parent);
    }

    public static <T> ResourcefulRegistry<T> create(Registry<T> registry, String id) {
        return Services.REGISTRY.create(registry, id);
    }

    public static <T, K extends Registry<T>> Pair<Supplier<CustomRegistryLookup<T>>, ResourcefulRegistry<T>> createCustomRegistryInternal(String modId, ResourceKey<K> key, boolean save, boolean sync, boolean allowModification) {
        return Services.REGISTRY.createCustomRegistryInternal(modId, key, save, sync, allowModification);
    }
}
