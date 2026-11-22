//? if >=1.20.6 {
package com.finndog.moogs_structures.gametest.neoforge;

import com.finndog.moogs_structures.gametest.EntityFacingCheck;
import com.mojang.serialization.MapCodec;
//? if <1.21.5 {
import net.minecraft.gametest.framework.GameTest;
//?}
import net.minecraft.gametest.framework.GameTestHelper;
//? if >=1.21.5 {
/*import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
*///?}
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
//? if <1.21.5 {
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
//?}

import java.util.List;

/** {@link EntityFacingCheck} on NeoForge, where MSL's entity processors run inside the game's own placement. */
//? if <1.21.5 {
@GameTestHolder("moogs_structures")
@PrefixGameTestTemplate(false)
//?}
//? if <1.21.10 {
@EventBusSubscriber(modid = "moogs_structures", bus = EventBusSubscriber.Bus.MOD)
//?} else {
/*@EventBusSubscriber(modid = "moogs_structures")
*///?}
public class EntityFacingTests {
    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        // Only in a game test run: a registered test instance has to sync to joining clients (1.21.5+).
        if (System.getProperty("neoforge.enabledGameTestNamespaces") == null) {
            return;
        }
        //? if <1.21.5 {
        event.register(EntityFacingTests.class);
        //?} else {
        /*var env = event.registerEnvironment(id("entity_facing_tests"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(id("entities_face_as_the_game_places_them"), new GameTestInstance(new TestData<>(env, id("msl_arena"), 100, 0, true)) {
            @Override
            public void run(GameTestHelper helper) {
                entitiesFaceAsTheGamePlacesThem(helper);
            }

            @Override
            public MapCodec<? extends GameTestInstance> codec() {
                throw new UnsupportedOperationException();
            }

            @Override
            protected MutableComponent typeDescription() {
                return Component.literal("EntityFacingTests");
            }
        });
        *///?}
    }

    //? if >=1.21.5 {
    /*private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("moogs_structures", path);
    }

    *///?}
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = "msl_arena")
    //?}
    public static void entitiesFaceAsTheGamePlacesThem(GameTestHelper helper) {
        List<String> wrong = EntityFacingCheck.run(helper);
        if (wrong.isEmpty()) {
            helper.succeed();
            return;
        }
        String message = wrong.size() + " wrong: " + String.join("; ", wrong.subList(0, Math.min(8, wrong.size())));
        //? if >=1.21.5 <1.21.11 {
        /*helper.fail(Component.literal(message));
        *///?} else {
        helper.fail(message);
        //?}
    }
}
//?}
