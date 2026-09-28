//? if >=1.21.1 <26.1.2 {
package com.finndog.moogs_structures.gametest.forge;

import com.finndog.moogs_structures.world.processors.EquipArmorStandProcessor;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
//? if <1.21.5 {
import net.minecraft.gametest.framework.GameTest;
//?}
import net.minecraft.gametest.framework.GameTestHelper;
//? if >=1.21.5 <1.21.11 {
/*import net.minecraft.network.chat.Component;
*///?}
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
//? if <1.21.5 {
import net.minecraftforge.gametest.GameTestHolder;
//?} else {
/*import net.minecraftforge.gametest.GameTest;
import net.minecraftforge.gametest.GameTestDontPrefix;
import net.minecraftforge.gametest.GameTestNamespace;
*///?}

// Forge 1.21.5+ dropped vanilla's @GameTest annotation for GameTestInstance, but kept its own
// annotation system (net.minecraftforge.gametest). Same idea as the older branches otherwise.
//? if <1.21.5 {
@GameTestHolder("moogs_structures")
//?} else {
/*@GameTestNamespace("moogs_structures")
@GameTestDontPrefix
*///?}
public class EquipArmorStandProcessorTest {

	private static final String PROCESSOR_JSON =
		"{\"armor_sets\":[{\"armor\":{\"chest\":{\"id\":\"minecraft:diamond_chestplate\",\"count\":1}},\"weight\":1}]}";

	// Calls Forge's NATIVE processEntity signature (the one structure placement invokes), NOT MSL's
	// overload. Before the forge bridge this runs Forge's no-op default and the stand stays unequipped.
	//? if <1.21.5 {
	@GameTest(template = "armor_stand_processor_test_empty")
	//?} else {
	/*@GameTest(structure = "armor_stand_processor_test_empty")
	*///?}
	public static void forgeNativeHookEquips(GameTestHelper helper) {
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

		//? if <1.21.5 {
		if (result == null || !result.nbt.contains("ArmorItems")
			|| result.nbt.getList("ArmorItems", 10).size() < 3
			|| result.nbt.getList("ArmorItems", 10).getCompound(2).isEmpty()) {
			helper.fail("Forge processEntity hook left the stand unequipped -- entity processors are not wired on Forge");
		//?} else {
		/*if (result == null || !result.nbt.contains("equipment")
			|| !result.nbt.getCompoundOrEmpty("equipment").contains("chest")) {
		*///?}
			//? if >=1.21.5 <1.21.11 {
			/*helper.fail(Component.literal("Forge processEntity hook left the stand unequipped -- entity processors are not wired on Forge"));
			*///?}
			//? if >=1.21.11 {
			/*helper.fail("Forge processEntity hook left the stand unequipped -- entity processors are not wired on Forge");
			*///?}
			return;
		}
		helper.succeed();
	}
}

//?}