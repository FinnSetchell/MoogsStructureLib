package com.finndog.moogs_structures.gametest;

import com.finndog.moogs_structures.world.processors.EquipArmorStandProcessor;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

@EventBusSubscriber(modid = "moogs_structures")
public class EquipArmorStandProcessorTest {

	private static final String PROCESSOR_JSON =
		"{\"armor_sets\":[{\"armor\":{\"chest\":{\"id\":\"minecraft:diamond_chestplate\",\"count\":1}},\"weight\":1}]}";

	@SubscribeEvent
	public static void register(RegisterGameTestsEvent event) {
		var env = event.registerEnvironment(
			Identifier.fromNamespaceAndPath("moogs_structures", "armor_stand_processor_test"),
			new TestEnvironmentDefinition.AllOf()
		);
		var data = new TestData<>(
			env,
			Identifier.fromNamespaceAndPath("moogs_structures", "armor_stand_processor_test_empty"),
			100, 0, true
		);
		event.registerTest(
			Identifier.fromNamespaceAndPath("moogs_structures", "equip_armor_stand_processor"),
			new GameTestInstance(data) {
				@Override
				public void run(GameTestHelper helper) {
					equipmentKeyIsWritten(helper);
				}

				@Override
				public MapCodec<? extends GameTestInstance> codec() {
					throw new UnsupportedOperationException();
				}

				@Override
				protected MutableComponent typeDescription() {
					return Component.literal("EquipArmorStandProcessorTest");
				}
			}
		);
	}

	private static void equipmentKeyIsWritten(GameTestHelper helper) {
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
		if (!resultNbt.contains("equipment")) {
			helper.fail("equipment key missing -- processor wrote wrong key for this MC version");
			return;
		}
		CompoundTag equipment = resultNbt.getCompoundOrEmpty("equipment");
		if (!equipment.contains("chest")) {
			helper.fail("chest slot missing from equipment compound");
			return;
		}
		helper.succeed();
	}
}
