package com.finndog.moogs_structures.world.structures.placements;

import com.finndog.moogs_structures.config.MslConfig;
import com.finndog.moogs_structures.modinit.MoogsStructuresStructurePlacementType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class AdvancedRandomSpread extends RandomSpreadStructurePlacement {
    public static final MapCodec<AdvancedRandomSpread> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", Vec3i.ZERO).forGetter(AdvancedRandomSpread::locateOffset),
            FrequencyReductionMethod.CODEC.optionalFieldOf("frequency_reduction_method", FrequencyReductionMethod.DEFAULT).forGetter(AdvancedRandomSpread::frequencyReductionMethod),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("frequency", 1.0F).forGetter(AdvancedRandomSpread::frequency),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(AdvancedRandomSpread::salt),
            ExclusionZone.CODEC.optionalFieldOf("exclusion_zone").forGetter(AdvancedRandomSpread::exclusionZone),
            SuperExclusionZone.CODEC.optionalFieldOf("super_exclusion_zone").forGetter(AdvancedRandomSpread::superExclusionZone),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("spacing").forGetter(AdvancedRandomSpread::spacing),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("separation").forGetter(AdvancedRandomSpread::separation),
            RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR).forGetter(AdvancedRandomSpread::spreadType),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("min_distance_from_world_origin").forGetter(AdvancedRandomSpread::minDistanceFromWorldOrigin),
            Codec.STRING.optionalFieldOf("spacing_key").forGetter(p -> p.spacingKey),
            Codec.STRING.optionalFieldOf("structure_id").forGetter(p -> p.structureId)
    ).apply(instance, instance.stable(AdvancedRandomSpread::new)));

    private final int spacing;
    private final int separation;
    private final RandomSpreadType spreadType;
    private final Optional<Integer> minDistanceFromWorldOrigin;
    private final Optional<SuperExclusionZone> superExclusionZone;
    private final Optional<String> spacingKey;
    // Optional owning structure id; when set and that structure is disabled in the config this
    // placement reports no positions, so a disabled structure never distorts neighbours' exclusion
    // zones (the tryGenerateStructure mixin remains the universal net for the general case).
    private final Optional<String> structureId;
    private final Identifier structureIdRL;

    // Stamped once at world load so a set needs no spacing_key/structure_id in its JSON; the placement
    // then resolves both from the set it belongs to. Volatile: published before any worldgen read.
    private volatile String owningSetId;
    private volatile Identifier owningSetIdRL;

    // Memoized against MslConfig's generation counter in one immutable holder behind a single volatile
    // field, so concurrent structure-starts (C2ME) snapshot a consistent triple rather than ever
    // observing a torn or spacing<=separation state.
    private volatile Memo memo;

    private record Memo(int generation, int spacing, int separation) {}

    public AdvancedRandomSpread(Vec3i locationOffset,
                                FrequencyReductionMethod frequencyReductionMethod,
                                float frequency,
                                int salt,
                                Optional<ExclusionZone> exclusionZone,
                                Optional<SuperExclusionZone> superExclusionZone,
                                int spacing,
                                int separation,
                                RandomSpreadType spreadType,
                                Optional<Integer> minDistanceFromWorldOrigin,
                                Optional<String> spacingKey,
                                Optional<String> structureId
    ) {
        super(locationOffset, frequencyReductionMethod, frequency, salt, exclusionZone, spacing, separation, spreadType);
        this.spacing = (int)Math.round(spacing * 1.65);
        this.separation = (int)Math.round(separation * 1.65);
        this.spreadType = spreadType;
        this.minDistanceFromWorldOrigin = minDistanceFromWorldOrigin;
        this.superExclusionZone = superExclusionZone;
        this.spacingKey = spacingKey;
        this.structureId = structureId;
        this.structureIdRL = structureId.map(Identifier::tryParse).orElse(null);

        if (spacing <= separation) {
            throw new RuntimeException("""
                Moog's Structure Lib: Spacing cannot be less or equal to separation.
                Please correct this error as there's no way to spawn this structure properly
                    Spacing: %s
                    Separation: %s.
            """.formatted(spacing, separation));
        }
    }

    private String effectiveSpacingKey() {
        return spacingKey.orElse(this.owningSetId);
    }

    private Memo memo() {
        int gen = MslConfig.get().spacingGeneration();
        Memo m = this.memo;
        if (m != null && m.generation() == gen) return m;
        double mult = MslConfig.get().getEffectiveSpacingMultiplier(effectiveSpacingKey());
        int es = Math.max(1, (int) Math.round(this.spacing * mult));
        int esep = (int) Math.round(this.separation * mult);
        if (esep >= es) esep = es - 1;   // keep spacing > separation so the grid diff stays >= 1
        Memo nm = new Memo(gen, es, esep);
        this.memo = nm;
        return nm;
    }

    /** Resets the memo so the next read recomputes with the stamped key. */
    public void setOwningSetId(Identifier setId) {
        this.owningSetIdRL = setId;
        this.owningSetId = setId.toString();
        this.memo = null;
    }

    private Identifier effectiveDisableId() {
        return structureIdRL != null ? structureIdRL : this.owningSetIdRL;
    }

    @Override
    public int spacing() {
        return memo().spacing();
    }

    @Override
    public int separation() {
        return memo().separation();
    }

    @Override
    public RandomSpreadType spreadType() {
        return this.spreadType;
    }

    public Optional<Integer> minDistanceFromWorldOrigin() {
        return this.minDistanceFromWorldOrigin;
    }

    public Optional<SuperExclusionZone> superExclusionZone() {
        return this.superExclusionZone;
    }

    @Override
    public boolean isStructureChunk(ChunkGeneratorStructureState chunkGeneratorStructureState, int i, int j) {
        Identifier disableId = effectiveDisableId();
        if (disableId != null && MslConfig.get().isStructureDisabled(disableId)) {
            return false;
        }
        if (!super.isStructureChunk(chunkGeneratorStructureState, i, j)) {
            return false;
        }
        else {
            return this.superExclusionZone.isEmpty() || !this.superExclusionZone.get().isPlacementForbidden(chunkGeneratorStructureState, i, j);
        }
    }

    @Override
    public ChunkPos getPotentialStructureChunk(long seed, int x, int z) {
        // One snapshot of the effective (config-scaled) values, matching the spacing()/separation()
        // accessors that /locate strides its search grid by, and internally consistent under threads.
        Memo m = memo();
        int sp = m.spacing();
        int sep = m.separation();
        int regionX = Math.floorDiv(x, sp);
        int regionZ = Math.floorDiv(z, sp);
        WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenrandom.setLargeFeatureWithSalt(seed, regionX, regionZ, this.salt());
        int diff = sp - sep;
        int offsetX = this.spreadType.evaluate(worldgenrandom, diff);
        int offsetZ = this.spreadType.evaluate(worldgenrandom, diff);
        return new ChunkPos(regionX * sp + offsetX, regionZ * sp + offsetZ);
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState chunkGeneratorStructureState, int x, int z) {
        if (minDistanceFromWorldOrigin.isPresent()) {
            int xBlockPos = x * 16;
            int zBlockPos = z * 16;
            if((xBlockPos * xBlockPos) + (zBlockPos * zBlockPos) <
                    (minDistanceFromWorldOrigin.get() * minDistanceFromWorldOrigin.get()))
            {
                return false;
            }
        }

        ChunkPos chunkpos = this.getPotentialStructureChunk(chunkGeneratorStructureState.getLevelSeed(), x, z);
        return chunkpos.x == x && chunkpos.z == z;
    }

    @Override
    public StructurePlacementType<?> type() {
        return MoogsStructuresStructurePlacementType.ADVANCED_RANDOM_SPREAD.get();
    }

    public record SuperExclusionZone(HolderSet<StructureSet> otherSet, int chunkCount, Optional<Integer> allowedChunkCount) {
        private static final ThreadLocal<Set<Identifier>> EVALUATING_SETS = ThreadLocal.withInitial(HashSet::new);

        public static final Codec<SuperExclusionZone> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                RegistryCodecs.homogeneousList(Registries.STRUCTURE_SET, StructureSet.DIRECT_CODEC).fieldOf("other_set").forGetter(SuperExclusionZone::otherSet),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("chunk_count").forGetter(SuperExclusionZone::chunkCount),
                Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("allowed_chunk_count").forGetter(SuperExclusionZone::allowedChunkCount)
        ).apply(builder, SuperExclusionZone::new));

        boolean isPlacementForbidden(ChunkGeneratorStructureState chunkGeneratorStructureState, int l, int j) {
            Set<Identifier> evaluating = EVALUATING_SETS.get();

            for (Holder<StructureSet> holder : this.otherSet) {
                Identifier setId = holder.unwrapKey().map(key -> key.identifier()).orElse(null);
                if (setId == null) continue;
                if (evaluating.contains(setId)) continue;
                evaluating.add(setId);
                try {
                    if (chunkGeneratorStructureState.hasStructureChunkInRange(holder, l, j, this.chunkCount)) {
                        return true;
                    }
                } finally {
                    evaluating.remove(setId);
                }
            }

            if (this.allowedChunkCount.isPresent() && this.allowedChunkCount.get() > this.chunkCount) {
                boolean isAnyInRange = false;
                for (Holder<StructureSet> holder : this.otherSet) {
                    Identifier setId = holder.unwrapKey().map(key -> key.identifier()).orElse(null);
                    if (setId == null) continue;
                    if (evaluating.contains(setId)) continue;
                    evaluating.add(setId);
                    try {
                        if (chunkGeneratorStructureState.hasStructureChunkInRange(holder, l, j, this.allowedChunkCount.get())) {
                            isAnyInRange = true;
                        }
                    } finally {
                        evaluating.remove(setId);
                    }
                }
                if (!isAnyInRange) {
                    return false;
                }
            }

            return false;
        }
    }
}