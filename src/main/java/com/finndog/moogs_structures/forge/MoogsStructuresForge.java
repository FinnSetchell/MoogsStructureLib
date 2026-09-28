package com.finndog.moogs_structures.forge;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.commands.DebugCommand;
import com.finndog.moogs_structures.events.lifecycle.RegisterReloadListenerEvent;
import com.finndog.moogs_structures.events.lifecycle.ServerGoingToStartEvent;
import com.finndog.moogs_structures.events.lifecycle.ServerGoingToStopEvent;
import com.finndog.moogs_structures.events.lifecycle.SetupEvent;
//? if <1.21.5 {
import com.finndog.moogs_structures.forge.client.MoogsStructuresForgeClient;
//?}
import com.finndog.moogs_structures.modinit.registry.forge.ResourcefulRegistriesImpl;
//? if <1.21.5 {
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
//?}
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
//? if <1.21.5 {
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
//?} else {
/*import net.minecraftforge.eventbus.api.bus.BusGroup;
*///?}
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
//? if >=1.21.5 {
/*import net.minecraftforge.registries.NewRegistryEvent;
*///?}

@Mod(MoogsStructuresCommon.MODID)
public class MoogsStructuresForge {

    //? if <1.21.5 {
    public static IEventBus modEventBusTempHolder = null;
    //?} else {
    /*public static BusGroup modBusGroupTempHolder = null;
    *///?}

    //? if <1.21.5 {
    public MoogsStructuresForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(EventPriority.NORMAL, ResourcefulRegistriesImpl::onRegisterForgeRegistries);
    //?} else {
    /*public MoogsStructuresForge(FMLJavaModLoadingContext context) {
        BusGroup modBusGroup = context.getModBusGroup();
        NewRegistryEvent.BUS.addListener(ResourcefulRegistriesImpl::onRegisterForgeRegistries);
    *///?}

        //? if <1.21.5 {
        modEventBusTempHolder = modEventBus;
        //?} else {
        /*modBusGroupTempHolder = modBusGroup;
        *///?}
        MoogsStructuresCommon.init();
        //? if <1.21.5 {
        modEventBusTempHolder = null;
        //?} else {
        /*modBusGroupTempHolder = null;
        *///?}

        //? if <1.21.5 {
        modEventBus.addListener(MoogsStructuresForge::onSetup);

        IEventBus eventBus = MinecraftForge.EVENT_BUS;
        eventBus.addListener(MoogsStructuresForge::onServerStarting);
        eventBus.addListener(MoogsStructuresForge::onServerStopping);
        eventBus.addListener(MoogsStructuresForge::onAddReloadListeners);
        eventBus.addListener(MoogsStructuresForge::onRegisterCommands);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> MoogsStructuresForgeClient::registerConfigScreen);
        //?} else {
        /*FMLCommonSetupEvent.getBus(modBusGroup).addListener(MoogsStructuresForge::onSetup);
        ServerAboutToStartEvent.BUS.addListener(MoogsStructuresForge::onServerStarting);
        ServerStoppingEvent.BUS.addListener(MoogsStructuresForge::onServerStopping);
        AddReloadListenerEvent.BUS.addListener(MoogsStructuresForge::onAddReloadListeners);
        RegisterCommandsEvent.BUS.addListener(MoogsStructuresForge::onRegisterCommands);
        *///?}
    }

    private static void onSetup(FMLCommonSetupEvent event) {
        SetupEvent.EVENT.invoke(new SetupEvent(event::enqueueWork));
    }

    private static void onServerStarting(ServerAboutToStartEvent event) {
        ServerGoingToStartEvent.EVENT.invoke(new ServerGoingToStartEvent(event.getServer()));
    }

    private static void onServerStopping(ServerStoppingEvent event) {
        ServerGoingToStopEvent.EVENT.invoke(ServerGoingToStopEvent.INSTANCE);
    }

    private static void onAddReloadListeners(AddReloadListenerEvent event) {
        RegisterReloadListenerEvent.EVENT.invoke(new RegisterReloadListenerEvent((id, listener) -> event.addListener(listener)));
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        DebugCommand.register(event.getDispatcher());
    }
}
