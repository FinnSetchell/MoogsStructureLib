package com.finndog.moogs_structures.world.processors;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;

/**
 * Captures the contextual data needed to run {@link StructureEntityProcessor}s during
 * {@code StructureTemplate.placeEntities}. Used by the fabric {@code EntityProcessorMixin}.
 * Ported/adapted from YUNG's API.
 */
public record StructureProcessingContext(
        ServerLevelAccessor serverLevelAccessor,
        StructurePlaceSettings structurePlaceSettings,
        BlockPos structurePiecePos,
        BlockPos structurePiecePivotPos,
        List<StructureTemplate.StructureEntityInfo> rawEntityInfos
) {}
