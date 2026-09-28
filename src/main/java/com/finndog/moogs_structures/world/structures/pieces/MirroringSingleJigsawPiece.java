package com.finndog.moogs_structures.world.structures.pieces;

import com.finndog.moogs_structures.mixins.structures.SinglePoolElementAccessor;
//? if >=1.21.2 <1.21.5 {
/*import com.finndog.moogs_structures.mixins.structures.TemplateAccessor;
*///?}
import com.finndog.moogs_structures.modinit.MoogsStructuresStructurePieces;
import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptation;
import com.finndog.moogs_structures.world.structures.terrainadaptation.PoolElementAdaptationOverride;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
//? if >=1.20.6 {
import com.mojang.serialization.MapCodec;
//?} else {
/*import com.mojang.serialization.Codec;
*///?}
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
//? if <1.21.11 {
import net.minecraft.Util;
//?} else {
/*import net.minecraft.util.Util;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
//? if <1.21.2 {
import net.minecraft.world.level.block.Blocks;
//?}
//? if >=1.21.5 {
/*import net.minecraft.world.level.block.Blocks;
*///?}
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
//? if >=1.21.1 {
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
//?} else {
/*import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
*///?}

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import com.finndog.moogs_structures.world.processors.HangingEntityAnchorProcessor;
//? if >=1.21.1 {
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
//?}

public class MirroringSingleJigsawPiece extends SinglePoolElement implements PoolElementAdaptationOverride {
    //? if >=1.20.6 {
    public static final MapCodec<MirroringSingleJigsawPiece> CODEC = RecordCodecBuilder.mapCodec((jigsawPieceInstance) ->
    //?} else {
    /*public static final Codec<MirroringSingleJigsawPiece> CODEC = RecordCodecBuilder.create((jigsawPieceInstance) ->
    *///?}
            jigsawPieceInstance.group(
                            //? if >=1.21.1 {
                            templateCodec(),
                            processorsCodec(),
                            projectionCodec(),
                            mirrorCodec(),
                            overrideLiquidSettingsCodec(),
                            adaptationOverrideCodec())
                    .apply(jigsawPieceInstance, MirroringSingleJigsawPiece::new));
                            //?} else {
                    /*templateCodec(),
                    processorsCodec(),
                    projectionCodec(),
                    mirrorCodec(),
                    adaptationOverrideCodec())
            .apply(jigsawPieceInstance, MirroringSingleJigsawPiece::new));
                            *///?}

    protected static <E extends MirroringSingleJigsawPiece> RecordCodecBuilder<E, Mirror> mirrorCodec() {
        return Codec.STRING.fieldOf("mirror")
                //? if >=1.21.1 {
                .xmap(Mirror::valueOf, Mirror::toString)
                //?} else {
                /*.xmap(Mirror::valueOf, Mirror::name)
                *///?}
                .forGetter((jigsawPieceInstance) -> jigsawPieceInstance.mirror);
    }

    protected static <E extends MirroringSingleJigsawPiece> RecordCodecBuilder<E, Optional<EnhancedTerrainAdaptation>> adaptationOverrideCodec() {
        return EnhancedTerrainAdaptation.CODEC.optionalFieldOf("enhanced_terrain_adaptation")
                .forGetter(MirroringSingleJigsawPiece::moogs_structures_getAdaptationOverride);
    }

    protected final Mirror mirror;
    protected final Optional<EnhancedTerrainAdaptation> adaptationOverride;

    //? if >=1.21.1 {
    public MirroringSingleJigsawPiece(SinglePoolElement singleJigsawPiece, Mirror mirror, Optional<LiquidSettings> liquidSettings) {
        this(((SinglePoolElementAccessor)singleJigsawPiece).moogs_structures_getTemplate(), ((SinglePoolElementAccessor)singleJigsawPiece).moogs_structures_getProcessors(), singleJigsawPiece.getProjection(), mirror, liquidSettings, Optional.empty());
    //?} else {
    /*public MirroringSingleJigsawPiece(SinglePoolElement singleJigsawPiece, Mirror mirror) {
        this(((SinglePoolElementAccessor)singleJigsawPiece).moogs_structures_getTemplate(), ((SinglePoolElementAccessor)singleJigsawPiece).moogs_structures_getProcessors(), singleJigsawPiece.getProjection(), mirror, Optional.empty());
    *///?}
    }

