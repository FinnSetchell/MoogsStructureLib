package com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier;

import com.finndog.moogs_structures.mixins.terrainadaptation.BeardifierAccessor;
import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptation;
import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptationStructure;
import com.finndog.moogs_structures.world.structures.terrainadaptation.PoolElementAdaptationOverride;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.List;
import java.util.Optional;

/**
 * Static helpers used by the Beardifier mixin to apply {@link EnhancedTerrainAdaptation} to
 * structures implementing {@link EnhancedTerrainAdaptationStructure}.
 * Reduced port of YUNG's API EnhancedBeardifierHelper (aquifer override + per-element override removed).
 */
public class EnhancedBeardifierHelper {

    public static Beardifier forStructuresInChunk(StructureManager structureManager, ChunkPos chunkPos, Beardifier original) {
        ObjectList<EnhancedBeardifierRigid> enhancedBeardifierRigidList = new ObjectArrayList<>(10);
        ObjectList<EnhancedJigsawJunction> enhancedJunctionList = new ObjectArrayList<>(10);
        int chunkMinBlockX = chunkPos.getMinBlockX();
        int chunkMinBlockZ = chunkPos.getMinBlockZ();

        List<StructureStart> structureStarts = structureManager.startsForStructure(chunkPos,
                structure -> structure instanceof EnhancedTerrainAdaptationStructure);

        for (StructureStart structureStart : structureStarts) {
            EnhancedTerrainAdaptation structureAdaptation =
                    ((EnhancedTerrainAdaptationStructure) structureStart.getStructure()).getEnhancedTerrainAdaptation();

            // Scan all pieces to find the max kernel radius. Individual pieces may have overrides
            // with a larger radius than the structure default (including when the default is NONE).
            int kernelRadius = structureAdaptation.getKernelRadius();
            for (StructurePiece structurePiece : structureStart.getPieces()) {
                if (structurePiece instanceof PoolElementStructurePiece poolPiece
                        && poolPiece.getElement() instanceof PoolElementAdaptationOverride override
                        && override.moogs_structures_getAdaptationOverride().isPresent()) {
                    kernelRadius = Math.max(kernelRadius, override.moogs_structures_getAdaptationOverride().get().getKernelRadius());
                }
            }

            int maxKernelRadius = kernelRadius;
            if (maxKernelRadius <= 0) {
                continue;
            }

            // A piece is "nearby" if its bounding box, padded by the max kernel radius, intersects this chunk.
            List<StructurePiece> nearbyPieces = structureStart.getPieces().stream()
                    .filter(structurePiece -> structurePiece.isCloseToChunk(chunkPos, maxKernelRadius))
                    .toList();

            for (StructurePiece nearbyPiece : nearbyPieces) {
                if (nearbyPiece instanceof PoolElementStructurePiece poolElementPiece) {
                    StructureTemplatePool.Projection projection = poolElementPiece.getElement().getProjection();

                    // Check if the piece has a per-piece override; fall back to the structure default.
                    EnhancedTerrainAdaptation pieceAdaptation = structureAdaptation;
                    if (poolElementPiece.getElement() instanceof PoolElementAdaptationOverride override
                            && override.moogs_structures_getAdaptationOverride().isPresent()) {
                        pieceAdaptation = override.moogs_structures_getAdaptationOverride().get();
                    }

                    if (pieceAdaptation == EnhancedTerrainAdaptation.NONE) {
                        continue;
                    }

                    int pieceKernelRadius = pieceAdaptation.getKernelRadius();

                    if (projection == StructureTemplatePool.Projection.RIGID) {
                        enhancedBeardifierRigidList.add(new EnhancedBeardifierRigid(
                                poolElementPiece.getBoundingBox(),
                                pieceAdaptation,
                                poolElementPiece.getGroundLevelDelta(),
                                poolElementPiece.getRotation()));
                    }

                    for (JigsawJunction jigsawJunction : poolElementPiece.getJunctions()) {
                        int sourceX = jigsawJunction.getSourceX();
                        int sourceZ = jigsawJunction.getSourceZ();
                        if (sourceX > chunkMinBlockX - pieceKernelRadius
                                && sourceZ > chunkMinBlockZ - pieceKernelRadius
                                && sourceX < chunkMinBlockX + 15 + pieceKernelRadius
                                && sourceZ < chunkMinBlockZ + 15 + pieceKernelRadius) {
                            enhancedJunctionList.add(new EnhancedJigsawJunction(jigsawJunction, pieceAdaptation));
                        }
                    }
                } else if (structureAdaptation != EnhancedTerrainAdaptation.NONE) {
                    enhancedBeardifierRigidList.add(new EnhancedBeardifierRigid(
                            nearbyPiece.getBoundingBox(),
                            structureAdaptation,
                            0,
                            Rotation.NONE));
                }
            }
        }

        Beardifier newBeardifier = new Beardifier(
                ((BeardifierAccessor) original).getPieceIterator(),
                ((BeardifierAccessor) original).getJunctionIterator());
        EnhancedBeardifierData enhancedBeardifier = (EnhancedBeardifierData) newBeardifier;
        enhancedBeardifier.setEnhancedPieceIterator(enhancedBeardifierRigidList.iterator());
        enhancedBeardifier.setEnhancedJunctionIterator(enhancedJunctionList.iterator());
        return newBeardifier;
    }

