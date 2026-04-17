package com.finndog.moogs_structures.modinit.registry.fabric;

import com.finndog.moogs_structures.modinit.registry.CustomRegistryLookup;
import com.finndog.moogs_structures.modinit.registry.ResourcefulRegistry;
import com.finndog.moogs_structures.platform.IRegistryPlatform;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Supplier;

public class ResourcefulRegistriesImpl implements IRegistryPlatform {

    @Override
    public <T> ResourcefulRegistry<T> create(Registry<T> registry, String id) {
        return new CustomResourcefulRegistry<>(registry, id);
    }

    @Override
    public <T, K extends Registry<T>> Pair<Supplier<CustomRegistryLookup<T>>, ResourcefulRegistry<T>> createCustomRegistryInternal(String modId, ResourceKey<K> key, boolean save, boolean sync, boolean allowModification) {
        @SuppressWarnings("unchecked")
        var registry = FabricRegistryBuilder.createSimple((ResourceKey<Registry<T>>) (Object) key);
        if (sync) registry.attribute(RegistryAttribute.SYNCED);
        if (allowModification) registry.attribute(RegistryAttribute.MODDED);
        var builtRegistry = registry.buildAndRegister();
        CustomRegistry<T> customRegistry = new CustomRegistry<>(builtRegistry);
        return Pair.of(() -> customRegistry, new CustomResourcefulRegistry<>(builtRegistry, modId));
    }
}
