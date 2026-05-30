package com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier;

import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptation;
import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptationStructure;
import com.finndog.moogs_structures.world.structures.terrainadaptation.PoolElementAdaptationOverride;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.core.BlockPos;
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

/**
 * Static helpers used by the Beardifier mixin to apply {@link EnhancedTerrainAdaptation} to
 * structures implementing {@link EnhancedTerrainAdaptationStructure}.
 * Reduced port of YUNG's API EnhancedBeardifierHelper (aquifer override + per-element override removed).
 */
public class EnhancedBeardifierHelper {

    // MC 1.21.9 changed Beardifier from iterator-based fields (pieceIterator, junctionIterator)
    // to list-based fields (pieces, junctions, affectedBox) with a different constructor.
    // We probe once at class-load time and cache the result + field/constructor handles.
    static final boolean USE_NEW_API;
    private static final Field PIECES_FIELD;
    private static final Field JUNCTIONS_FIELD;
    private static final Field AFFECTED_BOX_FIELD;
    private static final Field PIECE_ITER_FIELD;
    private static final Field JUNCTION_ITER_FIELD;
    @SuppressWarnings("rawtypes")
    private static final Constructor BEARDIFIER_CTOR;

    static {
        Field piecesF = null, junctionsF = null, affectedBoxF = null;
        Field pieceIterF = null, junctionIterF = null;
        Constructor<?> ctor = null;
        boolean newApi = false;

        try {
            // MC 1.21.9+ path: List-based fields + 3-arg constructor.
            // field_61465/66/67 are the Fabric intermediary names for pieces/junctions/affectedBox.
            piecesF = getField("pieces", "field_61465");
            piecesF.setAccessible(true);
            junctionsF = getField("junctions", "field_61466");
            junctionsF.setAccessible(true);
            affectedBoxF = getField("affectedBox", "field_61467");
            affectedBoxF.setAccessible(true);
            ctor = Beardifier.class.getDeclaredConstructor(List.class, List.class, BoundingBox.class);
            ctor.setAccessible(true);
            newApi = true;
        } catch (NoSuchFieldException | NoSuchMethodException ignored) {
            try {
                // MC 1.21.5-1.21.8 path: ObjectListIterator fields + 2-arg constructor.
                // field_28744/45 are the Fabric intermediary names for pieceIterator/junctionIterator.
                pieceIterF = getField("pieceIterator", "field_28744");
                pieceIterF.setAccessible(true);
                junctionIterF = getField("junctionIterator", "field_28745");
                junctionIterF.setAccessible(true);
                ctor = Beardifier.class.getDeclaredConstructor(ObjectListIterator.class, ObjectListIterator.class);
                ctor.setAccessible(true);
            } catch (ReflectiveOperationException fatal) {
                throw new RuntimeException("MSL: cannot locate Beardifier fields or constructor", fatal);
            }
        }

        USE_NEW_API = newApi;
        PIECES_FIELD = piecesF;
        JUNCTIONS_FIELD = junctionsF;
        AFFECTED_BOX_FIELD = affectedBoxF;
        PIECE_ITER_FIELD = pieceIterF;
        JUNCTION_ITER_FIELD = junctionIterF;
        BEARDIFIER_CTOR = ctor;
    }

    private static Field getField(String mojangName, String intermediaryName) throws NoSuchFieldException {
        try {
            return Beardifier.class.getDeclaredField(mojangName);
        } catch (NoSuchFieldException e) {
            return Beardifier.class.getDeclaredField(intermediaryName);
        }
    }

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

        try {
            Beardifier newBeardifier;
            if (USE_NEW_API) {
                // MC 1.21.9+: list-based fields, 3-arg constructor, nullable affectedBox short-circuit.
                // Union the original affectedBox with enhanced pieces/junctions so the compute() hook fires.
                @SuppressWarnings("unchecked")
                List<Beardifier.Rigid> pieces = (List<Beardifier.Rigid>) PIECES_FIELD.get(original);
                @SuppressWarnings("unchecked")
                List<JigsawJunction> junctions = (List<JigsawJunction>) JUNCTIONS_FIELD.get(original);
                BoundingBox originalBox = (BoundingBox) AFFECTED_BOX_FIELD.get(original);
                BoundingBox affectedBox = computeEnhancedAffectedBox(
                        enhancedBeardifierRigidList, enhancedJunctionList, originalBox);
                @SuppressWarnings("unchecked")
                Beardifier b = (Beardifier) BEARDIFIER_CTOR.newInstance(pieces, junctions, affectedBox);
                newBeardifier = b;
            } else {
                // MC 1.21.5-1.21.8: iterator-based fields, 2-arg constructor.
                @SuppressWarnings("unchecked")
                ObjectListIterator<Beardifier.Rigid> pieceIter =
                        (ObjectListIterator<Beardifier.Rigid>) PIECE_ITER_FIELD.get(original);
                @SuppressWarnings("unchecked")
                ObjectListIterator<JigsawJunction> junctionIter =
                        (ObjectListIterator<JigsawJunction>) JUNCTION_ITER_FIELD.get(original);
                @SuppressWarnings("unchecked")
                Beardifier b = (Beardifier) BEARDIFIER_CTOR.newInstance(pieceIter, junctionIter);
                newBeardifier = b;
            }
            EnhancedBeardifierData enhancedBeardifier = (EnhancedBeardifierData) newBeardifier;
            enhancedBeardifier.setEnhancedPieceIterator(enhancedBeardifierRigidList.iterator());
            enhancedBeardifier.setEnhancedJunctionIterator(enhancedJunctionList.iterator());
            return newBeardifier;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("MSL: failed to construct Beardifier via reflection", e);
        }
    }

    // Only called on MC 1.21.9+ (USE_NEW_API path).
    private static BoundingBox computeEnhancedAffectedBox(ObjectList<EnhancedBeardifierRigid> rigids,
                                                          ObjectList<EnhancedJigsawJunction> junctions,
                                                          BoundingBox originalBox) {
        BoundingBox box = originalBox;
        for (EnhancedBeardifierRigid rigid : rigids) {
            int radius = Math.max(1, rigid.pieceTerrainAdaptation().getKernelRadius());
            BoundingBox pieceBox = rigid.pieceBoundingBox().inflatedBy(radius);
            box = box == null ? pieceBox : box.encapsulate(pieceBox);
        }
        for (EnhancedJigsawJunction junction : junctions) {
            JigsawJunction jigsawJunction = junction.jigsawJunction();
            int radius = Math.max(1, junction.pieceTerrainAdaptation().getKernelRadius());
            BoundingBox junctionBox = new BoundingBox(new BlockPos(
                    jigsawJunction.getSourceX(),
                    jigsawJunction.getSourceGroundY(),
                    jigsawJunction.getSourceZ())).inflatedBy(radius);
            box = box == null ? junctionBox : box.encapsulate(junctionBox);
        }
        return box;
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
