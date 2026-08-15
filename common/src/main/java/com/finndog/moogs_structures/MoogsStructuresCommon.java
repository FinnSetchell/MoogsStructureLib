package com.finndog.moogs_structures;

import com.finndog.moogs_structures.events.lifecycle.RegisterReloadListenerEvent;
import com.finndog.moogs_structures.events.lifecycle.ServerGoingToStartEvent;
import com.finndog.moogs_structures.events.lifecycle.ServerGoingToStopEvent;
import com.finndog.moogs_structures.events.lifecycle.SetupEvent;
import com.finndog.moogs_structures.config.ReplaceVanillaManager;
import com.finndog.moogs_structures.config.StructureListManager;
import com.finndog.moogs_structures.config.StructureManifestReloadListener;
import com.finndog.moogs_structures.misc.structurepiececounter.StructurePieceCountsManager;
import com.finndog.moogs_structures.misc.trialspawnerconfig.TrialSpawnerConfigManager;
import com.finndog.moogs_structures.modinit.MoogsStructuresPlacements;
import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
import com.finndog.moogs_structures.modinit.MoogsStructuresStructurePieces;
import com.finndog.moogs_structures.modinit.MoogsStructuresStructurePlacementType;
import com.finndog.moogs_structures.modinit.MoogsStructuresStructures;
import com.finndog.moogs_structures.modinit.MoogsStructuresTags;
import com.finndog.moogs_structures.world.structures.placements.AdvancedRandomSpread;
import com.finndog.moogs_structures.world.structures.placements.ConditionalConcentricRings;
import com.finndog.moogs_structures.utils.AsyncLocator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;


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

        ReplaceVanillaManager.init();
        StructureListManager.init();

        SetupEvent.EVENT.addListener(MoogsStructuresCommon::setup);
        RegisterReloadListenerEvent.EVENT.addListener(MoogsStructuresCommon::registerDatapackListener);
        ServerGoingToStartEvent.EVENT.addListener(MoogsStructuresCommon::serverAboutToStart);
        ServerGoingToStopEvent.EVENT.addListener(MoogsStructuresCommon::onServerStopping);
    }

    private static void setup(final SetupEvent event) {
    }

    private static void serverAboutToStart(final ServerGoingToStartEvent event) {
        ReplaceVanillaManager.reloadConfig();
        stampOwningSetIds(event.getServer());
        AsyncLocator.handleServerAboutToStartEvent();
    }

    /**
     * Stamps each Moog's placement with the id of the structure_set it belongs to, so a set needs no
     * explicit spacing_key/structure_id in its JSON to get a working spacing slider and disable toggle.
     * Runs once on the server thread before any chunk generates; the placements' stamped fields are
     * volatile, so worldgen threads see the values.
     */
    private static void stampOwningSetIds(MinecraftServer server) {
        var registry = server.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET);
        for (Map.Entry<ResourceKey<StructureSet>, StructureSet> entry : registry.entrySet()) {
            ResourceLocation id = entry.getKey().location();
            StructurePlacement placement = entry.getValue().placement();
            if (placement instanceof AdvancedRandomSpread ars) {
                ars.setOwningSetId(id);
            } else if (placement instanceof ConditionalConcentricRings ccr) {
                ccr.setOwningSetId(id);
            }
        }
    }

    private static void onServerStopping(final ServerGoingToStopEvent event) {
        AsyncLocator.handleServerStoppingEvent();
    }

    public static void registerDatapackListener(final RegisterReloadListenerEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath(MODID, "trial_spawner_config_manager"), TrialSpawnerConfigManager.INSTANCE);
        event.register(ResourceLocation.fromNamespaceAndPath(MODID, "structure_manifests"), new StructureManifestReloadListener());
        // Without this the msl_pieces_spawn_counts data is never loaded, so per-piece spawn counts are inert.
        event.register(ResourceLocation.fromNamespaceAndPath(MODID, "msl_pieces_spawn_counts"), StructurePieceCountsManager.STRUCTURE_PIECE_COUNTS_MANAGER);
    }
}
