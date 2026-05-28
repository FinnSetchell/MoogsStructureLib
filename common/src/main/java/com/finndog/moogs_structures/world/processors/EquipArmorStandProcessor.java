package com.finndog.moogs_structures.world.processors;

import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
import com.finndog.moogs_structures.utils.GeneralUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Equips armor onto armor-stand entities as a structure is placed. Each armor stand rolls one
 * "armor set" from an inline weighted list; the chosen set's items (with any enchantments/trims
 * authored on them) are written into the stand's {@code ArmorItems} NBT.
 *
 * <p>Targets every {@code minecraft:armor_stand} in any piece whose processor list includes this
 * processor. Item slots are full {@link ItemStack}s (via {@link ItemStack#SINGLE_ITEM_CODEC}), so
 * enchantments and trims are expressed with the vanilla item-component format.
 *
 * <p>Being a {@link StructureEntityProcessor}, it is invoked by MSL's fabric
 * {@code EntityProcessorMixin} during entity placement.
 */
public class EquipArmorStandProcessor extends StructureEntityProcessor {

    /**
     * One full set of armor. Any slot may be omitted (left empty). Each item is a full ItemStack,
     * so enchantments/trims/etc. are authored via the {@code components} field.
     */
    public record ArmorSet(Optional<ItemStack> head, Optional<ItemStack> chest, Optional<ItemStack> legs, Optional<ItemStack> feet) {
        // Wrap vanilla ItemStack codec so datapacks authored against the pre-1.21.5
        // wrapped enchantments schema ({"minecraft:enchantments":{"levels":{...}}})
        // still load on MC 1.21.5+, which expects the flat schema.
        private static final Codec<ItemStack> ITEM_CODEC =
                EnchantmentsSchemaCompatCodec.wrap(ItemStack.SINGLE_ITEM_CODEC);

        public static final Codec<ArmorSet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ITEM_CODEC.optionalFieldOf("head").forGetter(ArmorSet::head),
                ITEM_CODEC.optionalFieldOf("chest").forGetter(ArmorSet::chest),
                ITEM_CODEC.optionalFieldOf("legs").forGetter(ArmorSet::legs),
                ITEM_CODEC.optionalFieldOf("feet").forGetter(ArmorSet::feet)
        ).apply(instance, ArmorSet::new));
    }

    public static final MapCodec<EquipArmorStandProcessor> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            Codec.mapPair(ArmorSet.CODEC.fieldOf("armor"), Codec.intRange(1, Integer.MAX_VALUE).fieldOf("weight"))
                    .codec().listOf().fieldOf("armor_sets").forGetter(p -> p.weightedSets)
    ).apply(instance, instance.stable(EquipArmorStandProcessor::new)));

    public final List<Pair<ArmorSet, Integer>> weightedSets;

    private EquipArmorStandProcessor(List<Pair<ArmorSet, Integer>> weightedSets) {
        this.weightedSets = weightedSets;
    }

    @Override
    public StructureTemplate.StructureEntityInfo processEntity(ServerLevelAccessor serverLevelAccessor,
                                                               BlockPos structurePiecePos,
                                                               BlockPos structurePieceBottomCenterPos,
                                                               StructureTemplate.StructureEntityInfo localEntityInfo,
                                                               StructureTemplate.StructureEntityInfo globalEntityInfo,
                                                               StructurePlaceSettings structurePlaceSettings) {
        CompoundTag nbt = globalEntityInfo.nbt;
        if (nbt == null || !"minecraft:armor_stand".equals(nbt.getString("id"))) {
            return globalEntityInfo;
        }
        if (weightedSets.isEmpty()) {
            return globalEntityInfo;
        }

        RandomSource random = structurePlaceSettings.getRandom(globalEntityInfo.blockPos);
        ArmorSet set = GeneralUtils.getRandomEntry(weightedSets, random);
        if (set == null) {
            return globalEntityInfo;
        }

        HolderLookup.Provider provider = serverLevelAccessor.registryAccess();
        CompoundTag newNbt = nbt.copy();

        // ArmorStand reads ArmorItems in EquipmentSlot armor index order: feet, legs, chest, head.
        ListTag armorItems = new ListTag();
        armorItems.add(saveOrEmpty(set.feet(), provider));
        armorItems.add(saveOrEmpty(set.legs(), provider));
        armorItems.add(saveOrEmpty(set.chest(), provider));
        armorItems.add(saveOrEmpty(set.head(), provider));
        newNbt.put("ArmorItems", armorItems);

        return new StructureTemplate.StructureEntityInfo(globalEntityInfo.pos, globalEntityInfo.blockPos, newNbt);
    }

    private static Tag saveOrEmpty(Optional<ItemStack> optionalStack, HolderLookup.Provider provider) {
        if (optionalStack.isEmpty() || optionalStack.get().isEmpty()) {
            return new CompoundTag();
        }
        return optionalStack.get().save(provider);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.EQUIP_ARMOR_STAND_PROCESSOR.get();
    }
}
