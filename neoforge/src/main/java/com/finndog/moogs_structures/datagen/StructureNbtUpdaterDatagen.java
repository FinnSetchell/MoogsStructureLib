package com.finndog.moogs_structures.datagen;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import net.minecraft.data.DataGenerator;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

// Source: https://github.com/BluSunrize/ImmersiveEngineering/blob/1.20.1/src/datagen/java/blusunrize/immersiveengineering/data/IEDataGenerator.java
@EventBusSubscriber(modid = MoogsStructuresCommon.MODID, bus = EventBusSubscriber.Bus.MOD)
public class StructureNbtUpdaterDatagen {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Server event) {
        ResourceManager resourceManager = event.getResourceManager(PackType.SERVER_DATA);
        DataGenerator gen = event.getGenerator();
        final var output = gen.getPackOutput();
        gen.addProvider(true, new StructureNbtUpdater("structure", MoogsStructuresCommon.MODID, resourceManager, output));
    }
}
