//? if >=1.20.6 {
package com.finndog.moogs_structures.gametest.neoforge;

import com.finndog.moogs_structures.world.processors.HangingEntityAnchorProcessor;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
//? if <1.21.5 {
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
//?}

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Paintings and item frames in a placed template, at every rotation and mirror, end up where their blocks do:
 * a template is saved from real entities, placed through the game's own placement with MSL's anchor
 * processor, and each placed entity is compared with its original moved the way the template's blocks move.
 * The paintings cover odd and even widths and heights (an even one is centred on the line between blocks).
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
public class HangingEntityPlacementTests {
    /** An empty 24 x 16 x 24 arena of air. */
    private static final String ARENA = "msl_arena";

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        // Only in a game test run: a registered test instance has to sync to joining clients (1.21.5+).
        if (System.getProperty("neoforge.enabledGameTestNamespaces") == null) {
            return;
        }
        //? if <1.21.5 {
        event.register(HangingEntityPlacementTests.class);
        //?} else {
        /*var env = event.registerEnvironment(id("hanging_entity_tests"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(id("hanging_entities_keep_their_place"), new GameTestInstance(new TestData<>(env, id(ARENA), 100, 0, true)) {
            @Override
            public void run(GameTestHelper helper) {
                hangingEntitiesKeepTheirPlace(helper);
            }

            @Override
            public MapCodec<? extends GameTestInstance> codec() {
                throw new UnsupportedOperationException();
            }

            @Override
            protected MutableComponent typeDescription() {
                return Component.literal("HangingEntityPlacementTests");
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
    @GameTest(templateNamespace = "moogs_structures", template = ARENA)
    //?}
    public static void hangingEntitiesKeepTheirPlace(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // The originals hang in the top of the arena, in a 10 x 6 x 10 box, with no wall needed: nothing ticks
        // long enough for a painting to check what it hangs on.
        BlockPos source = helper.absolutePos(new BlockPos(7, 9, 7));
        Vec3i size = new Vec3i(10, 6, 10);
        painting(helper, "kebab", 9, 10, 9, Direction.SOUTH);      // 1 x 1
        painting(helper, "sunset", 13, 10, 9, Direction.EAST);     // 2 wide
        painting(helper, "graham", 9, 10, 13, Direction.NORTH);    // 2 tall
        painting(helper, "bust", 13, 10, 13, Direction.WEST);      // 2 x 2
        painting(helper, "skeleton", 10, 12, 11, Direction.SOUTH); // 4 wide, 3 tall
        painting(helper, "pointer", 11, 11, 15, Direction.NORTH);  // 4 x 4
        itemFrame(helper, 8, 10, 11, Direction.EAST);
        itemFrame(helper, 15, 9, 11, Direction.UP);
        List<HangingEntity> originals = level.getEntitiesOfClass(HangingEntity.class,
                AABB.encapsulatingFullBlocks(source, source.offset(size).offset(-1, -1, -1)));
        List<String> wrong = new ArrayList<>();
        if (originals.size() != 8) {
            wrong.add("summoned 8 paintings and item frames but found " + originals.size());
        }

        StructureTemplate template = new StructureTemplate();
        //? if >=1.21.6 {
        /*template.fillFromWorld(level, source, size, true, List.of(Blocks.STRUCTURE_VOID));
        *///?} else {
        template.fillFromWorld(level, source, size, true, Blocks.STRUCTURE_VOID);
        //?}

        // Placed in the bottom of the arena, turned and mirrored about the template's corner.
        BlockPos target = helper.absolutePos(new BlockPos(12, 1, 12));
        AABB placedArea = AABB.encapsulatingFullBlocks(helper.absolutePos(BlockPos.ZERO), helper.absolutePos(new BlockPos(23, 7, 23)));
        for (Mirror mirror : Mirror.values()) {
            for (Rotation rotation : Rotation.values()) {
                StructurePlaceSettings settings = new StructurePlaceSettings()
                        .setMirror(mirror)
                        .setRotation(rotation)
                        .addProcessor(HangingEntityAnchorProcessor.INSTANCE);
                template.placeInWorld(level, target, target, settings, RandomSource.create(1), Block.UPDATE_CLIENTS);
                List<HangingEntity> placed = level.getEntitiesOfClass(HangingEntity.class, placedArea);
                for (HangingEntity original : originals) {
                    Vec3 expected = StructureTemplate.transform(original.position().subtract(Vec3.atLowerCornerOf(source)), mirror, rotation, BlockPos.ZERO)
                            .add(Vec3.atLowerCornerOf(target));
                    Direction facing = rotation.rotate(mirror.mirror(original.getDirection()));
                    List<HangingEntity> sameType = placed.stream().filter(e -> e.getType() == original.getType()).toList();
                    if (sameType.stream().noneMatch(e -> e.getDirection() == facing && e.position().distanceTo(expected) < 1.0E-3)) {
                        String found = sameType.stream()
                                .min(Comparator.comparingDouble(e -> e.position().distanceTo(expected)))
                                .map(e -> "found " + describe(e.position(), e.getDirection(), target))
                                .orElse("found none");
                        wrong.add(mirror + " " + rotation + ": " + EntityType.getKey(original.getType()).getPath()
                                + " from " + original.getPos().subtract(source).toShortString()
                                + " should be " + describe(expected, facing, target) + ", " + found);
                    }
                }
                placed.forEach(e -> e.discard());
            }
        }
        originals.forEach(e -> e.discard());

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

    private static void painting(GameTestHelper helper, String variant, int x, int y, int z, Direction facing) {
        summon(helper, "painting", x, y, z, "{facing:" + facing.get2DDataValue() + "b,variant:\"minecraft:" + variant + "\"}");
    }

    private static void itemFrame(GameTestHelper helper, int x, int y, int z, Direction facing) {
        summon(helper, "item_frame", x, y, z, "{Facing:" + facing.get3DDataValue() + "b}");
    }

    /** Summoned at the centre of the block it hangs in, so that block is its anchor. */
    private static void summon(GameTestHelper helper, String type, int x, int y, int z, String nbt) {
        Vec3 at = Vec3.atCenterOf(helper.absolutePos(new BlockPos(x, y, z)));
        ServerLevel level = helper.getLevel();
        level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack().withSuppressedOutput(),
                String.format(Locale.ROOT, "summon minecraft:%s %.1f %.1f %.1f %s", type, at.x, at.y, at.z, nbt));
    }

    private static String describe(Vec3 pos, Direction facing, BlockPos target) {
        Vec3 local = pos.subtract(Vec3.atLowerCornerOf(target));
        return String.format(Locale.ROOT, "(%.3f, %.3f, %.3f) facing %s", local.x, local.y, local.z, facing.getName());
    }
}
//?}
