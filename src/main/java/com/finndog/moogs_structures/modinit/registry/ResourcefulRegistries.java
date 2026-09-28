package com.finndog.moogs_structures.modinit.registry;

//? if <26.1.2 {
import com.finndog.moogs_structures.platform.Services;
//?}
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.apache.commons.lang3.tuple.Pair;

//? if >=26.1.2 {
/*import java.util.ServiceLoader;
*///?}
import java.util.function.Supplier;

public class ResourcefulRegistries {

    //? if >=26.1.2 {
    /*private static final IResourcefulRegistriesProvider IMPL = ServiceLoader
            .load(IResourcefulRegistriesProvider.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No IResourcefulRegistriesProvider implementation found"));
    *///?}

    public static <T> ResourcefulRegistry<T> create(ResourcefulRegistry<T> parent) {
        return new ResourcefulRegistryChild<>(parent);
    }

    public static <T> ResourcefulRegistry<T> create(Registry<T> registry, String id) {
        //? if <26.1.2 {
        return Services.REGISTRY.create(registry, id);
        //?} else {
        /*return IMPL.create(registry, id);
        *///?}
    }

    public static <T, K extends Registry<T>> Pair<Supplier<CustomRegistryLookup<T>>, ResourcefulRegistry<T>> createCustomRegistryInternal(String modId, ResourceKey<K> key, boolean save, boolean sync, boolean allowModification) {
        //? if <26.1.2 {
        return Services.REGISTRY.createCustomRegistryInternal(modId, key, save, sync, allowModification);
        //?} else {
        /*return IMPL.createCustomRegistryInternal(modId, key, save, sync, allowModification);
        *///?}
    }
}
