package com.finndog.moogs_structures.modinit.registry.forge;

import com.finndog.moogs_structures.modinit.registry.RegistryEntries;
import com.finndog.moogs_structures.modinit.registry.RegistryEntry;
import com.finndog.moogs_structures.modinit.registry.ResourcefulRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.function.Supplier;

public class ForgeResourcefulRegistry<T> implements ResourcefulRegistry<T> {

    private final DeferredRegister<T> register;
    private final RegistryEntries<T> entries = new RegistryEntries<>();

    public ForgeResourcefulRegistry(ResourceKey<? extends Registry<T>> registry, String id) {
        this.register = DeferredRegister.create(registry, id);
    }

    public ForgeResourcefulRegistry(Registry<T> registry, String id) {
        this.register = DeferredRegister.create(registry.key(), id);
    }

    @Override
    public <I extends T> RegistryEntry<I> register(String id, Supplier<I> supplier) {
        return this.entries.add(new ForgeRegistryEntry<>(register.register(id, supplier)));
    }

    @Override
    public Collection<RegistryEntry<T>> getEntries() {
        return this.entries.getEntries();
    }

    @Override
    public void init() {
        register.register(MoogsStructuresForge.modEventBusTempHolder);
    }
}
