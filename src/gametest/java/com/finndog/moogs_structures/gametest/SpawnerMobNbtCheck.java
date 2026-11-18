package com.finndog.moogs_structures.gametest;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The desert temple guardian (gold gear, a name, 35 health, low drop chances) written once in an old version's
 * format with {@code nbt_version}, through MSL's spawner and trial spawner processors. Each mob is summoned from the
 * NBT the processor gave the block, which loads it the way its spawner would, and has to come out with all of it on
 * whatever version runs the test. Shared by the Fabric and NeoForge game tests, in the 24 x 16 x 24
 * {@code msl_arena}.
 */
public final class SpawnerMobNbtCheck {
    private SpawnerMobNbtCheck() { }

    /** The guardian as 1.20 writes it: items with Count and tag, Attributes with Name and Base, gear in lists. */
    private static final String GUARDIAN_1_20 = """
            {"CustomName": "{\\"text\\": \\"Desert Temple Guardian\\", \\"italic\\": false}",
             "PersistenceRequired": 1, "CanPickUpLoot": 0, "Health": 35.0,
             "Attributes": [{"Name": "minecraft:generic.max_health", "Base": 35.0}],
             "ArmorItems": [{}, {},
               {"id": "minecraft:golden_chestplate", "Count": 1, "tag": {"Enchantments": [{"id": "minecraft:thorns", "lvl": 1}, {"id": "minecraft:protection", "lvl": 3}]}},
               {"id": "minecraft:golden_helmet", "Count": 1, "tag": {"Enchantments": [{"id": "minecraft:protection", "lvl": 4}],
                 "Trim": {"material": "minecraft:gold", "pattern": "minecraft:dune"}}}],
             "HandItems": [{"id": "minecraft:golden_sword", "Count": 1, "tag": {"Enchantments": [{"id": "minecraft:sharpness", "lvl": 4}]}}, {}],
             "ArmorDropChances": [0.085, 0.085, 0.085, 0.02], "HandDropChances": [0.05, 0.0]}""";

    /** MTR's guardian exactly as its 1.21-1.21.1 overlay writes it (components, wrapped enchantments, generic. ids). */
    static final String GUARDIAN_1_21 = """
            {"CustomName": "{\\"text\\": \\"Desert Temple Guardian\\", \\"italic\\": false}",
             "PersistenceRequired": 1, "CanPickUpLoot": 0, "Health": 35.0,
             "attributes": [{"id": "minecraft:generic.armor", "base": 2.0}, {"id": "minecraft:generic.attack_damage", "base": 3.0},
               {"id": "minecraft:generic.max_health", "base": 35.0}, {"id": "minecraft:generic.movement_speed", "base": 0.23}],
             "ArmorItems": [{}, {},
               {"id": "minecraft:golden_chestplate", "count": 1, "components": {"minecraft:enchantments": {"levels": {"minecraft:thorns": 1, "minecraft:protection": 3}}}},
               {"id": "minecraft:golden_helmet", "count": 1, "components": {"minecraft:enchantments": {"levels": {"minecraft:power": 4, "minecraft:thorns": 1, "minecraft:protection": 3}},
                 "minecraft:trim": {"material": "minecraft:gold", "pattern": "minecraft:dune"}}}],
             "HandItems": [{"id": "minecraft:golden_sword", "count": 1, "components": {"minecraft:enchantments": {"levels": {"minecraft:sharpness": 4, "minecraft:fire_aspect": 1}}}}, {}],
             "ArmorDropChances": [0.001, 0.001, 0.001, 0.001], "HandDropChances": [0.001, 0.001]}""";

    /** What came out wrong, empty when every guardian has everything. */
    public static List<String> run(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        long start = System.nanoTime();
        CompoundTag mob = spawnerMob(helper, wrong, "1.20", GUARDIAN_1_20);
        MoogsStructuresCommon.LOGGER.info("MSL game test: first mob NBT upgrade took {} ms", (System.nanoTime() - start) / 1_000_000);
        check(helper, "the 1.20 guardian", mob, 0.02f, 0.05f, wrong);
        // Without nbt_version the same NBT goes in as it is: what the game makes of it on its own.
        CompoundTag asItIs = spawnerMob(helper, wrong, null, GUARDIAN_1_20);
        List<String> unupgraded = new ArrayList<>();
        check(helper, "the 1.20 guardian without nbt_version", asItIs, 0.02f, 0.05f, unupgraded);
        MoogsStructuresCommon.LOGGER.info("MSL game test: without nbt_version: {}", unupgraded.isEmpty() ? "everything kept" : unupgraded);
        //? if >=1.21.1 {
        check(helper, "the 1.21 guardian", spawnerMob(helper, wrong, "1.21", GUARDIAN_1_21), 0.001f, 0.001f, wrong);
        check(helper, "the 1.21 trial spawner guardian", trialSpawnerMob(helper, wrong), 0.001f, 0.001f, wrong);
        //?}
        return wrong;
    }

    private static CompoundTag spawnerMob(GameTestHelper helper, List<String> wrong, String nbtVersion, String nbt) {
        String json = "{\"processor_type\": \"moogs_structures:spawner_randomizing_processor\","
                + (nbtVersion == null ? "" : "\"nbt_version\": \"" + nbtVersion + "\",")
                + "\"weighted_entities\": [{\"entity\": \"minecraft:husk\", \"weight\": 1, \"nbt\": " + nbt + "}]}";
        CompoundTag spawner = processed(helper, json, Blocks.SPAWNER.defaultBlockState(), wrong);
        return spawner != null && spawner.get("SpawnData") instanceof CompoundTag data && data.get("entity") instanceof CompoundTag mob ? mob : null;
    }

