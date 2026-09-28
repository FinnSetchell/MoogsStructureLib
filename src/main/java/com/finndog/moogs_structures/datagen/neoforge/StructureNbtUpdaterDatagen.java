package com.finndog.moogs_structures.datagen.neoforge;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import net.minecraft.data.DataGenerator;
//? if >=1.21.4 {
/*import net.minecraft.server.packs.PackType;
*///?}
//? if >=1.21.2 {
/*import net.minecraft.server.packs.resources.ResourceManager;
*///?}
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
//? if <1.21.2 {
import net.neoforged.neoforge.common.data.ExistingFileHelper;
//?}
import net.neoforged.neoforge.data.event.GatherDataEvent;

// Source: https://github.com/BluSunrize/ImmersiveEngineering/blob/1.20.1/src/datagen/java/blusunrize/immersiveengineering/data/IEDataGenerator.java
@EventBusSubscriber(modid = MoogsStructuresCommon.MODID, bus = EventBusSubscriber.Bus.MOD)
public class StructureNbtUpdaterDatagen {

    @SubscribeEvent
    //? if <1.21.4 {
    public static void gatherData(GatherDataEvent event) {
    //?}
        //? if <1.21.2 {
        ExistingFileHelper exHelper = event.getExistingFileHelper();
        //?}
        //? if >=1.21.2 <1.21.4 {
        /*if (!event.includeServer()) {
            return;
        }
        ResourceManager resourceManager = (ResourceManager) event.getExistingFileHelper();
        *///?}
    //? if >=1.21.4 {
    /*public static void gatherData(GatherDataEvent.Server event) {
        ResourceManager resourceManager = event.getResourceManager(PackType.SERVER_DATA);
    *///?}
        DataGenerator gen = event.getGenerator();
        final var output = gen.getPackOutput();

        //? if <1.21.2 {
        if (event.includeServer()) {
            gen.addProvider(true, new StructureNbtUpdater("structures", MoogsStructuresCommon.MODID, exHelper, output));
        }
        //?} else {
        /*gen.addProvider(true, new StructureNbtUpdater("structure", MoogsStructuresCommon.MODID, resourceManager, output));
        *///?}
    }
}
