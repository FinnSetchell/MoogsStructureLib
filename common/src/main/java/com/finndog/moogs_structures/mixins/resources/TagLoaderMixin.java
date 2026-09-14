package com.finndog.moogs_structures.mixins.resources;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.config.ReplaceVanillaManager;
import com.finndog.moogs_structures.config.ReplaceVanillaManager.Replacement;
import com.finndog.moogs_structures.replacement.ReplacementAliases;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adds a replacement structure to every structure tag that lists the vanilla structure it stands in for,
 * so anything keyed on those tags (other mods, biome modifiers, tag-based locate) still matches.
 */
@Mixin(TagLoader.class)
public class TagLoaderMixin {

    @Shadow
    @Final
    private String directory;

    @Shadow
    @Final
    TagLoader.ElementLookup<Object> elementLookup;

    @Inject(method = "build(Ljava/util/Map;)Ljava/util/Map;", at = @At("HEAD"))
    private void moogs_structures_refreshAliases(Map<ResourceLocation, List<TagLoader.EntryWithSource>> entries,
                                                 CallbackInfoReturnable<Map<ResourceLocation, List<Object>>> cir) {
        // Tags are built ahead of the reload listener that re-reads the config, so without this a
        // config edit would only reach the tags on the reload after it.
        if (ReplaceVanillaManager.hasAnyBindings() && moogs_structures_isStructureLoader(this.directory)) {
            ReplaceVanillaManager.reloadConfig();
        }
    }

    @Inject(method = "build(Ljava/util/Map;)Ljava/util/Map;", at = @At("RETURN"))
    private void moogs_structures_mirrorTags(Map<ResourceLocation, List<TagLoader.EntryWithSource>> entries,
                                             CallbackInfoReturnable<Map<ResourceLocation, List<Object>>> cir) {
        if (!ReplacementAliases.hasAny() || !moogs_structures_isStructureLoader(this.directory)) return;

        ReplacementAliases.Snapshot aliases = ReplacementAliases.snapshot();
        for (Map.Entry<ResourceLocation, List<Object>> tag : cir.getReturnValue().entrySet()) {
            List<Object> holders = tag.getValue();
            Map<ResourceLocation, Object> additions = null;

            for (Object holder : holders) {
                ResourceLocation id = ((Holder<?>) holder).unwrapKey().map(ResourceKey::location).orElse(null);
                if (id == null) continue;

                ResourceLocation replacementId = aliases.forVanilla(id)
                        .filter(r -> r.options().mirrorTags())
                        .map(Replacement::replacementStructure)
                        .orElse(null);
                if (replacementId == null) continue;

                Object replacementHolder = this.elementLookup.get(replacementId, false).orElse(null);
                if (replacementHolder == null || holders.contains(replacementHolder)) continue;

                if (additions == null) additions = new LinkedHashMap<>();
                additions.put(replacementId, replacementHolder);
            }

            if (additions == null) continue;
            List<Object> mirrored = new ArrayList<>(holders);
            mirrored.addAll(additions.values());
            tag.setValue(List.copyOf(mirrored));
            MoogsStructuresCommon.LOGGER.debug("Moogs Structures: mirrored {} into structure tag {}", additions.keySet(), tag.getKey());
        }
    }

    private static boolean moogs_structures_isStructureLoader(String directory) {
        return Registries.tagsDirPath(Registries.STRUCTURE).equals(directory);
    }
}
