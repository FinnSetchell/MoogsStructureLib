package com.finndog.moogs_structures.world.structures.pieces;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.modinit.MoogsStructuresStructurePieces;
import com.finndog.moogs_structures.utils.VersionResolver;
import com.finndog.moogs_structures.utils.VersionResolver.VersionEntry;
import com.finndog.moogs_structures.utils.VersionResolver.VersionNumber;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A {@link SinglePoolElement} that can resolve different structure templates based
 * on the running Minecraft version.
 */
public class VersionAwareSinglePoolElement extends SinglePoolElement {

    private static final Codec<List<VersionEntry>> VERSION_ENTRIES_CODEC =
            Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC)
                    .flatXmap(VersionResolver::parseVersionMap, VersionResolver::encodeVersionEntries);

    public static final MapCodec<VersionAwareSinglePoolElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.optionalFieldOf("location").forGetter(VersionAwareSinglePoolElement::singleLocation),
                    VERSION_ENTRIES_CODEC.optionalFieldOf("locations").forGetter(VersionAwareSinglePoolElement::versionEntriesOptional),
                    processorsCodec(),
                    projectionCodec(),
                    overrideLiquidSettingsCodec()
            ).apply(instance, VersionAwareSinglePoolElement::new));

    private final Optional<ResourceLocation> singleLocation;
    private final List<VersionEntry> versionEntries;
    private final ResourceLocation defaultLocation;
    private final String versionEntriesDescription;
    private boolean loggedFallback;

    public VersionAwareSinglePoolElement(Optional<ResourceLocation> singleLocation,
                                         Optional<List<VersionEntry>> versionEntries,
                                         Holder<StructureProcessorList> processors,
                                         StructureTemplatePool.Projection projection,
                                         Optional<LiquidSettings> overrideLiquidSettings) {
        super(resolveBaseTemplate(singleLocation, versionEntries), processors, projection, overrideLiquidSettings);
        this.singleLocation = singleLocation;
        this.versionEntries = versionEntries.map(List::copyOf).orElse(List.of());
        this.defaultLocation = singleLocation.orElseGet(() -> this.versionEntries.isEmpty()
                ? null
                : this.versionEntries.get(0).location());
        if (this.defaultLocation == null) {
            throw new IllegalArgumentException("Version-aware single pool element requires at least one template location");
        }
        this.versionEntriesDescription = this.versionEntries.isEmpty()
                ? ""
                : this.versionEntries.stream()
                .map(entry -> entry.rawRange() + "->" + entry.location())
                .collect(Collectors.joining(", "));
    }

    private Optional<List<VersionEntry>> versionEntriesOptional() {
        return this.versionEntries.isEmpty() ? Optional.empty() : Optional.of(this.versionEntries);
    }

    private Optional<ResourceLocation> singleLocation() {
        return this.singleLocation;
    }

    private static Either<ResourceLocation, StructureTemplate> resolveBaseTemplate(Optional<ResourceLocation> singleLocation,
                                                                                   Optional<List<VersionEntry>> versionEntries) {
        Optional<ResourceLocation> fallback = singleLocation;
        if (fallback.isEmpty()) {
            fallback = versionEntries.flatMap(entries -> entries.isEmpty()
                    ? Optional.empty()
                    : Optional.of(entries.get(0).location()));
        }
        return fallback.<Either<ResourceLocation, StructureTemplate>>map(Either::left)
                .orElseThrow(() -> new IllegalArgumentException("Version-aware single pool element requires at least one template location"));
    }

    @Override
    protected StructureTemplate getTemplate(StructureTemplateManager templateManager) {
        VersionNumber current = VersionResolver.getCurrentVersion();
        Optional<VersionEntry> match = VersionResolver.resolve(this.versionEntries, current);

        ResourceLocation target = match
                .map(VersionEntry::location)
                .orElseGet(() -> {
                    if (!this.versionEntries.isEmpty() && !this.loggedFallback) {
                        MoogsStructuresCommon.LOGGER.warn(
                                "Moog's Structure Lib: No version mapping matched runtime version {}. Falling back to template {}. Defined mappings: [{}]",
                                VersionResolver.getCurrentVersionString(),
                                this.singleLocation.orElse(this.defaultLocation),
                                this.versionEntriesDescription);
                        this.loggedFallback = true;
                    }
                    return this.singleLocation.orElse(this.defaultLocation);
                });

        return templateManager.getOrCreate(target);
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return MoogsStructuresStructurePieces.VERSIONED_SINGLE.get();
    }

    @Override
    public String toString() {
        if (this.versionEntriesDescription.isEmpty()) {
            return "VersionAwareSingle[" + this.defaultLocation + "]";
        }
        return "VersionAwareSingle[" + this.defaultLocation + " | " + this.versionEntriesDescription + "]";
    }
}

