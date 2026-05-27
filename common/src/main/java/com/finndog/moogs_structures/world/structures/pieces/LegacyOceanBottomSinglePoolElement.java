package com.finndog.moogs_structures.world.structures.pieces;

import com.finndog.moogs_structures.modinit.MoogsStructuresStructurePieces;
import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptation;
import com.finndog.moogs_structures.world.structures.terrainadaptation.PoolElementAdaptationOverride;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

public class LegacyOceanBottomSinglePoolElement extends SinglePoolElement implements PoolElementAdaptationOverride {
    public static final MapCodec<LegacyOceanBottomSinglePoolElement> CODEC = RecordCodecBuilder.mapCodec(
            (legacyOceanBottomSinglePoolElementInstance) -> legacyOceanBottomSinglePoolElementInstance
                    .group(templateCodec(),
                            processorsCodec(),
                            projectionCodec(),
                            overrideLiquidSettingsCodec(),
                            EnhancedTerrainAdaptation.CODEC.optionalFieldOf("enhanced_terrain_adaptation")
                                    .forGetter(LegacyOceanBottomSinglePoolElement::moogs_structures_getAdaptationOverride))
                    .apply(legacyOceanBottomSinglePoolElementInstance, LegacyOceanBottomSinglePoolElement::new));

    protected final Optional<EnhancedTerrainAdaptation> adaptationOverride;

    protected LegacyOceanBottomSinglePoolElement(Either<ResourceLocation, StructureTemplate> resourceLocationStructureTemplateEither, Holder<StructureProcessorList> structureProcessorListHolder, StructureTemplatePool.Projection projection, Optional<LiquidSettings> liquidSettings, Optional<EnhancedTerrainAdaptation> adaptationOverride) {
        super(resourceLocationStructureTemplateEither, structureProcessorListHolder, projection, liquidSettings);
        this.adaptationOverride = adaptationOverride;
    }

    @Override
    public Optional<EnhancedTerrainAdaptation> moogs_structures_getAdaptationOverride() {
        return this.adaptationOverride;
    }

    @Override
    protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox mutableBoundingBox, LiquidSettings liquidSettings, boolean doNotReplaceJigsaw) {
        StructurePlaceSettings structureplacesettings = super.getSettings(rotation, mutableBoundingBox, liquidSettings, doNotReplaceJigsaw);
        structureplacesettings.popProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        structureplacesettings.addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        return structureplacesettings;
    }

    public StructurePoolElementType<?> getType() {
        return MoogsStructuresStructurePieces.LEGACY_OCEAN_BOTTOM.get();
    }

    public String toString() {
        return "LegacyOceanBottomSingle[" + this.template + "]";
    }
}