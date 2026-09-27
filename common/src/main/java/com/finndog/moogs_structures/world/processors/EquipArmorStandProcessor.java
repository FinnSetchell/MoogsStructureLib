package com.finndog.moogs_structures.world.processors;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
import com.finndog.moogs_structures.utils.GeneralUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
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
 * processor. Item slots are full {@link ItemStack}s (via {@link ItemStack#CODEC}), so enchantments
 * and trims are expressed in the vanilla item NBT format.
 *
 * <p>Being a {@link StructureEntityProcessor}, it is invoked by MSL's fabric
 * {@code EntityProcessorMixin} during entity placement.
 */
public class EquipArmorStandProcessor extends StructureEntityProcessor {

    /**
     * One full set of armor. Any slot may be omitted (left empty). Each item is a full ItemStack,
     * so enchantments/trims/etc. are authored via the item's {@code tag} NBT.
     */
    public record ArmorSet(Optional<ItemStack> head, Optional<ItemStack> chest, Optional<ItemStack> legs, Optional<ItemStack> feet) {
        public static final Codec<ArmorSet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                loggedItemCodec("head").optionalFieldOf("head").forGetter(ArmorSet::head),
                loggedItemCodec("chest").optionalFieldOf("chest").forGetter(ArmorSet::chest),
                loggedItemCodec("legs").optionalFieldOf("legs").forGetter(ArmorSet::legs),
                loggedItemCodec("feet").optionalFieldOf("feet").forGetter(ArmorSet::feet)
        ).apply(instance, ArmorSet::new));
    }

    // 1.20's ItemStack.CODEC requires "Count", and this era's optionalFieldOf drops a decode error
    // silently, so an item written as {"id": ...} (or with 1.20.5's lowercase "count") left the stand
    // bare with no log line. Either spelling is accepted here and a missing count means one.
    private static final Codec<ItemStack> ITEM_CODEC = RecordCodecBuilder.create(instance -> instance.group(
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
            Codec.mapPair(ArmorSet.CODEC.fieldOf("armor"), Codec.intRange(1, Integer.MAX_VALUE).fieldOf("weight"))
                    .codec().listOf().fieldOf("armor_sets").forGetter(p -> p.weightedSets)
    ).apply(instance, EquipArmorStandProcessor::new));

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
        armorItems.add(saveOrEmpty(set.feet()));
        armorItems.add(saveOrEmpty(set.legs()));
        armorItems.add(saveOrEmpty(set.chest()));
        armorItems.add(saveOrEmpty(set.head()));
        newNbt.put("ArmorItems", armorItems);

        return new StructureTemplate.StructureEntityInfo(globalEntityInfo.pos, globalEntityInfo.blockPos, newNbt);
    }

    private static Tag saveOrEmpty(Optional<ItemStack> optionalStack) {
        if (optionalStack.isEmpty() || optionalStack.get().isEmpty()) {
            return new CompoundTag();
        }
        return optionalStack.get().save(new CompoundTag());
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.EQUIP_ARMOR_STAND_PROCESSOR.get();
    }
}