    //? if >=1.21.1 {
    private static CompoundTag trialSpawnerMob(GameTestHelper helper, List<String> wrong) {
        CompoundTag trialSpawner = processed(helper, "{\"processor_type\": \"moogs_structures:trial_spawner_randomizing_processor\","
                + "\"normal_config\": \"moogs_structures:gametest_guardian\", \"nbt_version\": \"1.21\"}", Blocks.TRIAL_SPAWNER.defaultBlockState(), wrong);
        return trialSpawner != null && trialSpawner.get("normal_config") instanceof CompoundTag config
                && config.get("spawn_potentials") instanceof ListTag potentials && !potentials.isEmpty()
                && potentials.get(0) instanceof CompoundTag potential && potential.get("data") instanceof CompoundTag data
                && data.get("entity") instanceof CompoundTag mob ? mob : null;
    }
    //?}

    /** The block entity NBT the processor gives a block of this state. */
    private static CompoundTag processed(GameTestHelper helper, String json, BlockState state, List<String> wrong) {
        ServerLevel level = helper.getLevel();
        DataResult<StructureProcessor> parsed = StructureProcessorType.SINGLE_CODEC.parse(
                RegistryOps.create(JsonOps.INSTANCE, level.registryAccess()), JsonParser.parseString(json));
        StructureProcessor processor = parsed.result().orElse(null);
        if (processor == null) {
            wrong.add("couldn't read " + json + ": " + parsed);
            return null;
        }
        BlockPos at = helper.absolutePos(new BlockPos(1, 2, 1));
        List<StructureTemplate.StructureBlockInfo> out = StructureTemplate.processBlockInfos(level, at, at,
                new StructurePlaceSettings().addProcessor(processor), List.of(new StructureTemplate.StructureBlockInfo(BlockPos.ZERO, state, null)));
        return out.isEmpty() ? null : out.get(0).nbt();
    }

    /** Summons the mob from its NBT, as its spawner loads it, and checks it has all of the guardian. */
    private static void check(GameTestHelper helper, String which, CompoundTag nbt, float headDrop, float mainhandDrop, List<String> wrong) {
        if (nbt == null) {
            wrong.add(which + ": the processor gave its block no mob");
            return;
        }
        ServerLevel level = helper.getLevel();
        BlockPos at = helper.absolutePos(new BlockPos(12, 8, 12));
        CompoundTag summoned = nbt.copy();
        summoned.putBoolean("NoAI", true);
        summoned.putBoolean("NoGravity", true);
        summoned.putBoolean("Silent", true);
        level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack().withSuppressedOutput(),
                String.format(Locale.ROOT, "summon minecraft:husk %d.5 %d %d.5 %s", at.getX(), at.getY(), at.getZ(), summoned));
        AABB box = new AABB(at.getX() - 1, at.getY() - 1, at.getZ() - 1, at.getX() + 2, at.getY() + 3, at.getZ() + 2);
        List<Entity> found = level.getEntities((Entity) null, box, e -> e instanceof Mob);
        if (found.size() != 1) {
            wrong.add(which + ": summoning it from " + nbt + " gave " + found.size() + " mobs");
            found.forEach(Entity::discard);
            return;
        }
        Mob mob = (Mob) found.get(0);
        mob.discard();

        enchanted(which, mob, EquipmentSlot.HEAD, Items.GOLDEN_HELMET, wrong);
        enchanted(which, mob, EquipmentSlot.CHEST, Items.GOLDEN_CHESTPLATE, wrong);
        enchanted(which, mob, EquipmentSlot.MAINHAND, Items.GOLDEN_SWORD, wrong);
        String name = mob.getCustomName() == null ? null : mob.getCustomName().getString();
        if (!"Desert Temple Guardian".equals(name)) {
            wrong.add(which + " is named " + name);
        }
        if (mob.getMaxHealth() != 35.0f) {
            wrong.add(which + " has " + mob.getMaxHealth() + " max health, not 35");
        }
        dropChance(which, mob, EquipmentSlot.HEAD, headDrop, wrong);
        dropChance(which, mob, EquipmentSlot.MAINHAND, mainhandDrop, wrong);
    }

    private static void enchanted(String which, Mob mob, EquipmentSlot slot, Item item, List<String> wrong) {
        ItemStack worn = mob.getItemBySlot(slot);
        if (!worn.is(item) || !worn.isEnchanted()) {
            wrong.add(which + " has " + worn + " in " + slot.getName() + ", not an enchanted " + item);
        }
    }

    private static void dropChance(String which, Mob mob, EquipmentSlot slot, float want, List<String> wrong) {
        //? if <1.21.5 {
        ListTag chances = mob.saveWithoutId(new CompoundTag()).getList(slot.getType() == EquipmentSlot.Type.HAND ? "HandDropChances" : "ArmorDropChances", 5);
        float got = chances.getFloat(slot.getIndex());
        //?} else {
        /*float got = mob.getDropChances().byEquipment(slot);
        *///?}
        if (got != want) {
            wrong.add(which + " drops its " + slot.getName() + " item " + got + " of the time, not " + want);
        }
    }
}