    public static double computeDensity(DensityFunction.FunctionContext ctx, double density, EnhancedBeardifierData data) {
        int x = ctx.blockX();
        int y = ctx.blockY();
        int z = ctx.blockZ();

        while (data.getEnhancedPieceIterator() != null && data.getEnhancedPieceIterator().hasNext()) {
            EnhancedBeardifierRigid rigid = data.getEnhancedPieceIterator().next();
            BoundingBox originalBox = rigid.pieceBoundingBox();
            BoundingBox pieceBoundingBox = originalBox;
            EnhancedTerrainAdaptation adaptation = rigid.pieceTerrainAdaptation();
            Rotation pieceRotation = rigid.rotation();

            Optional<EnhancedTerrainAdaptation.Band> bandOpt = adaptation.getBand();
            // If a band targets specific piece heights, skip pieces whose Y-span isn't in the list.
            if (bandOpt.isPresent()
                    && bandOpt.get().pieceHeights().isPresent()
                    && !bandOpt.get().pieceHeights().get().contains(originalBox.getYSpan())) {
                continue;
            }

            // Apply bottom offset
            pieceBoundingBox = pieceBoundingBox.moved(0, (int) adaptation.getBottomOffset(), 0);

            // Apply x/z padding (rotation-aware)
            Direction.Axis xPaddingDirection = pieceRotation.rotate(Direction.EAST).getAxis();
            int xPadding = xPaddingDirection == Direction.Axis.X ? adaptation.getPadding().x() : adaptation.getPadding().z();
            int zPadding = xPaddingDirection == Direction.Axis.X ? adaptation.getPadding().z() : adaptation.getPadding().x();
            pieceBoundingBox = pieceBoundingBox.inflatedBy(xPadding, 0, zPadding);

            if (bandOpt.isPresent()) {
                // Clamp the adapted region to a vertical band in piece-local rows (0 = piece floor).
                // The band overrides any top/bottom padding. Kernel falloff still bleeds a few
                // blocks above/below for a natural blend.
                EnhancedTerrainAdaptation.Band band = bandOpt.get();
                int floor = originalBox.minY() + (int) adaptation.getBottomOffset();
                pieceBoundingBox = new BoundingBox(
                        pieceBoundingBox.minX(), floor + band.bottom(), pieceBoundingBox.minZ(),
                        pieceBoundingBox.maxX(), floor + band.top(), pieceBoundingBox.maxZ());
            } else {
                // Apply top/bottom padding
                if (adaptation.getPadding().top() != 0) {
                    pieceBoundingBox = new BoundingBox(
                            pieceBoundingBox.minX(), pieceBoundingBox.minY(), pieceBoundingBox.minZ(),
                            pieceBoundingBox.maxX(), pieceBoundingBox.maxY() + adaptation.getPadding().top(), pieceBoundingBox.maxZ());
                }
                if (adaptation.getPadding().bottom() != 0) {
                    pieceBoundingBox = new BoundingBox(
                            pieceBoundingBox.minX(), pieceBoundingBox.minY() - adaptation.getPadding().bottom(), pieceBoundingBox.minZ(),
                            pieceBoundingBox.maxX(), pieceBoundingBox.maxY(), pieceBoundingBox.maxZ());
                }
            }

            int xDistanceToBoundingBox = Math.max(0, Math.max(pieceBoundingBox.minX() - x, x - pieceBoundingBox.maxX()));
            int yDistanceToBoundingBox = Math.max(0, Math.max(pieceBoundingBox.minY() - y, y - pieceBoundingBox.maxY()));
            int zDistanceToBoundingBox = Math.max(0, Math.max(pieceBoundingBox.minZ() - z, z - pieceBoundingBox.maxZ()));
            int yDistanceToPieceBottom = y - pieceBoundingBox.minY();

            double densityFactor = adaptation.computeDensityFactor(
                    xDistanceToBoundingBox, yDistanceToBoundingBox, zDistanceToBoundingBox, yDistanceToPieceBottom) * 0.8D;
            density += densityFactor;
        }
        data.getEnhancedPieceIterator().back(Integer.MAX_VALUE);

        while (data.getEnhancedJunctionIterator() != null && data.getEnhancedJunctionIterator().hasNext()) {
            EnhancedJigsawJunction enhancedJigsawJunction = data.getEnhancedJunctionIterator().next();
            JigsawJunction jigsawJunction = enhancedJigsawJunction.jigsawJunction();
            EnhancedTerrainAdaptation adaptation = enhancedJigsawJunction.pieceTerrainAdaptation();

            // Band-limited adaptation is piece-local; junction beards sit at connection ground level
            // and would carve outside the band, so skip them when a band is configured.
            if (adaptation.getBand().isPresent()) {
                continue;
            }

            int groundY = jigsawJunction.getSourceGroundY() + (int) adaptation.getBottomOffset();
            int xDistanceToJunction = x - jigsawJunction.getSourceX();
            int yDistanceToJunction = y - groundY;
            int zDistanceToJunction = z - jigsawJunction.getSourceZ();
            double densityFactor = adaptation.computeDensityFactor(
                    xDistanceToJunction, yDistanceToJunction, zDistanceToJunction, yDistanceToJunction) * 0.4D;
            density += densityFactor;
        }
        data.getEnhancedJunctionIterator().back(Integer.MAX_VALUE);

        return density;
    }
}
