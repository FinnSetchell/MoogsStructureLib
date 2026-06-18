package com.finndog.moogs_structures.gametest;

import com.finndog.moogs_structures.world.processors.EquipArmorStandProcessor;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * Smoke test: verifies that EquipArmorStandProcessor writes the correct NBT key for the
 * running MC version. Catches silent key-drift (e.g. "ArmorItems" -> "equipment" in 1.21.5)
 * before players see naked armor stands.
 *
 * Runs via: ./gradlew :neoforge:runGameTestServer
 */
@GameTestHolder("moogs_structures")
public class EquipArmorStandProcessorTest {

    // Minimal processor config: one armor set with a diamond chestplate, weight 1.
    // The test uses raw CompoundTag NBT (not ItemStack) because this branch's ArmorSet
    // stores raw item NBT to defer component validation -- see EquipArmorStandProcessor.
    private static final String PROCESSOR_JSON =
        "{\"armor_sets\":[{\"armor\":{\"chest\":{\"id\":\"minecraft:diamond_chestplate\",\"count\":1}},\"weight\":1}]}";

    @GameTest(template = "moogs_structures:armor_stand_processor_test_empty")
    public void equipment_key_is_written(GameTestHelper helper) {
        EquipArmorStandProcessor processor = EquipArmorStandProcessor.CODEC
            .codec()
            .parse(JsonOps.INSTANCE, JsonParser.parseString(PROCESSOR_JSON))
            .result()
            .orElseThrow(() -> new AssertionError("processor JSON failed to decode"));

        CompoundTag armorStandNbt = new CompoundTag();
        armorStandNbt.putString("id", "minecraft:armor_stand");

        StructureTemplate.StructureEntityInfo info = new StructureTemplate.StructureEntityInfo(
            Vec3.ZERO, BlockPos.ZERO, armorStandNbt
        );

        // serverLevelAccessor is unused on this branch -- pass the helper level anyway
        // so the test also works if a future branch re-introduces registryAccess() usage.
        StructureTemplate.StructureEntityInfo result = processor.processEntity(
            helper.getLevel(), BlockPos.ZERO, BlockPos.ZERO, info, info, new StructurePlaceSettings()
        );

        CompoundTag resultNbt = result.nbt;
        if (!resultNbt.contains("equipment")) {
            helper.fail("equipment key missing from armor stand NBT -- processor wrote wrong key for this MC version");
            return;
        }
        CompoundTag equipment = resultNbt.getCompound("equipment");
        if (!equipment.contains("chest")) {
            helper.fail("chest slot missing from equipment compound");
            return;
        }
        helper.succeed();
    }
}
