package com.finndog.moogs_structures.neoforge;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.commands.DebugCommand;
import com.finndog.moogs_structures.events.lifecycle.RegisterReloadListenerEvent;
import com.finndog.moogs_structures.events.lifecycle.ServerGoingToStartEvent;
import com.finndog.moogs_structures.events.lifecycle.ServerGoingToStopEvent;
import com.finndog.moogs_structures.events.lifecycle.SetupEvent;
import com.finndog.moogs_structures.modinit.registry.neoforge.ResourcefulRegistriesImpl;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;


@Mod(MoogsStructuresCommon.MODID)
public class MoogsStructuresNeoforge {

    public static IEventBus modEventBusTempHolder = null;

    public MoogsStructuresNeoforge(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(EventPriority.NORMAL, ResourcefulRegistriesImpl::onRegisterForgeRegistries);

        modEventBusTempHolder = modEventBus;
        MoogsStructuresCommon.init();
        modEventBusTempHolder = null;

        modEventBus.addListener(MoogsStructuresNeoforge::onSetup);
        modEventBus.addListener(MoogsStructuresNeoforge::registerGameTests);

        IEventBus eventBus = NeoForge.EVENT_BUS;
        eventBus.addListener(MoogsStructuresNeoforge::onServerStarting);
        eventBus.addListener(MoogsStructuresNeoforge::onServerStopping);
        eventBus.addListener(MoogsStructuresNeoforge::onAddReloadListeners);
        eventBus.addListener(MoogsStructuresNeoforge::onRegisterCommands);
    }

    // Register game tests when the test class is on the classpath (dev/gametest only).
    // Class.forName silently fails in production where the test class is absent.
    private static void registerGameTests(RegisterGameTestsEvent event) {
        try {
            event.register(Class.forName("com.finndog.moogs_structures.gametest.EquipArmorStandProcessorTest"));
        } catch (ClassNotFoundException ignored) {
        }
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
