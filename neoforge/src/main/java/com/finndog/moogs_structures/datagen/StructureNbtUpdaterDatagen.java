package com.finndog.moogs_structures.datagen;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import net.minecraft.data.DataGenerator;
import net.minecraft.server.packs.PackType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

// Source: https://github.com/BluSunrize/ImmersiveEngineering/blob/1.20.1/src/datagen/java/blusunrize/immersiveengineering/data/IEDataGenerator.java
@EventBusSubscriber(modid = MoogsStructuresCommon.MODID)
public class StructureNbtUpdaterDatagen {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Server event) {
        DataGenerator gen = event.getGenerator();
        final var output = gen.getPackOutput();

        // Use the server data resource manager directly; ExistingFileHelper is no longer required here.
        final var resourceManager = event.getResourceManager(PackType.SERVER_DATA);
        gen.addProvider(true, new StructureNbtUpdater("structures", MoogsStructuresCommon.MODID, output, resourceManager));
    }
}
