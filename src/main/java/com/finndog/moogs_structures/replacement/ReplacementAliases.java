package com.finndog.moogs_structures.replacement;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.config.ReplaceVanillaManager;
import com.finndog.moogs_structures.config.ReplaceVanillaManager.Replacement;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The currently active vanilla-to-replacement bindings, in both directions, as an immutable snapshot
 * published through one volatile field so worldgen and spawning threads never read a half-built table.
 * Rebuilt whenever the config is re-read or a preset is toggled in game.
 * <p>
 * A vanilla structure can have several active replacements when more than one mod replaces it, and a
 * replacement can stand in for several vanilla structures. Each list is in the manager's fixed order,
 * so the answer never depends on the order the mods loaded in.
 */
public final class ReplacementAliases {
    private ReplacementAliases() {}

    /**
     * {@code keptInReplacedTags}: every replacement structure, its preset on or off, that is put back into a
     * structure tag when another pack's {@code "replace": true} file wipes it out (TagLoaderMixin).
     */
    public record Snapshot(Map<ResourceLocation, List<Replacement>> byVanilla, Map<ResourceLocation, List<Replacement>> byReplacement,
                           Set<ResourceLocation> keptInReplacedTags) {
        public static final Snapshot EMPTY = new Snapshot(Map.of(), Map.of(), Set.of());

        public boolean hasAny() {
            return !this.byVanilla.isEmpty();
        }

        public List<Replacement> forVanilla(ResourceLocation vanillaStructure) {
            return this.byVanilla.getOrDefault(vanillaStructure, List.of());
        }

        public List<Replacement> forReplacement(ResourceLocation replacementStructure) {
            return this.byReplacement.getOrDefault(replacementStructure, List.of());
        }
    }

    private static volatile Snapshot current = Snapshot.EMPTY;

    public static void rebuild() {
        List<Replacement> active = ReplaceVanillaManager.getReplacements().stream()
                .filter(ReplaceVanillaManager::isActive)
                .toList();
        // A structure that an active preset replaces never generates, so an alias pointing at it would
        // send searches, mob spawns and tags to nothing: MFR replaces MNS's mega fortress, so the mega
        // fortress must not stand in for the vanilla fortress. Leaving these out also means no alias
        // target has an alias of its own, which the lookups that re-enter with the replacement rely on.
        Set<ResourceLocation> replaced = active.stream().map(Replacement::vanillaStructure).collect(Collectors.toSet());

        Map<ResourceLocation, List<Replacement>> byVanilla = new HashMap<>();
        Map<ResourceLocation, List<Replacement>> byReplacement = new HashMap<>();
        for (Replacement replacement : active) {
            if (replacement.replacementStructure() == null || replaced.contains(replacement.replacementStructure())) continue;
            byVanilla.computeIfAbsent(replacement.vanillaStructure(), k -> new ArrayList<>()).add(replacement);
            byReplacement.computeIfAbsent(replacement.replacementStructure(), k -> new ArrayList<>()).add(replacement);
        }

        // A mod's own tag entries for its replacement structure, kept whether or not the preset is on. Otherwise
        // whether one survives another pack's "replace": true comes down to which mod's data loaded first: MTR
        // adds its stronghold to eye_of_ender_located, and Integrated Stronghold replaces that tag, so eyes of
        // ender found MTR's strongholds on Fabric but not on NeoForge.
        Set<ResourceLocation> kept = ReplaceVanillaManager.getReplacements().stream()
                .filter(replacement -> replacement.options().mirrorTags() && replacement.replacementStructure() != null
                        && !replaced.contains(replacement.replacementStructure()))
                .map(Replacement::replacementStructure)
                .collect(Collectors.toUnmodifiableSet());

        current = new Snapshot(freeze(byVanilla), freeze(byReplacement), kept);

        if (MoogsStructuresCommon.LOGGER.isDebugEnabled()) {
            MoogsStructuresCommon.LOGGER.debug("Moogs Structures: active structure aliases: {}",
                    byVanilla.isEmpty() ? "none" : byVanilla.values().stream().flatMap(List::stream)
                            .map(r -> r.vanillaStructure() + " -> " + r.replacementStructure())
                            .collect(Collectors.joining(", ")));
        }
    }

    private static Map<ResourceLocation, List<Replacement>> freeze(Map<ResourceLocation, List<Replacement>> map) {
        Map<ResourceLocation, List<Replacement>> frozen = new HashMap<>();
        map.forEach((id, list) -> frozen.put(id, List.copyOf(list)));
        return Map.copyOf(frozen);
    }

    public static Snapshot snapshot() {
        return current;
    }

    public static boolean hasAny() {
        return current.hasAny();
    }

    public static List<Replacement> forVanilla(ResourceLocation vanillaStructure) {
        return current.forVanilla(vanillaStructure);
    }

    public static List<Replacement> forReplacement(ResourceLocation replacementStructure) {
        return current.forReplacement(replacementStructure);
    }
}
