package com.finndog.moogs_structures.world.processors;

import com.finndog.moogs_structures.modinit.MoogsStructuresProcessors;
import com.finndog.moogs_structures.utils.GeneralUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
 * authored on them) are written into the stand's {@code equipment} NBT.
 *
 * <p>Targets every {@code minecraft:armor_stand} in any piece whose processor list includes this
 * processor. Item slots are authored as full item NBT (the same shape vanilla's
 * {@link ItemStack#CODEC} accepts), so enchantments and trims are expressed with the vanilla
 * item-component format.
 *
 * <p>Being a {@link StructureEntityProcessor}, it is invoked by MSL's fabric
 * {@code EntityProcessorMixin} during entity placement.
 */
public class EquipArmorStandProcessor extends StructureEntityProcessor {

    /**
     * One full set of armor. Any slot may be omitted (left empty). Each slot is captured as the
     * raw item NBT — enchantments/trims/etc. are authored via the {@code components} field but
     * deserialised lazily by vanilla's armor-stand entity loader, not at processor decode time.
     *
     * <p>MC 26.1 made item-component validation eager: {@link ItemStack#CODEC} refuses to build
     * an {@link ItemStack} whose default components haven't been registered yet. The worldgen
     * {@code processor_list} registry loads before item components are populated, so invoking
     * the item codec during processor decode crashes registry load. Capturing the raw
     * (already-normalised) NBT here defers the item decode to entity-load time, where components
     * are available.
     */
    public record ArmorSet(Optional<CompoundTag> head, Optional<CompoundTag> chest, Optional<CompoundTag> legs, Optional<CompoundTag> feet) {
        // Wrap CompoundTag.CODEC (not ItemStack.CODEC) so the pre-1.21.5 wrapped enchantments
        // schema ({"minecraft:enchantments":{"levels":{...}}}) is still normalised to the flat
        // 1.21.5+ shape at decode time, but no eager item-component validation runs.
        private static final Codec<CompoundTag> ITEM_CODEC =
                EnchantmentsSchemaCompatCodec.wrap(CompoundTag.CODEC);

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
        if (nbt == null || !nbt.getString("id").map("minecraft:armor_stand"::equals).orElse(false)) {
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

        CompoundTag newNbt = nbt.copy();

        CompoundTag equipment = new CompoundTag();
        set.feet().ifPresent(tag -> equipment.put("feet", tag));
        set.legs().ifPresent(tag -> equipment.put("legs", tag));
        set.chest().ifPresent(tag -> equipment.put("chest", tag));
        set.head().ifPresent(tag -> equipment.put("head", tag));
        newNbt.put("equipment", equipment);

        return new StructureTemplate.StructureEntityInfo(globalEntityInfo.pos, globalEntityInfo.blockPos, newNbt);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return MoogsStructuresProcessors.EQUIP_ARMOR_STAND_PROCESSOR.get();
    }
}
