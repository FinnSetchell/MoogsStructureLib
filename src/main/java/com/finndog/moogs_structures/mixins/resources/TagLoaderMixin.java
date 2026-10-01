package com.finndog.moogs_structures.mixins.resources;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.config.ReplaceVanillaManager;
import com.finndog.moogs_structures.config.ReplaceVanillaManager.Replacement;
import com.finndog.moogs_structures.replacement.ReplacementAliases;
//? if <1.21.2 {
import com.google.common.collect.ImmutableSet;
//?}
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
//? if >=1.21.11 {
/*import net.minecraft.resources.ResourceLocation;
*///?}
import net.minecraft.resources.ResourceKey;
//? if <1.21.11 {
import net.minecraft.resources.ResourceLocation;
//?}
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
//? if <1.21.1 {
/*import net.minecraft.tags.TagManager;
*///?}
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
//? if <1.21.2 {
import java.util.Optional;
import java.util.function.Function;
//?}

/**
 * Adds a replacement structure to every structure tag that lists the vanilla structure it stands in for,
 * so anything keyed on those tags (other mods, biome modifiers, tag-based locate) still matches.
 * <p>
 * That includes tags another pack overrides with {@code "replace": true}. The override drops the vanilla
 * structure before the tag is built, so the entries it wipes out are remembered while the tag files load.
 */
@Mixin(TagLoader.class)
public class TagLoaderMixin {

    @Shadow
    @Final
    private String directory;

    @Shadow
    @Final
    //? if <1.21.2 {
    Function<ResourceLocation, Optional<?>> idToValue;
    //?}
    //? if >=1.21.2 <26.1.2 {
    /*TagLoader.ElementLookup<Object> elementLookup;
    *///?}
    //? if >=26.1.2 {
    /*private TagLoader.ElementLookup<Object> elementLookup;
    *///?}

    // Structures that "replace": true files wiped out of a tag, first keyed by the entry list that was
    // cleared, then by tag id once loading has finished. Both are created on first use: Mixin left
    // field initialisers here unset, so a null means nothing was replaced.
    @Unique
    private Map<List<?>, List<ResourceLocation>> moogs_structures_replacedByList;
    @Unique
    private Map<ResourceLocation, List<ResourceLocation>> moogs_structures_replacedByTag;

    @Redirect(method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;)Ljava/util/Map;",
            at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V"))
    private void moogs_structures_rememberReplaced(List<TagLoader.EntryWithSource> entries) {
        if (moogs_structures_isStructureLoader(this.directory)) {
            if (this.moogs_structures_replacedByList == null) this.moogs_structures_replacedByList = new IdentityHashMap<>();
            List<ResourceLocation> replaced = this.moogs_structures_replacedByList.computeIfAbsent(entries, k -> new ArrayList<>());
            for (TagLoader.EntryWithSource entry : entries) {
                TagEntryAccessor tagEntry = (TagEntryAccessor) entry.entry();
                if (!tagEntry.moogs_structures_isTag()) replaced.add(tagEntry.moogs_structures_getId());
            }
        }
        entries.clear();
    }

    @Inject(method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;)Ljava/util/Map;", at = @At("RETURN"))
    private void moogs_structures_keyReplacedByTag(ResourceManager resourceManager,
                                                   CallbackInfoReturnable<Map<ResourceLocation, List<TagLoader.EntryWithSource>>> cir) {
        if (this.moogs_structures_replacedByList == null) return;

        Map<ResourceLocation, List<ResourceLocation>> byTag = new HashMap<>();
        cir.getReturnValue().forEach((tag, entries) -> {
            List<ResourceLocation> replaced = this.moogs_structures_replacedByList.get(entries);
            if (replaced != null && !replaced.isEmpty()) byTag.put(tag, replaced);
        });
        this.moogs_structures_replacedByList = null;
        this.moogs_structures_replacedByTag = byTag;
    }

    @Inject(method = "build(Ljava/util/Map;)Ljava/util/Map;", at = @At("HEAD"))
    private void moogs_structures_refreshAliases(Map<ResourceLocation, List<TagLoader.EntryWithSource>> entries,
                                                 //? if <1.21.2 {
                                                 CallbackInfoReturnable<Map<ResourceLocation, Collection<Object>>> cir) {
        // Tags are built in TagManager's prepare stage, ahead of the reload listener that re-reads the config,
        // so without this a config edit would only reach the tags on the reload after it.
                                                 //?} else {
                                                 /*CallbackInfoReturnable<Map<ResourceLocation, List<Object>>> cir) {
        // Tags are built ahead of the reload listener that re-reads the config, so without this a
        // config edit would only reach the tags on the reload after it.
                                                 *///?}
        if (ReplaceVanillaManager.hasAnyBindings() && moogs_structures_isStructureLoader(this.directory)) {
            ReplaceVanillaManager.reloadConfig();
        }
    }

