package com.finndog.moogs_structures.world.randomize;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

/**
 * Weighted, codec-serializable set of BlockStates with optional per-entry altitude bounds.
 * Inspired by YUNG's API BlockStateRandomizer, trimmed to a self-contained MSL form:
 * instead of the full structure-condition framework, each entry may carry simple
 * min_y / max_y bounds so block choice can vary by altitude (e.g. magma near the lava sea).
 *
 * Probability semantics mirror YUNG: entries occupy probability bands in order; any leftover
 * probability (or an entry whose altitude bound fails) falls through to the default BlockState.
 */
public class BlockStateRandomizer {
    public static final Codec<BlockStateRandomizer> CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(
                    Entry.CODEC.listOf().optionalFieldOf("entries", List.of()).forGetter(r -> r.entries),
                    BlockState.CODEC.fieldOf("default").forGetter(r -> r.defaultBlockState))
            .apply(instance, BlockStateRandomizer::new));

    private final List<Entry> entries;
    private final BlockState defaultBlockState;

    public BlockStateRandomizer(List<Entry> entries, BlockState defaultBlockState) {
        this.entries = entries;
        this.defaultBlockState = defaultBlockState;
    }

    public BlockState getDefaultBlockState() {
        return defaultBlockState;
    }

    /** Pick a BlockState, honouring each entry's optional altitude bounds against the given y. */
    public BlockState get(RandomSource random, int y) {
        float target = random.nextFloat();
        float currBottom = 0;
        for (Entry entry : entries) {
            if (currBottom <= target && target < currBottom + entry.probability && entry.passesAltitude(y)) {
                return entry.blockState;
            }
            currBottom += entry.probability;
        }
        return this.defaultBlockState;
    }

    public static class Entry {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(
                        BlockState.CODEC.fieldOf("block").forGetter(e -> e.blockState),
                        Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(e -> e.probability),
                        Codec.INT.optionalFieldOf("min_y").forGetter(e -> e.minY),
                        Codec.INT.optionalFieldOf("max_y").forGetter(e -> e.maxY))
                .apply(instance, Entry::new));

        public final BlockState blockState;
        public final float probability;
        public final Optional<Integer> minY;
        public final Optional<Integer> maxY;

        public Entry(BlockState blockState, float probability, Optional<Integer> minY, Optional<Integer> maxY) {
            this.blockState = blockState;
            this.probability = probability;
            this.minY = minY;
            this.maxY = maxY;
        }

        public boolean passesAltitude(int y) {
            if (minY.isPresent() && y < minY.get()) return false;
            return maxY.isEmpty() || y <= maxY.get();
        }
    }

    public static BlockStateRandomizer single(BlockState state) {
        return new BlockStateRandomizer(List.of(), state == null ? Blocks.AIR.defaultBlockState() : state);
    }
}
