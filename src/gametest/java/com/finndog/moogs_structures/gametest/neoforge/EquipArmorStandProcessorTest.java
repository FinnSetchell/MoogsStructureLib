package com.finndog.moogs_structures.gametest.neoforge;

//? if >=1.21.4 <1.21.5 {
import com.finndog.moogs_structures.world.processors.HangingEntityAnchorProcessor;
//?}
//? if >=1.21.11 <26.1.2 {
import com.finndog.moogs_structures.world.processors.HangingEntityAnchorProcessor;
//?}
import com.finndog.moogs_structures.world.processors.EquipArmorStandProcessor;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
//? if >=1.21.5 {
import com.mojang.serialization.MapCodec;
//?}
import net.minecraft.core.BlockPos;
//? if <1.21.5 {
import net.minecraft.gametest.framework.GameTest;
//?}
import net.minecraft.gametest.framework.GameTestHelper;
//? if >=1.21.5 {
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
//?}
import net.minecraft.nbt.CompoundTag;
//? if <1.21.5 {
import net.minecraft.nbt.ListTag;
//?} else {
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
//?}
//? if >=1.21.11 {
import net.minecraft.resources.ResourceLocation;
//?}
//? if <26.1.2 {
import net.minecraft.resources.RegistryOps;
//?}
//? if >=1.21.5 <1.21.11 {
import net.minecraft.resources.ResourceLocation;
//?}
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
//? if <1.21.5 {
import net.neoforged.neoforge.gametest.GameTestHolder;
//?}

