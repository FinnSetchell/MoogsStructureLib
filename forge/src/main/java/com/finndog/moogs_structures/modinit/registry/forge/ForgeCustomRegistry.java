package com.finndog.moogs_structures.modinit.registry.forge;

import com.finndog.moogs_structures.modinit.registry.CustomRegistryLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

public class ForgeCustomRegistry<T> implements CustomRegistryLookup<T> {

    private final Registry<T> registry;

    public ForgeCustomRegistry(Registry<T> registry) {
        this.registry = registry;
    }

    @Override
    public boolean containsKey(Identifier id) {
        return registry.containsKey(id);
    }

    @Override
    public @Nullable T get(Identifier id) {
        return registry.get(id).map(net.minecraft.core.Holder.Reference::value).orElse(null);
    }

    @Override
    public Collection<T> getValues() {
        return registry.stream().collect(Collectors.toList());
    }

    @Override
    public Collection<Identifier> getKeys() {
        return registry.keySet();
    }

    @Override
    public @Nullable Identifier getKey(Object value) {
        return registry.getKey((T) value);
    }

    @Override
    public boolean containsValue(Object value) {
        return registry.containsValue((T) value);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return registry.iterator();
    }
}
