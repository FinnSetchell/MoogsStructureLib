package com.finndog.moogs_structures.modinit.registry.forge;

import com.finndog.moogs_structures.modinit.registry.CustomRegistryLookup;
import com.finndog.moogs_structures.modinit.registry.ResourcefulRegistry;
//? if <26.1.2 {
import com.finndog.moogs_structures.platform.IRegistryPlatform;
//?} else {
/*import com.finndog.moogs_structures.modinit.registry.IResourcefulRegistriesProvider;
*///?}
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

//? if <26.1.2 {
public class ResourcefulRegistriesImpl implements IRegistryPlatform {
//?} else {
/*public class ResourcefulRegistriesImpl implements IResourcefulRegistriesProvider {
*///?}

    //? if >=1.21.1 {
    private static final List<CustomRegistryInfo<?, ?>> CUSTOM_REGISTRIES = new ArrayList<>();
    //?} else {
    /*private static final List<CustomRegistryInfo<?>> CUSTOM_REGISTRIES = new ArrayList<>();
    *///?}

    @Override
    public <T> ResourcefulRegistry<T> create(Registry<T> registry, String id) {
        //? if >=1.21.1 {
        return new ForgeResourcefulRegistry<>(registry, id);
        //?} else {
        /*return new ForgeResourcefulRegistry<>(registry.key(), id);
        *///?}
    }

    @Override
    public <T, K extends Registry<T>> Pair<Supplier<CustomRegistryLookup<T>>, ResourcefulRegistry<T>> createCustomRegistryInternal(String modId, ResourceKey<K> key, boolean save, boolean sync, boolean allowModification) {
        //? if >=1.21.1 {
        CustomRegistryInfo<T, T> info = new CustomRegistryInfo<>(new LateSupplier<>(), key, save, sync, allowModification);
        //?} else {
        /*CustomRegistryInfo<T> info = new CustomRegistryInfo<>(new LateSupplier<>(), key, save, sync, allowModification);
        *///?}
        CUSTOM_REGISTRIES.add(info);
        return Pair.of(info.lookup(), new ForgeResourcefulRegistry<>(key, modId));
    }

    public static void onRegisterForgeRegistries(NewRegistryEvent event) {
        CUSTOM_REGISTRIES.forEach(registry -> registry.build(event));
    }

    public static class LateSupplier<T> implements Supplier<T> {
        private T value;
        private boolean initialized = false;

        public void set(T value) {
            this.value = value;
            this.initialized = true;
        }

        @Override
        public T get() {
            if (!initialized) {
                throw new IllegalStateException("LateSupplier not initialized");
            }
            return value;
        }
    }

    //? if >=1.21.1 {
    public record CustomRegistryInfo<T, K extends T>(
    //?} else {
    /*public record CustomRegistryInfo<T>(
    *///?}
            LateSupplier<CustomRegistryLookup<T>> lookup,
            ResourceKey<? extends Registry<T>> key,
            boolean save,
            boolean sync,
            boolean allowModification
    ) {

        public void build(NewRegistryEvent event) {
            lookup.set(new ForgeCustomRegistry<>(event.create(getBuilder())));
        }

        public RegistryBuilder<T> getBuilder() {
            //? if >=1.21.1 {
            RegistryBuilder<T> builder = new RegistryBuilder<T>()
            //?}
                    //? if >=1.21.1 <1.21.11 {
                    .setName(key.location());
                    //?}
            //? if <1.21.1 {
            /*RegistryBuilder<T> builder = new RegistryBuilder<>();
            builder.setName(key.location());
            if (!save) builder.disableSaving();
            *///?}
                    //? if >=1.21.11 {
                    /*.setName(key.identifier());
                    *///?}
            if (!sync) builder.disableSync();
            //? if <1.21.1 {
            /*if (allowModification) builder.allowModification();
            *///?}
            return builder;
        }
    }
}