//? if <1.21.5 {
@GameTestHolder("moogs_structures")
//?}
//? if <1.21.10 {
@EventBusSubscriber(modid = "moogs_structures", bus = EventBusSubscriber.Bus.MOD)
//?} else {
@EventBusSubscriber(modid = "moogs_structures")
//?}
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
		//? if <1.21.5 {
		event.register(EquipArmorStandProcessorTest.class);
		//?} else {
		var env = event.registerEnvironment(
			ResourceLocation.fromNamespaceAndPath("moogs_structures", "armor_stand_processor_test"),
			new TestEnvironmentDefinition.AllOf()
		);
		var data = new TestData<>(
			env,
			ResourceLocation.fromNamespaceAndPath("moogs_structures", "armor_stand_processor_test_empty"),
			100, 0, true
		);
		event.registerTest(
			ResourceLocation.fromNamespaceAndPath("moogs_structures", "equip_armor_stand_processor"),
			new GameTestInstance(data) {
				@Override
				public void run(GameTestHelper helper) {
		//?}
					//? if >=1.21.5 <26.2 {
					equipmentKeyIsWritten(helper);
					//?}
					//? if >=26.2 {
					equipArmorStand(helper);
					//?}
				//? if >=1.21.5 {
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
				//?}
	}

	//? if <1.21.5 {
	@GameTest(templateNamespace = "moogs_structures", template = "armor_stand_processor_test_empty")
	public static void equipArmorItemsIsWritten(GameTestHelper helper) {
	//?}
	//? if >=1.21.5 <26.2 {
	private static void equipmentKeyIsWritten(GameTestHelper helper) {
	//?}
		//? if <26.1.2 {
		var ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
		//?}
		//? if <26.2 {
		EquipArmorStandProcessor processor = EquipArmorStandProcessor.CODEC
		//?} else {
	private static EquipArmorStandProcessor decode() {
		return EquipArmorStandProcessor.MAP_CODEC
		//?}
			.codec()
			//? if <26.1.2 {
			.parse(ops, JsonParser.parseString(PROCESSOR_JSON))
			//?} else {
			.parse(JsonOps.INSTANCE, JsonParser.parseString(PROCESSOR_JSON))
			//?}
			.result()
			.orElseThrow(() -> new AssertionError("processor decode failed"));
	//? if >=26.2 {
	}
	//?}

		//? if <26.2 {
		CompoundTag armorStandNbt = new CompoundTag();
		armorStandNbt.putString("id", "minecraft:armor_stand");
		//?} else {
	private static StructureTemplate.StructureEntityInfo standInfo() {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("id", "minecraft:armor_stand");
		return new StructureTemplate.StructureEntityInfo(Vec3.ZERO, BlockPos.ZERO, nbt);
	}
		//?}

		//? if <26.2 {
		StructureTemplate.StructureEntityInfo info = new StructureTemplate.StructureEntityInfo(
			Vec3.ZERO, BlockPos.ZERO, armorStandNbt
		);
		//?} else {
	private static boolean equipped(StructureTemplate.StructureEntityInfo info) {
		return info != null && info.nbt.contains("equipment")
			&& info.nbt.getCompoundOrEmpty("equipment").contains("chest");
	}
		//?}

		//? if <26.2 {
		StructureTemplate.StructureEntityInfo result = processor.processEntity(
			helper.getLevel(), BlockPos.ZERO, BlockPos.ZERO, info, info, new StructurePlaceSettings()
		);
		//?} else {
	private static void equipArmorStand(GameTestHelper helper) {
		EquipArmorStandProcessor processor = decode();
		//?}

		//? if <26.2 {
		CompoundTag resultNbt = result.nbt;
		//?}
		//? if <1.21.5 {
		if (!resultNbt.contains("ArmorItems")) {
			helper.fail("ArmorItems key missing from result nbt");
		//?}
		//? if >=1.21.5 <26.2 {
		if (!resultNbt.contains("equipment")) {
		//?}
			//? if >=1.21.5 <1.21.11 {
			helper.fail(Component.literal("equipment key missing -- processor wrote wrong key for this MC version"));
			//?}
			//? if >=1.21.11 <26.2 {
			helper.fail("equipment key missing -- processor wrote wrong key for this MC version");
			//?}
			//? if <26.2 {
			return;
		}
			//?}
		//? if <1.21.5 {
		ListTag armorItems = resultNbt.getList("ArmorItems", 10);
		if (armorItems.size() < 3 || armorItems.getCompound(2).isEmpty()) {
			helper.fail("chest slot (index 2) of ArmorItems is empty");
		//?}
		//? if >=1.21.5 <26.2 {
		CompoundTag equipment = resultNbt.getCompoundOrEmpty("equipment");
		if (!equipment.contains("chest")) {
		//?}
			//? if >=1.21.5 <1.21.11 {
			helper.fail(Component.literal("chest slot missing from equipment compound"));
			//?}
			//? if >=1.21.11 <26.2 {
			helper.fail("chest slot missing from equipment compound");
			//?}
		// (1) MSL's own overload. Always works even when the NeoForge hook is unwired, which is why
		// the old test passed while structures still generated unequipped (card 272's blind spot).
		//? if >=26.2 {
		StructureTemplate.StructureEntityInfo direct = processor.processEntity(
			helper.getLevel(), BlockPos.ZERO, BlockPos.ZERO, standInfo(), standInfo(), new StructurePlaceSettings());
		if (!equipped(direct)) {
			helper.fail("MSL processEntity overload did not equip the stand");
		//?}
			//? if >=1.21.5 {
			return;
		}

		// NeoForge's NATIVE processEntity signature -- the hook structure placement actually invokes.
		// Before the neoforge bridge this runs NeoForge's no-op default and the stand stays unequipped.
		StructureTemplate.StructureEntityInfo viaHook = ((StructureProcessor) processor).processEntity(
			//?}
			//? if >=1.21.5 <26.2 {
			helper.getLevel(), BlockPos.ZERO, info, info, new StructurePlaceSettings(), new StructureTemplate());
		if (viaHook == null || !viaHook.nbt.getCompoundOrEmpty("equipment").contains("chest")) {
			//?}
			//? if >=26.2 {
			helper.getLevel(), BlockPos.ZERO, standInfo(), standInfo(), new StructurePlaceSettings(), new StructureTemplate());
		if (!equipped(viaHook)) {
			//?}
			//? if >=1.21.5 <1.21.11 {
			helper.fail(Component.literal("NeoForge processEntity hook left the stand unequipped -- entity processors are not wired on NeoForge"));
			//?}
			//? if >=1.21.11 {
			helper.fail("NeoForge processEntity hook left the stand unequipped -- entity processors are not wired on NeoForge");
			//?}
			return;
		}
		// Block-attached entity anchors (issue 17). A template's baked anchor is stale wherever the
		// structure lands, so it must come back rewritten to the entity's placed position.
		//? if >=1.21.11 <26.1.2 {
		CompoundTag frameNbt = new CompoundTag();
		frameNbt.putString("id", "minecraft:item_frame");
		StructureTemplate.StructureEntityInfo frameInfo = new StructureTemplate.StructureEntityInfo(
			new Vec3(5.0, 6.0, 7.0), new BlockPos(5, 6, 7), frameNbt);
		StructureTemplate.StructureEntityInfo anchored = HangingEntityAnchorProcessor.INSTANCE.processEntity(
			helper.getLevel(), BlockPos.ZERO, BlockPos.ZERO, frameInfo, frameInfo, new StructurePlaceSettings());
		if (anchored == null || !anchored.nbt.read("block_pos", BlockPos.CODEC)
				.map(new BlockPos(5, 6, 7)::equals).orElse(false)) {
			helper.fail("item frame anchor was not rewritten to its placed position");
			return;
		}

		//?}
		helper.succeed();
	}

	// Calls NeoForge's NATIVE processEntity signature (the one structure placement invokes), NOT MSL's
	// overload. Before the neoforge bridge this runs NeoForge's no-op default and the stand stays unequipped.
	//? if <1.21.5 {
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
	//?}

	// Block-attached entity anchors (issue 17). A template's baked anchor is stale wherever the
	// structure lands, so it must come back rewritten to the entity's placed position.
	//? if >=1.21.4 <1.21.5 {
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

	//?}
}
