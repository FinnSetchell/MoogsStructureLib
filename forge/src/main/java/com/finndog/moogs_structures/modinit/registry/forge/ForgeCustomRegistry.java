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

    private final Supplier<IForgeRegistry<T>> registrySupplier;

    public ForgeCustomRegistry(Supplier<IForgeRegistry<T>> registrySupplier) {
        this.registrySupplier = registrySupplier;
    }

    private IForgeRegistry<T> reg() {
        return registrySupplier.get();
    }

    @Override
    public boolean containsKey(ResourceLocation id) {
        return reg().containsKey(id);
    }

    @Override
    public @Nullable T get(ResourceLocation id) {
        return reg().getValue(id);
    }

    @Override
    public Collection<T> getValues() {
        return reg().getValues();
    }

    @Override
    public Collection<ResourceLocation> getKeys() {
        return reg().getKeys();
    }

    @Override
    public @Nullable ResourceLocation getKey(Object value) {
        return reg().getKey((T) value);
    }

    @Override
    public boolean containsValue(Object value) {
        return reg().getValues().contains(value);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return reg().getValues().iterator();
    }
}