    @Inject(method = "build(Ljava/util/Map;)Ljava/util/Map;", at = @At("RETURN"))
    private void moogs_structures_mirrorTags(Map<ResourceLocation, List<TagLoader.EntryWithSource>> entries,
                                             //? if <1.21.2 {
                                             CallbackInfoReturnable<Map<ResourceLocation, Collection<Object>>> cir) {
                                             //?} else {
                                             /*CallbackInfoReturnable<Map<ResourceLocation, List<Object>>> cir) {
                                             *///?}
        if (!ReplacementAliases.hasAny() || !moogs_structures_isStructureLoader(this.directory)) return;

        ReplacementAliases.Snapshot aliases = ReplacementAliases.snapshot();
        //? if <1.21.2 {
        for (Map.Entry<ResourceLocation, Collection<Object>> tag : cir.getReturnValue().entrySet()) {
            Collection<Object> holders = tag.getValue();
        //?} else {
        /*for (Map.Entry<ResourceLocation, List<Object>> tag : cir.getReturnValue().entrySet()) {
            List<Object> holders = tag.getValue();
        *///?}
            Map<ResourceLocation, Object> additions = new LinkedHashMap<>();

            for (Object holder : holders) {
                //? if <1.21.11 {
                ResourceLocation id = ((Holder<?>) holder).unwrapKey().map(ResourceKey::location).orElse(null);
                //?} else {
                /*ResourceLocation id = ((Holder<?>) holder).unwrapKey().map(ResourceKey::identifier).orElse(null);
                *///?}
                if (id != null) moogs_structures_addReplacement(aliases, id, holders, additions);
            }

            // A pack that replaces a tag with nothing wants it empty, so those are left alone.
            int beforeReplaced = additions.size();
            List<ResourceLocation> replaced = this.moogs_structures_replacedByTag != null
                    ? this.moogs_structures_replacedByTag.get(tag.getKey()) : null;
            if (replaced != null && !holders.isEmpty()) {
                for (ResourceLocation id : replaced) moogs_structures_addReplacement(aliases, id, holders, additions);
            }
            if (additions.size() > beforeReplaced) {
                MoogsStructuresCommon.LOGGER.info("Moogs Structures: another pack replaced structure tag {}; added {} to it anyway",
                        tag.getKey(), List.copyOf(additions.keySet()).subList(beforeReplaced, additions.size()));
            }

            if (additions.isEmpty()) continue;
            //? if <1.21.2 {
            tag.setValue(ImmutableSet.builder().addAll(holders).addAll(additions.values()).build());
            //?} else {
            /*List<Object> mirrored = new ArrayList<>(holders);
            mirrored.addAll(additions.values());
            tag.setValue(List.copyOf(mirrored));
            *///?}
            MoogsStructuresCommon.LOGGER.debug("Moogs Structures: mirrored {} into structure tag {}", additions.keySet(), tag.getKey());
        }
    }

    private void moogs_structures_addReplacement(ReplacementAliases.Snapshot aliases, ResourceLocation vanillaId,
                                                 Collection<Object> holders, Map<ResourceLocation, Object> additions) {
        ResourceLocation replacementId = aliases.forVanilla(vanillaId)
                .filter(r -> r.options().mirrorTags())
                .map(Replacement::replacementStructure)
                .orElse(null);
        if (replacementId == null || additions.containsKey(replacementId)) return;

        //? if <1.21.2 {
        Object replacementHolder = this.idToValue.apply(replacementId).orElse(null);
        //?} else {
        /*Object replacementHolder = this.elementLookup.get(replacementId, false).orElse(null);
        *///?}
        if (replacementHolder != null && !holders.contains(replacementHolder)) additions.put(replacementId, replacementHolder);
    }

    private static boolean moogs_structures_isStructureLoader(String directory) {
        //? if >=1.21.1 {
        return Registries.tagsDirPath(Registries.STRUCTURE).equals(directory);
        //?} else {
        /*return TagManager.getTagDir(Registries.STRUCTURE).equals(directory);
        *///?}
    }
}