    //? if >=1.21.1 {
    protected MirroringSingleJigsawPiece(Either<ResourceLocation, StructureTemplate> locationTemplateEither, Holder<StructureProcessorList> processorListSupplier, StructureTemplatePool.Projection placementBehaviour, Mirror mirror, Optional<LiquidSettings> liquidSettings, Optional<EnhancedTerrainAdaptation> adaptationOverride) {
        super(locationTemplateEither, processorListSupplier, placementBehaviour, liquidSettings);
    //?} else {
    /*protected MirroringSingleJigsawPiece(Either<ResourceLocation, StructureTemplate> locationTemplateEither, Holder<StructureProcessorList> processorListSupplier, StructureTemplatePool.Projection placementBehaviour, Mirror mirror, Optional<EnhancedTerrainAdaptation> adaptationOverride) {
        super(locationTemplateEither, processorListSupplier, placementBehaviour);
    *///?}
        this.mirror = mirror;
        this.adaptationOverride = adaptationOverride;
    }

    @Override
    public Optional<EnhancedTerrainAdaptation> moogs_structures_getAdaptationOverride() {
        return this.adaptationOverride;
    }

    private StructureTemplate getTemplate(StructureTemplateManager templateManager) {
        return this.template.map(templateManager::getOrCreate, Function.identity());
    }

