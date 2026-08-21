package com.finndog.moogs_structures.world.processors;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link StructureProcessor} that processes entities within a structure/jigsaw piece.
 * May or may not also process blocks normally (override {@code processBlock} for that).
 *
 * <p>This method's descriptor differs from the {@code processEntity} hook the loaders actually
 * call, so MSL wires it per loader: on Fabric, {@code EntityProcessorMixin} runs every processor
 * during {@code StructureTemplate.placeEntities} (which never fires a vanilla entity hook) and
 * spawns the results; on NeoForge, {@code StructureEntityProcessorMixin} overrides NeoForge's
 * native {@code processEntity} and forwards to this method. Ported/adapted from YUNG's API.
 */
public abstract class StructureEntityProcessor implements StructureProcessor {

    /**
     * Applies a processor to an entity in a structure component or jigsaw piece.
     *
     * @param serverLevelAccessor          the world view (use {@code registryAccess()} for item/registry ops)
     * @param structurePiecePos            global block pos of the current piece (usually a corner)
     * @param structurePieceBottomCenterPos global block pos of the bottom-center of the current piece
     * @param localEntityInfo              raw entity info; its {@code pos}/{@code blockPos} are LOCAL. DO NOT modify those fields.
     * @param globalEntityInfo             entity info with real-world {@code pos}/{@code blockPos} (good for a per-entity seed),
     *                                     reflecting updates from previously-run processors. DO NOT modify its {@code pos}/{@code blockPos}.
     * @param structurePlaceSettings       the structure's placement data
     * @return the processed entity info (returned object becomes the new globalEntityInfo for later processors);
     *         return {@code null} to discard the entity.
     */
    @Nullable
    public abstract StructureTemplate.StructureEntityInfo processEntity(ServerLevelAccessor serverLevelAccessor,
                                                                        BlockPos structurePiecePos,
                                                                        BlockPos structurePieceBottomCenterPos,
                                                                        StructureTemplate.StructureEntityInfo localEntityInfo,
                                                                        StructureTemplate.StructureEntityInfo globalEntityInfo,
                                                                        StructurePlaceSettings structurePlaceSettings);
}
