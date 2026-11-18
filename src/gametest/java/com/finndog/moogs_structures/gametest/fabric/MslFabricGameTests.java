package com.finndog.moogs_structures.gametest.fabric;

import com.finndog.moogs_structures.gametest.EntityFacingCheck;
import com.finndog.moogs_structures.gametest.SpawnerMobNbtCheck;
//? if <1.21.5 {
import net.minecraft.gametest.framework.GameTest;
//?} else {
/*import net.fabricmc.fabric.api.gametest.v1.GameTest;
*///?}
import net.minecraft.gametest.framework.GameTestHelper;
//? if >=1.21.5 <1.21.11 {
/*import net.minecraft.network.chat.Component;
*///?}

import java.util.List;

/**
 * MSL's game tests on Fabric, registered by this dev-only mod's {@code fabric-gametest} entrypoint and run with
 * {@code ./gradlew :<node>:runGametest}. On Fabric MSL places a template's entities itself whenever one of its entity
 * processors is in the placement, so this is where that code is checked against the game's.
 */
public class MslFabricGameTests {
    //? if <1.21.5 {
    @GameTest(template = "moogs_structures:msl_arena")
    //?} else {
    /*@GameTest(structure = "moogs_structures:msl_arena", maxTicks = 100)
    *///?}
    public void entitiesFaceAsTheGamePlacesThem(GameTestHelper helper) {
        finish(helper, EntityFacingCheck.run(helper));
    }

    //? if <1.21.5 {
    @GameTest(template = "moogs_structures:msl_arena")
    //?} else {
    /*@GameTest(structure = "moogs_structures:msl_arena", maxTicks = 100)
    *///?}
    public void spawnerMobsKeepTheirNbt(GameTestHelper helper) {
        finish(helper, SpawnerMobNbtCheck.run(helper));
    }

    private static void finish(GameTestHelper helper, List<String> wrong) {
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
