package com.finndog.moogs_structures.world.structures.terrainadaptation.beardifier;

//? if <1.21.5 {
import com.finndog.moogs_structures.mixins.terrainadaptation.BeardifierAccessor;
//?}
//? if >=1.21.11 {
/*import com.finndog.moogs_structures.mixins.terrainadaptation.BeardifierAccessor;
*///?}
import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptation;
import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptationStructure;
import com.finndog.moogs_structures.world.structures.terrainadaptation.PoolElementAdaptationOverride;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
//? if >=1.21.5 <1.21.11 {
/*import it.unimi.dsi.fastutil.objects.ObjectListIterator;
*///?}
//? if >=1.21.5 {
/*import net.minecraft.core.BlockPos;
*///?}
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
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

//? if >=1.21.5 <1.21.11 {
/*import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
*///?}
import java.util.List;
import java.util.Optional;

/**
 * Static helpers used by the Beardifier mixin to apply {@link EnhancedTerrainAdaptation} to
 * structures implementing {@link EnhancedTerrainAdaptationStructure}.
 * Reduced port of YUNG's API EnhancedBeardifierHelper (aquifer override removed; per-element overrides kept).
 */
public class EnhancedBeardifierHelper {

    // MC 1.21.9 changed Beardifier from iterator-based fields (pieceIterator, junctionIterator)
    // to list-based fields (pieces, junctions, affectedBox) with a different constructor.
    // We probe once at class-load time and cache the result + field/constructor handles.
    //? if >=1.21.5 <1.21.11 {
    /*static final boolean USE_NEW_API;
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
    *///?}

