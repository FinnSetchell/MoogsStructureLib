package com.finndog.moogs_structures.world.structures.pieces;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.modinit.MoogsStructuresStructurePieces;
import com.finndog.moogs_structures.utils.DebugFlags;
import com.finndog.moogs_structures.utils.VersionResolver;
import com.finndog.moogs_structures.utils.VersionResolver.VersionEntry;
import com.finndog.moogs_structures.utils.VersionResolver.VersionNumber;
import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptation;
import com.finndog.moogs_structures.world.structures.terrainadaptation.PoolElementAdaptationOverride;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A {@link SinglePoolElement} that can resolve different structure templates based
 * on the running Minecraft version.
 */
public class VersionAwareSinglePoolElement extends SinglePoolElement implements PoolElementAdaptationOverride {

    private static final Codec<List<VersionEntry>> VERSION_ENTRIES_CODEC =
            Codec.unboundedMap(Codec.STRING, Identifier.CODEC)
                    .flatXmap(VersionResolver::parseVersionMap, VersionResolver::encodeVersionEntries);

    public static final MapCodec<VersionAwareSinglePoolElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Identifier.CODEC.optionalFieldOf("location").forGetter(VersionAwareSinglePoolElement::singleLocation),
                    VERSION_ENTRIES_CODEC.optionalFieldOf("locations").forGetter(VersionAwareSinglePoolElement::versionEntriesOptional),
                    processorsCodec(),
                    projectionCodec(),
                    overrideLiquidSettingsCodec(),
                    EnhancedTerrainAdaptation.CODEC.optionalFieldOf("enhanced_terrain_adaptation")
                            .forGetter(VersionAwareSinglePoolElement::moogs_structures_getAdaptationOverride)
            ).apply(instance, (singleLocation, versionEntries, processors, projection, overrideLiquidSettings, adaptationOverride) ->
                    new VersionAwareSinglePoolElement(
                            singleLocation.orElse(null),
                            versionEntries.map(List::copyOf).orElse(List.of()),
                            processors,
                            projection,
                            overrideLiquidSettings.orElse(null),
                            adaptationOverride
                    )));

    @Nullable
    private final Identifier singleLocation;
    private final List<VersionEntry> versionEntries;
    private final Identifier defaultLocation;
    private final String versionEntriesDescription;
    private final Optional<EnhancedTerrainAdaptation> adaptationOverride;

    private VersionAwareSinglePoolElement(@Nullable Identifier singleLocation,
                                          List<VersionEntry> versionEntries,
                                          Holder<StructureProcessorList> processors,
                                          StructureTemplatePool.Projection projection,
                                          @Nullable LiquidSettings overrideLiquidSettings,
                                          Optional<EnhancedTerrainAdaptation> adaptationOverride) {
        super(Either.left(resolveTargetLocation(singleLocation, versionEntries)),
                processors,
                projection,
                Optional.ofNullable(overrideLiquidSettings));
        this.singleLocation = singleLocation;
        this.versionEntries = List.copyOf(versionEntries);
        Identifier fallback = computeDefaultLocation(singleLocation, this.versionEntries);
        if (fallback == null) {
            throw new IllegalArgumentException("Version-aware single pool element requires at least one template location");
        }
        this.defaultLocation = fallback;
        this.versionEntriesDescription = describeVersionEntries(this.versionEntries);
        this.adaptationOverride = adaptationOverride;
        logFallbackIfNeeded();
    }

    @Nullable
    private static Identifier computeDefaultLocation(@Nullable Identifier singleLocation, List<VersionEntry> entries) {
        if (singleLocation != null) {
            return singleLocation;
        }
        return entries.stream()
                .findFirst()
                .map(VersionEntry::location)
                .orElse(null);
    }

    private static Identifier resolveTargetLocation(@Nullable Identifier singleLocation,
                                                          List<VersionEntry> entries) {
        Identifier fallback = computeDefaultLocation(singleLocation, entries);
        if (fallback == null) {
            throw new IllegalArgumentException("Version-aware single pool element requires at least one template location");
        }
        VersionNumber current = VersionResolver.getCurrentVersion();
        Identifier target = VersionResolver.resolve(entries, current)
                .map(VersionEntry::location)
                .orElse(fallback);
        if (DebugFlags.isEnabled()) {
            MoogsStructuresCommon.LOGGER.info(
                    "Moog's Structure Lib: Version-aware pool element selected template {} (fallback: {}, mappings: [{}])",
                    target,
                    fallback,
                    entries.stream()
                            .map(entry -> entry.rawRange() + "->" + entry.location())
                            .collect(Collectors.joining(", "))
            );
        }
        return target;
    }

    private static String describeVersionEntries(List<VersionEntry> entries) {
        if (entries.isEmpty()) {
            return "";
        }
        return entries.stream()
                .map(entry -> entry.rawRange() + "->" + entry.location())
                .collect(Collectors.joining(", "));
    }

    private void logFallbackIfNeeded() {
        if (this.versionEntries.isEmpty()) {
            return;
        }

        VersionNumber current = VersionResolver.getCurrentVersion();
        if (VersionResolver.resolve(this.versionEntries, current).isPresent()) {
            return;
        }

        Identifier fallback = this.template.left().orElse(this.defaultLocation);
        MoogsStructuresCommon.LOGGER.warn(
                "Moog's Structure Lib: No version mapping matched runtime version {}. Falling back to template {}. Defined mappings: [{}]",
                VersionResolver.getCurrentVersionString(),
                fallback,
                this.versionEntriesDescription);
    }

    private Optional<List<VersionEntry>> versionEntriesOptional() {
        return this.versionEntries.isEmpty() ? Optional.empty() : Optional.of(this.versionEntries);
    }

    private Optional<Identifier> singleLocation() {
        return Optional.ofNullable(this.singleLocation);
    }

    @Override
    public Optional<EnhancedTerrainAdaptation> moogs_structures_getAdaptationOverride() {
        return this.adaptationOverride;
    }

    @Override
    public @NotNull StructurePoolElementType<?> getType() {
        return MoogsStructuresStructurePieces.VERSIONED_SINGLE.get();
    }

    @Override
    public @NotNull String toString() {
        Identifier resolved = this.template.left().orElse(this.defaultLocation);
        if (this.versionEntriesDescription.isEmpty()) {
            return "VersionAwareSingle[" + resolved + "]";
        }
        return "VersionAwareSingle[" + resolved + " | " + this.versionEntriesDescription + "]";
    }
}

