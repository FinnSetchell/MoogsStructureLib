package com.finndog.moogs_structures.modinit.registry.forge;

//? if >=1.21.1 {
import com.finndog.moogs_structures.forge.MoogsStructuresForge;
//?}
import com.finndog.moogs_structures.modinit.registry.RegistryEntries;
import com.finndog.moogs_structures.modinit.registry.RegistryEntry;
import com.finndog.moogs_structures.modinit.registry.ResourcefulRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
//? if <1.21.1 {
/*import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
*///?}
import net.minecraftforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.function.Supplier;

public class ForgeResourcefulRegistry<T> implements ResourcefulRegistry<T> {

    private final DeferredRegister<T> register;
    private final RegistryEntries<T> entries = new RegistryEntries<>();

    public ForgeResourcefulRegistry(ResourceKey<? extends Registry<T>> registry, String id) {
        this.register = DeferredRegister.create(registry, id);
    }

    //? if >=1.21.1 {
    public ForgeResourcefulRegistry(Registry<T> registry, String id) {
        this.register = DeferredRegister.create(registry.key(), id);
    }

    //?}
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
        //? if >=1.21.1 <1.21.5 {
        register.register(MoogsStructuresForge.modEventBusTempHolder);
        //?}
        //? if <1.21.1 {
        /*IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        register.register(bus);
        *///?}
        //? if >=1.21.5 {
        /*register.register(MoogsStructuresForge.modBusGroupTempHolder);
        *///?}
    }
}
