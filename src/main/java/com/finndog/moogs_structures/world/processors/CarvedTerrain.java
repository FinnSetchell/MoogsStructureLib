package com.finndog.moogs_structures.world.processors;

import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
//? if <26.3 {
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
//?}
//? if <1.21.2 {
import net.minecraft.world.level.levelgen.GenerationStep;
//?}
//? if >=26.3 {
/*import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.CarverOutput;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import java.util.BitSet;
*///?}
import org.jetbrains.annotations.Nullable;

/**
 * Which blocks of a chunk the world's carvers (Nether caves, Overworld caves and canyons) cut away,
 * read without looking at the blocks themselves. Features and structures from neighbouring chunks
 * can already have filled a carved space by the time a piece is placed, and whether they have
 * depends on the order chunks generate in; the carvers' own record of what they cut does not.
 *
 * <p>Up to 26.2 the game keeps that record on a chunk until the chunk is finished, which is always
 * true for the chunk being placed and its direct neighbours. From 26.3 it is thrown away after
 * carving, so it is worked out again here the same way the game does: the carvers only decide
 * where to cut from the world seed and the chunk positions, never from the blocks.
 */
final class CarvedTerrain {

    interface Mask {
        boolean isCarved(int x, int y, int z);
    }

    private static final Mask NOTHING_CARVED = (x, y, z) -> false;

    private CarvedTerrain() {}

    /** The carved blocks of {@code chunk}, or null when the game no longer keeps them for it (a finished chunk). */
    @Nullable
    static Mask of(ServerLevelAccessor level, ChunkAccess chunk) {
        //? if <26.3 {
        if (!(chunk instanceof ProtoChunk proto) || chunk instanceof ImposterProtoChunk) {
            return null;
        }
        //?}
        //? if <1.21.2 {
        CarvingMask air = proto.getCarvingMask(GenerationStep.Carving.AIR);
        CarvingMask liquid = proto.getCarvingMask(GenerationStep.Carving.LIQUID);
        if (air == null && liquid == null) {
            return NOTHING_CARVED;
        }
        return (x, y, z) -> (air != null && air.get(x, y, z)) || (liquid != null && liquid.get(x, y, z));
        //?}
        //? if >=1.21.2 <26.3 {
        /*CarvingMask mask = proto.getCarvingMask();
        return mask == null ? NOTHING_CARVED : mask::get;
        *///?}
        //? if >=26.3 {
        /*ChunkGenerator generator = level.getLevel().getChunkSource().getGenerator();
        if (!(generator instanceof NoiseBasedChunkGenerator)) {
            return NOTHING_CARVED;
        }
        RandomState randomState = level.getLevel().getChunkSource().randomState();
        BiomeResolver biomes = generator.getBiomeSource().createUncachedResolver(randomState);
        WorldGenerationContext context = new WorldGenerationContext(generator, chunk.getHeightAccessorForGeneration());
        int maxY = context.getMinGenY() + context.getGenDepth() - 1 - (chunk.isUpgrading() ? 0 : 7);
        Recorded mask = new Recorded(context.getMinGenY() + 1, maxY);
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        ChunkPos pos = chunk.getPos();
        // Same loop as NoiseBasedChunkGenerator.generateCarvers: every carver that starts within 8 chunks.
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                ChunkPos source = new ChunkPos(pos.x() + dx, pos.z() + dz);
                Holder<net.minecraft.world.level.biome.Biome> biome = biomes.getNoiseBiome(QuartPos.fromBlock(source.getMinBlockX()), 0, QuartPos.fromBlock(source.getMinBlockZ()));
                int index = 0;
                for (Holder<WorldCarver> carver : generator.getBiomeGenerationSettings(biome).getCarvers()) {
                    random.setLargeFeatureSeed(randomState.seed() + index, source.x(), source.z());
                    if (carver.value().isStartChunk(random)) {
                        carver.value().carve(context, random, pos, source, mask);
                    }
                    index++;
                }
            }
        }
        return mask.isEmpty() ? NOTHING_CARVED : mask;
        *///?}
    }

    //? if >=26.3 {
    /*private static final class Recorded implements CarverOutput, Mask {
        private final int minY;
        private final int maxY;
        private final int height;
        private final BitSet carved;

        private Recorded(int minY, int maxY) {
            this.minY = minY;
            this.maxY = maxY;
            this.height = maxY - minY + 1;
            this.carved = new BitSet(256 * this.height);
        }

        @Override
        public void carve(int x, int y, int z) {
            this.carved.set(index(x, y, z));
        }

        @Override
        public boolean isCarved(int x, int y, int z) {
            return y >= this.minY && y <= this.maxY && this.carved.get(index(x & 15, y, z & 15));
        }

        private int index(int x, int y, int z) {
            return y - this.minY + (z + (x << 4)) * this.height;
        }

        private boolean isEmpty() {
            return this.carved.isEmpty();
        }

        @Override
        public int minY() {
            return this.minY;
        }

        @Override
        public int maxY() {
            return this.maxY;
        }
    }
    *///?}
}