    public static Beardifier forStructuresInChunk(StructureManager structureManager, ChunkPos chunkPos, Beardifier original) {
        ObjectList<EnhancedBeardifierRigid> enhancedBeardifierRigidList = new ObjectArrayList<>(10);
        ObjectList<EnhancedJigsawJunction> enhancedJunctionList = new ObjectArrayList<>(10);
        int chunkMinBlockX = chunkPos.getMinBlockX();
        int chunkMinBlockZ = chunkPos.getMinBlockZ();

        //? if <26.3 {
        List<StructureStart> structureStarts = structureManager.startsForStructure(chunkPos,
        //?} else {
        /*List<StructureStart> structureStarts = structureManager.startsForStructure(chunkPos.x(), chunkPos.z(),
        *///?}
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
                            Rotation.NONE));
                }
            }
        }

        //? if <1.21.5 {
        Beardifier newBeardifier = new Beardifier(
                ((BeardifierAccessor) original).getPieceIterator(),
                ((BeardifierAccessor) original).getJunctionIterator());
        EnhancedBeardifierData enhancedBeardifier = (EnhancedBeardifierData) newBeardifier;
        enhancedBeardifier.moogs_structures_setEnhancedPieceIterator(enhancedBeardifierRigidList.iterator());
        enhancedBeardifier.moogs_structures_setEnhancedJunctionIterator(enhancedJunctionList.iterator());
        return newBeardifier;
        //?}
        //? if >=1.21.5 <1.21.11 {
        /*try {
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
            enhancedBeardifier.moogs_structures_setEnhancedPieceIterator(enhancedBeardifierRigidList.iterator());
            enhancedBeardifier.moogs_structures_setEnhancedJunctionIterator(enhancedJunctionList.iterator());
            return newBeardifier;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("MSL: failed to construct Beardifier via reflection", e);
        }
        *///?}
        // 1.21.11's Beardifier is List-based with a nullable affectedBox; both compute() and
        // fillArray() short-circuit to 0 when affectedBox is null. So the reconstructed Beardifier
        // must carry an affectedBox covering the enhanced regions, or the enhanced density (added
        // via the compute mixin) would never be evaluated. Union the original box with the enhanced
        // pieces/junctions (inflated by their kernel radius) to keep enhanced adaptation active.
        //? if >=1.21.11 {
        /*BoundingBox affectedBox = computeEnhancedAffectedBox(
                enhancedBeardifierRigidList,
                enhancedJunctionList,
                ((BeardifierAccessor) original).getAffectedBox());

        *///?}
        //? if >=1.21.11 <26.1.2 {
        /*Beardifier newBeardifier = new Beardifier(
                ((BeardifierAccessor) original).getPieces(),
                ((BeardifierAccessor) original).getJunctions(),
                affectedBox);
        EnhancedBeardifierData enhancedBeardifier = (EnhancedBeardifierData) newBeardifier;
        enhancedBeardifier.moogs_structures_setEnhancedPieceIterator(enhancedBeardifierRigidList.iterator());
        enhancedBeardifier.moogs_structures_setEnhancedJunctionIterator(enhancedJunctionList.iterator());
        return newBeardifier;
        *///?}
        // Vanilla returns its shared static Beardifier.EMPTY whenever no *vanilla* terrain-adapting
        // structure start touches this chunk. That is the normal case for our structures, which adapt
        // terrain through enhanced adaptation rather than vanilla's terrainAdaptation(). Writing this
        // chunk's data onto EMPTY would publish it to a global singleton that every concurrently
        // generating chunk shares across every world-gen worker thread, so each chunk would read
        // whichever chunk wrote last. Give ourselves a private instance instead.
        //? if >=26.1.2 {
        /*Beardifier target = original;
        if (target == Beardifier.EMPTY) {
            if (enhancedBeardifierRigidList.isEmpty() && enhancedJunctionList.isEmpty()) {
                // Nothing enhanced here either; leave vanilla's empty fast path untouched.
                return original;
            }
            target = new Beardifier(List.of(), List.of(), null);
        }

        // For a per-chunk instance, mutate in place rather than constructing a replacement. YUNG's API's
        // BeardifierMixin also swaps the returned Beardifier at this same injection point; if
        // either mod replaces the other's instance, the replaced instance's @Unique duck data
        // is lost and that mod's compute handler sees null iterators (BUG17: deterministic
        // world-gen crash on fabric/26.1.x with both mods installed). YUNG's handler runs first
        // (priority 1000 vs our 1500) and always returns a fresh Beardifier, so when it is present
        // `original` is never EMPTY and we never take the replacement branch above.
        ((BeardifierAccessor) target).setAffectedBox(affectedBox);
        EnhancedBeardifierData enhancedBeardifier = (EnhancedBeardifierData) target;
        // Store the lists themselves, never iterators: compute() is called from world-gen worker
        // threads and re-entrantly per noise cell, so a shared cursor gets advanced out from under
        // a running loop (hasNext() passes, next() throws NoSuchElementException). computeDensity()
        // takes a fresh local cursor per call instead.
        enhancedBeardifier.moogs_structures_setEnhancedPieces(enhancedBeardifierRigidList);
        enhancedBeardifier.moogs_structures_setEnhancedJunctions(enhancedJunctionList);
        return target;
        *///?}
    //? if >=1.21.5 {
    /*}

    // Only called on MC 1.21.9+ (USE_NEW_API path).
    private static BoundingBox computeEnhancedAffectedBox(ObjectList<EnhancedBeardifierRigid> rigids,
                                                          ObjectList<EnhancedJigsawJunction> junctions,
                                                          BoundingBox originalBox) {
        BoundingBox box = originalBox;
        for (EnhancedBeardifierRigid rigid : rigids) {
            int radius = Math.max(1, rigid.pieceTerrainAdaptation().getKernelRadius());
            BoundingBox pieceBox = rigid.pieceBoundingBox().inflatedBy(radius);
    *///?}
            //? if >=1.21.5 <1.21.11 {
            /*box = box == null ? pieceBox : box.encapsulate(pieceBox);
            *///?}
            //? if >=1.21.11 {
            /*box = box == null ? pieceBox : BoundingBox.encapsulating(box, pieceBox);
            *///?}
        //? if >=1.21.5 {
        /*}
        for (EnhancedJigsawJunction junction : junctions) {
            JigsawJunction jigsawJunction = junction.jigsawJunction();
            int radius = Math.max(1, junction.pieceTerrainAdaptation().getKernelRadius());
            BoundingBox junctionBox = new BoundingBox(new BlockPos(
                    jigsawJunction.getSourceX(),
                    jigsawJunction.getSourceGroundY(),
                    jigsawJunction.getSourceZ())).inflatedBy(radius);
        *///?}
            //? if >=1.21.5 <1.21.11 {
            /*box = box == null ? junctionBox : box.encapsulate(junctionBox);
            *///?}
            //? if >=1.21.11 {
            /*box = box == null ? junctionBox : BoundingBox.encapsulating(box, junctionBox);
            *///?}
        //? if >=1.21.5 {
        /*}
        return box;
        *///?}
    }

    //? if <26.3 {
    public static double computeDensity(DensityFunction.FunctionContext ctx, double density, EnhancedBeardifierData data) {
        int x = ctx.blockX();
        int y = ctx.blockY();
        int z = ctx.blockZ();

    //?}
        //? if <26.1.2 {
        while (data.moogs_structures_getEnhancedPieceIterator() != null && data.moogs_structures_getEnhancedPieceIterator().hasNext()) {
            EnhancedBeardifierRigid rigid = data.moogs_structures_getEnhancedPieceIterator().next();
            if (rigid == null) continue;
            BoundingBox originalBox = rigid.pieceBoundingBox();
            BoundingBox pieceBoundingBox = originalBox;
            EnhancedTerrainAdaptation adaptation = rigid.pieceTerrainAdaptation();
            Rotation pieceRotation = rigid.rotation();
        //?}
        // Iterate with local cursors over the stored lists. compute() runs on world-gen worker
    //? if >=26.3 {
    /*public static double computeDensity(int x, int y, int z, double density, EnhancedBeardifierData data) {
        // Iterate with local cursors over the stored lists. Sampling runs on world-gen worker
    *///?}
        // threads, so anything cursor-shaped kept on the Beardifier would be shared mutable state.
        //? if >=26.1.2 {
        /*ObjectList<EnhancedBeardifierRigid> pieces = data.moogs_structures_getEnhancedPieces();
        if (pieces != null) {
            for (EnhancedBeardifierRigid rigid : pieces) {
                BoundingBox originalBox = rigid.pieceBoundingBox();
                BoundingBox pieceBoundingBox = originalBox;
                EnhancedTerrainAdaptation adaptation = rigid.pieceTerrainAdaptation();
                Rotation pieceRotation = rigid.rotation();
        *///?}

            //? if <26.1.2 {
            Optional<EnhancedTerrainAdaptation.Band> bandOpt = adaptation.getBand();
            // If a band targets specific piece heights, skip pieces whose Y-span isn't in the list.
            if (bandOpt.isPresent()
                    && bandOpt.get().pieceHeights().isPresent()
                    && !bandOpt.get().pieceHeights().get().contains(originalBox.getYSpan())) {
                continue;
            //?} else {
                /*Optional<EnhancedTerrainAdaptation.Band> bandOpt = adaptation.getBand();
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
            *///?}
            }
        //? if >=26.1.2 {
        /*}
        *///?}

            // Apply bottom offset
            //? if <26.1.2 {
            pieceBoundingBox = pieceBoundingBox.moved(0, (int) adaptation.getBottomOffset(), 0);
            //?} else {
        /*ObjectList<EnhancedJigsawJunction> junctions = data.moogs_structures_getEnhancedJunctions();
        if (junctions != null) {
            for (EnhancedJigsawJunction enhancedJigsawJunction : junctions) {
                JigsawJunction jigsawJunction = enhancedJigsawJunction.jigsawJunction();
                EnhancedTerrainAdaptation adaptation = enhancedJigsawJunction.pieceTerrainAdaptation();
            *///?}

            // Apply x/z padding (rotation-aware)
                // Band-limited adaptation is piece-local; junction beards sit at connection ground level
                // and would carve outside the band, so skip them when a band is configured.
            //? if <26.1.2 {
            Direction.Axis xPaddingDirection = pieceRotation.rotate(Direction.EAST).getAxis();
            int xPadding = xPaddingDirection == Direction.Axis.X ? adaptation.getPadding().x() : adaptation.getPadding().z();
            int zPadding = xPaddingDirection == Direction.Axis.X ? adaptation.getPadding().z() : adaptation.getPadding().x();
            pieceBoundingBox = pieceBoundingBox.inflatedBy(xPadding, 0, zPadding);
            //?} else {
                /*if (adaptation.getBand().isPresent()) {
                    continue;
                }
            *///?}

            //? if <26.1.2 {
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
            //?} else {
                /*int groundY = jigsawJunction.getSourceGroundY() + (int) adaptation.getBottomOffset();
                int xDistanceToJunction = x - jigsawJunction.getSourceX();
                int yDistanceToJunction = y - groundY;
                int zDistanceToJunction = z - jigsawJunction.getSourceZ();
                double densityFactor = adaptation.computeDensityFactor(
                        xDistanceToJunction, yDistanceToJunction, zDistanceToJunction, yDistanceToJunction) * 0.4D;
                density += densityFactor;
            *///?}
            }

            //? if <26.1.2 {
            int xDistanceToBoundingBox = Math.max(0, Math.max(pieceBoundingBox.minX() - x, x - pieceBoundingBox.maxX()));
            int yDistanceToBoundingBox = Math.max(0, Math.max(pieceBoundingBox.minY() - y, y - pieceBoundingBox.maxY()));
            int zDistanceToBoundingBox = Math.max(0, Math.max(pieceBoundingBox.minZ() - z, z - pieceBoundingBox.maxZ()));
            int yDistanceToPieceBottom = y - pieceBoundingBox.minY();

            double densityFactor = adaptation.computeDensityFactor(
                    xDistanceToBoundingBox, yDistanceToBoundingBox, zDistanceToBoundingBox, yDistanceToPieceBottom) * 0.8D;
            density += densityFactor;
            //?}
        }
        //? if <26.1.2 {
        data.moogs_structures_getEnhancedPieceIterator().back(Integer.MAX_VALUE);

        while (data.moogs_structures_getEnhancedJunctionIterator() != null && data.moogs_structures_getEnhancedJunctionIterator().hasNext()) {
            EnhancedJigsawJunction enhancedJigsawJunction = data.moogs_structures_getEnhancedJunctionIterator().next();
            if (enhancedJigsawJunction == null) continue;
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
        data.moogs_structures_getEnhancedJunctionIterator().back(Integer.MAX_VALUE);
        //?}

        return density;
    }
}
