package com.finndog.moogs_structures.world.structures.terrainadaptation;

import com.mojang.serialization.MapCodec;
//? if <1.20.6 {
/*import com.finndog.moogs_structures.mixins.terrainadaptation.BeardifierAccessor;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.Optional;
*///?}

/**
 * The game's {@code "terrain_adaptation": "encapsulate"} for MSL structures on 1.20 to 1.20.4.
 * <p>
 * 1.20.5 added encapsulate: the terrain closes around every piece in a shell, which keeps aquifer water and lava
 * out of underground structures. Before that the game reads "encapsulate" as "none" without a word (its optional
 * fields drop values they can't parse), so a structure written for 1.20.5 and later fills with water underground.
 * MSL reads the field itself there and adds 1.20.5's own shell to the Beardifier, for MSL structures that ask
 * for it and nothing else. From 1.20.5 the game does all of this itself, so the field reads as false.
 */
public final class EncapsulateBackport {
    private EncapsulateBackport() {}

    //? if <1.20.6 {
    /*/^* True when the structure asks for encapsulate. Writes the value back only then, so it never doubles the game's own field. ^/
    public static final MapCodec<Boolean> FIELD = Codec.STRING.optionalFieldOf("terrain_adaptation").xmap(
            value -> value.filter("encapsulate"::equals).isPresent(),
            encapsulate -> encapsulate ? Optional.of("encapsulate") : Optional.empty());

    /^*
     * 1.20.5's Beardifier.forStructuresInChunk for one encapsulated structure start: the boxes of its rigid
     * pieces (and of any piece that isn't a pool element) within 12 blocks of the chunk, and its jigsaw junctions
     * near the chunk.
     ^/
    public static void collect(StructureStart structureStart, ChunkPos chunkPos, ObjectList<BoundingBox> pieces, ObjectList<JigsawJunction> junctions) {
        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();
        for (StructurePiece piece : structureStart.getPieces()) {
            if (!piece.isCloseToChunk(chunkPos, 12)) {
                continue;
            }
            if (piece instanceof PoolElementStructurePiece poolPiece) {
                if (poolPiece.getElement().getProjection() == StructureTemplatePool.Projection.RIGID) {
                    pieces.add(poolPiece.getBoundingBox());
                }
                for (JigsawJunction junction : poolPiece.getJunctions()) {
                    int sourceX = junction.getSourceX();
                    int sourceZ = junction.getSourceZ();
                    if (sourceX > chunkMinX - 12 && sourceZ > chunkMinZ - 12 && sourceX < chunkMinX + 15 + 12 && sourceZ < chunkMinZ + 15 + 12) {
                        junctions.add(junction);
                    }
                }
            } else {
                pieces.add(piece.getBoundingBox());
            }
        }
    }

    /^* 1.20.5's Beardifier.compute for the encapsulated pieces and their junctions at one point. ^/
    public static double density(int x, int y, int z, ObjectList<BoundingBox> pieces, ObjectList<JigsawJunction> junctions) {
        double density = 0.0;
        for (BoundingBox box : pieces) {
            int xDistance = Math.max(0, Math.max(box.minX() - x, x - box.maxX()));
            int yDistance = Math.max(0, Math.max(box.minY() - y, y - box.maxY()));
            int zDistance = Math.max(0, Math.max(box.minZ() - z, z - box.maxZ()));
            double distance = Mth.length(xDistance / 2.0, yDistance / 2.0, zDistance / 2.0);
            density += Mth.clampedMap(distance, 0.0, 6.0, 1.0, 0.0) * 0.8;
        }
        for (JigsawJunction junction : junctions) {
            int yToGround = y - junction.getSourceGroundY();
            density += BeardifierAccessor.moogs_structures_getBeardContribution(x - junction.getSourceX(), yToGround, z - junction.getSourceZ(), yToGround) * 0.4;
        }
        return density;
    }
    *///?} else {
    public static final MapCodec<Boolean> FIELD = MapCodec.unit(false);
    //?}
}
