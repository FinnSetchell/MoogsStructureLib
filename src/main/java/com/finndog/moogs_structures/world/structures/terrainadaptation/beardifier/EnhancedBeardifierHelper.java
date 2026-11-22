package com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier;

//? if >=1.21.10 {
/*import com.finndog.moogs_structures.mixins.terrainadaptation.BeardifierAccessor;
*///?}
//? if <1.20.6 {
/*import com.finndog.moogs_structures.world.structures.GenericJigsawStructure;
import com.finndog.moogs_structures.world.structures.terrainadaptation.EncapsulateBackport;
*///?}
import com.finndog.moogs_structures.mixins.terrainadaptation.StructureManagerAccessor;
import com.finndog.moogs_structures.world.structures.terrainadaptation.AdaptationArea;
import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptation;
import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptationStructure;
import com.finndog.moogs_structures.world.structures.terrainadaptation.PoolElementAdaptationOverride;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
//? if <26.1.2 {
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
//?}
//? if >=1.21.10 {
/*import net.minecraft.core.BlockPos;
*///?}
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Beardifier;
//? if <26.3 {
import net.minecraft.world.level.levelgen.DensityFunction;
//?}
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
 * Reduced port of YUNG's API EnhancedBeardifierHelper (aquifer override removed; per-element overrides kept).
 */
public class EnhancedBeardifierHelper {

