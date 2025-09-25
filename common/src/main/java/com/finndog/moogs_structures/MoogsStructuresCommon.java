package com.finndog.moogs_structures;

import com.finndog.moogs_structures.events.lifecycle.RegisterReloadListenerEvent;
import com.finndog.moogs_structures.events.lifecycle.ServerGoingToStartEvent;
import com.finndog.moogs_structures.events.lifecycle.ServerGoingToStopEvent;
import com.finndog.moogs_structures.events.lifecycle.SetupEvent;
import com.finndog.moogs_structures.modinit.MoogsStructuresPlacements;
import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
import com.finndog.moogs_structures.modinit.MoogsStructuresStructurePieces;
import com.finndog.moogs_structures.modinit.MoogsStructuresStructurePlacementType;
import com.finndog.moogs_structures.modinit.MoogsStructuresStructures;
import com.finndog.moogs_structures.modinit.MoogsStructuresTags;
import com.finndog.moogs_structures.utils.AsyncLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class MoogsStructuresCommon {
    public static final String MODID = "moogs_structures";
    public static final Logger LOGGER = LogManager.getLogger();

    public static void init() {
        MoogsStructuresTags.initTags();

        MoogsStructuresStructures.STRUCTURE_TYPE.init();
        MoogsStructuresPlacements.PLACEMENT_MODIFIER.init();
        MoogsStructuresProcessors.STRUCTURE_PROCESSOR.init();
        MoogsStructuresStructurePieces.STRUCTURE_PIECE.init();
        MoogsStructuresStructurePieces.STRUCTURE_POOL_ELEMENT.init();
        MoogsStructuresStructurePlacementType.STRUCTURE_PLACEMENT_TYPE.init();

        SetupEvent.EVENT.addListener(MoogsStructuresCommon::setup);
        RegisterReloadListenerEvent.EVENT.addListener(MoogsStructuresCommon::registerDatapackListener);
        ServerGoingToStartEvent.EVENT.addListener(MoogsStructuresCommon::serverAboutToStart);
        ServerGoingToStopEvent.EVENT.addListener(MoogsStructuresCommon::onServerStopping);
    }

    private static void setup(final SetupEvent event) {
    }

    private static void serverAboutToStart(final ServerGoingToStartEvent event) {

        AsyncLocator.handleServerAboutToStartEvent();
    }

    private static void onServerStopping(final ServerGoingToStopEvent event) {
        AsyncLocator.handleServerStoppingEvent();
    }

    public static void registerDatapackListener(final RegisterReloadListenerEvent event) {
    }
}
