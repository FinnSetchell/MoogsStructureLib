package com.finndog.moogs_structures.world.processors;

//? if <1.20.6 {
/*import com.finndog.moogs_structures.MoogsStructuresCommon;
*///?}
//? if <26.2 {
import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
//?}
import com.finndog.moogs_structures.utils.GeneralUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
//? if >=1.20.6 {
import com.mojang.serialization.MapCodec;
//?} else {
/*import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
*///?}
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
//? if <26.1.2 {
import net.minecraft.core.HolderLookup;
//?}
//? if <1.20.6 {
/*import net.minecraft.core.registries.BuiltInRegistries;
*///?}
import net.minecraft.nbt.CompoundTag;
//? if <1.21.5 {
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
//?}
//? if >=1.21.5 <26.1.2 {
/*import net.minecraft.nbt.NbtOps;
*///?}
//? if <1.20.6 {
/*import net.minecraft.resources.ResourceLocation;
*///?}
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
//? if <26.2 {
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
//?}
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
    //? if <26.1.2 {
    public record ArmorSet(Optional<ItemStack> head, Optional<ItemStack> chest, Optional<ItemStack> legs, Optional<ItemStack> feet) {
    //?}
        // Wrap vanilla ItemStack codec so datapacks authored against the pre-1.21.5
        // wrapped enchantments schema ({"minecraft:enchantments":{"levels":{...}}})
        // still load on MC 1.21.5+, which expects the flat schema.
        //? if >=1.21.5 <26.1.2 {
        /*private static final Codec<ItemStack> ITEM_CODEC =
                EnchantmentsSchemaCompatCodec.wrap(ItemStack.SINGLE_ITEM_CODEC);
        *///?}
    //? if >=26.1.2 {
    /*public record ArmorSet(Optional<CompoundTag> head, Optional<CompoundTag> chest, Optional<CompoundTag> legs, Optional<CompoundTag> feet) {
        // Wrap CompoundTag.CODEC (not ItemStack.CODEC) so the pre-1.21.5 wrapped enchantments
        // schema ({"minecraft:enchantments":{"levels":{...}}}) is still normalised to the flat
        // 1.21.5+ shape at decode time, but no eager item-component validation runs.
        private static final Codec<CompoundTag> ITEM_CODEC =
                EnchantmentsSchemaCompatCodec.wrap(CompoundTag.CODEC);
    *///?}

        // Also accepts the pre-1.20.5 item shape ({id, Count, tag}), upgrading it to components, so
        // one processor JSON works on both sides of the 1.20.5 data-components split.
        //? if >=1.20.6 <1.21.1 {
        /*private static final Codec<ItemStack> ITEM_CODEC = ItemComponentsCompatCodec.wrap(ItemStack.SINGLE_ITEM_CODEC);

        *///?}
        public static final Codec<ArmorSet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                //? if >=1.21.1 <1.21.5 {
                ItemStack.SINGLE_ITEM_CODEC.optionalFieldOf("head").forGetter(ArmorSet::head),
                ItemStack.SINGLE_ITEM_CODEC.optionalFieldOf("chest").forGetter(ArmorSet::chest),
                ItemStack.SINGLE_ITEM_CODEC.optionalFieldOf("legs").forGetter(ArmorSet::legs),
                ItemStack.SINGLE_ITEM_CODEC.optionalFieldOf("feet").forGetter(ArmorSet::feet)
                //?}
                //? if >=1.20.6 <1.21.1 {
                /*ITEM_CODEC.optionalFieldOf("head").forGetter(ArmorSet::head),
                ITEM_CODEC.optionalFieldOf("chest").forGetter(ArmorSet::chest),
                ITEM_CODEC.optionalFieldOf("legs").forGetter(ArmorSet::legs),
                ITEM_CODEC.optionalFieldOf("feet").forGetter(ArmorSet::feet)
                *///?}
                //? if <1.20.6 {
                /*loggedItemCodec("head").optionalFieldOf("head").forGetter(ArmorSet::head),
                loggedItemCodec("chest").optionalFieldOf("chest").forGetter(ArmorSet::chest),
                loggedItemCodec("legs").optionalFieldOf("legs").forGetter(ArmorSet::legs),
                loggedItemCodec("feet").optionalFieldOf("feet").forGetter(ArmorSet::feet)
                *///?}
                //? if >=1.21.5 {
                /*ITEM_CODEC.optionalFieldOf("head").forGetter(ArmorSet::head),
                ITEM_CODEC.optionalFieldOf("chest").forGetter(ArmorSet::chest),
                ITEM_CODEC.optionalFieldOf("legs").forGetter(ArmorSet::legs),
                ITEM_CODEC.optionalFieldOf("feet").forGetter(ArmorSet::feet)
                *///?}
        ).apply(instance, ArmorSet::new));
    }

    //? if >=1.20.6 <26.2 {
    public static final MapCodec<EquipArmorStandProcessor> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
    //?}
    // 1.20's ItemStack.CODEC requires "Count", and this era's optionalFieldOf drops a decode error
    // silently, so an item written as {"id": ...} (or with 1.20.5's lowercase "count") left the stand
    // bare with no log line. Either spelling is accepted here and a missing count means one.
    //? if <1.20.6 {
    /*private static final Codec<ItemStack> ITEM_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            // The item registry defaults unknown ids to air, so resolve by hand to report a typo.
            ResourceLocation.CODEC.comapFlatMap(
                    id -> BuiltInRegistries.ITEM.getOptional(id).map(DataResult::success)
                            .orElseGet(() -> DataResult.error(() -> "unknown item " + id)),
                    BuiltInRegistries.ITEM::getKey).fieldOf("id").forGetter(ItemStack::getItem),
            Codec.INT.optionalFieldOf("Count").forGetter(stack -> Optional.of(stack.getCount())),
            Codec.INT.optionalFieldOf("count").forGetter(stack -> Optional.empty()),
            CompoundTag.CODEC.optionalFieldOf("tag").forGetter(stack -> Optional.ofNullable(stack.getTag()))
    ).apply(instance, (item, upperCount, lowerCount, tag) -> {
        ItemStack stack = new ItemStack(item, upperCount.or(() -> lowerCount).orElse(1));
        tag.ifPresent(stack::setTag);
        return stack;
    }));

    private static Codec<ItemStack> loggedItemCodec(String slot) {
        return Codec.of(ITEM_CODEC, new Decoder<>() {
            @Override
            public <T> DataResult<Pair<ItemStack, T>> decode(DynamicOps<T> ops, T input) {
                DataResult<Pair<ItemStack, T>> result = ITEM_CODEC.decode(ops, input);
                result.error().ifPresent(error -> MoogsStructuresCommon.LOGGER.warn(
                        "equip_armor_stand: could not read the {} item, that slot stays empty ({})", slot, error.message()));
                return result;
            }
        });
    }

    public static final Codec<EquipArmorStandProcessor> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
    *///?}
    //? if >=26.2 {
    /*public static final MapCodec<EquipArmorStandProcessor> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
    *///?}
            Codec.mapPair(ArmorSet.CODEC.fieldOf("armor"), Codec.intRange(1, Integer.MAX_VALUE).fieldOf("weight"))
                    .codec().listOf().fieldOf("armor_sets").forGetter(p -> p.weightedSets)
    //? if >=1.20.6 {
    ).apply(instance, instance.stable(EquipArmorStandProcessor::new)));
    //?} else {
    /*).apply(instance, EquipArmorStandProcessor::new));
    *///?}

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
        //? if <1.21.5 {
        if (nbt == null || !"minecraft:armor_stand".equals(nbt.getString("id"))) {
        //?} else {
        /*if (nbt == null || !nbt.getString("id").map("minecraft:armor_stand"::equals).orElse(false)) {
        *///?}
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

        //? if <26.1.2 {
        HolderLookup.Provider provider = serverLevelAccessor.registryAccess();
        //?}
        CompoundTag newNbt = nbt.copy();

        // ArmorStand reads ArmorItems in EquipmentSlot armor index order: feet, legs, chest, head.
        //? if <1.21.5 {
        ListTag armorItems = new ListTag();
        //?}
        //? if >=1.20.6 <1.21.5 {
        armorItems.add(saveOrEmpty(set.feet(), provider));
        armorItems.add(saveOrEmpty(set.legs(), provider));
        armorItems.add(saveOrEmpty(set.chest(), provider));
        armorItems.add(saveOrEmpty(set.head(), provider));
        //?}
        //? if <1.20.6 {
        /*armorItems.add(saveOrEmpty(set.feet()));
        armorItems.add(saveOrEmpty(set.legs()));
        armorItems.add(saveOrEmpty(set.chest()));
        armorItems.add(saveOrEmpty(set.head()));
        *///?}
        //? if <1.21.5 {
        newNbt.put("ArmorItems", armorItems);
        //?} else {
        /*CompoundTag equipment = new CompoundTag();
        *///?}
        //? if >=1.21.5 <26.1.2 {
        /*putSlot(equipment, "feet", set.feet(), provider);
        putSlot(equipment, "legs", set.legs(), provider);
        putSlot(equipment, "chest", set.chest(), provider);
        putSlot(equipment, "head", set.head(), provider);
        *///?}
        //? if >=26.1.2 {
        /*set.feet().ifPresent(tag -> equipment.put("feet", tag));
        set.legs().ifPresent(tag -> equipment.put("legs", tag));
        set.chest().ifPresent(tag -> equipment.put("chest", tag));
        set.head().ifPresent(tag -> equipment.put("head", tag));
        *///?}
        //? if >=1.21.5 {
        /*newNbt.put("equipment", equipment);
        *///?}

        return new StructureTemplate.StructureEntityInfo(globalEntityInfo.pos, globalEntityInfo.blockPos, newNbt);
    //? if <26.1.2 {
    }

    //?}
    //? if >=1.20.6 <1.21.5 {
    private static Tag saveOrEmpty(Optional<ItemStack> optionalStack, HolderLookup.Provider provider) {
    //?}
    //? if <1.20.6 {
    /*private static Tag saveOrEmpty(Optional<ItemStack> optionalStack) {
    *///?}
        //? if <1.21.5 {
        if (optionalStack.isEmpty() || optionalStack.get().isEmpty()) {
            return new CompoundTag();
        }
        //?}
        //? if >=1.20.6 <1.21.5 {
        return optionalStack.get().save(provider);
        //?}
        //? if <1.20.6 {
        /*return optionalStack.get().save(new CompoundTag());
        *///?}
    //? if >=1.21.5 <26.1.2 {
    /*private static void putSlot(CompoundTag equipment, String slot, Optional<ItemStack> optionalStack, HolderLookup.Provider provider) {
        if (optionalStack.isEmpty() || optionalStack.get().isEmpty()) return;
    *///?}
        //? if >=1.21.5 <1.21.11 {
        /*equipment.put(slot, ItemStack.SINGLE_ITEM_CODEC.encodeStart(
        *///?}
        //? if >=1.21.11 <26.1.2 {
        /*equipment.put(slot, ItemStack.CODEC.encodeStart(
        *///?}
                //? if >=1.21.5 <26.1.2 {
                /*provider.createSerializationContext(NbtOps.INSTANCE),
                optionalStack.get()).getOrThrow());
                *///?}
    }

    @Override
    //? if <26.2 {
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.EQUIP_ARMOR_STAND_PROCESSOR.get();
    //?} else {
    /*public MapCodec<EquipArmorStandProcessor> codec() {
        return MAP_CODEC;
    *///?}
    }
}