    public static Beardifier forStructuresInChunk(StructureManager structureManager, ChunkPos chunkPos, Beardifier original) {
        ObjectList<EnhancedBeardifierRigid> enhancedBeardifierRigidList = new ObjectArrayList<>(10);
        ObjectList<EnhancedJigsawJunction> enhancedJunctionList = new ObjectArrayList<>(10);
        // The bedrock roof only forms on netherrack, so carving the roof rows would leave a hole to the void.
        LevelAccessor level = ((StructureManagerAccessor) structureManager).moogs_structures_getLevel();
        int roofFloorY = Integer.MAX_VALUE;
        if (level.dimensionType().hasCeiling()) {
            //? if <1.21.2 {
            roofFloorY = level.getMinBuildHeight() + level.dimensionType().logicalHeight() - 5;
            //?} else {
            /*roofFloorY = level.getMinY() + level.dimensionType().logicalHeight() - 5;
            *///?}
        }
        int chunkMinBlockX = chunkPos.getMinBlockX();
        int chunkMinBlockZ = chunkPos.getMinBlockZ();

        //? if <26.3 {
        List<StructureStart> structureStarts = structureManager.startsForStructure(chunkPos,
        //?} else {
        /*List<StructureStart> structureStarts = structureManager.startsForStructure(chunkPos.x(), chunkPos.z(),
        *///?}
                structure -> structure instanceof EnhancedTerrainAdaptationStructure);

        //? if <1.20.6 {
        /*ObjectList<BoundingBox> encapsulatedPieces = new ObjectArrayList<>(10);
        ObjectList<JigsawJunction> encapsulatedJunctions = new ObjectArrayList<>(32);
        *///?}

        for (StructureStart structureStart : structureStarts) {
            //? if <1.20.6 {
            /*if (structureStart.getStructure() instanceof GenericJigsawStructure structure && structure.encapsulateBackport) {
                EncapsulateBackport.collect(structureStart, chunkPos, encapsulatedPieces, encapsulatedJunctions);
            }
            *///?}
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
                    PoolElementAdaptationOverride override = poolElementPiece.getElement() instanceof PoolElementAdaptationOverride o ? o : null;
                    if (override != null && override.moogs_structures_getAdaptationOverride().isPresent()) {
                        pieceAdaptation = override.moogs_structures_getAdaptationOverride().get();
                    }

                    if (pieceAdaptation == EnhancedTerrainAdaptation.NONE) {
                        continue;
                    }

                    int pieceKernelRadius = pieceAdaptation.getKernelRadius();

                    if (projection == StructureTemplatePool.Projection.RIGID) {
                        // An adaptation area narrows the beard to its columns; without one it covers the box.
                        List<BoundingBox> areas = null;
                        if (override != null) {
                            Optional<AdaptationArea> area = override.moogs_structures_getAdaptationArea();
                            if (area.isPresent()) {
                                areas = area.get().worldColumns(poolElementPiece);
                            }
                        }
                        enhancedBeardifierRigidList.add(new EnhancedBeardifierRigid(
                                poolElementPiece.getBoundingBox(),
                                pieceAdaptation,
                                poolElementPiece.getGroundLevelDelta(),
                                poolElementPiece.getRotation(),
                                areas));
                        // A rigid piece is covered by its own bounding-box kernel. Its junctions carry the start
                        // piece's ground level all the way down a rigid chain (the jigsaw maths keep minY plus
                        // groundLevelDelta constant), so bearding them raised a lump of terrain at surface height
                        // over every connection of a deep tunnel. Junction beards are for terrain-matching pieces.
                        continue;
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
                            Rotation.NONE,
                            null));
                }
            }
        }

        //? if <1.21.10 {
        // Attach to the instance we were handed rather than replacing it, or whatever another mod
        // stored on it would be lost.
        EnhancedBeardifierData enhancedBeardifier = (EnhancedBeardifierData) original;
        enhancedBeardifier.moogs_structures_setEnhancedPieceIterator(enhancedBeardifierRigidList.iterator());
        enhancedBeardifier.moogs_structures_setEnhancedJunctionIterator(enhancedJunctionList.iterator());
        enhancedBeardifier.moogs_structures_setRoofFloorY(roofFloorY);
        //?}
        //? if <1.20.6 {
        /*enhancedBeardifier.moogs_structures_setEncapsulated(encapsulatedPieces, encapsulatedJunctions);
        *///?}
        //? if <1.21.10 {
        return original;
        //?}
        // Vanilla returns its shared static Beardifier.EMPTY whenever no *vanilla* terrain-adapting
        // structure start touches this chunk. That is the normal case for our structures, which adapt
        // terrain through enhanced adaptation rather than vanilla's terrainAdaptation(). Writing this
        // chunk's data onto EMPTY would publish it to a global singleton that every concurrently
        // generating chunk shares across every world-gen worker thread, so each chunk would read
        // whichever chunk wrote last. Give ourselves a private instance instead.
        //? if >=1.21.10 {
        /*Beardifier target = original;
        if (target == Beardifier.EMPTY) {
            if (enhancedBeardifierRigidList.isEmpty() && enhancedJunctionList.isEmpty()) {
                // Nothing enhanced here either; leave vanilla's empty fast path untouched.
                return original;
            }
            target = new Beardifier(List.of(), List.of(), null);
        }

        // compute() and fillArray() return 0 outside affectedBox, so it has to cover the enhanced pieces.
        BeardifierAccessor accessor = (BeardifierAccessor) target;
        accessor.setAffectedBox(computeEnhancedAffectedBox(enhancedBeardifierRigidList, enhancedJunctionList, accessor.getAffectedBox()));
        EnhancedBeardifierData enhancedBeardifier = (EnhancedBeardifierData) target;
        *///?}
        //? if >=1.21.10 <26.1.2 {
        /*enhancedBeardifier.moogs_structures_setEnhancedPieceIterator(enhancedBeardifierRigidList.iterator());
        enhancedBeardifier.moogs_structures_setEnhancedJunctionIterator(enhancedJunctionList.iterator());
        *///?}
        // Store the lists themselves, never iterators: compute() is called from world-gen worker
        // threads and re-entrantly per noise cell, so a shared cursor gets advanced out from under
        // a running loop (hasNext() passes, next() throws NoSuchElementException). computeDensity()
        // takes a fresh local cursor per call instead.
        //? if >=26.1.2 {
        /*enhancedBeardifier.moogs_structures_setEnhancedPieces(enhancedBeardifierRigidList);
        enhancedBeardifier.moogs_structures_setEnhancedJunctions(enhancedJunctionList);
        *///?}
        //? if >=1.21.10 {
        /*enhancedBeardifier.moogs_structures_setRoofFloorY(roofFloorY);
        return target;
        *///?}
    //? if >=1.21.10 {
    /*}

    private static BoundingBox computeEnhancedAffectedBox(ObjectList<EnhancedBeardifierRigid> rigids,
                                                          ObjectList<EnhancedJigsawJunction> junctions,
                                                          BoundingBox originalBox) {
        BoundingBox box = originalBox;
        for (EnhancedBeardifierRigid rigid : rigids) {
            int radius = Math.max(1, rigid.pieceTerrainAdaptation().getKernelRadius());
            BoundingBox pieceBox = rigid.pieceBoundingBox().inflatedBy(radius);
    *///?}
            //? if >=1.21.10 <1.21.11 {
            /*box = box == null ? pieceBox : box.encapsulate(pieceBox);
            *///?}
            //? if >=1.21.11 {
            /*box = box == null ? pieceBox : BoundingBox.encapsulating(box, pieceBox);
            *///?}
        //? if >=1.21.10 {
        /*}
        for (EnhancedJigsawJunction junction : junctions) {
            JigsawJunction jigsawJunction = junction.jigsawJunction();
            int radius = Math.max(1, junction.pieceTerrainAdaptation().getKernelRadius());
            BoundingBox junctionBox = new BoundingBox(new BlockPos(
                    jigsawJunction.getSourceX(),
                    jigsawJunction.getSourceGroundY(),
                    jigsawJunction.getSourceZ())).inflatedBy(radius);
        *///?}
            //? if >=1.21.10 <1.21.11 {
            /*box = box == null ? junctionBox : box.encapsulate(junctionBox);
            *///?}
            //? if >=1.21.11 {
            /*box = box == null ? junctionBox : BoundingBox.encapsulating(box, junctionBox);
            *///?}
        //? if >=1.21.10 {
        /*}
        return box;
        *///?}
    }

    //? if <26.3 {
    public static double computeDensity(DensityFunction.FunctionContext ctx, double density, EnhancedBeardifierData data) {
        int x = ctx.blockX();
        int y = ctx.blockY();
        int z = ctx.blockZ();
    //?} else {
    /*public static double computeDensity(int x, int y, int z, double density, EnhancedBeardifierData data) {
    *///?}
        // From 26.1.2 the lists are stored and walked with local cursors. Sampling runs on world-gen worker
        // threads, so anything cursor-shaped kept on the Beardifier would be shared mutable state.
        // MSL's own contributions are summed apart from vanilla's, so the roof rows can be kept from carving.
        double added = 0;
        //? if <1.20.6 {
        /*ObjectList<BoundingBox> encapsulatedPieces = data.moogs_structures_getEncapsulatedPieces();
        ObjectList<JigsawJunction> encapsulatedJunctions = data.moogs_structures_getEncapsulatedJunctions();
        if (encapsulatedPieces != null && encapsulatedJunctions != null) {
            added += EncapsulateBackport.density(x, y, z, encapsulatedPieces, encapsulatedJunctions);
        }
        *///?}
        //? if <26.1.2 {
        ObjectListIterator<EnhancedBeardifierRigid> pieces = data.moogs_structures_getEnhancedPieceIterator();
        ObjectListIterator<EnhancedJigsawJunction> junctions = data.moogs_structures_getEnhancedJunctionIterator();
        if (pieces == null || junctions == null) {
            return density + added;
        }
        while (pieces.hasNext()) {
            EnhancedBeardifierRigid rigid = pieces.next();
            if (rigid != null) {
                added += rigidContribution(rigid, x, y, z);
            }
        }
        pieces.back(Integer.MAX_VALUE);
        while (junctions.hasNext()) {
            EnhancedJigsawJunction junction = junctions.next();
            if (junction != null) {
                added += junctionContribution(junction, x, y, z);
            }
        }
        junctions.back(Integer.MAX_VALUE);
        //?} else {
        /*ObjectList<EnhancedBeardifierRigid> pieces = data.moogs_structures_getEnhancedPieces();
        if (pieces != null) {
            for (EnhancedBeardifierRigid rigid : pieces) {
                added += rigidContribution(rigid, x, y, z);
            }
        }
        ObjectList<EnhancedJigsawJunction> junctions = data.moogs_structures_getEnhancedJunctions();
        if (junctions != null) {
            for (EnhancedJigsawJunction junction : junctions) {
                added += junctionContribution(junction, x, y, z);
            }
        }
        *///?}
        if (y >= data.moogs_structures_getRoofFloorY() && added < 0) {
            added = 0;
        }
        return density + added;
    }

    /**
     * One rigid piece's push on the density at a point. Positive buries, negative carves. The piece's
     * box (or its adaptation area's columns) is moved by bottom_offset and grown by padding; a band,
     * when set, replaces the box's y extent with rows counted from the piece floor. Kernel falloff
     * still bleeds a few blocks past the edges for a natural blend.
     */
    private static double rigidContribution(EnhancedBeardifierRigid rigid, int x, int y, int z) {
        BoundingBox box = rigid.pieceBoundingBox();
        EnhancedTerrainAdaptation adaptation = rigid.pieceTerrainAdaptation();
        Optional<EnhancedTerrainAdaptation.Band> bandOpt = adaptation.getBand();
        // If a band targets specific piece heights, skip pieces whose Y-span isn't in the list.
        if (bandOpt.isPresent()
                && bandOpt.get().pieceHeights().isPresent()
                && !bandOpt.get().pieceHeights().get().contains(box.getYSpan())) {
            return 0;
        }

        int bottomOffset = (int) adaptation.getBottomOffset();
        int minY;
        int maxY;
        if (bandOpt.isPresent()) {
            // The band overrides any top/bottom padding.
            int floor = box.minY() + bottomOffset;
            minY = floor + bandOpt.get().bottom();
            maxY = floor + bandOpt.get().top();
        } else {
            minY = box.minY() + bottomOffset - adaptation.getPadding().bottom();
            maxY = box.maxY() + bottomOffset + adaptation.getPadding().top();
        }
        int yDistance = Math.max(0, Math.max(minY - y, y - maxY));
        // Out of the kernel's reach above or below: nothing to add, so skip the column search.
        if (yDistance >= adaptation.getKernelSize() - adaptation.getKernelRadius()) {
            return 0;
        }

        // x/z padding is given in the piece's own frame, so it turns with the piece.
        Direction.Axis xPaddingDirection = rigid.rotation().rotate(Direction.EAST).getAxis();
        int xPadding = xPaddingDirection == Direction.Axis.X ? adaptation.getPadding().x() : adaptation.getPadding().z();
        int zPadding = xPaddingDirection == Direction.Axis.X ? adaptation.getPadding().z() : adaptation.getPadding().x();

        int xDistance;
        int zDistance;
        List<BoundingBox> areas = rigid.areas();
        if (areas == null) {
            xDistance = Math.max(0, Math.max(box.minX() - xPadding - x, x - box.maxX() - xPadding));
            zDistance = Math.max(0, Math.max(box.minZ() - zPadding - z, z - box.maxZ() - zPadding));
        } else {
            // Nearest column of the area. Taking the nearest rather than summing every rectangle keeps
            // overlapping or touching rectangles from counting twice.
            xDistance = Integer.MAX_VALUE;
            zDistance = Integer.MAX_VALUE;
            long best = Long.MAX_VALUE;
            for (BoundingBox area : areas) {
                int dx = Math.max(0, Math.max(area.minX() - xPadding - x, x - area.maxX() - xPadding));
                int dz = Math.max(0, Math.max(area.minZ() - zPadding - z, z - area.maxZ() - zPadding));
                long squared = (long) dx * dx + (long) dz * dz;
                if (squared < best) {
                    best = squared;
                    xDistance = dx;
                    zDistance = dz;
                }
            }
            if (best == Long.MAX_VALUE) {
                return 0;
            }
        }
        int yDistanceToPieceBottom = y - minY;

        return adaptation.computeDensityFactor(xDistance, yDistance, zDistance, yDistanceToPieceBottom) * 0.8D;
    }

    private static double junctionContribution(EnhancedJigsawJunction enhancedJigsawJunction, int x, int y, int z) {
        JigsawJunction jigsawJunction = enhancedJigsawJunction.jigsawJunction();
        EnhancedTerrainAdaptation adaptation = enhancedJigsawJunction.pieceTerrainAdaptation();
        // Band-limited adaptation is piece-local; junction beards sit at connection ground level
        // and would carve outside the band, so skip them when a band is configured.
        if (adaptation.getBand().isPresent()) {
            return 0;
        }
        int groundY = jigsawJunction.getSourceGroundY() + (int) adaptation.getBottomOffset();
        int xDistanceToJunction = x - jigsawJunction.getSourceX();
        int yDistanceToJunction = y - groundY;
        int zDistanceToJunction = z - jigsawJunction.getSourceZ();
        return adaptation.computeDensityFactor(
                xDistanceToJunction, yDistanceToJunction, zDistanceToJunction, yDistanceToJunction) * 0.4D;
    }
}
