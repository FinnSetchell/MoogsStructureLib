//? if >=1.21.1 {
package com.finndog.moogs_structures.gametest.neoforge;

import com.finndog.moogs_structures.mixins.structures.SinglePoolElementAccessor;
import com.finndog.moogs_structures.utils.BoxOctree;
import com.finndog.moogs_structures.config.MslConfig;
import com.finndog.moogs_structures.config.ReplaceVanillaManager;
import com.finndog.moogs_structures.utils.GeneralUtils;
import com.finndog.moogs_structures.utils.VersionResolver;
import com.finndog.moogs_structures.world.processors.RailRotationProcessor;
import com.finndog.moogs_structures.world.structures.pieces.PieceLimitedJigsawManager;
import com.finndog.moogs_structures.world.structures.GenericJigsawStructure;
import com.finndog.moogs_structures.world.structures.GenericNetherJigsawStructure;
import com.finndog.moogs_structures.world.structures.placements.AdvancedRandomSpread;
import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptation;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.entity.Entity;
//? if >=1.21.2 {
/*import net.minecraft.world.entity.EntitySpawnReason;
*///?}
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
//? if >=26.3 {
/*import net.minecraft.world.level.biome.BiomeResolver;
*///?}
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
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
        tests.put("jigsaw_joint_rules", MslGameTests::jigsawJointRules);
        tests.put("box_octree_matches_every_box", MslGameTests::boxOctreeMatchesEveryBox);
        tests.put("start_reroll_places_the_new_piece", MslGameTests::startRerollPlacesTheNewPiece);
        tests.put("start_reroll_frees_the_old_room", MslGameTests::startRerollFreesTheOldRoom);
        tests.put("start_reroll_tries_100_times", MslGameTests::startRerollTries100Times);
        tests.put("mirrored_jigsaws_face_out", MslGameTests::mirroredJigsawsFaceOut);
        tests.put("placement_field_values", MslGameTests::placementFieldValues);
        tests.put("mc_versions_gate_biomes", MslGameTests::mcVersionsGateBiomes);
        tests.put("version_gated_set_turned_off", MslGameTests::versionGatedSetTurnedOff);
        tests.put("liquid_check_at_start_piece_centre", MslGameTests::liquidCheckAtStartPieceCentre);
        tests.put("footprint_in_liquid_share", MslGameTests::footprintInLiquidShare);
        tests.put("biome_radius_reads_the_corner_height", MslGameTests::biomeRadiusReadsTheCornerHeight);
        tests.put("disabled_set_skipped_by_searches", MslGameTests::disabledSetSkippedBySearches);
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

    // ------------------------------------------------------------------ jigsaw assembly

    /**
     * Whether two jigsaws join, asked the way this version's jigsaw assembler asks it. They join when they face each
     * other, the first's target is the second's name, and their tops line up or the first's joint rolls. A joint
     * written as "rollable" or "aligned" is used as written; any other value, or none, rolls when the jigsaw faces up
     * or down and stays aligned when it faces sideways (vanilla's rule).
     */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void jigsawJointRules(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        // The first jigsaw's facing, its joint (null: none written, 1: a number), the second's facing, whether they join.
        Object[][] cases = {
                {FrontAndTop.UP_NORTH, null, FrontAndTop.DOWN_EAST, true},
                {FrontAndTop.UP_NORTH, "", FrontAndTop.DOWN_EAST, true},
                {FrontAndTop.UP_NORTH, "rollable", FrontAndTop.DOWN_EAST, true},
                {FrontAndTop.UP_NORTH, "aligned", FrontAndTop.DOWN_EAST, false},
                {FrontAndTop.UP_NORTH, "aligned", FrontAndTop.DOWN_NORTH, true},
                {FrontAndTop.UP_NORTH, "Rollable", FrontAndTop.DOWN_EAST, true},
                {FrontAndTop.UP_NORTH, "sideways", FrontAndTop.DOWN_EAST, true},
                {FrontAndTop.UP_NORTH, 1, FrontAndTop.DOWN_EAST, true},
                {FrontAndTop.DOWN_SOUTH, null, FrontAndTop.UP_WEST, true},
                {FrontAndTop.DOWN_SOUTH, "sideways", FrontAndTop.UP_WEST, true},
                {FrontAndTop.DOWN_SOUTH, "aligned", FrontAndTop.UP_WEST, false},
                {FrontAndTop.EAST_UP, null, FrontAndTop.WEST_UP, true},
                {FrontAndTop.EAST_UP, "rollable", FrontAndTop.WEST_UP, true},
                {FrontAndTop.EAST_UP, "aligned", FrontAndTop.WEST_UP, true},
                {FrontAndTop.EAST_UP, "sideways", FrontAndTop.WEST_UP, true},
                {FrontAndTop.EAST_UP, null, FrontAndTop.EAST_UP, false},
                {FrontAndTop.EAST_UP, "rollable", FrontAndTop.NORTH_UP, false},
                {FrontAndTop.UP_NORTH, "rollable", FrontAndTop.UP_NORTH, false},
                {FrontAndTop.UP_NORTH, "rollable", FrontAndTop.WEST_UP, false},
        };
        for (Object[] c : cases) {
            for (boolean named : new boolean[]{true, false}) {
                StructureTemplate.StructureBlockInfo first = jigsawInfo((FrontAndTop) c[0], "moogs_structures:first", "moogs_structures:second", c[1]);
                StructureTemplate.StructureBlockInfo second = jigsawInfo((FrontAndTop) c[2], named ? "moogs_structures:second" : "moogs_structures:other", "moogs_structures:first", null);
                boolean want = named && (Boolean) c[3];
                String what = c[0] + " with joint " + (c[1] == null ? "none" : c[1] instanceof String ? "\"" + c[1] + "\"" : c[1]) + " to " + c[2]
                        + (named ? "" : " named something else");
                boolean msl = mslJigsawsAttach(first, second);
                if (msl != want) {
                    wrong.add("MSL: " + what + (msl ? " joins" : " doesn't join"));
                }
                //? if >=26.3 {
                /*// The 26.3 assembler asks vanilla, from the fields vanilla parses out of the jigsaw.
                boolean vanilla = JigsawBlock.canAttach(StructureTemplate.JigsawBlockInfo.parse(first), StructureTemplate.JigsawBlockInfo.parse(second));
                if (vanilla != want) {
                    wrong.add("vanilla: " + what + (vanilla ? " joins" : " doesn't join"));
                }
                *///?}
            }
        }
        finish(helper, wrong);
    }

    /**
     * The jigsaw assembler's octree of placed pieces says whether a box overlaps one of them exactly as checking every
     * placed piece does, for any box inside its bounds (the only boxes the assembler asks about: a pool that ignores
     * bounds skips the question, and its pieces are still added). Random layouts dense enough to split the tree to its
     * full depth, some with bounds off the block grid so its halves don't divide evenly.
     */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void boxOctreeMatchesEveryBox(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        RandomSource random = RandomSource.create(538L);
        int asked = 0;
        int overlapping = 0;
        for (int layout = 0; layout < 400 && wrong.size() < 5; layout++) {
            int range = 4 + random.nextInt(60);
            int cx = random.nextInt(2001) - 1000;
            int cy = random.nextInt(321) - 64;
            int cz = random.nextInt(2001) - 1000;
            double off = layout % 10 == 0 ? random.nextDouble() : 0;
            BoxOctree octree = new BoxOctree(new AABB(cx - range + off, cy - 120, cz - range, cx + range + 1, cy + 181, cz + range + 1 - off));
            List<AABB> placed = new ArrayList<>();
            for (int step = 0; step < 150; step++) {
                int x = cx - range - 10 + random.nextInt(2 * range + 20);
                int y = cy - 20 + random.nextInt(60);
                int z = cz - range - 10 + random.nextInt(2 * range + 20);
                AABB box = new AABB(x, y, z, x + 1 + random.nextInt(16), y + 1 + random.nextInt(12), z + 1 + random.nextInt(16));
                if (random.nextInt(6) == 0) {
                    // A piece from a pool that ignores bounds: added without asking, and it may stick out.
                    octree.addBox(box);
                    placed.add(box);
                    continue;
                }
                AABB question = box.deflate(0.25D);
                if (!octree.boundaryContains(question)) {
                    continue;
                }
                asked++;
                boolean overlaps = false;
                for (AABB other : placed) {
                    overlaps |= other.intersects(question);
                }
                boolean said = octree.intersectsAnyBox(question);
                if (said != overlaps) {
                    wrong.add("layout " + layout + ", " + question + " among " + placed.size() + " pieces: the octree says " + (said ? "it overlaps" : "it's clear")
                            + ", checking every piece says " + (overlaps ? "it overlaps" : "it's clear"));
                }
                if (overlaps) {
                    overlapping++;
                } else {
                    octree.addBox(box);
                    placed.add(box);
                }
            }
        }
        if (asked < 10000 || overlapping < 1000) {
            wrong.add("only " + asked + " questions, " + overlapping + " of them overlapping: too few to show anything");
        }
        finish(helper, wrong);
    }

    /**
     * A structure with a required piece whose first start can't reach it is laid out again from a fresh pick of its
     * start pool, and the start placed is that new one, with the required piece on one of its jigsaws. The start pool
     * holds a lone stone block and a start whose jigsaw leads to the required piece; the structure starts in the first
     * chunk where the stone block is picked first.
     */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void startRerollPlacesTheNewPiece(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        buildRerollPieces(helper);
        checkReroll(helper, "start_small", "small", 1, 39, wrong);
        finish(helper, wrong);
    }

    /**
     * As above, with a first pick big enough to cover the spot where the required piece goes: the room the first pick
     * took is free again for the new start's pieces.
     */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void startRerollFreesTheOldRoom(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        buildRerollPieces(helper);
        checkReroll(helper, "start_big", "big", 1, 39, wrong);
        finish(helper, wrong);
    }

    /**
     * A structure gets 100 tries at its required pieces: with the dead end 30 times likelier than the good start, in
     * the chosen chunk the good start first comes up on a try between the 41st and the 100th, and it still generates.
     */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void startRerollTries100Times(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        buildRerollPieces(helper);
        checkReroll(helper, "start_rare", "rare", 40, 99, wrong);
        finish(helper, wrong);
    }

    /**
     * A mirroring piece, for every mirror and turn: each jigsaw it offers to the assembler is the jigsaw the piece
     * places at that spot, facing the same way, and it faces out of the piece, so pieces attach to its outside. Placed
     * with its jigsaws kept, as the keepjigsaws debug option does.
     */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void mirroredJigsawsFaceOut(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        ServerLevel level = helper.getLevel();
        StructureTemplateManager templates = templates(level);
        // An uneven 5 x 3 x 4 block with a jigsaw out of each side, its top and its bottom; the top and bottom ones
        // have their tops along an axis, so a mirror shows on them too.
        String path = "gametest/mirror/piece";
        buildPiece(helper, path, new Vec3i(5, 3, 4),
                new TestJigsaw(new BlockPos(4, 1, 1), FrontAndTop.EAST_UP),
                new TestJigsaw(new BlockPos(0, 1, 2), FrontAndTop.WEST_UP),
                new TestJigsaw(new BlockPos(2, 1, 3), FrontAndTop.SOUTH_UP),
                new TestJigsaw(new BlockPos(1, 1, 0), FrontAndTop.NORTH_UP),
                new TestJigsaw(new BlockPos(3, 2, 2), FrontAndTop.UP_EAST),
                new TestJigsaw(new BlockPos(1, 0, 1), FrontAndTop.DOWN_SOUTH));
        BlockPos origin = helper.absolutePos(new BlockPos(12, 5, 12));
        for (Mirror mirror : Mirror.values()) {
            StructurePoolElement element = decode(helper, StructurePoolElement.CODEC, "{\"element_type\":\"moogs_structures:mirroring_single_pool_element\","
                    + "\"location\":\"moogs_structures:" + path + "\",\"processors\":\"minecraft:empty\",\"projection\":\"rigid\",\"mirror\":\"" + mirror.name() + "\"}");
            for (Rotation rotation : Rotation.values()) {
                String where = "mirrored " + mirror + ", turned " + rotation + ": ";
                BoundingBox box = element.getBoundingBox(templates, origin, rotation);
                List<StructureTemplate.StructureBlockInfo> offered = jigsaws(element, templates, origin, rotation);
                element.place(templates, level, level.structureManager(), level.getChunkSource().getGenerator(), origin, origin, rotation, box,
                        RandomSource.create(0), LiquidSettings.IGNORE_WATERLOGGING, true);
                int placed = 0;
                for (BlockPos p : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
                    placed += level.getBlockState(p).is(Blocks.JIGSAW) ? 1 : 0;
                }
                if (offered.size() != 6 || placed != 6) {
                    wrong.add(where + offered.size() + " jigsaws offered and " + placed + " placed, not 6");
                }
                for (StructureTemplate.StructureBlockInfo jigsaw : offered) {
                    BlockPos at = jigsaw.pos();
                    FrontAndTop said = jigsaw.state().getValue(JigsawBlock.ORIENTATION);
                    BlockState there = level.getBlockState(at);
                    if (!there.is(Blocks.JIGSAW) || there.getValue(JigsawBlock.ORIENTATION) != said) {
                        wrong.add(where + "offered a jigsaw at " + at.subtract(origin) + " facing " + said + ", where the piece placed "
                                + (there.is(Blocks.JIGSAW) ? "one facing " + there.getValue(JigsawBlock.ORIENTATION) : there));
                    }
                    if (!box.isInside(at) || box.isInside(at.relative(JigsawBlock.getFrontFacing(jigsaw.state())))) {
                        wrong.add(where + "the jigsaw offered at " + at.subtract(origin) + " faces " + said + ", into the piece");
                    }
                }
                for (BlockPos p : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
        finish(helper, wrong);
    }

    // ------------------------------------------------------------------ jigsaw helpers

    private static final String REROLL = "gametest/reroll/";
    private static final long REROLL_SEED = 5381L;

    /** A jigsaw for {@link #buildPiece}, at a spot in the piece, with what it calls itself, joins to and draws from. */
    private record TestJigsaw(BlockPos at, FrontAndTop facing, String name, String target, String pool) {
        TestJigsaw(BlockPos at, FrontAndTop facing) {
            this(at, facing, "minecraft:empty", "minecraft:empty", "minecraft:empty");
        }
    }

    /** The reroll tests' pieces, stored as the templates their pools (in this source set's data) name. */
    private static void buildRerollPieces(GameTestHelper helper) {
        String link = "moogs_structures:" + REROLL + "link";
        buildPiece(helper, REROLL + "dead_end_small", new Vec3i(1, 1, 1));
        buildPiece(helper, REROLL + "dead_end_big", new Vec3i(9, 1, 9));
        buildPiece(helper, REROLL + "start", new Vec3i(3, 1, 3),
                new TestJigsaw(new BlockPos(2, 0, 1), FrontAndTop.EAST_UP, "minecraft:empty", link, "moogs_structures:" + REROLL + "children"));
        buildPiece(helper, REROLL + "required", new Vec3i(3, 1, 3),
                new TestJigsaw(new BlockPos(0, 0, 1), FrontAndTop.WEST_UP, link, "minecraft:empty", "minecraft:empty"));
    }

    /** A stone block of this size with these jigsaws, built in the arena, stored as the template of this id, and cleared again. */
    private static void buildPiece(GameTestHelper helper, String path, Vec3i size, TestJigsaw... jigsaws) {
        BlockPos corner = new BlockPos(1, 2, 1);
        BlockPos far = corner.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1);
        fill(helper, corner, far, Blocks.STONE);
        for (TestJigsaw jigsaw : jigsaws) {
            BlockPos at = corner.offset(jigsaw.at());
            helper.setBlock(at, Blocks.JIGSAW.defaultBlockState().setValue(JigsawBlock.ORIENTATION, jigsaw.facing()));
            JigsawBlockEntity entity = (JigsawBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(at));
            entity.setName(ResourceLocation.parse(jigsaw.name()));
            entity.setTarget(ResourceLocation.parse(jigsaw.target()));
            entity.setPool(ResourceKey.create(Registries.TEMPLATE_POOL, ResourceLocation.parse(jigsaw.pool())));
            entity.setFinalState("minecraft:stone");
            entity.setJoint(JigsawBlockEntity.JointType.ALIGNED);
        }
        fillFromWorld(templates(helper.getLevel()).getOrCreate(id(path)), helper, corner, size);
        fill(helper, corner, far, Blocks.AIR);
    }

    /**
     * Lays out a reroll test structure in the first chunk (along x) where the start pool's own draws, as the assembler
     * makes them, first give the good start on a try from {@code from} to {@code to} (0 is the first pick), and checks
     * the start placed is the good start, carrying the required piece on one of its jigsaws, on its ground level.
     */
    private static void checkReroll(GameTestHelper helper, String pool, String structure, int from, int to, List<String> wrong) {
        ServerLevel level = helper.getLevel();
        StructureTemplateManager templates = templates(level);
        Holder<StructureTemplatePool> startPool = level.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL)
                .getOrThrow(ResourceKey.create(Registries.TEMPLATE_POOL, id(REROLL + pool)));
        ResourceLocation start = id(REROLL + "start");
        int chunkX = -1;
        for (int x = 0; x < 5000 && chunkX < 0; x++) {
            WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
            random.setLargeFeatureSeed(REROLL_SEED, x, 0);
            Rotation.getRandom(random);
            int firstGood = -1;
            for (int pick = 0; pick < 100 && firstGood < 0; pick++) {
                if (start.equals(templateId(startPool.value().getRandomTemplate(random)))) {
                    firstGood = pick;
                }
            }
            if (firstGood >= from && firstGood <= to) {
                chunkX = x;
            }
        }
        if (chunkX < 0) {
            wrong.add(structure + ": no chunk where the good start first comes up on try " + from + " to " + to);
            return;
        }
        List<StructurePiece> pieces = assemble(helper, startPool, id(REROLL + structure), chunkX);
        if (pieces.isEmpty()) {
            wrong.add(structure + ": nothing generated in chunk " + chunkX);
            return;
        }
        List<ResourceLocation> names = new ArrayList<>();
        PoolElementStructurePiece required = null;
        for (StructurePiece piece : pieces) {
            ResourceLocation name = piece instanceof PoolElementStructurePiece p ? templateId(p.getElement()) : null;
            names.add(name);
            if (id(REROLL + "required").equals(name)) {
                required = (PoolElementStructurePiece) piece;
            }
        }
        PoolElementStructurePiece first = (PoolElementStructurePiece) pieces.get(0);
        if (!start.equals(names.get(0))) {
            wrong.add(structure + ": the start piece placed is " + names.get(0) + ", not the good start the structure was laid out from (pieces " + names + ")");
        }
        if (required == null) {
            wrong.add(structure + ": no required piece (pieces " + names + ")");
        } else if (!joined(templates, first, required)) {
            wrong.add(structure + ": the required piece hangs on no jigsaw of the start piece placed (pieces " + names + ")");
        }
        if (first.getBoundingBox().minY() != 99) {
            wrong.add(structure + ": the start piece's bottom is at y " + first.getBoundingBox().minY() + ", not 99 (ground level 100, ground level delta 1)");
        }
    }

    /** The structure's pieces as PieceLimitedJigsawManager lays them out from this start pool, size 2, at y 100 in this chunk. */
    private static List<StructurePiece> assemble(GameTestHelper helper, Holder<StructureTemplatePool> startPool, ResourceLocation structure, int chunkX) {
        ServerLevel level = helper.getLevel();
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        RandomState randomState = level.getChunkSource().randomState();
        //? if <26.3 {
        Structure.GenerationContext context = new Structure.GenerationContext(level.registryAccess(), generator, generator.getBiomeSource(),
                randomState, templates(level), REROLL_SEED, new ChunkPos(chunkX, 0), level, biome -> true);
        //?} else {
        /*Structure.GenerationContext context = new Structure.GenerationContext(level.registryAccess(), generator, generator.getBiomeSource(),
                randomState.createClimateSampler(net.minecraft.world.level.levelgen.densityfunction.SamplerContext.builder().build()), randomState,
                templates(level), REROLL_SEED, new ChunkPos(chunkX, 0), level, biome -> true);
        *///?}
        return PieceLimitedJigsawManager.assembleJigsawStructure(context, startPool, 2, structure, new BlockPos(chunkX * 16, 100, 0), false,
                        Optional.empty(), 1000, -1000, Set.of(), Optional.empty(), Optional.empty(), LiquidSettings.IGNORE_WATERLOGGING, (builder, laidOut) -> { })
                .map(stub -> stub.getPiecesBuilder().build().pieces())
                .orElse(List.of());
    }

    /** Whether a jigsaw of the child sits right in front of a jigsaw of the parent, facing back at it. */
    private static boolean joined(StructureTemplateManager templates, PoolElementStructurePiece parent, PoolElementStructurePiece child) {
        for (StructureTemplate.StructureBlockInfo p : jigsaws(parent.getElement(), templates, parent.getPosition(), parent.getRotation())) {
            Direction facing = JigsawBlock.getFrontFacing(p.state());
            for (StructureTemplate.StructureBlockInfo c : jigsaws(child.getElement(), templates, child.getPosition(), child.getRotation())) {
                if (p.pos().relative(facing).equals(c.pos()) && JigsawBlock.getFrontFacing(c.state()) == facing.getOpposite()) {
                    return true;
                }
            }
        }
        return false;
    }

    /** The jigsaws an element offers the assembler, as plain block infos on every version. */
    private static List<StructureTemplate.StructureBlockInfo> jigsaws(StructurePoolElement element, StructureTemplateManager templates, BlockPos at, Rotation rotation) {
        List<StructureTemplate.StructureBlockInfo> infos = new ArrayList<>();
        //? if <1.21.2 {
        infos.addAll(element.getShuffledJigsawBlocks(templates, at, rotation, RandomSource.create(0)));
        //?}
        //? if >=1.21.2 <26.3 {
        /*element.getShuffledJigsawBlocks(templates, at, rotation, RandomSource.create(0)).forEach(jigsaw -> infos.add(jigsaw.info()));
        *///?}
        //? if >=26.3 {
        /*element.getShuffledJigsawBlocks(templates, at, rotation, RandomSource.create(0)).forEach(jigsaw -> infos.add(new StructureTemplate.StructureBlockInfo(jigsaw.pos(), jigsaw.state(), null)));
        *///?}
        return infos;
    }

    /** GeneralUtils.canJigsawsAttach, passed jigsaws the way this version's assembler passes them. */
    private static boolean mslJigsawsAttach(StructureTemplate.StructureBlockInfo first, StructureTemplate.StructureBlockInfo second) {
        //? if >=1.21.2 <1.21.5 {
        /*return GeneralUtils.canJigsawsAttach(StructureTemplate.JigsawBlockInfo.of(first), StructureTemplate.JigsawBlockInfo.of(second));
        *///?} else {
        return GeneralUtils.canJigsawsAttach(first, second);
        //?}
    }

    private static StructureTemplate.StructureBlockInfo jigsawInfo(FrontAndTop facing, String name, String target, Object joint) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("name", name);
        nbt.putString("target", target);
        nbt.putString("pool", "minecraft:empty");
        nbt.putString("final_state", "minecraft:air");
        if (joint instanceof String text) {
            nbt.putString("joint", text);
        } else if (joint instanceof Integer number) {
            nbt.putInt("joint", number);
        }
        return new StructureTemplate.StructureBlockInfo(BlockPos.ZERO, Blocks.JIGSAW.defaultBlockState().setValue(JigsawBlock.ORIENTATION, facing), nbt);
    }

    private static ResourceLocation templateId(StructurePoolElement element) {
        return element instanceof SinglePoolElement ? ((SinglePoolElementAccessor) element).moogs_structures_getTemplate().left().orElse(null) : null;
    }

    private static StructureTemplateManager templates(ServerLevel level) {
        //? if <26.3 {
        return level.getStructureManager();
        //?} else {
        /*return level.getStructureTemplateManager();
        *///?}
    }

    // ------------------------------------------------------------------ placement checks

    /**
     * max_footprint_in_liquid is a number from 0 to 1, and mc_versions one version range, on both structure types
     * that read them; anything else is refused. Both share a codec slot with older fields, and still write as plain
     * top-level keys.
     */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void placementFieldValues(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        for (String good : List.of("0", "0.25", "1")) {
            accepted(helper, Structure.DIRECT_CODEC, generic("\"max_footprint_in_liquid\":" + good), wrong);
        }
        refused(helper, Structure.DIRECT_CODEC, generic("\"max_footprint_in_liquid\":1.5"), "outside of range", wrong);
        refused(helper, Structure.DIRECT_CODEC, generic("\"max_footprint_in_liquid\":-0.1"), "outside of range", wrong);
        refused(helper, Structure.DIRECT_CODEC, generic("\"max_footprint_in_liquid\":\"half\""), "", wrong);
        accepted(helper, Structure.DIRECT_CODEC, generic("\"cannot_spawn_in_liquid\":true,\"fully_submerged\":true,\"max_footprint_in_liquid\":0.5"), wrong);
        refused(helper, Structure.DIRECT_CODEC, generic("\"fully_submerged\":\"yes\""), "", wrong);

        for (String good : List.of("1.21.4-26.3", "1.21.1", "1.0-99")) {
            accepted(helper, Structure.DIRECT_CODEC, generic("\"mc_versions\":\"" + good + "\""), wrong);
            accepted(helper, Structure.DIRECT_CODEC, nether("\"mc_versions\":\"" + good + "\""), wrong);
        }
        refused(helper, Structure.DIRECT_CODEC, generic("\"mc_versions\":\"1.21.4-\""), "must specify both", wrong);
        refused(helper, Structure.DIRECT_CODEC, generic("\"mc_versions\":\"1.21.4-1.21\""), "minimum greater than its maximum", wrong);
        refused(helper, Structure.DIRECT_CODEC, generic("\"mc_versions\":\"latest\""), "non-numeric", wrong);
        refused(helper, Structure.DIRECT_CODEC, generic("\"mc_versions\":1.21"), "", wrong);
        refused(helper, Structure.DIRECT_CODEC, nether("\"mc_versions\":\"1.21.4-\""), "must specify both", wrong);

        // Written back, both are top-level keys beside the fields they share a slot with.
        Structure both = decode(helper, generic("\"max_footprint_in_liquid\":0.25,\"mc_versions\":\"1.21-99\""));
        JsonElement written = Structure.DIRECT_CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess()), both)
                .result().orElse(null);
        if (written == null || !written.isJsonObject()
                || !written.getAsJsonObject().has("max_footprint_in_liquid") || !written.getAsJsonObject().has("mc_versions")
                || !written.getAsJsonObject().has("start_pool") || !written.getAsJsonObject().has("cannot_spawn_in_liquid")) {
            wrong.add("written back as " + written + ", not with max_footprint_in_liquid, mc_versions, start_pool and cannot_spawn_in_liquid at the top");
        }
        finish(helper, wrong);
    }

    /**
     * Outside mc_versions a structure has no biome, so the game gives it no placement, and it turns down every spot;
     * inside, or without the field, it has its biomes as usual. On both structure types.
     */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void mcVersionsGateBiomes(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        testPiece(helper);
        String current = VersionResolver.getCurrentVersionString();
        Set<?> everyVersion = Set.copyOf(decode(helper, generic("")).biomes().stream().toList());
        if (everyVersion.isEmpty()) {
            wrong.add("the test structure has no biomes without mc_versions");
        }
        Map<String, Boolean> ranges = new LinkedHashMap<>();
        ranges.put("1.0-1.20.6", false);
        ranges.put("100-200", false);
        ranges.put(current, true);
        ranges.put("1.21-99", true);
        for (Map.Entry<String, Boolean> range : ranges.entrySet()) {
            for (String json : List.of(generic("\"mc_versions\":\"" + range.getKey() + "\""), nether("\"mc_versions\":\"" + range.getKey() + "\""))) {
                GenericJigsawStructure structure = (GenericJigsawStructure) decode(helper, json);
                Set<?> biomes = Set.copyOf(structure.biomes().stream().toList());
                String type = structure instanceof GenericNetherJigsawStructure ? "nether" : "generic";
                if (structure.generatesOnThisVersion() != range.getValue()) {
                    wrong.add(type + " mc_versions " + range.getKey() + " on " + current + ": generates " + structure.generatesOnThisVersion());
                }
                if (range.getValue() ? !biomes.equals(everyVersion) : !biomes.isEmpty()) {
                    wrong.add(type + " mc_versions " + range.getKey() + " on " + current + ": " + biomes.size() + " biomes, not " + (range.getValue() ? everyVersion.size() : 0));
                }
            }
        }
        // Even handed every biome, a structure outside its range turns the spot down before reading any terrain.
        ChunkPos chunk = new ChunkPos(100, 100);
        ChunkGenerator dry = puddles(helper, (x, z) -> false);
        if (decode(helper, generic("\"mc_versions\":\"1.0-1.20.6\"")).findValidGenerationPoint(context(helper, dry, chunk)).isPresent()) {
            wrong.add("a structure outside its mc_versions still found a spot");
        }
        if (decode(helper, generic("\"mc_versions\":\"1.21-99\"")).findValidGenerationPoint(context(helper, dry, chunk)).isEmpty()) {
            wrong.add("a structure inside its mc_versions found no spot on dry ground");
        }
        finish(helper, wrong);
    }

    /**
     * The test pack's msl_test_gated set holds one structure outside its mc_versions, so at world load MSL marks the
     * set as starting nothing, and it stops pushing other sets away; msl_test_shared also holds a structure that
     * generates, so it stays on.
     */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void versionGatedSetTurnedOff(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        //? if <1.21.2 {
        var sets = helper.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
        var structures = helper.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);
        StructureSet gated = sets.get(id("msl_test_gated"));
        StructureSet shared = sets.get(id("msl_test_shared"));
        Structure gatedStructure = structures.get(id("msl_test_gated"));
        //?} else {
        /*var sets = helper.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE_SET);
        var structures = helper.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE);
        StructureSet gated = sets.getValue(id("msl_test_gated"));
        StructureSet shared = sets.getValue(id("msl_test_shared"));
        Structure gatedStructure = structures.getValue(id("msl_test_gated"));
        *///?}
        if (gated == null || shared == null || !(gatedStructure instanceof GenericJigsawStructure gatedJigsaw)) {
            wrong.add("the test pack's gated sets or structure did not load");
        } else {
            if (gatedJigsaw.generatesOnThisVersion() || gatedJigsaw.biomes().size() != 0) {
                wrong.add("msl_test_gated generates here, or has " + gatedJigsaw.biomes().size() + " biomes");
            }
            if (!(gated.placement() instanceof AdvancedRandomSpread gatedSpread) || !gatedSpread.isDisabled()) {
                wrong.add("the set holding only a structure outside its mc_versions is still on");
            }
            if (!(shared.placement() instanceof AdvancedRandomSpread sharedSpread) || sharedSpread.isDisabled()) {
                wrong.add("the set that also holds a structure that generates was turned off");
            }
        }
        finish(helper, wrong);
    }

    /**
     * cannot_spawn_in_liquid reads the column at the middle of the start piece, which MSL centres on the start chunk's
     * corner. A 5x5 start piece over dry land in a sea spawns, though the chunk's middle (7 blocks off in x and z,
     * where the check used to read) is water; the same piece over a pond in dry land doesn't. The piece lands with
     * its middle on the corner.
     */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void liquidCheckAtStartPieceCentre(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        testPiece(helper);
        ChunkPos chunk = new ChunkPos(100, 100);
        int cx = chunk.getMinBlockX();
        int cz = chunk.getMinBlockZ();
        BiPredicate<Integer, Integer> underPiece = (x, z) -> Math.abs(x - cx) <= 2 && Math.abs(z - cz) <= 2;
        ChunkGenerator islandInSea = puddles(helper, (x, z) -> !underPiece.test(x, z));
        ChunkGenerator pondInLand = puddles(helper, underPiece);
        Structure checked = decode(helper, generic("\"cannot_spawn_in_liquid\":true"));
        Structure unchecked = decode(helper, generic(""));

        Optional<Structure.GenerationStub> onIsland = checked.findValidGenerationPoint(context(helper, islandInSea, chunk));
        if (onIsland.isEmpty()) {
            wrong.add("turned down dry land under the start piece (water 7 blocks off, at the chunk's middle)");
        } else {
            BoundingBox start = onIsland.get().getPiecesBuilder().build().pieces().get(0).getBoundingBox();
            if (start.minX() != cx - 2 || start.maxX() != cx + 2 || start.minZ() != cz - 2 || start.maxZ() != cz + 2) {
                wrong.add("the start piece landed at " + start + ", not centred on the chunk corner " + cx + ", " + cz);
            }
        }
        if (checked.findValidGenerationPoint(context(helper, pondInLand, chunk)).isPresent()) {
            wrong.add("spawned with water under the start piece (dry land at the chunk's middle)");
        }
        if (unchecked.findValidGenerationPoint(context(helper, pondInLand, chunk)).isEmpty()) {
            wrong.add("without cannot_spawn_in_liquid, water under the piece still turned the spot down");
        }
        if (!GenericJigsawStructure.isColumnInLiquid(context(helper, pondInLand, chunk), cx, cz)
                || GenericJigsawStructure.isColumnInLiquid(context(helper, pondInLand, chunk), cx + 7, cz + 7)) {
            wrong.add("the test terrain's columns read wrong");
        }
        finish(helper, wrong);
    }

    /**
     * max_footprint_in_liquid reads an even grid over the start piece, edges and corners included, at most five
     * columns a side, and turns the spot down when more than that share is over liquid. Here two of the 5x5 piece's
     * five columns of samples are in water: a share of 0.4.
     */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void footprintInLiquidShare(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        Map<String, int[]> grids = new LinkedHashMap<>();
        grids.put("0-0", new int[]{0});
        grids.put("0-1", new int[]{0, 1});
        grids.put("0-4", new int[]{0, 1, 2, 3, 4});
        grids.put("0-5", new int[]{0, 1, 3, 4, 5});
        grids.put("0-8", new int[]{0, 2, 4, 6, 8});
        grids.put("-10-89", new int[]{-10, 15, 40, 64, 89});
        for (Map.Entry<String, int[]> grid : grids.entrySet()) {
            String[] ends = grid.getKey().split("(?<=\\d)-");
            int[] got = GenericJigsawStructure.footprintSamples(Integer.parseInt(ends[0]), Integer.parseInt(ends[1]));
            if (!java.util.Arrays.equals(got, grid.getValue())) {
                wrong.add("columns " + grid.getKey() + " sampled at " + java.util.Arrays.toString(got) + ", not " + java.util.Arrays.toString(grid.getValue()));
            }
        }

        testPiece(helper);
        ChunkPos chunk = new ChunkPos(100, 100);
        int cx = chunk.getMinBlockX();
        int cz = chunk.getMinBlockZ();
        ChunkGenerator westWet = puddles(helper, (x, z) -> x <= cx - 1);
        double share = GenericJigsawStructure.footprintInLiquid(context(helper, westWet, chunk), new BoundingBox(cx - 2, 60, cz - 2, cx + 2, 70, cz + 2));
        if (share != 0.4) {
            wrong.add("a 5x5 box with two of its five columns in water read " + share + " in liquid, not 0.4");
        }
        Map<String, Boolean> spawns = new LinkedHashMap<>();
        spawns.put("", true);
        spawns.put("\"max_footprint_in_liquid\":0.4", true);
        spawns.put("\"max_footprint_in_liquid\":1", true);
        spawns.put("\"max_footprint_in_liquid\":0.39", false);
        spawns.put("\"max_footprint_in_liquid\":0", false);
        for (Map.Entry<String, Boolean> spawn : spawns.entrySet()) {
            boolean found = decode(helper, generic(spawn.getKey())).findValidGenerationPoint(context(helper, westWet, chunk)).isPresent();
            if (found != spawn.getValue()) {
                wrong.add("with {" + spawn.getKey() + "} over a footprint 0.4 in water it " + (found ? "spawned" : "was turned down"));
            }
        }
        finish(helper, wrong);
    }

    /**
     * valid_biome_radius_check reads the biomes at the ground's height under the start piece, whose middle MSL puts
     * on the start chunk's corner, on every version. Biomes here change with height (desert from y 120 up), the
     * ground is 13 blocks lower under a pond than on dry land, and start_height 64 sits on top of the ground. With
     * the pond at the corner the check finds no desert and the structure spawns; with the pond 7 blocks off in x and
     * z (at the chunk's middle, where some versions used to read) it finds desert and is turned down.
     */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void biomeRadiusReadsTheCornerHeight(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        testPiece(helper);
        Structure checked = decode(helper, generic("\"project_start_to_heightmap\":\"OCEAN_FLOOR_WG\",\"valid_biome_radius_check\":1"));
        Structure unchecked = decode(helper, generic("\"project_start_to_heightmap\":\"OCEAN_FLOOR_WG\""));
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        BiomeSource layered = new LayeredBiomes(biomes.getOrThrow(Biomes.PLAINS), biomes.getOrThrow(Biomes.DESERT), 120);
        Predicate<Holder<Biome>> notDesert = biome -> !biome.is(Biomes.DESERT);

        // Chunks of their own, so no other test's terrain is in the height cache at these columns.
        ChunkPos pondAtCorner = new ChunkPos(-300, 200);
        if (checked.findValidGenerationPoint(context(helper, pond(helper, pondAtCorner, 0), layered, pondAtCorner, notDesert)).isEmpty()) {
            wrong.add("turned down with the ground under the start piece below the desert (the chunk's middle is in it)");
        }
        ChunkPos pondAtMiddle = new ChunkPos(-310, 200);
        if (checked.findValidGenerationPoint(context(helper, pond(helper, pondAtMiddle, 7), layered, pondAtMiddle, notDesert)).isPresent()) {
            wrong.add("spawned with the ground under the start piece in the desert (the chunk's middle is below it)");
        }
        ChunkPos control = new ChunkPos(-320, 200);
        if (unchecked.findValidGenerationPoint(context(helper, pond(helper, control, 0), layered, control, notDesert)).isEmpty()) {
            wrong.add("without valid_biome_radius_check, the pond at the corner was still turned down");
        }
        finish(helper, wrong);
    }

    /** The flat test terrain with a 7x7 pond whose middle is {@code offset} blocks into the chunk in x and z. */
    private static ChunkGenerator pond(GameTestHelper helper, ChunkPos chunk, int offset) {
        int x = chunk.getMinBlockX() + offset;
        int z = chunk.getMinBlockZ() + offset;
        return puddles(helper, (px, pz) -> Math.abs(px - x) <= 3 && Math.abs(pz - z) <= 3);
    }

    /**
     * A set turned off in the config tells a structure search there is no start in its chunks before the structure is
     * asked or the chunk loaded (vanilla's StructureCheck, as /locate, maps, dolphins and eyes use it), and isn't
     * generated there; turned back on, both find the chunk again. The answer isn't cached.
     */
    //? if <1.21.5 {
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void disabledSetSkippedBySearches(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        testPiece(helper);
        ServerLevel level = helper.getLevel();
        String off = "moogs_structures:msl_test_off";
        AdvancedRandomSpread spread = (AdvancedRandomSpread) StructurePlacement.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, level.registryAccess()),
                JsonParser.parseString("{\"type\":\"moogs_structures:advanced_random_spread\",\"salt\":7,\"spacing\":6,\"separation\":2,\"structure_id\":\"" + off + "\"}"))
                .getOrThrow();
        Structure structure = decode(helper, generic(""));
        var state = level.getChunkSource().getGeneratorState();
        // A chunk this set tries, far from anything generated.
        ChunkPos chunk = spread.getPotentialStructureChunk(state.getLevelSeed(), 3000, 3000);
        int[] xz = xz(chunk);
        try {
            setDisabled(off, true);
            expectSearch(level, spread, structure, chunk, state, false, "turned off", wrong);
            setDisabled(off, false);
            expectSearch(level, spread, structure, chunk, state, true, "turned on", wrong);
            setDisabled(off, true);
            expectSearch(level, spread, structure, chunk, state, false, "turned off again", wrong);
        } finally {
            setDisabled(off, false);
        }
        if (wrong.isEmpty() && !spread.applyAdditionalChunkRestrictions(xz[0], xz[1], state.getLevelSeed())) {
            wrong.add("turned back on, the set still refuses its chunk");
        }
        finish(helper, wrong);
    }

    private static void expectSearch(ServerLevel level, AdvancedRandomSpread spread, Structure structure, ChunkPos chunk,
                                     net.minecraft.world.level.chunk.ChunkGeneratorStructureState state, boolean on, String when, List<String> wrong) {
        int[] xz = xz(chunk);
        if (spread.isDisabled() == on) {
            wrong.add(when + ": the set reads as " + (on ? "off" : "on"));
        }
        if (spread.isStructureChunk(state, xz[0], xz[1]) != on) {
            wrong.add(when + ": generation " + (on ? "skips" : "still tries") + " the set's chunk");
        }
        StructureCheckResult result = level.structureManager().checkStructurePresence(chunk, structure, spread, false);
        StructureCheckResult want = on ? StructureCheckResult.CHUNK_LOAD_NEEDED : StructureCheckResult.START_NOT_PRESENT;
        if (result != want) {
            wrong.add(when + ": a search got " + result + ", not " + want);
        }
    }

    private static void setDisabled(String id, boolean disabled) {
        MslConfig.get().setStructureDisabledAndSave(id, disabled);
        // As /reload does: the worldgen side reads the config's snapshot, rebuilt on load.
        ReplaceVanillaManager.reloadConfig();
    }

    /** A generic jigsaw structure whose start pool is msl_test_piece alone, in every overworld biome, with these fields. */
    private static String generic(String fields) {
        return structureJson("moogs_structures:moogs_structures_generic_jigsaw_structure", "", fields);
    }

    private static String nether(String fields) {
        return structureJson("moogs_structures:moogs_structures_generic_nether_jigsaw_structure", "\"land_search_direction\":\"HIGHEST_LAND\",", fields);
    }

    private static String structureJson(String type, String extra, String fields) {
        return "{\"type\":\"" + type + "\",\"start_pool\":{\"fallback\":\"minecraft:empty\",\"elements\":[{\"weight\":1,\"element\":"
                + "{\"element_type\":\"minecraft:single_pool_element\",\"location\":\"moogs_structures:msl_test_piece\",\"processors\":\"minecraft:empty\",\"projection\":\"rigid\"}}]},"
                + "\"size\":1,\"start_height\":{\"absolute\":64},\"biomes\":\"#minecraft:is_overworld\",\"step\":\"surface_structures\",\"spawn_overrides\":{},"
                + extra + "\"use_bounding_box_hack\":false" + (fields.isEmpty() ? "" : "," + fields) + "}";
    }

    private static Structure decode(GameTestHelper helper, String json) {
        return Structure.DIRECT_CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess()), JsonParser.parseString(json)).getOrThrow();
    }

    /** msl_test_piece: a 5x1x5 slab of stone, the start piece the liquid tests lay out. */
    private static void testPiece(GameTestHelper helper) {
        fill(helper, new BlockPos(1, 10, 1), new BlockPos(5, 10, 5), Blocks.STONE);
        StructureTemplate piece = templates(helper).getOrCreate(id("msl_test_piece"));
        //? if >=1.21.6 {
        /*piece.fillFromWorld(helper.getLevel(), helper.absolutePos(new BlockPos(1, 10, 1)), new Vec3i(5, 1, 5), false, List.of(Blocks.STRUCTURE_VOID));
        *///?} else {
        piece.fillFromWorld(helper.getLevel(), helper.absolutePos(new BlockPos(1, 10, 1)), new Vec3i(5, 1, 5), false, Blocks.STRUCTURE_VOID);
        //?}
        fill(helper, new BlockPos(1, 10, 1), new BlockPos(5, 10, 5), Blocks.AIR);
    }

    private static StructureTemplateManager templates(GameTestHelper helper) {
        //? if <26.3 {
        return helper.getLevel().getServer().getStructureManager();
        //?} else {
        /*return helper.getLevel().getServer().getStructureTemplateManager();
        *///?}
    }

    /** The test world's flat generator, its terrain stone to y 63 except where {@code wet} puts water over y 50. */
    private static ChunkGenerator puddles(GameTestHelper helper, BiPredicate<Integer, Integer> wet) {
        if (!(helper.getLevel().getChunkSource().getGenerator() instanceof FlatLevelSource flat)) {
            throw new IllegalStateException("the game test world isn't flat");
        }
        return new PuddleGenerator(flat.settings(), wet);
    }

    /** What structures are told about a chunk while they choose a spot, here over the given terrain, in any biome. */
    private static Structure.GenerationContext context(GameTestHelper helper, ChunkGenerator generator, ChunkPos chunk) {
        return context(helper, generator, generator.getBiomeSource(), chunk, biome -> true);
    }

    /** The same, with these biomes and only the biomes {@code valid} lets through. */
    private static Structure.GenerationContext context(GameTestHelper helper, ChunkGenerator generator, BiomeSource biomes, ChunkPos chunk,
                                                       Predicate<Holder<Biome>> valid) {
        ServerLevel level = helper.getLevel();
        RandomState random = level.getChunkSource().randomState();
        //? if <26.3 {
        return new Structure.GenerationContext(level.registryAccess(), generator, biomes, random, templates(helper),
                level.getSeed(), chunk, level, valid);
        //?} else {
        /*// As the game's own StructureCheck makes its climate sampler.
        var climate = random.createClimateSampler(net.minecraft.world.level.levelgen.densityfunction.SamplerContext.builder().enableCaches().build());
        return new Structure.GenerationContext(level.registryAccess(), generator, biomes, climate, random, templates(helper),
                level.getSeed(), chunk, level, valid);
        *///?}
    }

    /** A chunk's x and z: fields before 26.1.2, record components from it. */
    private static int[] xz(ChunkPos chunk) {
        //? if <26.1.2 {
        return new int[]{chunk.x, chunk.z};
        //?} else {
        /*return new int[]{chunk.x(), chunk.z()};
        *///?}
    }

    /** Flat-world terrain the liquid checks can read: stone to y 63, or water from y 51 up where {@code wet} says. */
    private static final class PuddleGenerator extends FlatLevelSource {
        private static final int TOP = 63;
        private static final int FLOOR = 50;
        private final BiPredicate<Integer, Integer> wet;

        private PuddleGenerator(FlatLevelGeneratorSettings settings, BiPredicate<Integer, Integer> wet) {
            super(settings);
            this.wet = wet;
        }

        @Override
        public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
            // Water fills the surface heightmaps, not the ocean floor ones.
            boolean water = this.wet.test(x, z) && !type.isOpaque().test(Blocks.WATER.defaultBlockState());
            return (water ? FLOOR : TOP) + 1;
        }

        @Override
        public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
            //? if <1.21.2 {
            int minY = level.getMinBuildHeight();
            //?} else {
            /*int minY = level.getMinY();
            *///?}
            boolean water = this.wet.test(x, z);
            BlockState[] states = new BlockState[TOP - minY + 1];
            for (int y = minY; y <= TOP; y++) {
                states[y - minY] = water && y > FLOOR ? Blocks.WATER.defaultBlockState() : Blocks.STONE.defaultBlockState();
            }
            return new NoiseColumn(minY, states);
        }
    }

    /** One biome below a height and another from it up, the same in every column. */
    private static final class LayeredBiomes extends BiomeSource {
        private final Holder<Biome> below;
        private final Holder<Biome> above;
        private final int fromQuart;

        private LayeredBiomes(Holder<Biome> below, Holder<Biome> above, int fromY) {
            this.below = below;
            this.above = above;
            this.fromQuart = QuartPos.fromBlock(fromY);
        }

        @Override
        protected MapCodec<? extends BiomeSource> codec() {
            throw new UnsupportedOperationException("only built in game tests");
        }

        @Override
        protected Stream<Holder<Biome>> collectPossibleBiomes() {
            return Stream.of(this.below, this.above);
        }

        //? if <26.3 {
        @Override
        public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
            return y >= this.fromQuart ? this.above : this.below;
        }
        //?} else {
        /*@Override
        public BiomeResolver createResolver(Climate.Sampler sampler) {
            return (x, y, z) -> y >= this.fromQuart ? this.above : this.below;
        }
        *///?}
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
        return decode(helper, StructureProcessorType.SINGLE_CODEC, json);
    }

    private static <T> T decode(GameTestHelper helper, Codec<T> codec, String json) {
        return codec.parse(RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess()), JsonParser.parseString(json)).getOrThrow();
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
        return fillFromWorld(new StructureTemplate(), helper, from, size);
    }

    private static StructureTemplate fillFromWorld(StructureTemplate template, GameTestHelper helper, BlockPos from, Vec3i size) {
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
