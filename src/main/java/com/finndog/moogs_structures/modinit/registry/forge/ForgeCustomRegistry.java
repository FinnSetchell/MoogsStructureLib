package com.finndog.moogs_structures.modinit.registry.forge;

import com.finndog.moogs_structures.modinit.registry.CustomRegistryLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Supplier;

public class ForgeCustomRegistry<T> implements CustomRegistryLookup<T> {

    //? if >=1.21.1 {
    private final Supplier<IForgeRegistry<T>> registrySupplier;
    //?} else {
    /*private final Supplier<IForgeRegistry<T>> registry;
    *///?}

    //? if >=1.21.1 {
    public ForgeCustomRegistry(Supplier<IForgeRegistry<T>> registrySupplier) {
        this.registrySupplier = registrySupplier;
    }

    private IForgeRegistry<T> reg() {
        return registrySupplier.get();
    //?} else {
    /*public ForgeCustomRegistry(Supplier<IForgeRegistry<T>> registry) {
        this.registry = registry;
    *///?}
    }

    @Override
    public boolean containsKey(ResourceLocation id) {
        //? if >=1.21.1 {
        return reg().containsKey(id);
        //?} else {
        /*return registry.get().containsKey(id);
    }

    @Override
    public boolean containsValue(T value) {
        return registry.get().containsValue(value);
        *///?}
    }

    @Override
    public @Nullable T get(ResourceLocation id) {
        //? if >=1.21.1 {
        return reg().getValue(id);
        //?} else {
        /*return registry.get().getValue(id);
    }

    @Override
    public @Nullable ResourceLocation getKey(T value) {
        return registry.get().getKey(value);
        *///?}
    }

    @Override
    public Collection<T> getValues() {
        //? if >=1.21.1 {
        return reg().getValues();
        //?} else {
        /*return registry.get().getValues();
        *///?}
    }

    @Override
    public Collection<ResourceLocation> getKeys() {
        //? if >=1.21.1 {
        return reg().getKeys();
    }

    @Override
    public @Nullable ResourceLocation getKey(Object value) {
        return reg().getKey((T) value);
    }

    @Override
    public boolean containsValue(Object value) {
        return reg().getValues().contains(value);
        //?} else {
        /*return registry.get().getKeys();
        *///?}
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        //? if >=1.21.1 {
        return reg().getValues().iterator();
        //?} else {
        /*return registry.get().iterator();
        *///?}
    }
}
