package com.finndog.moogs_structures.modinit.registry.fabric;

import com.finndog.moogs_structures.modinit.registry.CustomRegistryLookup;
//? if >=26.1.2 {
/*import com.finndog.moogs_structures.modinit.registry.IResourcefulRegistriesProvider;
*///?}
import com.finndog.moogs_structures.modinit.registry.ResourcefulRegistry;
//? if <26.1.2 {
import com.finndog.moogs_structures.platform.IRegistryPlatform;
//?}
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
//? if <1.21.11 {
import net.minecraft.core.MappedRegistry;
//?}
//? if >=26.1.2 {
/*import net.minecraft.core.MappedRegistry;
*///?}
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Supplier;

//? if <26.1.2 {
public class ResourcefulRegistriesImpl implements IRegistryPlatform {
//?} else {
/*public class ResourcefulRegistriesImpl implements IResourcefulRegistriesProvider {
*///?}

    @Override
    public <T> ResourcefulRegistry<T> create(Registry<T> registry, String id) {
        return new CustomResourcefulRegistry<>(registry, id);
    }

    @Override
    //? if >=26.1.2 {
    /*@SuppressWarnings("unchecked")
    *///?}
    public <T, K extends Registry<T>> Pair<Supplier<CustomRegistryLookup<T>>, ResourcefulRegistry<T>> createCustomRegistryInternal(String modId, ResourceKey<K> key, boolean save, boolean sync, boolean allowModification) {
        //? if <1.21.11 {
        FabricRegistryBuilder<T, MappedRegistry<T>> registry = FabricRegistryBuilder.createSimple(null, key.location());
        //?}
        //? if >=1.21.11 <26.1.2 {
        /*@SuppressWarnings("unchecked")
        var registry = FabricRegistryBuilder.createSimple((ResourceKey<Registry<T>>) (Object) key);
        *///?}
        //? if <1.20.6 {
        /*if (save) registry.attribute(RegistryAttribute.PERSISTED);
        *///?}
        //? if <26.1.2 {
        if (sync) registry.attribute(RegistryAttribute.SYNCED);
        if (allowModification) registry.attribute(RegistryAttribute.MODDED);
        //?}
        //? if <1.21.11 {
        MappedRegistry<T> builtRegistry = registry.buildAndRegister();
        //?}
        //? if >=1.21.11 <26.1.2 {
        /*var builtRegistry = registry.buildAndRegister();
        *///?}
        //? if >=26.1.2 {
        /*FabricRegistryBuilder<T, MappedRegistry<T>> registryBuilder = FabricRegistryBuilder.create((ResourceKey<Registry<T>>) (ResourceKey<?>) key);
        if (sync) registryBuilder.attribute(RegistryAttribute.SYNCED);
        MappedRegistry<T> builtRegistry = registryBuilder.buildAndRegister();
        *///?}
        CustomRegistry<T> customRegistry = new CustomRegistry<>(builtRegistry);
        return Pair.of(() -> customRegistry, new CustomResourcefulRegistry<>(builtRegistry, modId));
    }
}
