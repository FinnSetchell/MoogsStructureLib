package com.finndog.moogs_structures.world.structures.pieces;

import com.finndog.moogs_structures.modinit.MoogsStructuresStructurePieces;
import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptation;
import com.finndog.moogs_structures.world.structures.terrainadaptation.PoolElementAdaptationOverride;
import com.mojang.datafixers.util.Either;
//? if >=1.20.6 {
import com.mojang.serialization.MapCodec;
//?} else {
/*import com.mojang.serialization.Codec;
*///?}
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
//? if >=1.21.1 {
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
//?}
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;
import com.finndog.moogs_structures.world.processors.HangingEntityAnchorProcessor;

public class LegacyOceanBottomSinglePoolElement extends SinglePoolElement implements PoolElementAdaptationOverride {
    //? if >=1.20.6 {
    public static final MapCodec<LegacyOceanBottomSinglePoolElement> CODEC = RecordCodecBuilder.mapCodec(
    //?} else {
    /*public static final Codec<LegacyOceanBottomSinglePoolElement> CODEC = RecordCodecBuilder.create(
    *///?}
            (legacyOceanBottomSinglePoolElementInstance) -> legacyOceanBottomSinglePoolElementInstance
                    .group(templateCodec(),
                            processorsCodec(),
                            projectionCodec(),
                            //? if >=1.21.1 {
                            overrideLiquidSettingsCodec(),
                            //?}
                            EnhancedTerrainAdaptation.CODEC.optionalFieldOf("enhanced_terrain_adaptation")
                                    .forGetter(LegacyOceanBottomSinglePoolElement::moogs_structures_getAdaptationOverride))
                    .apply(legacyOceanBottomSinglePoolElementInstance, LegacyOceanBottomSinglePoolElement::new));

    protected final Optional<EnhancedTerrainAdaptation> adaptationOverride;

    //? if >=1.21.1 {
    protected LegacyOceanBottomSinglePoolElement(Either<ResourceLocation, StructureTemplate> resourceLocationStructureTemplateEither, Holder<StructureProcessorList> structureProcessorListHolder, StructureTemplatePool.Projection projection, Optional<LiquidSettings> liquidSettings, Optional<EnhancedTerrainAdaptation> adaptationOverride) {
        super(resourceLocationStructureTemplateEither, structureProcessorListHolder, projection, liquidSettings);
    //?} else {
    /*protected LegacyOceanBottomSinglePoolElement(Either<ResourceLocation, StructureTemplate> p_210348_, Holder<StructureProcessorList> p_210349_, StructureTemplatePool.Projection p_210350_, Optional<EnhancedTerrainAdaptation> adaptationOverride) {
        super(p_210348_, p_210349_, p_210350_);
    *///?}
        this.adaptationOverride = adaptationOverride;
    }

    @Override
    public Optional<EnhancedTerrainAdaptation> moogs_structures_getAdaptationOverride() {
        return this.adaptationOverride;
    }

    //? if >=1.21.1 {
    @Override
    protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox mutableBoundingBox, LiquidSettings liquidSettings, boolean doNotReplaceJigsaw) {
        StructurePlaceSettings structureplacesettings = super.getSettings(rotation, mutableBoundingBox, liquidSettings, doNotReplaceJigsaw);
    //?} else {
    /*protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox boundingBox, boolean replaceJigsaw) {
        StructurePlaceSettings structureplacesettings = super.getSettings(rotation, boundingBox, replaceJigsaw);
    *///?}
        structureplacesettings.popProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        structureplacesettings.addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        structureplacesettings.addProcessor(HangingEntityAnchorProcessor.INSTANCE);
        return structureplacesettings;
    }

    public StructurePoolElementType<?> getType() {
        return MoogsStructuresStructurePieces.LEGACY_OCEAN_BOTTOM.get();
    }

    public String toString() {
        return "LegacyOceanBottomSingle[" + this.template + "]";
    }
}