package com.finndog.moogs_structures.world.structures.pieces;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.modinit.MoogsStructuresStructurePieces;
import com.finndog.moogs_structures.utils.DebugFlags;
import com.finndog.moogs_structures.utils.StrictOptionalField;
import com.finndog.moogs_structures.utils.VersionResolver;
import com.finndog.moogs_structures.utils.VersionResolver.VersionEntry;
import com.finndog.moogs_structures.utils.VersionResolver.VersionNumber;
import com.finndog.moogs_structures.world.processors.FollowFloorProcessor;
import com.finndog.moogs_structures.world.structures.terrainadaptation.AdaptationArea;
import com.finndog.moogs_structures.world.structures.terrainadaptation.EnhancedTerrainAdaptation;
import com.finndog.moogs_structures.world.structures.terrainadaptation.PoolElementAdaptationOverride;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
//? if >=1.21.1 {
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
//?}
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.finndog.moogs_structures.world.processors.HangingEntityAnchorProcessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

/**
 * A {@link SinglePoolElement} that can resolve different structure templates based
 * on the running Minecraft version.
 */
public class VersionAwareSinglePoolElement extends SinglePoolElement implements PoolElementAdaptationOverride {

    private static final Codec<List<VersionEntry>> VERSION_ENTRIES_CODEC =
            Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC)
                    .flatXmap(VersionResolver::parseVersionMap, VersionResolver::encodeVersionEntries);

    // A map codec on every version, also once adaptation_area is checked: from 1.20 to 1.20.4 the pool
    // element dispatch only reads the fields beside "element_type" through a map codec, and any other
    // codec makes every versioned element fail to load.
    private static final MapCodec<VersionAwareSinglePoolElement> MAP_CODEC = RecordCodecBuilder.<VersionAwareSinglePoolElement>mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.optionalFieldOf("location").forGetter(VersionAwareSinglePoolElement::singleLocation),
                    VERSION_ENTRIES_CODEC.optionalFieldOf("locations").forGetter(VersionAwareSinglePoolElement::versionEntriesOptional),
                    processorsCodec(),
                    projectionCodec(),
                    //? if >=1.21.1 {
                    overrideLiquidSettingsCodec(),
                    //?}
                    EnhancedTerrainAdaptation.CODEC.optionalFieldOf("enhanced_terrain_adaptation")
                            .forGetter(VersionAwareSinglePoolElement::moogs_structures_getAdaptationOverride),
                    StrictOptionalField.of(AdaptationArea.CODEC, "adaptation_area")
                            .forGetter(VersionAwareSinglePoolElement::moogs_structures_getAdaptationArea)
            //? if >=1.21.1 {
            ).apply(instance, (singleLocation, versionEntries, processors, projection, overrideLiquidSettings, adaptationOverride, adaptationArea) ->
            //?} else {
            /*).apply(instance, (singleLocation, versionEntries, processors, projection, adaptationOverride, adaptationArea) ->
            *///?}
                    new VersionAwareSinglePoolElement(
                            singleLocation.orElse(null),
                            versionEntries.map(List::copyOf).orElse(List.of()),
                            processors,
                            projection,
                            //? if >=1.21.1 {
                            overrideLiquidSettings.orElse(null),
                            //?}
                            adaptationOverride,
                            adaptationArea
                    ))).flatXmap(VersionAwareSinglePoolElement::checkArea, VersionAwareSinglePoolElement::checkArea);

    //? if >=1.20.6 {
    public static final MapCodec<VersionAwareSinglePoolElement> CODEC = MAP_CODEC;
    //?} else {
    /*public static final Codec<VersionAwareSinglePoolElement> CODEC = MAP_CODEC.codec();
    *///?}

    @Nullable
    private final ResourceLocation singleLocation;
    private final List<VersionEntry> versionEntries;
    private final ResourceLocation defaultLocation;
    private final String versionEntriesDescription;
    private final Optional<EnhancedTerrainAdaptation> adaptationOverride;
    private final Optional<AdaptationArea> adaptationArea;

    private VersionAwareSinglePoolElement(@Nullable ResourceLocation singleLocation,
                                          List<VersionEntry> versionEntries,
                                          Holder<StructureProcessorList> processors,
                                          StructureTemplatePool.Projection projection,
                                          //? if >=1.21.1 {
                                          @Nullable LiquidSettings overrideLiquidSettings,
                                          //?}
                                          Optional<EnhancedTerrainAdaptation> adaptationOverride,
                                          Optional<AdaptationArea> adaptationArea) {
        super(Either.left(resolveTargetLocation(singleLocation, versionEntries)),
                processors,
                //? if >=1.21.1 {
                projection,
                Optional.ofNullable(overrideLiquidSettings));
                //?} else {
                /*projection);
                *///?}
        this.singleLocation = singleLocation;
        this.versionEntries = List.copyOf(versionEntries);
        ResourceLocation fallback = computeDefaultLocation(singleLocation, this.versionEntries);
        if (fallback == null) {
            throw new IllegalArgumentException("Version-aware single pool element requires at least one template location");
        }
        this.defaultLocation = fallback;
        this.versionEntriesDescription = describeVersionEntries(this.versionEntries);
        this.adaptationOverride = adaptationOverride;
        this.adaptationArea = adaptationArea;
        logFallbackIfNeeded();
    }

    // Only rigid pieces get a beard, and terrain_matching's own gravity would undo the following columns.
    private static DataResult<VersionAwareSinglePoolElement> checkArea(VersionAwareSinglePoolElement element) {
        if (element.adaptationArea.isPresent() && element.getProjection() != StructureTemplatePool.Projection.RIGID) {
            return DataResult.error(() -> "adaptation_area needs \"projection\": \"rigid\" (" + element + ")");
        }
        return DataResult.success(element);
    }

    @Nullable
    private static ResourceLocation computeDefaultLocation(@Nullable ResourceLocation singleLocation, List<VersionEntry> entries) {
        if (singleLocation != null) {
            return singleLocation;
        }
        return entries.stream()
                .findFirst()
                .map(VersionEntry::location)
                .orElse(null);
    }

    private static ResourceLocation resolveTargetLocation(@Nullable ResourceLocation singleLocation,
                                                          List<VersionEntry> entries) {
        ResourceLocation fallback = computeDefaultLocation(singleLocation, entries);
        if (fallback == null) {
            throw new IllegalArgumentException("Version-aware single pool element requires at least one template location");
        }
        VersionNumber current = VersionResolver.getCurrentVersion();
        ResourceLocation target = VersionResolver.resolve(entries, current)
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

        ResourceLocation fallback = this.template.left().orElse(this.defaultLocation);
        if (DebugFlags.isEnabled()) {
            MoogsStructuresCommon.LOGGER.info(
                    "Moog's Structure Lib: No version mapping matched runtime version {}. Falling back to template {}. Defined mappings: [{}]",
                    VersionResolver.getCurrentVersionString(),
                    fallback,
                    this.versionEntriesDescription);
        }
    }

    private Optional<List<VersionEntry>> versionEntriesOptional() {
        return this.versionEntries.isEmpty() ? Optional.empty() : Optional.of(this.versionEntries);
    }

    private Optional<ResourceLocation> singleLocation() {
        return Optional.ofNullable(this.singleLocation);
    }

    @Override
    public Optional<EnhancedTerrainAdaptation> moogs_structures_getAdaptationOverride() {
        return this.adaptationOverride;
    }

    @Override
    public Optional<AdaptationArea> moogs_structures_getAdaptationArea() {
        return this.adaptationArea;
    }

    // Block-attached entities need their anchor rewritten at placement time; attaching the
    // processor here means structures get the fix without touching their datapacks.
    @Override
    //? if >=1.21.1 {
    protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox boundingBox, LiquidSettings liquidSettings, boolean replaceJigsaw) {
        StructurePlaceSettings settings = super.getSettings(rotation, boundingBox, liquidSettings, replaceJigsaw);
    //?} else {
    /*protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox boundingBox, boolean replaceJigsaw) {
        StructurePlaceSettings settings = super.getSettings(rotation, boundingBox, replaceJigsaw);
    *///?}
        settings.addProcessor(HangingEntityAnchorProcessor.INSTANCE);
        // Columns outside the adaptation area follow the ground. First in the list, as the follow
        // floor processor asks, so the element's own processors see each block where it ends up.
        if (this.adaptationArea.isPresent() && this.adaptationArea.get().outsideFollowsGround()) {
            List<AdaptationArea.ColumnRect> columns = this.adaptationArea.get().localColumns(this, null);
            if (columns != null) {
                settings.getProcessors().add(0, FollowFloorProcessor.outside(columns));
            }
        }
        return settings;
    }

    @Override
    public @NotNull StructurePoolElementType<?> getType() {
        return MoogsStructuresStructurePieces.VERSIONED_SINGLE.get();
    }

    @Override
    public @NotNull String toString() {
        ResourceLocation resolved = this.template.left().orElse(this.defaultLocation);
        if (this.versionEntriesDescription.isEmpty()) {
            return "VersionAwareSingle[" + resolved + "]";
        }
        return "VersionAwareSingle[" + resolved + " | " + this.versionEntriesDescription + "]";
    }
}

