package com.finndog.moogs_structures;

import com.finndog.moogs_structures.events.lifecycle.RegisterReloadListenerEvent;
import com.finndog.moogs_structures.events.lifecycle.ServerGoingToStartEvent;
import com.finndog.moogs_structures.events.lifecycle.ServerGoingToStopEvent;
import com.finndog.moogs_structures.events.lifecycle.SetupEvent;
import com.finndog.moogs_structures.config.ReplaceVanillaManager;
import com.finndog.moogs_structures.config.StructureListManager;
import com.finndog.moogs_structures.config.StructureManifestReloadListener;
import com.finndog.moogs_structures.misc.structurepiececounter.StructurePieceCountsManager;
//? if >=1.21.1 <26.1.2 {
import com.finndog.moogs_structures.misc.trialspawnerconfig.TrialSpawnerConfigManager;
//?}
//? if <1.21.1 {
/*import com.finndog.moogs_structures.world.structures.placements.AdvancedRandomSpread;
import com.finndog.moogs_structures.world.structures.placements.ConditionalConcentricRings;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
*///?}
import com.finndog.moogs_structures.modinit.MoogsStructuresPlacements;
import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
import com.finndog.moogs_structures.modinit.MoogsStructuresStructurePieces;
import com.finndog.moogs_structures.modinit.MoogsStructuresStructurePlacementType;
import com.finndog.moogs_structures.modinit.MoogsStructuresStructures;
import com.finndog.moogs_structures.modinit.MoogsStructuresTags;
//? if >=1.21.1 {
import com.finndog.moogs_structures.world.structures.placements.AdvancedRandomSpread;
import com.finndog.moogs_structures.world.structures.placements.ConditionalConcentricRings;
//?}
import com.finndog.moogs_structures.utils.AsyncLocator;
//? if >=1.21.1 {
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
//?}
import com.finndog.moogs_structures.world.structures.GenericJigsawStructure;
import com.finndog.moogs_structures.world.structures.terrainadaptation.AdaptationArea;
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
        //? if <26.3 {
        AdaptationArea.setServerTemplates(event.getServer().getStructureManager());
        //?} else {
        /*AdaptationArea.setServerTemplates(event.getServer().getStructureTemplateManager());
        *///?}
        AsyncLocator.handleServerAboutToStartEvent();
    }

    /**
     * Stamps each Moog's placement with the id of the structure_set it belongs to, so a set needs no
     * explicit spacing_key/structure_id in its JSON to get a working spacing slider and disable toggle.
     * Runs once on the server thread before any chunk generates; the placements' stamped fields are
     * volatile, so worldgen threads see the values.
     */
    private static void stampOwningSetIds(MinecraftServer server) {
        //? if <1.21.2 {
        var registry = server.registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
        //?} else {
        /*var registry = server.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET);
        *///?}
        for (Map.Entry<ResourceKey<StructureSet>, StructureSet> entry : registry.entrySet()) {
            //? if <1.21.11 {
            ResourceLocation id = entry.getKey().location();
            //?} else {
            /*ResourceLocation id = entry.getKey().identifier();
            *///?}
            StructurePlacement placement = entry.getValue().placement();
            if (placement instanceof AdvancedRandomSpread ars) {
                ars.setOwningSetId(id);
                ars.setNoStructureOnThisVersion(noStructureOnThisVersion(entry.getValue()));
            } else if (placement instanceof ConditionalConcentricRings ccr) {
                ccr.setOwningSetId(id);
            }
        }
    }

    /**
     * A set none of whose structures generates on this Minecraft version (mc_versions). The game already skips such
     * a set itself, as it has no biome; this also stops it pushing other sets away through their exclusion zones.
     * A concentric rings set needs nothing: the game gives it no ring positions.
     */
    private static boolean noStructureOnThisVersion(StructureSet set) {
        return !set.structures().isEmpty() && set.structures().stream()
                .allMatch(entry -> entry.structure().value() instanceof GenericJigsawStructure structure && !structure.generatesOnThisVersion());
    }

    private static void onServerStopping(final ServerGoingToStopEvent event) {
        AsyncLocator.handleServerStoppingEvent();
    }

    public static void registerDatapackListener(final RegisterReloadListenerEvent event) {
        //? if >=1.21.1 <26.1.2 {
        event.register(ResourceLocation.fromNamespaceAndPath(MODID, "trial_spawner_config_manager"), TrialSpawnerConfigManager.INSTANCE);
        //?}
        //? if >=1.21.1 {
        event.register(ResourceLocation.fromNamespaceAndPath(MODID, "structure_manifests"), new StructureManifestReloadListener());
        //?} else {
        /*event.register(new ResourceLocation(MODID, "structure_manifests"), new StructureManifestReloadListener());
        *///?}
        // Without this the msl_pieces_spawn_counts data is never loaded, so per-piece spawn counts are inert.
        //? if >=1.21.1 {
        event.register(ResourceLocation.fromNamespaceAndPath(MODID, "msl_pieces_spawn_counts"), StructurePieceCountsManager.STRUCTURE_PIECE_COUNTS_MANAGER);
        //?} else {
        /*event.register(new ResourceLocation(MODID, "msl_pieces_spawn_counts"), StructurePieceCountsManager.STRUCTURE_PIECE_COUNTS_MANAGER);
        *///?}
    }
}
