package com.finndog.moogs_structures.world.structures.placements;

import com.finndog.moogs_structures.config.MslConfig;
import com.finndog.moogs_structures.config.ReplaceVanillaManager;
import com.finndog.moogs_structures.utils.StrictOptionalField;
//? if <26.3 {
import com.finndog.moogs_structures.modinit.MoogsStructuresStructurePlacementType;
//?}
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
//? if <26.3 {
import net.minecraft.core.RegistryCodecs;
//?} else {
/*import net.minecraft.core.registries.codec.RegistryCodecs;
*///?}
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
//? if <1.21.1 {
/*import net.minecraft.resources.ResourceLocation;
*///?}
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
//? if <26.3 {
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
//?}

//? if >=1.21.1 {
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
//?}
import java.util.Optional;
//? if >=1.21.1 {
import java.util.Set;
//?}

public class AdvancedRandomSpread extends RandomSpreadStructurePlacement {
    // A MapCodec on every version. Before 1.20.6 the placement type hands the game a Codec, and the type dispatch reads
    // the fields inline only from a MapCodec's own codec; from any other codec it reads them under a "value" key.
    private static final MapCodec<AdvancedRandomSpread> MAP_CODEC = RecordCodecBuilder.<AdvancedRandomSpread>mapCodec((instance) -> instance.group(
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
            Codec.STRING.optionalFieldOf("structure_id").forGetter(p -> p.structureId),
            StrictOptionalField.of(ReplacementSpacing.CODEC, "when_replacing").forGetter(p -> p.whenReplacing),
            StrictOptionalField.of(Generates.CODEC, "generates", Generates.ALWAYS).forGetter(p -> p.generates)
    ).apply(instance, instance.stable(AdvancedRandomSpread::new))).flatXmap(AdvancedRandomSpread::followsAReplacement, DataResult::success);

    //? if >=1.20.6 {
    public static final MapCodec<AdvancedRandomSpread> CODEC = MAP_CODEC;
    //?} else {
    /*public static final Codec<AdvancedRandomSpread> CODEC = MAP_CODEC.codec();
    *///?}

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
    private final ResourceLocation structureIdRL;
    // The replacement this set follows. Its spacing pair, when given, is used instead of the declared one while
    // the replacement's preset is on, so a set that stands in for a vanilla structure can match its density only
    // when it is replacing it; generates can keep the set off while the preset is on, or while it is off.
    private final Optional<ReplacementSpacing> whenReplacing;
    private final Generates generates;

    // Stamped once at world load so a set needs no spacing_key/structure_id in its JSON; the placement
    // then resolves both from the set it belongs to. Volatile: published before any worldgen read.
    private volatile String owningSetId;
    private volatile ResourceLocation owningSetIdRL;
    // Stamped with the set id: true when none of the set's structures generates on this Minecraft version
    // (mc_versions), so the set acts as turned off and doesn't push its neighbours away either.
    private volatile boolean noStructureOnThisVersion;

    //? if <1.20.6 {
    /*/^* Before 1.20.6: set while a structure search checks a chunk of a turned-off set (StructureSearchMixin). ^/
    public static final ThreadLocal<Boolean> SEARCHING_TURNED_OFF_SET = new ThreadLocal<>();
    *///?}

    // Memoized against MslConfig's generation counter in one immutable holder behind a single volatile
    // field, so concurrent structure-starts (C2ME) snapshot a consistent triple rather than ever
    // observing a torn or spacing<=separation state.
    private volatile Memo memo;

