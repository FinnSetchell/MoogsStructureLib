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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
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
		// Only register inside an actual gametest run. The gameTest classes sit on the dev
		// client/server classpath too, and a registered test instance has to be encodable for
		// the test_instance registry sync (1.21.5+) - otherwise joining clients get dropped.
		if (System.getProperty("neoforge.enabledGameTestNamespaces") == null) {
			return;
		}
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
					equipArmorStand(helper);
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

	private static EquipArmorStandProcessor decode() {
		return EquipArmorStandProcessor.MAP_CODEC
			.codec()
			.parse(JsonOps.INSTANCE, JsonParser.parseString(PROCESSOR_JSON))
			.result()
			.orElseThrow(() -> new AssertionError("processor decode failed"));
	}

	private static StructureTemplate.StructureEntityInfo standInfo() {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("id", "minecraft:armor_stand");
		return new StructureTemplate.StructureEntityInfo(Vec3.ZERO, BlockPos.ZERO, nbt);
	}

	private static boolean equipped(StructureTemplate.StructureEntityInfo info) {
		return info != null && info.nbt.contains("equipment")
			&& info.nbt.getCompoundOrEmpty("equipment").contains("chest");
	}

	private static void equipArmorStand(GameTestHelper helper) {
		EquipArmorStandProcessor processor = decode();

		// (1) MSL's own overload. Always works even when the NeoForge hook is unwired, which is why
		// the old test passed while structures still generated unequipped (card 272's blind spot).
		StructureTemplate.StructureEntityInfo direct = processor.processEntity(
			helper.getLevel(), BlockPos.ZERO, BlockPos.ZERO, standInfo(), standInfo(), new StructurePlaceSettings());
		if (!equipped(direct)) {
			helper.fail("MSL processEntity overload did not equip the stand");
			return;
		}

		// (2) NeoForge's NATIVE processEntity signature -- the one StructureTemplate placement
		// actually invokes. Before the neoforge bridge this runs NeoForge's no-op default and the
		// stand comes back unequipped (card 271: entity processors dead on NeoForge).
		StructureTemplate.StructureEntityInfo viaHook = ((StructureProcessor) processor).processEntity(
			helper.getLevel(), BlockPos.ZERO, standInfo(), standInfo(), new StructurePlaceSettings(), new StructureTemplate());
		if (!equipped(viaHook)) {
			helper.fail("NeoForge processEntity hook left the stand unequipped -- entity processors are not wired on NeoForge");
			return;
		}

		helper.succeed();
	}
}
