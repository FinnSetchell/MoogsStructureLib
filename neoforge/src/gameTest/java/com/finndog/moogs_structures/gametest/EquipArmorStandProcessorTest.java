package com.finndog.moogs_structures.gametest;

import com.finndog.moogs_structures.world.processors.HangingEntityAnchorProcessor;
import com.finndog.moogs_structures.world.processors.EquipArmorStandProcessor;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("moogs_structures")
@EventBusSubscriber(modid = "moogs_structures", bus = EventBusSubscriber.Bus.MOD)
public class EquipArmorStandProcessorTest {

	private static final String PROCESSOR_JSON =
		"{\"armor_sets\":[{\"armor\":{\"chest\":{\"id\":\"minecraft:diamond_chestplate\",\"count\":1}},\"weight\":1}]}";

	@SubscribeEvent
	public static void register(RegisterGameTestsEvent event) {
		event.register(EquipArmorStandProcessorTest.class);
	}

	@GameTest(templateNamespace = "moogs_structures", template = "armor_stand_processor_test_empty")
	public static void equipArmorItemsIsWritten(GameTestHelper helper) {
		var ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
		EquipArmorStandProcessor processor = EquipArmorStandProcessor.CODEC
			.codec()
			.parse(ops, JsonParser.parseString(PROCESSOR_JSON))
			.result()
			.orElseThrow(() -> new AssertionError("processor decode failed"));

		CompoundTag armorStandNbt = new CompoundTag();
		armorStandNbt.putString("id", "minecraft:armor_stand");

		StructureTemplate.StructureEntityInfo info = new StructureTemplate.StructureEntityInfo(
			Vec3.ZERO, BlockPos.ZERO, armorStandNbt
		);

		StructureTemplate.StructureEntityInfo result = processor.processEntity(
			helper.getLevel(), BlockPos.ZERO, BlockPos.ZERO, info, info, new StructurePlaceSettings()
		);

		CompoundTag resultNbt = result.nbt;
		if (!resultNbt.contains("ArmorItems")) {
			helper.fail("ArmorItems key missing from result nbt");
			return;
		}
		ListTag armorItems = resultNbt.getList("ArmorItems", 10);
		if (armorItems.size() < 3 || armorItems.getCompound(2).isEmpty()) {
			helper.fail("chest slot (index 2) of ArmorItems is empty");
			return;
		}
		helper.succeed();
	}

	// Calls NeoForge's NATIVE processEntity signature (the one structure placement invokes), NOT MSL's
	// overload. Before the neoforge bridge this runs NeoForge's no-op default and the stand stays unequipped.
	@GameTest(templateNamespace = "moogs_structures", template = "armor_stand_processor_test_empty")
	public static void neoforgeNativeHookEquips(GameTestHelper helper) {
		var ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
		EquipArmorStandProcessor processor = EquipArmorStandProcessor.CODEC
			.codec()
			.parse(ops, JsonParser.parseString(PROCESSOR_JSON))
			.result()
			.orElseThrow(() -> new AssertionError("processor decode failed"));

		CompoundTag armorStandNbt = new CompoundTag();
		armorStandNbt.putString("id", "minecraft:armor_stand");
		StructureTemplate.StructureEntityInfo info = new StructureTemplate.StructureEntityInfo(
			Vec3.ZERO, BlockPos.ZERO, armorStandNbt);

		StructureTemplate.StructureEntityInfo result = ((StructureProcessor) processor).processEntity(
			helper.getLevel(), BlockPos.ZERO, info, info, new StructurePlaceSettings(), new StructureTemplate());

		if (result == null || !result.nbt.contains("ArmorItems")
			|| result.nbt.getList("ArmorItems", 10).size() < 3
			|| result.nbt.getList("ArmorItems", 10).getCompound(2).isEmpty()) {
			helper.fail("NeoForge processEntity hook left the stand unequipped -- entity processors are not wired on NeoForge");
			return;
		}
		helper.succeed();
	}

	// Block-attached entity anchors (issue 17). A template's baked anchor is stale wherever the
	// structure lands, so it must come back rewritten to the entity's placed position.
	@GameTest(templateNamespace = "moogs_structures", template = "armor_stand_processor_test_empty")
	public static void hangingEntityAnchorIsRewritten(GameTestHelper helper) {
		CompoundTag frameNbt = new CompoundTag();
		frameNbt.putString("id", "minecraft:item_frame");
		frameNbt.putInt("TileX", 9999);
		StructureTemplate.StructureEntityInfo info = new StructureTemplate.StructureEntityInfo(
			new Vec3(5.0, 6.0, 7.0), new BlockPos(5, 6, 7), frameNbt);

		StructureTemplate.StructureEntityInfo out = HangingEntityAnchorProcessor.INSTANCE.processEntity(
			helper.getLevel(), BlockPos.ZERO, BlockPos.ZERO, info, info, new StructurePlaceSettings());

		if (out == null || out.nbt.getInt("TileX") != 5 || out.nbt.getInt("TileY") != 6 || out.nbt.getInt("TileZ") != 7) {
			helper.fail("item frame anchor was not rewritten to its placed position");
			return;
		}
		helper.succeed();
	}

}
