package com.finndog.moogs_structures.gametest;

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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;

@GameTestHolder("moogs_structures")
@Mod.EventBusSubscriber(modid = "moogs_structures", bus = Mod.EventBusSubscriber.Bus.MOD)
public class EquipArmorStandProcessorTest {

	private static final String PROCESSOR_JSON =
		"{\"armor_sets\":[{\"armor\":{\"chest\":{\"id\":\"minecraft:diamond_chestplate\",\"Count\":1}},\"weight\":1}]}";

	@SubscribeEvent
	public static void register(RegisterGameTestsEvent event) {
		event.register(EquipArmorStandProcessorTest.class);
	}

	@GameTest(templateNamespace = "moogs_structures", template = "armor_stand_processor_test_empty")
	public static void equipArmorItemsIsWritten(GameTestHelper helper) {
		var ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
		EquipArmorStandProcessor processor = EquipArmorStandProcessor.CODEC
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
}