    private record Memo(int generation, boolean replacing, int spacing, int separation) {}

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
                                Optional<String> structureId,
                                Optional<ReplacementSpacing> whenReplacing,
                                Generates generates
    ) {
        super(locationOffset, frequencyReductionMethod, frequency, salt, exclusionZone, spacing, separation, spreadType);
        this.spacing = (int)Math.round(spacing * 1.65);
        this.separation = (int)Math.round(separation * 1.65);
        this.spreadType = spreadType;
        this.minDistanceFromWorldOrigin = minDistanceFromWorldOrigin;
        this.superExclusionZone = superExclusionZone;
        this.spacingKey = spacingKey;
        this.structureId = structureId;
        this.structureIdRL = structureId.map(ResourceLocation::tryParse).orElse(null);
        this.whenReplacing = whenReplacing.map(w -> new ReplacementSpacing(w.modid(), w.vanillaKey(),
                w.spacing().map(s -> (int)Math.round(s * 1.65)), w.separation().map(s -> (int)Math.round(s * 1.65))));
        this.generates = generates;

        if (spacing <= separation) {
            throw new RuntimeException("""
                Moog's Structure Lib: Spacing cannot be less or equal to separation.
                Please correct this error as there's no way to spawn this structure properly
                    Spacing: %s
                    Separation: %s.
            """.formatted(spacing, separation));
        }

        if (whenReplacing.isPresent() && whenReplacing.get().spacing().isPresent()
                && whenReplacing.get().spacing().get() <= whenReplacing.get().separation().get()) {
            throw new RuntimeException("""
                Moog's Structure Lib: when_replacing spacing cannot be less or equal to separation.
                Please correct this error as there's no way to spawn this structure properly
                    Spacing: %s
                    Separation: %s.
            """.formatted(whenReplacing.get().spacing().get(), whenReplacing.get().separation().get()));
        }
    }

    /** generates other than always reads the state of the replacement when_replacing names, so it needs one. */
    private static DataResult<AdvancedRandomSpread> followsAReplacement(AdvancedRandomSpread spread) {
        if (spread.generates != Generates.ALWAYS && spread.whenReplacing.isEmpty()) {
            return DataResult.error(() -> "generates \"" + spread.generates.getSerializedName()
                    + "\" needs a when_replacing naming the replacement it follows");
        }
        return DataResult.success(spread);
    }

    private String effectiveSpacingKey() {
        return spacingKey.orElse(this.owningSetId);
    }

    private boolean isReplacing() {
        return this.whenReplacing.isPresent()
                && ReplaceVanillaManager.isEnabled(this.whenReplacing.get().modid(), this.whenReplacing.get().vanillaKey());
    }

    private Memo memo() {
        int gen = MslConfig.get().spacingGeneration();
        // Part of the memo key, not just its value: an in-game preset toggle swaps the base pair
        // without bumping the generation counter.
        boolean replacing = isReplacing();
        Memo m = this.memo;
        if (m != null && m.generation() == gen && m.replacing() == replacing) return m;
        boolean swap = replacing && this.whenReplacing.get().spacing().isPresent();
        int baseSpacing = swap ? this.whenReplacing.get().spacing().get() : this.spacing;
        int baseSeparation = swap ? this.whenReplacing.get().separation().get() : this.separation;
        double mult = MslConfig.get().getEffectiveSpacingMultiplier(effectiveSpacingKey());
        int es = Math.max(1, (int) Math.round(baseSpacing * mult));
        int esep = (int) Math.round(baseSeparation * mult);
        if (esep >= es) esep = es - 1;   // keep spacing > separation so the grid diff stays >= 1
        Memo nm = new Memo(gen, replacing, es, esep);
        this.memo = nm;
        return nm;
    }

    /** Resets the memo so the next read recomputes with the stamped key. */
    public void setOwningSetId(ResourceLocation setId) {
        this.owningSetIdRL = setId;
        this.owningSetId = setId.toString();
        this.memo = null;
    }

    /** Stamped at world load: whether none of this set's structures generates on this Minecraft version. */
    public void setNoStructureOnThisVersion(boolean noStructureOnThisVersion) {
        this.noStructureOnThisVersion = noStructureOnThisVersion;
    }

    private ResourceLocation effectiveDisableId() {
        return structureIdRL != null ? structureIdRL : this.owningSetIdRL;
    }

    /**
     * Whether this set starts no structure anywhere: turned off in the config, none of its structures generates on
     * this Minecraft version, or generates keeps it off for the current state of its replacement. Generation,
     * structure searches and other sets' exclusion zones all ask it.
     */
    public boolean isDisabled() {
        if (this.noStructureOnThisVersion || isOffForReplacement()) {
            return true;
        }
        ResourceLocation disableId = effectiveDisableId();
        return disableId != null && MslConfig.get().isStructureDisabled(disableId);
    }

    /**
     * Whether generates keeps this set off now: the preset holding its replacement is off and the set generates only
     * when replacing, or it is on and the set generates only unless replacing. Read live, so toggling the preset in
     * game applies to the next chunks.
     */
    public boolean isOffForReplacement() {
        return switch (this.generates) {
            case ALWAYS -> false;
            case WHEN_REPLACING -> !isReplacing();
            case UNLESS_REPLACING -> isReplacing();
        };
    }

    public Generates generates() {
        return this.generates;
    }

    public Optional<ReplacementSpacing> whenReplacing() {
        return this.whenReplacing;
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
        // Before 1.20.6 the game has no applyAdditionalChunkRestrictions to ask, so the gate is here too.
        if (isDisabled()) {
            return false;
        }
        if (!super.isStructureChunk(chunkGeneratorStructureState, i, j)) {
            return false;
        }
        else {
            return this.superExclusionZone.isEmpty() || !this.superExclusionZone.get().isPlacementForbidden(chunkGeneratorStructureState, i, j);
        }
    }

    //? if >=1.20.6 {
    /**
     * The game asks this before it tries a chunk for a structure search (/locate, explorer maps, dolphins, eyes of
     * ender, other mods' searches), after looking for a start already there and before the structure lays itself
     * out or the chunk is loaded. A disabled set says no here, so a search for its structures costs nothing per
     * chunk instead of a layout and a chunk load that can only find nothing. A start made before the set was
     * turned off is still found. Generation asks it too, through isStructureChunk.
     */
    @Override
    public boolean applyAdditionalChunkRestrictions(int x, int z, long seed) {
        return !isDisabled() && super.applyAdditionalChunkRestrictions(x, z, seed);
    }
    //?}

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
        //? if <26.1.2 {
        return chunkpos.x == x && chunkpos.z == z;
        //?} else {
        /*return chunkpos.x() == x && chunkpos.z() == z;
        *///?}
    }

    @Override
    //? if <26.3 {
    public StructurePlacementType<?> type() {
        return MoogsStructuresStructurePlacementType.ADVANCED_RANDOM_SPREAD.get();
    //?} else {
    /*@SuppressWarnings({"unchecked", "rawtypes"})
    public MapCodec<RandomSpreadStructurePlacement> codec() {
        return (MapCodec) CODEC;
    *///?}
    }

    /**
     * The replacement a set follows, named by the mod that declares it and its vanilla_key, with the spacing pair
     * (both or neither) the set uses instead of its own while that replacement's preset is on.
     */
    public record ReplacementSpacing(String modid, String vanillaKey, Optional<Integer> spacing, Optional<Integer> separation) {
        public static final Codec<ReplacementSpacing> CODEC = RecordCodecBuilder.<ReplacementSpacing>create(builder -> builder.group(
                Codec.STRING.fieldOf("modid").forGetter(ReplacementSpacing::modid),
                Codec.STRING.fieldOf("vanilla_key").forGetter(ReplacementSpacing::vanillaKey),
                StrictOptionalField.of(Codec.intRange(0, Integer.MAX_VALUE), "spacing").forGetter(ReplacementSpacing::spacing),
                StrictOptionalField.of(Codec.intRange(0, Integer.MAX_VALUE), "separation").forGetter(ReplacementSpacing::separation)
        ).apply(builder, ReplacementSpacing::new)).flatXmap(ReplacementSpacing::bothOrNeither, DataResult::success);

        private static DataResult<ReplacementSpacing> bothOrNeither(ReplacementSpacing spacing) {
            if (spacing.spacing().isPresent() != spacing.separation().isPresent()) {
                return DataResult.error(() -> "when_replacing needs both spacing and separation, or neither");
            }
            return DataResult.success(spacing);
        }
    }

    /** When a set generates, by the state of the preset holding the replacement its when_replacing names. */
    public enum Generates implements StringRepresentable {
        ALWAYS("always"),
        WHEN_REPLACING("when_replacing"),
        UNLESS_REPLACING("unless_replacing");

        public static final Codec<Generates> CODEC = StringRepresentable.fromEnum(Generates::values);

        private final String name;

        Generates(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public record SuperExclusionZone(HolderSet<StructureSet> otherSet, int chunkCount, Optional<Integer> allowedChunkCount) {
        //? if >=1.21.1 {
        private static final ThreadLocal<Set<ResourceLocation>> EVALUATING_SETS = ThreadLocal.withInitial(HashSet::new);

        //?}
        public static final Codec<SuperExclusionZone> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                //? if <26.3 {
                RegistryCodecs.homogeneousList(Registries.STRUCTURE_SET, StructureSet.DIRECT_CODEC).fieldOf("other_set").forGetter(SuperExclusionZone::otherSet),
                //?} else {
                /*RegistryCodecs.holderSet(Registries.STRUCTURE_SET, StructureSet.DIRECT_CODEC).fieldOf("other_set").forGetter(SuperExclusionZone::otherSet),
                *///?}
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("chunk_count").forGetter(SuperExclusionZone::chunkCount),
                Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("allowed_chunk_count").forGetter(SuperExclusionZone::allowedChunkCount)
        ).apply(builder, SuperExclusionZone::new));

        boolean isPlacementForbidden(ChunkGeneratorStructureState chunkGeneratorStructureState, int l, int j) {
            //? if >=1.21.1 {
            Set<ResourceLocation> evaluating = EVALUATING_SETS.get();

            //?}
            for (Holder<StructureSet> holder : this.otherSet) {
                //? if >=1.21.1 <1.21.11 {
                ResourceLocation setId = holder.unwrapKey().map(key -> key.location()).orElse(null);
                //?}
                //? if >=1.21.11 {
                /*ResourceLocation setId = holder.unwrapKey().map(key -> key.identifier()).orElse(null);
                *///?}
                //? if >=1.21.1 {
                if (setId == null) continue;
                if (evaluating.contains(setId)) continue;
                evaluating.add(setId);
                try {
                    if (chunkGeneratorStructureState.hasStructureChunkInRange(holder, l, j, this.chunkCount)) {
                        return true;
                    }
                } finally {
                    evaluating.remove(setId);
                //?} else {
                /*if (chunkGeneratorStructureState.hasStructureChunkInRange(holder, l, j, this.chunkCount)) {
                    return true;
                *///?}
                }
            }

            if (this.allowedChunkCount.isPresent() && this.allowedChunkCount.get() > this.chunkCount) {
                boolean isAnyInRange = false;
                for (Holder<StructureSet> holder : this.otherSet) {
                    //? if >=1.21.1 <1.21.11 {
                    ResourceLocation setId = holder.unwrapKey().map(key -> key.location()).orElse(null);
                    //?}
                    //? if >=1.21.11 {
                    /*ResourceLocation setId = holder.unwrapKey().map(key -> key.identifier()).orElse(null);
                    *///?}
                    //? if >=1.21.1 {
                    if (setId == null) continue;
                    if (evaluating.contains(setId)) continue;
                    evaluating.add(setId);
                    try {
                        if (chunkGeneratorStructureState.hasStructureChunkInRange(holder, l, j, this.allowedChunkCount.get())) {
                            isAnyInRange = true;
                        }
                    } finally {
                        evaluating.remove(setId);
                    //?} else {
                    /*if (chunkGeneratorStructureState.hasStructureChunkInRange(holder, l, j, this.allowedChunkCount.get())) {
                        isAnyInRange = true;
                    *///?}
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