package com.finndog.moogs_structures.world.structures.placements;

import com.finndog.moogs_structures.config.MslConfig;
import com.finndog.moogs_structures.config.ReplaceVanillaManager;
import com.finndog.moogs_structures.modinit.MoogsStructuresStructurePlacementType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Optional;

/**
 * Concentric-rings placement whose ring count depends on whether a replacement preset is enabled:
 * enabled_count when the preset is on (e.g. 1:1 with the vanilla structure it replaces),
 * disabled_count when off (a reduced coexisting density). Extends the vanilla type so Minecraft's
 * special ring handling still applies; the count is read once when ring positions are generated
 * at world load.
 */
public class ConditionalConcentricRings extends ConcentricRingsStructurePlacement {
    public static final Codec<ConditionalConcentricRings> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", Vec3i.ZERO).forGetter(ConditionalConcentricRings::locateOffset),
            FrequencyReductionMethod.CODEC.optionalFieldOf("frequency_reduction_method", FrequencyReductionMethod.DEFAULT).forGetter(ConditionalConcentricRings::frequencyReductionMethod),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("frequency", 1.0F).forGetter(ConditionalConcentricRings::frequency),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(ConditionalConcentricRings::salt),
            ExclusionZone.CODEC.optionalFieldOf("exclusion_zone").forGetter(ConditionalConcentricRings::exclusionZone),
            Codec.intRange(0, 1023).fieldOf("distance").forGetter(ConditionalConcentricRings::distance),
            Codec.intRange(0, 1023).fieldOf("spread").forGetter(ConditionalConcentricRings::spread),
            RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("preferred_biomes").forGetter(ConditionalConcentricRings::preferredBiomes),
            Codec.STRING.fieldOf("modid").forGetter(p -> p.modid),
            Codec.STRING.fieldOf("vanilla_key").forGetter(p -> p.vanillaKey),
            Codec.intRange(1, 4095).fieldOf("enabled_count").forGetter(p -> p.enabledCount),
            Codec.intRange(0, 4095).fieldOf("disabled_count").forGetter(p -> p.disabledCount),
            Codec.STRING.optionalFieldOf("structure_id").forGetter(p -> p.structureId)
    ).apply(instance, instance.stable(ConditionalConcentricRings::new)));

    private final String modid;
    private final String vanillaKey;
    private final int enabledCount;
    private final int disabledCount;
    private final Optional<String> structureId;
    private final ResourceLocation structureIdRL;

    public ConditionalConcentricRings(Vec3i locateOffset, FrequencyReductionMethod frequencyReductionMethod, float frequency,
                                      int salt, Optional<ExclusionZone> exclusionZone, int distance, int spread,
                                      HolderSet<Biome> preferredBiomes, String modid, String vanillaKey,
                                      int enabledCount, int disabledCount, Optional<String> structureId) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone, distance, spread, enabledCount, preferredBiomes);
        this.modid = modid;
        this.vanillaKey = vanillaKey;
        this.enabledCount = enabledCount;
        this.disabledCount = disabledCount;
        this.structureId = structureId;
        this.structureIdRL = structureId.map(ResourceLocation::tryParse).orElse(null);
    }

    @Override
    public int count() {
        // 0 rings when the structure is disabled - returns an empty ring list (never skips
        // registration), so /locate and eye-of-ender searches stay safe.
        if (structureIdRL != null && MslConfig.get().isStructureDisabled(structureIdRL)) {
            return 0;
        }
        return ReplaceVanillaManager.isEnabled(modid, vanillaKey) ? enabledCount : disabledCount;
    }

    @Override
    public StructurePlacementType<?> type() {
        return MoogsStructuresStructurePlacementType.CONDITIONAL_CONCENTRIC_RINGS.get();
    }
}
