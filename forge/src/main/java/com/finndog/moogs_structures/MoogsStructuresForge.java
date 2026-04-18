package com.finndog.moogs_structures;

import com.finndog.moogs_structures.commands.DebugCommand;
import com.finndog.moogs_structures.events.lifecycle.RegisterReloadListenerEvent;
import com.finndog.moogs_structures.events.lifecycle.ServerGoingToStartEvent;
import com.finndog.moogs_structures.events.lifecycle.ServerGoingToStopEvent;
import com.finndog.moogs_structures.events.lifecycle.SetupEvent;
import com.finndog.moogs_structures.modinit.registry.forge.ResourcefulRegistriesImpl;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.NewRegistryEvent;

@Mod(MoogsStructuresCommon.MODID)
public class MoogsStructuresForge {

    public static BusGroup modBusGroupTempHolder = null;

    public MoogsStructuresForge(FMLJavaModLoadingContext context) {
        BusGroup modBusGroup = context.getModBusGroup();
        NewRegistryEvent.BUS.addListener(ResourcefulRegistriesImpl::onRegisterForgeRegistries);

        modBusGroupTempHolder = modBusGroup;
        MoogsStructuresCommon.init();
        modBusGroupTempHolder = null;

        FMLCommonSetupEvent.getBus(modBusGroup).addListener(MoogsStructuresForge::onSetup);
        ServerAboutToStartEvent.BUS.addListener(MoogsStructuresForge::onServerStarting);
        ServerStoppingEvent.BUS.addListener(MoogsStructuresForge::onServerStopping);
        AddReloadListenerEvent.BUS.addListener(MoogsStructuresForge::onAddReloadListeners);
        RegisterCommandsEvent.BUS.addListener(MoogsStructuresForge::onRegisterCommands);
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
