//? if >=1.21.1 {
package com.finndog.moogs_structures.gametest.neoforge;

import com.finndog.moogs_structures.utils.GeneralUtils;
import com.finndog.moogs_structures.utils.VersionResolver;
import com.finndog.moogs_structures.world.processors.RailRotationProcessor;
import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptation;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
//? if >=1.21.5 {
/*import com.mojang.serialization.MapCodec;
*///?}
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
//? if >=1.21.2 {
/*import net.minecraft.world.entity.EntitySpawnReason;
*///?}
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
//? if <1.21.5 {
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
//?}

/**
 * MSL's deterministic checks, on a headless game test server: what its codecs refuse (and the message),
 * what its version ranges pick, and what its terrain-reading processors do to a template placed over
 * ground built in the test's own arena. Structures generating in a real world are the mod-director
 * suite's (mod-director/suites/msl) and the structure tester's.
 *
 * <p>Each test is one static method; below 1.21.5 the annotations register them, from 1.21.5 the event
 * registers the same methods as test instances. Run with {@code ./gradlew :<node>:runGameTestServer}.
 */
//? if <1.21.5 {
@GameTestHolder("moogs_structures")
@PrefixGameTestTemplate(false)
//?}
//? if <1.21.10 {
@EventBusSubscriber(modid = "moogs_structures", bus = EventBusSubscriber.Bus.MOD)
//?} else {
/*@EventBusSubscriber(modid = "moogs_structures")
*///?}
public class MslGameTests {
    /** An empty 24 x 16 x 24 arena of air, so each test starts on nothing it didn't build. */
    private static final String ARENA = "msl_arena";

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        // Only in a game test run: a registered test instance has to sync to joining clients (1.21.5+).
        if (System.getProperty("neoforge.enabledGameTestNamespaces") == null) {
            return;
        }
        //? if <1.21.5 {
        event.register(MslGameTests.class);
        //?} else {
        /*var env = event.registerEnvironment(id("msl_tests"), new TestEnvironmentDefinition.AllOf());
        for (Map.Entry<String, Consumer<GameTestHelper>> test : tests().entrySet()) {
            String name = test.getKey();
            event.registerTest(id(name), new GameTestInstance(new TestData<>(env, id(ARENA), 100, 0, true)) {
                @Override
                public void run(GameTestHelper helper) {
                    test.getValue().accept(helper);
                }

                @Override
                public MapCodec<? extends GameTestInstance> codec() {
                    throw new UnsupportedOperationException();
                }

                @Override
                protected MutableComponent typeDescription() {
                    return Component.literal("MslGameTests " + name);
                }
            });
        }
        *///?}
    }

    /** Every test by its name, for the 1.21.5+ registration. */
    static Map<String, Consumer<GameTestHelper>> tests() {
        Map<String, Consumer<GameTestHelper>> tests = new LinkedHashMap<>();
        tests.put("follow_floor_search_range", MslGameTests::followFloorSearchRange);
        tests.put("adaptation_area_refusals", MslGameTests::adaptationAreaRefusals);
        tests.put("terrain_adaptation_values", MslGameTests::terrainAdaptationValues);
        tests.put("versioned_element_needs_a_template", MslGameTests::versionedElementNeedsATemplate);
        tests.put("version_ranges", MslGameTests::versionRanges);
        tests.put("spread_spacing_above_separation", MslGameTests::spreadSpacingAboveSeparation);
        tests.put("nether_y_allowance_order", MslGameTests::netherYAllowanceOrder);
        tests.put("rail_rotation_every_shape", MslGameTests::railRotationEveryShape);
        tests.put("follow_floor_onto_a_step", MslGameTests::followFloorOntoAStep);
        tests.put("pillar_down_to_ground", MslGameTests::pillarDownToGround);
        tests.put("rail_slope_towards_higher_ground", MslGameTests::railSlopeTowardsHigherGround);
        tests.put("cached_height_matches_the_generator", MslGameTests::cachedHeightMatchesTheGenerator);
        tests.put("height_cache_lets_go_of_a_closed_world", MslGameTests::heightCacheLetsGoOfAClosedWorld);
        return tests;
    }

    // ------------------------------------------------------------------ codecs

    /** follow_floor's search_range is 1 to 64: 0 and 65 are refused with the range, 16 is the default. */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void followFloorSearchRange(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        String type = "{\"processor_type\":\"moogs_structures:follow_floor_processor\"";
        refused(helper, StructureProcessorType.SINGLE_CODEC, type + ",\"search_range\":0}", "outside of range [1:64]", wrong);
        refused(helper, StructureProcessorType.SINGLE_CODEC, type + ",\"search_range\":65}", "outside of range [1:64]", wrong);
        accepted(helper, StructureProcessorType.SINGLE_CODEC, type + ",\"search_range\":64}", wrong);
        accepted(helper, StructureProcessorType.SINGLE_CODEC, type + "}", wrong);
        accepted(helper, StructureProcessorType.SINGLE_CODEC, type + ",\"offset\":-1}", wrong);
        finish(helper, wrong);
    }

    /** A broken adaptation_area is refused on every version, naming what's wrong; a good one loads. */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void adaptationAreaRefusals(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        refused(helper, StructurePoolElement.CODEC, element("terrain_matching", "{\"columns\":[{\"x\":[0,2],\"z\":[0,2]}]}"),
                "adaptation_area needs \"projection\": \"rigid\"", wrong);
        refused(helper, StructurePoolElement.CODEC, element("rigid", "{\"columns\":[{\"x\":[5,2],\"z\":[0,2]}]}"), "min <= max", wrong);
        refused(helper, StructurePoolElement.CODEC, element("rigid", "{\"columns\":[]}"), "at least one rectangle", wrong);
        refused(helper, StructurePoolElement.CODEC, element("rigid", "{\"columns\":[{\"x\":[0,2],\"z\":[0,2]}],\"footprint\":{}}"),
                "exactly one of \"columns\" or \"footprint\"", wrong);
        refused(helper, StructurePoolElement.CODEC, element("rigid", "{}"), "exactly one of \"columns\" or \"footprint\"", wrong);
        refused(helper, StructurePoolElement.CODEC, element("rigid", "{\"footprint\":{\"layers\":0}}"), "outside of range [1:64]", wrong);
        refused(helper, StructurePoolElement.CODEC, element("rigid", "{\"footprint\":{\"margin\":17}}"), "outside of range [0:16]", wrong);
        accepted(helper, StructurePoolElement.CODEC, element("rigid", "{\"columns\":[{\"x\":[0,2],\"z\":[0,2]}],\"outside_follows_ground\":true}"), wrong);
        accepted(helper, StructurePoolElement.CODEC, element("rigid", "{\"footprint\":{\"layers\":2,\"margin\":1}}"), wrong);
        finish(helper, wrong);
    }

    /** enhanced_terrain_adaptation is "none" or a settings object (with a band and piece heights); any other text is refused. */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void terrainAdaptationValues(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        refused(helper, EnhancedTerrainAdaptation.CODEC, "\"flatten\"", "\"none\" or a settings object", wrong);
        accepted(helper, EnhancedTerrainAdaptation.CODEC, "\"none\"", wrong);
        accepted(helper, EnhancedTerrainAdaptation.CODEC, "{\"kernel_size\":12,\"kernel_distance\":6,\"top\":\"bury\",\"bottom\":\"carve\"}", wrong);
        accepted(helper, EnhancedTerrainAdaptation.CODEC,
                "{\"kernel_size\":12,\"kernel_distance\":6,\"top\":\"bury\",\"bottom\":\"none\",\"band\":{\"bottom\":-2,\"top\":3,\"piece_heights\":[5,7]}}", wrong);
        refused(helper, EnhancedTerrainAdaptation.CODEC, "{\"kernel_size\":12,\"kernel_distance\":6,\"top\":\"flatten\",\"bottom\":\"none\"}", "", wrong);
        // A piece's own value is read the same way, and "none" switches the structure's adaptation off for it.
        accepted(helper, StructurePoolElement.CODEC, versioned("\"location\":\"moogs_structures:msl_arena\",\"enhanced_terrain_adaptation\":\"none\""), wrong);
        refused(helper, StructurePoolElement.CODEC, versioned("\"location\":\"moogs_structures:msl_arena\",\"enhanced_terrain_adaptation\":\"flatten\""),
                "\"none\" or a settings object", wrong);
        finish(helper, wrong);
    }

    /** A versioned element with neither location nor locations has nothing to place, so it's refused. */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void versionedElementNeedsATemplate(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        refused(helper, StructurePoolElement.CODEC, versioned(""), "", wrong);
        accepted(helper, StructurePoolElement.CODEC, versioned("\"locations\":{\"1.21-26.3\":\"moogs_structures:msl_arena\"}"), wrong);
        refused(helper, StructurePoolElement.CODEC, versioned("\"locations\":{\"1.21.4-1.21\":\"moogs_structures:msl_arena\"}"), "", wrong);
        finish(helper, wrong);
    }

    /**
     * Version ranges: an exact version or min-max, both ends included; a backwards or three-part range is
     * refused. Of a map, the first range holding the running version picks the template.
     */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void versionRanges(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        for (String good : List.of("1.21", "1.21.4", "1.21-1.21.4", "26.1-26.3")) {
            if (VersionResolver.parseRange(good).error().isPresent()) {
                wrong.add("range \"" + good + "\" was refused: " + VersionResolver.parseRange(good).error().get().message());
            }
        }
        for (String bad : List.of("", "1.21.4-1.21", "1.21-1.21.4-1.21.5", "-1.21", "1.21-", "one")) {
            if (VersionResolver.parseRange(bad).result().isPresent()) {
                wrong.add("range \"" + bad + "\" was accepted");
            }
        }
        String current = VersionResolver.getCurrentVersionString();
        Map<String, ResourceLocation> map = new LinkedHashMap<>();
        map.put("1.19-1.20.6", id("old"));
        map.put(current, id("exact"));
        map.put("1.0-99", id("wide"));
        var entries = VersionResolver.parseVersionMap(map).result();
        if (entries.isEmpty()) {
            wrong.add("the version map " + map + " was refused");
        } else {
            var picked = VersionResolver.resolve(entries.get(), VersionResolver.getCurrentVersion());
            if (picked.isEmpty() || !picked.get().location().equals(id("exact"))) {
                wrong.add("on " + current + " the map picked " + picked.map(e -> e.location().toString()).orElse("nothing") + ", not the first range holding it (exact)");
            }
            Map<String, ResourceLocation> none = new LinkedHashMap<>();
            none.put("1.19-1.20.6", id("old"));
            if (VersionResolver.resolve(VersionResolver.parseVersionMap(none).result().orElseThrow(), VersionResolver.getCurrentVersion()).isPresent()) {
                wrong.add("a map with no range holding " + current + " still picked one");
            }
        }
        finish(helper, wrong);
    }

    /** advanced_random_spread needs its spacing above its separation, as vanilla's spread does. */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void spreadSpacingAboveSeparation(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        String spread = "{\"type\":\"moogs_structures:advanced_random_spread\",\"salt\":1,";
        refused(helper, StructurePlacement.CODEC, spread + "\"spacing\":5,\"separation\":5}", "", wrong);
        refused(helper, StructurePlacement.CODEC, spread + "\"spacing\":4,\"separation\":6}", "", wrong);
        accepted(helper, StructurePlacement.CODEC, spread + "\"spacing\":6,\"separation\":2}", wrong);
        accepted(helper, StructurePlacement.CODEC, spread + "\"spacing\":6,\"separation\":2,\"frequency\":0.5,"
                + "\"when_replacing\":{\"modid\":\"moogs_structures\",\"vanilla_key\":\"fortress\",\"spacing\":4,\"separation\":1}}", wrong);
        finish(helper, wrong);
    }

    /** The Nether type refuses a y_allowance whose top is under its bottom. */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void netherYAllowanceOrder(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        String nether = "{\"type\":\"moogs_structures:moogs_structures_generic_nether_jigsaw_structure\",\"start_pool\":\"minecraft:empty\","
                + "\"size\":1,\"start_height\":{\"absolute\":0},\"land_search_direction\":\"HIGHEST_LAND\",\"biomes\":\"#minecraft:is_nether\","
                + "\"step\":\"surface_structures\",\"spawn_overrides\":{}";
        refused(helper, Structure.DIRECT_CODEC, nether + ",\"y_allowance\":{\"min_y_allowed\":80,\"max_y_allowed\":60}}", "", wrong);
        accepted(helper, Structure.DIRECT_CODEC, nether + ",\"y_allowance\":{\"min_y_allowed\":60,\"max_y_allowed\":80}}", wrong);
        refused(helper, Structure.DIRECT_CODEC, nether.replace("HIGHEST_LAND", "SIDEWAYS") + "}", "", wrong);
        finish(helper, wrong);
    }

    // ------------------------------------------------------------------ processors

    /**
     * MSL's rail rotation, for every rail shape, mirror and turn: the template state it picks, once the
     * game mirrors and turns it as placement does, joins the same two neighbours the template rail
     * joined, mirrored and turned (MC-196102 gets some of these wrong on its own).
     */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void railRotationEveryShape(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        BlockState rail = Blocks.RAIL.defaultBlockState();
        var shape = ((BaseRailBlock) Blocks.RAIL).getShapeProperty();
        for (RailShape s : shape.getPossibleValues()) {
            for (Mirror mirror : Mirror.values()) {
                for (Rotation rotation : Rotation.values()) {
                    BlockState template = RailRotationProcessor.placeCorrectly(rail.setValue(shape, s), shape, mirror, rotation);
                    RailShape placed = template.mirror(mirror).rotate(rotation).getValue(shape);
                    Set<String> want = new HashSet<>();
                    for (String end : joins(s)) {
                        boolean up = end.startsWith("up ");
                        Direction d = Direction.byName(up ? end.substring(3) : end);
                        want.add((up ? "up " : "") + rotation.rotate(mirror.mirror(d)).getName());
                    }
                    if (!joins(placed).equals(want)) {
                        wrong.add(s + " mirrored " + mirror + ", turned " + rotation + ": placed as " + placed + ", joining " + joins(placed) + ", not " + want);
                    }
                }
            }
        }
        finish(helper, wrong);
    }

    /**
     * follow_floor over a step: a 5x5 sheet of planks placed at y 2, its far corner over a 3x3 step two
     * high (y 2 and 3) on stone ground (y 1). Each column's bottom layer lands in its floor's top block:
     * over the step at y 3, elsewhere in the ground at y 1; none is left in between or lost.
     */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void followFloorOntoAStep(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        ground(helper, 1);
        fill(helper, new BlockPos(6, 2, 6), new BlockPos(8, 3, 8), Blocks.STONE);
        fill(helper, new BlockPos(1, 10, 1), new BlockPos(5, 10, 5), Blocks.OAK_PLANKS);
        StructureTemplate sheet = capture(helper, new BlockPos(1, 10, 1), new Vec3i(5, 1, 5));
        fill(helper, new BlockPos(1, 10, 1), new BlockPos(5, 10, 5), Blocks.AIR);
        StructureProcessor follow = processor(helper, "{\"processor_type\":\"moogs_structures:follow_floor_processor\"}");
        place(helper, sheet, new BlockPos(4, 2, 4), new StructurePlaceSettings().addProcessor(follow));
        // The bottom layer lands in the floor's top block: the step's at y 3, the ground's at y 1.
        int onTop = count(helper, new BlockPos(4, 3, 4), new BlockPos(8, 3, 8), Blocks.OAK_PLANKS);
        int onGround = count(helper, new BlockPos(4, 1, 4), new BlockPos(8, 1, 8), Blocks.OAK_PLANKS);
        int between = count(helper, new BlockPos(4, 2, 4), new BlockPos(8, 2, 8), Blocks.OAK_PLANKS)
                + count(helper, new BlockPos(4, 4, 4), new BlockPos(8, 6, 8), Blocks.OAK_PLANKS);
        if (onTop != 9 || onGround != 16 || between != 0) {
            wrong.add("planks in the step's top " + onTop + " (want 9), in the ground " + onGround + " (want 16), elsewhere " + between + " (want 0)");
        }
        finish(helper, wrong);
    }

    /** pillar_processor: a trigger block placed in the air becomes the replacement and runs down to the ground. */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void pillarDownToGround(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        ground(helper, 1);
        helper.setBlock(new BlockPos(1, 12, 1), Blocks.POLISHED_ANDESITE.defaultBlockState());
        StructureTemplate trigger = capture(helper, new BlockPos(1, 12, 1), new Vec3i(1, 1, 1));
        helper.setBlock(new BlockPos(1, 12, 1), Blocks.AIR.defaultBlockState());
        StructureProcessor pillar = processor(helper, "{\"processor_type\":\"moogs_structures:pillar_processor\",\"pillar_trigger_and_replacements\":"
                + "[{\"trigger\":" + state("minecraft:polished_andesite") + ",\"replacement\":" + state("minecraft:stone_bricks") + "}]}");
        place(helper, trigger, new BlockPos(10, 8, 10), new StructurePlaceSettings().addProcessor(pillar));
        int bricks = count(helper, new BlockPos(10, 2, 10), new BlockPos(10, 8, 10), Blocks.STONE_BRICKS);
        if (bricks != 7) {
            wrong.add(bricks + " stone bricks from the trigger down to the ground, not 7");
        }
        if (helper.getBlockState(new BlockPos(10, 8, 10)).is(Blocks.POLISHED_ANDESITE)) {
            wrong.add("the trigger block itself was left in place");
        }
        if (!helper.getBlockState(new BlockPos(10, 1, 10)).is(Blocks.STONE)) {
            wrong.add("the ground under the pillar was replaced");
        }
        finish(helper, wrong);
    }

    /**
     * rail_slope_processor: a lone flat rail placed beside ground one block higher at one of its ends turns
     * to climb that way; one on level ground stays flat; a curve is left alone.
     */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void railSlopeTowardsHigherGround(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        ground(helper, 1);
        var shape = ((BaseRailBlock) Blocks.RAIL).getShapeProperty();
        StructureProcessor slope = processor(helper, "{\"processor_type\":\"moogs_structures:rail_slope_processor\"}");
        // The game test framework roofs the test area with barriers, which the surface heightmap reads;
        // open the sky over the columns the processor reads, so it reads the ground built here.
        for (int x = 2; x <= 18; x++) {
            for (int z = 2; z <= 18; z++) {
                for (int y = 16; y <= 24; y++) {
                    if (helper.getBlockState(new BlockPos(x, y, z)).is(Blocks.BARRIER)) {
                        helper.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        // Higher ground east of the east-west rail, north of the north-south one; nothing round the others.
        helper.setBlock(new BlockPos(5, 2, 4), Blocks.STONE.defaultBlockState());
        helper.setBlock(new BlockPos(10, 2, 3), Blocks.STONE.defaultBlockState());
        Object[][] cases = {
                {new BlockPos(4, 2, 4), RailShape.EAST_WEST, RailShape.ASCENDING_EAST},
                {new BlockPos(10, 2, 4), RailShape.NORTH_SOUTH, RailShape.ASCENDING_NORTH},
                {new BlockPos(16, 2, 16), RailShape.EAST_WEST, RailShape.EAST_WEST},
                {new BlockPos(16, 2, 4), RailShape.NORTH_EAST, RailShape.NORTH_EAST},
        };
        for (Object[] c : cases) {
            // The processor's own answer, before placing: a rail's placement logic can reshape it after.
            BlockPos at = helper.absolutePos((BlockPos) c[0]);
            StructurePlaceSettings settings = new StructurePlaceSettings().addProcessor(slope);
            List<StructureTemplate.StructureBlockInfo> out = StructureTemplate.processBlockInfos(helper.getLevel(), at, at, settings,
                    List.of(new StructureTemplate.StructureBlockInfo(BlockPos.ZERO, Blocks.RAIL.defaultBlockState().setValue(shape, (RailShape) c[1]), null)));
            BlockState got = out.isEmpty() ? Blocks.AIR.defaultBlockState() : out.get(0).state();
            if (!got.is(Blocks.RAIL) || got.getValue(shape) != c[2]) {
                var surface = net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE;
                wrong.add("a " + c[1] + " rail at " + c[0] + " came out " + (got.is(Blocks.RAIL) ? got.getValue(shape) : got) + ", not " + c[2]
                        + " (surface heights here " + helper.getLevel().getHeight(surface, at.getX(), at.getZ()) + ", east " + helper.getLevel().getHeight(surface, at.getX() + 1, at.getZ())
                        + ", north " + helper.getLevel().getHeight(surface, at.getX(), at.getZ() - 1) + ", at y " + at.getY() + ")");
            }
        }
        finish(helper, wrong);
    }

    // ------------------------------------------------------------------ spawners

    //? if <1.21.5 {
    /**
     * A trial spawner config's mob keeps the drop chances its JSON gives. Below 1.21.5 the game reads them
     * only as float lists, and JSON's 0.02 arrives as a double, so it fell back to its 8.5% default.
     */
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    public static void trialSpawnerDropChances(GameTestHelper helper) {
        StructureProcessor trial = processor(helper, "{\"processor_type\":\"moogs_structures:trial_spawner_randomizing_processor\","
                + "\"normal_config\":\"moogs_structures:gametest_drop_chances\"}");
        CompoundTag placed = processed(helper, trial, Blocks.TRIAL_SPAWNER.defaultBlockState());
        CompoundTag mob = placed == null ? null : placed.getCompound("normal_config").getList("spawn_potentials", Tag.TAG_COMPOUND)
                .getCompound(0).getCompound("data").getCompound("entity");
        finish(helper, dropChancesWrong(helper, mob));
    }

    /** The same mob, as a spawner_randomizing_processor's nbt, keeps its drop chances too. */
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    public static void spawnerDropChances(GameTestHelper helper) {
        StructureProcessor spawner = processor(helper, "{\"processor_type\":\"moogs_structures:spawner_randomizing_processor\","
                + "\"weighted_entities\":[{\"entity\":\"minecraft:wither_skeleton\",\"weight\":1,\"nbt\":{"
                + "\"ArmorDropChances\":[0.02,0.02,0.02,0.02],\"HandDropChances\":[0.05,0.0],"
                + "\"Passengers\":[{\"id\":\"minecraft:zombie\",\"ArmorDropChances\":[1.0,1.0,0.0,0.0],\"HandDropChances\":[0.5,0.25]}]}}]}");
        CompoundTag placed = processed(helper, spawner, Blocks.SPAWNER.defaultBlockState());
        CompoundTag mob = placed == null ? null : placed.getCompound("SpawnData").getCompound("entity");
        finish(helper, dropChancesWrong(helper, mob));
    }

    /** The block entity NBT a processor gives a block of this state. */
    private static CompoundTag processed(GameTestHelper helper, StructureProcessor processor, BlockState state) {
        BlockPos at = helper.absolutePos(new BlockPos(1, 2, 1));
        List<StructureTemplate.StructureBlockInfo> out = StructureTemplate.processBlockInfos(helper.getLevel(), at, at,
                new StructurePlaceSettings().addProcessor(processor), List.of(new StructureTemplate.StructureBlockInfo(BlockPos.ZERO, state, null)));
        return out.isEmpty() ? null : out.get(0).nbt();
    }

    /**
     * Loads the mob the way its spawner does, saves it and its passenger, and compares the drop chances
     * they saved with the JSON's: armour [0.02 x4] (doubles in NBT), hand [0.05, 0.0] (a double and a byte),
     * and the passenger's armour [1, 1, 0, 0] (a byte array) and hand [0.5, 0.25] (floats, always read).
     */
    private static List<String> dropChancesWrong(GameTestHelper helper, CompoundTag mobNbt) {
        List<String> wrong = new ArrayList<>();
        if (mobNbt == null || mobNbt.isEmpty()) {
            wrong.add("the processor gave the spawner no mob");
            return wrong;
        }
        //? if <1.21.2 {
        Entity mob = EntityType.loadEntityRecursive(mobNbt.copy(), helper.getLevel(), entity -> entity);
        //?} else {
        /*Entity mob = EntityType.loadEntityRecursive(mobNbt.copy(), helper.getLevel(), EntitySpawnReason.SPAWNER, entity -> entity);
        *///?}
        if (mob == null || mob.getPassengers().isEmpty()) {
            wrong.add("the mob or its passenger didn't load from " + mobNbt);
            return wrong;
        }
        savedDropChances(mob, "the wither skeleton's", new float[]{0.02f, 0.02f, 0.02f, 0.02f}, new float[]{0.05f, 0f}, wrong);
        savedDropChances(mob.getPassengers().get(0), "its zombie passenger's", new float[]{1f, 1f, 0f, 0f}, new float[]{0.5f, 0.25f}, wrong);
        return wrong;
    }

    private static void savedDropChances(Entity mob, String whose, float[] armour, float[] hand, List<String> wrong) {
        CompoundTag saved = mob.saveWithoutId(new CompoundTag());
        compareFloats(whose + " armour", saved.getList("ArmorDropChances", Tag.TAG_FLOAT), armour, wrong);
        compareFloats(whose + " hand", saved.getList("HandDropChances", Tag.TAG_FLOAT), hand, wrong);
    }

    private static void compareFloats(String what, ListTag saved, float[] want, List<String> wrong) {
        float[] got = new float[saved.size()];
        for (int i = 0; i < got.length; i++) {
            got[i] = saved.getFloat(i);
        }
        if (!Arrays.equals(got, want)) {
            wrong.add(what + " drop chances saved as " + Arrays.toString(got) + ", not " + Arrays.toString(want));
        }
    }
    //?}

    // ------------------------------------------------------------------ heights

    /**
     * MSL's cached height is the generator's own on real overworld terrain: for every heightmap, it equals
     * the first free height and the base height and is one above the first occupied height, the same again
     * when read from the cache, and kept apart per seed, so a second seed on the same generator gets its
     * own terrain's heights rather than the first's.
     */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void cachedHeightMatchesTheGenerator(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        ServerLevel level = helper.getLevel();
        NoiseBasedChunkGenerator generator = overworldGenerator(level);
        Map<String, Integer> firstSeed = new HashMap<>();
        int differentAcrossSeeds = 0;
        for (long seed : new long[]{1L, 2L}) {
            RandomState random = randomState(level, seed);
            for (int i = 0; i < 64; i++) {
                int x = (i % 8 - 4) * 1031 + 7;
                int z = (i / 8 - 4) * 977;
                for (Heightmap.Types type : Heightmap.Types.values()) {
                    int base = generator.getBaseHeight(x, z, type, level, random);
                    int cached = GeneralUtils.getCachedFreeHeight(generator, x, z, type, level, random);
                    int again = GeneralUtils.getCachedFreeHeight(generator, x, z, type, level, random);
                    int free = generator.getFirstFreeHeight(x, z, type, level, random);
                    int occupied = generator.getFirstOccupiedHeight(x, z, type, level, random);
                    if (cached != base || again != base || free != base || occupied != base - 1) {
                        wrong.add("seed " + seed + " " + type + " at " + x + ", " + z + ": base " + base + ", cached " + cached + " then " + again
                                + ", first free " + free + ", first occupied " + occupied);
                    }
                    String spot = type + " " + x + " " + z;
                    if (seed == 1L) {
                        firstSeed.put(spot, base);
                    } else if (firstSeed.get(spot) != base) {
                        differentAcrossSeeds++;
                    }
                }
            }
        }
        if (differentAcrossSeeds == 0) {
            wrong.add("the two seeds gave the same terrain everywhere, so the per-seed check proved nothing");
        }
        finish(helper, wrong);
    }

    /**
     * The height cache doesn't keep a world it read heights from once nothing else does (GitHub issue #30:
     * it kept every closed world's generator, and that world's registries, for the rest of the game).
     */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void heightCacheLetsGoOfAClosedWorld(GameTestHelper helper) {
        List<WeakReference<Object>> closedWorld = readHeightsOfAThrowawayWorld(helper.getLevel());
        for (int i = 0; i < 20 && closedWorld.stream().anyMatch(ref -> ref.get() != null); i++) {
            System.gc();
        }
        List<String> wrong = new ArrayList<>();
        if (closedWorld.get(0).get() != null) {
            wrong.add("the height cache still holds a generator nothing else uses");
        }
        if (closedWorld.get(1).get() != null) {
            wrong.add("the height cache still holds a random state nothing else uses");
        }
        finish(helper, wrong);
    }

    /** Reads heights through the cache from a generator and random state made here, and hands back only weak references to them. */
    private static List<WeakReference<Object>> readHeightsOfAThrowawayWorld(ServerLevel level) {
        NoiseBasedChunkGenerator generator = overworldGenerator(level);
        RandomState random = randomState(level, 3L);
        for (int i = 0; i < 16; i++) {
            GeneralUtils.getCachedFreeHeight(generator, i * 64, 0, Heightmap.Types.WORLD_SURFACE_WG, level, random);
        }
        return List.of(new WeakReference<>(generator), new WeakReference<>(random));
    }

    /** A fresh overworld noise generator (plains everywhere: heights come from the noise, not the biomes). */
    private static NoiseBasedChunkGenerator overworldGenerator(ServerLevel level) {
        var access = level.registryAccess();
        return new NoiseBasedChunkGenerator(new FixedBiomeSource(access.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS)),
                access.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD));
    }

    private static RandomState randomState(ServerLevel level, long seed) {
        var access = level.registryAccess();
        NoiseGeneratorSettings settings = access.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD).value();
        //? if <26.3 {
        return RandomState.create(settings, access.lookupOrThrow(Registries.NOISE), seed);
        //?} else {
        /*return RandomState.create(access.lookupOrThrow(Registries.NOISE), seed, settings);
        *///?}
    }

    // ------------------------------------------------------------------ helpers

    /** A block state as this version's codec reads it: 26.3 names the block "id" (MSL upgrades the older "Name" only in datapacks). */
    private static String state(String block) {
        //? if >=26.3 {
        /*return "{\"id\":\"" + block + "\"}";
        *///?} else {
        return "{\"Name\":\"" + block + "\"}";
        //?}
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("moogs_structures", path);
    }

    private static String element(String projection, String area) {
        return versioned("\"location\":\"moogs_structures:msl_arena\",\"projection\":\"" + projection + "\",\"adaptation_area\":" + area);
    }

    // A versioned element with these fields; processors and (unless given) projection filled in.
    private static String versioned(String fields) {
        String json = "{\"element_type\":\"moogs_structures:versioned_single_pool_element\",\"processors\":\"minecraft:empty\"";
        if (!fields.contains("\"projection\"")) {
            json += ",\"projection\":\"rigid\"";
        }
        return json + (fields.isEmpty() ? "" : "," + fields) + "}";
    }

    private static <T> String decodeProblem(GameTestHelper helper, Codec<T> codec, String json) {
        JsonElement parsed = JsonParser.parseString(json);
        try {
            DataResult<T> result = codec.parse(RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess()), parsed);
            return result.error().map(e -> e.message()).orElse(null);
        } catch (RuntimeException e) {
            return String.valueOf(e.getMessage());
        }
    }

    private static <T> void refused(GameTestHelper helper, Codec<T> codec, String json, String message, List<String> wrong) {
        String problem = decodeProblem(helper, codec, json);
        if (problem == null) {
            wrong.add("accepted " + json);
        } else if (!problem.contains(message)) {
            wrong.add("refused " + json + " saying \"" + problem + "\", without \"" + message + "\"");
        }
    }

    private static <T> void accepted(GameTestHelper helper, Codec<T> codec, String json, List<String> wrong) {
        String problem = decodeProblem(helper, codec, json);
        if (problem != null) {
            wrong.add("refused " + json + ": " + problem);
        }
    }

    private static StructureProcessor processor(GameTestHelper helper, String json) {
        return StructureProcessorType.SINGLE_CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess()), JsonParser.parseString(json))
                .getOrThrow();
    }

    /** The ends a rail shape joins: two flat directions, and for a slope "up <direction>" for the end it climbs to. */
    private static Set<String> joins(RailShape shape) {
        return switch (shape) {
            case NORTH_SOUTH -> Set.of("north", "south");
            case EAST_WEST -> Set.of("east", "west");
            case ASCENDING_EAST -> Set.of("east", "west", "up east");
            case ASCENDING_WEST -> Set.of("east", "west", "up west");
            case ASCENDING_NORTH -> Set.of("north", "south", "up north");
            case ASCENDING_SOUTH -> Set.of("north", "south", "up south");
            case SOUTH_EAST -> Set.of("south", "east");
            case SOUTH_WEST -> Set.of("south", "west");
            case NORTH_WEST -> Set.of("north", "west");
            case NORTH_EAST -> Set.of("north", "east");
        };
    }

    private static void ground(GameTestHelper helper, int y) {
        fill(helper, new BlockPos(0, y, 0), new BlockPos(23, y, 23), Blocks.STONE);
    }

    private static void fill(GameTestHelper helper, BlockPos from, BlockPos to, Block block) {
        for (BlockPos p : BlockPos.betweenClosed(from, to)) {
            helper.setBlock(p, block.defaultBlockState());
        }
    }

    private static int count(GameTestHelper helper, BlockPos from, BlockPos to, Block block) {
        int n = 0;
        for (BlockPos p : BlockPos.betweenClosed(from, to)) {
            if (helper.getBlockState(p).is(block)) {
                n++;
            }
        }
        return n;
    }

    /** A template made from the arena's blocks, as a structure block saves one, air included. */
    private static StructureTemplate capture(GameTestHelper helper, BlockPos from, Vec3i size) {
        StructureTemplate template = new StructureTemplate();
        //? if >=1.21.6 {
        /*template.fillFromWorld(helper.getLevel(), helper.absolutePos(from), size, false, List.of(Blocks.STRUCTURE_VOID));
        *///?} else {
        template.fillFromWorld(helper.getLevel(), helper.absolutePos(from), size, false, Blocks.STRUCTURE_VOID);
        //?}
        return template;
    }

    private static void place(GameTestHelper helper, StructureTemplate template, BlockPos at, StructurePlaceSettings settings) {
        BlockPos where = helper.absolutePos(at);
        template.placeInWorld(helper.getLevel(), where, where, settings, RandomSource.create(1), Block.UPDATE_CLIENTS);
    }

    private static void finish(GameTestHelper helper, List<String> wrong) {
        if (wrong.isEmpty()) {
            helper.succeed();
            return;
        }
        String message = String.join("; ", wrong);
        //? if >=1.21.5 <1.21.11 {
        /*helper.fail(Component.literal(message));
        *///?} else {
        helper.fail(message);
        //?}
    }
}
//?}
