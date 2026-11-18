package com.finndog.moogs_structures.gametest;

import com.finndog.moogs_structures.world.processors.HangingEntityAnchorProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Armour stands and a mob, each facing its own way, saved into a template and placed at every rotation and mirror:
 * once the way the game places a template, and once with one of MSL's entity processors, which on Fabric makes MSL
 * place the entities itself. Each entity has to come out in the same spot facing the same way both times. Shared by
 * the Fabric and NeoForge game tests, in the 24 x 16 x 24 {@code msl_arena}.
 */
public final class EntityFacingCheck {
    private EntityFacingCheck() { }

    /** What came out differently, empty when everything matched. */
    public static List<String> run(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // The originals stand in the top of the arena, with nothing needed under them: nothing ticks long enough
        // for anything to move.
        BlockPos source = helper.absolutePos(new BlockPos(9, 10, 9));
        Vec3i size = new Vec3i(5, 3, 5);
        summon(helper, "armor_stand", 10, 10, 10, 30);
        summon(helper, "armor_stand", 12, 10, 11, 90);
        summon(helper, "armor_stand", 11, 10, 13, 200);
        summon(helper, "pig", 13, 10, 13, 135);
        List<Entity> originals = level.getEntities((Entity) null,
                box(source, source.offset(size).offset(-1, -1, -1)), e -> !(e instanceof Player));
        List<String> wrong = new ArrayList<>();
        if (originals.size() != 4) {
            wrong.add("summoned 3 armour stands and a pig but found " + originals.size() + " entities");
        }

        StructureTemplate template = new StructureTemplate();
        //? if >=1.21.6 {
        /*template.fillFromWorld(level, source, size, true, List.of(Blocks.STRUCTURE_VOID));
        *///?} else {
        template.fillFromWorld(level, source, size, true, Blocks.STRUCTURE_VOID);
        //?}

        // Placed in the bottom of the arena, turned and mirrored about the template's corner.
        BlockPos target = helper.absolutePos(new BlockPos(12, 1, 12));
        AABB placedArea = box(helper.absolutePos(BlockPos.ZERO), helper.absolutePos(new BlockPos(23, 7, 23)));
        for (Mirror mirror : Mirror.values()) {
            for (Rotation rotation : Rotation.values()) {
                StructurePlaceSettings plain = new StructurePlaceSettings().setMirror(mirror).setRotation(rotation);
                StructurePlaceSettings msl = new StructurePlaceSettings().setMirror(mirror).setRotation(rotation)
                        .addProcessor(HangingEntityAnchorProcessor.INSTANCE);
                List<Entity> byGame = place(level, template, target, plain, placedArea);
                List<Entity> byMsl = place(level, template, target, msl, placedArea);
                if (byGame.size() != byMsl.size()) {
                    wrong.add(mirror + " " + rotation + ": the game placed " + byGame.size() + " entities, MSL " + byMsl.size());
                }
                for (Entity expected : byGame) {
                    Entity match = byMsl.stream()
                            .filter(e -> e.getType() == expected.getType() && e.position().distanceTo(expected.position()) < 1.0E-3)
                            .findFirst().orElse(null);
                    if (match == null) {
                        wrong.add(mirror + " " + rotation + ": " + name(expected) + " the game put at " + local(expected, target)
                                + " is missing from MSL's placement");
                    } else if (Mth.degreesDifferenceAbs(match.getYRot(), expected.getYRot()) > 0.01F) {
                        wrong.add(mirror + " " + rotation + ": " + name(expected) + " at " + local(expected, target)
                                + " faces " + yaw(match.getYRot()) + " with MSL, " + yaw(expected.getYRot()) + " as the game places it");
                    }
                }
            }
        }
        originals.forEach(e -> e.discard());
        return wrong;
    }

    /** Places the template, returns what it put in the world, and takes it away again. */
    private static List<Entity> place(ServerLevel level, StructureTemplate template, BlockPos target, StructurePlaceSettings settings, AABB area) {
        template.placeInWorld(level, target, target, settings, RandomSource.create(1), Block.UPDATE_CLIENTS);
        List<Entity> placed = level.getEntities((Entity) null, area, e -> !(e instanceof Player));
        placed.forEach(e -> e.discard());
        return placed;
    }

    private static void summon(GameTestHelper helper, String type, int x, int y, int z, float yaw) {
        Vec3 at = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(x, y, z)));
        ServerLevel level = helper.getLevel();
        level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack().withSuppressedOutput(),
                String.format(Locale.ROOT, "summon minecraft:%s %.1f %.1f %.1f {NoGravity:1b,NoAI:1b,Silent:1b,Rotation:[%.1ff,0.0f]}",
                        type, at.x, at.y, at.z, yaw));
    }

    /** The blocks from one corner to the other, both included. */
    private static AABB box(BlockPos from, BlockPos to) {
        return new AABB(from.getX(), from.getY(), from.getZ(), to.getX() + 1, to.getY() + 1, to.getZ() + 1);
    }

    private static String name(Entity entity) {
        return EntityType.getKey(entity.getType()).getPath();
    }

    private static String local(Entity entity, BlockPos target) {
        Vec3 pos = entity.position().subtract(Vec3.atLowerCornerOf(target));
        return String.format(Locale.ROOT, "(%.2f, %.2f, %.2f)", pos.x, pos.y, pos.z);
    }

    private static String yaw(float degrees) {
        return String.format(Locale.ROOT, "%.0f", Mth.wrapDegrees(degrees));
    }
}