    @Override
    //? if <1.21.2 {
    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager templateManager, BlockPos blockPos, Rotation rotation, RandomSource random) {
    //?}
    //? if >=1.21.2 <1.21.5 {
    /*public List<StructureTemplate.JigsawBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager templateManager, BlockPos blockPos, Rotation rotation, RandomSource random) {
    *///?}
    //? if >=1.21.5 {
    /*public List<StructureTemplate.JigsawBlockInfo> getShuffledJigsawBlocks(
            StructureTemplateManager templateManager,
            BlockPos blockPos,
            Rotation rotation,
            RandomSource random
    ) {
    *///?}
        StructureTemplate template = this.getTemplate(templateManager);
        //? if <1.21.2 {
        ObjectArrayList<StructureTemplate.StructureBlockInfo> list = template.filterBlocks(blockPos, (new StructurePlaceSettings()).setRotation(rotation).setMirror(mirror), Blocks.JIGSAW, true);
        //?}
        //? if >=1.21.2 <1.21.5 {
        /*ObjectArrayList<StructureTemplate.JigsawBlockInfo> list = getJigsaws(template, blockPos, (new StructurePlaceSettings()).setRotation(rotation).setMirror(mirror));
        *///?}
        //? if >=1.20.6 <1.21.5 {
        Util.shuffle(list, random);
        //?}
        //? if <1.20.6 {
        /*shuffle(list, random);
        *///?}

        //? if <1.21.5 {
        return list;
        //?} else {
        /*ObjectArrayList<StructureTemplate.StructureBlockInfo> raw =
                template.filterBlocks(
                        blockPos,
                        new StructurePlaceSettings().setRotation(rotation).setMirror(mirror),
                        Blocks.JIGSAW,
                        true
                );

        ObjectArrayList<StructureTemplate.JigsawBlockInfo> out = new ObjectArrayList<>(raw.size());
        for (StructureTemplate.StructureBlockInfo info : raw) {
        *///?}
            //? if >=1.21.5 <26.3 {
            /*out.add(StructureTemplate.JigsawBlockInfo.of(info));
            *///?}
            //? if >=26.3 {
            /*out.add(StructureTemplate.JigsawBlockInfo.parse(info));
            *///?}
        //? if >=1.21.5 {
        /*}

        Util.shuffle(out, random);
        return out;
        *///?}
    //? if <1.20.6 {
    /*}

    // Vanilla's Util.shuffle, inlined: it takes an ObjectArrayList up to 1.20.2 and a List from 1.20.3,
    // so a call compiled against 1.20 throws NoSuchMethodError on 1.20.3-1.20.4. Same draws, same order.
    private static <T> void shuffle(List<T> list, RandomSource random) {
        for (int i = list.size(); i > 1; --i) {
            int j = random.nextInt(i);
            list.set(i - 1, list.set(j, list.get(i - 1)));
        }
    *///?}
    }

    //? if >=1.21.2 <1.21.5 {
    /*private ObjectArrayList<StructureTemplate.JigsawBlockInfo> getJigsaws(StructureTemplate template, BlockPos blockPos, StructurePlaceSettings structurePlaceSettings) {
        if (((TemplateAccessor)template).moogs_structures_getPalettes().isEmpty()) {
            return new ObjectArrayList<>();
        }
        else {
            List<StructureTemplate.JigsawBlockInfo> list = structurePlaceSettings.getRandomPalette(((TemplateAccessor)template).moogs_structures_getPalettes(), blockPos).jigsaws();
            ObjectArrayList<StructureTemplate.JigsawBlockInfo> list2 = new ObjectArrayList<>(list.size());

            for (StructureTemplate.JigsawBlockInfo jigsawBlockInfo : list) {
                StructureTemplate.StructureBlockInfo structureBlockInfo = jigsawBlockInfo.info();
                list2.add(jigsawBlockInfo.withInfo(new StructureTemplate.StructureBlockInfo(StructureTemplate.calculateRelativePosition(structurePlaceSettings, structureBlockInfo.pos()).offset(blockPos), structureBlockInfo.state().rotate(structurePlaceSettings.getRotation()), structureBlockInfo.nbt())));
            }

            return list2;
        }
    }

    *///?}

    @Override
    public BoundingBox getBoundingBox(StructureTemplateManager templateManager, BlockPos blockPos, Rotation rotation) {
        StructureTemplate template = this.getTemplate(templateManager);
        return template.getBoundingBox((new StructurePlaceSettings()).setRotation(rotation).setMirror(this.mirror), blockPos);
    }

    @Override
    //? if >=1.21.1 {
    public boolean place(StructureTemplateManager templateManager,
                         WorldGenLevel worldGenLevel,
                         StructureManager StructureTemplateManager,
                         ChunkGenerator chunkGenerator,
                         BlockPos blockPos,
                         BlockPos blockPos1,
                         Rotation rotation,
                         BoundingBox mutableBoundingBox,
                         RandomSource random,
                         LiquidSettings liquidSettings,
                         boolean doNotReplaceJigsaw)
    {
    //?} else {
    /*public boolean place(StructureTemplateManager templateManager, WorldGenLevel worldGenLevel, StructureManager StructureTemplateManager, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockPos blockPos1, Rotation rotation, BoundingBox mutableBoundingBox, RandomSource random, boolean doNotReplaceJigsaw) {
    *///?}
        StructureTemplate template = this.getTemplate(templateManager);
        //? if >=1.21.1 {
        StructurePlaceSettings placementsettings = this.getSettings(rotation, mutableBoundingBox, liquidSettings, doNotReplaceJigsaw);
        //?} else {
        /*StructurePlaceSettings placementsettings = this.getSettings(rotation, mutableBoundingBox, doNotReplaceJigsaw);
        *///?}
        if (!template.placeInWorld(worldGenLevel, blockPos, blockPos1, placementsettings, random, 18)) {
            return false;
        } else {
            for(StructureTemplate.StructureBlockInfo template$blockinfo : StructureTemplate.processBlockInfos(worldGenLevel, blockPos, blockPos1, placementsettings, this.getDataMarkers(templateManager, blockPos, rotation, false))) {
                this.handleDataMarker(worldGenLevel, template$blockinfo, blockPos, rotation, random, mutableBoundingBox);
            }

            return true;
        }
    }

    @Override
    //? if >=1.21.1 {
    protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox mutableBoundingBox, LiquidSettings liquidSettings, boolean doNotReplaceJigsaw) {
    //?} else {
    /*protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox mutableBoundingBox, boolean doNotReplaceJigsaw) {
    *///?}
        StructurePlaceSettings placementsettings = new StructurePlaceSettings();
        placementsettings.setBoundingBox(mutableBoundingBox);
        placementsettings.setRotation(rotation);
        placementsettings.setMirror(mirror);
        placementsettings.setIgnoreEntities(false);
        placementsettings.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        placementsettings.setFinalizeEntities(true);
        //? if >=1.21.1 {
        placementsettings.setLiquidSettings(this.overrideLiquidSettings.orElse(liquidSettings));
        //?}
        if (!doNotReplaceJigsaw) {
            placementsettings.addProcessor(JigsawReplacementProcessor.INSTANCE);
        }

        this.processors.value().list().forEach(placementsettings::addProcessor);
        this.getProjection().getProcessors().forEach(placementsettings::addProcessor);
        placementsettings.addProcessor(HangingEntityAnchorProcessor.INSTANCE);
        return placementsettings;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return MoogsStructuresStructurePieces.MIRROR_SINGLE.get();
    }

    @Override
    public String toString() {
        return "Mirror_Single[" + this.template + "]";
    }
}
