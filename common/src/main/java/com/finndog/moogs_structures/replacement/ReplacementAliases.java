package com.finndog.moogs_structures.replacement;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.config.ReplaceVanillaManager;
import com.finndog.moogs_structures.config.ReplaceVanillaManager.Replacement;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The currently active vanilla-to-replacement bindings, in both directions, as an immutable snapshot
 * published through one volatile field so worldgen and spawning threads never read a half-built table.
 * Rebuilt whenever the config is re-read or a preset is toggled in game.
 */
public final class ReplacementAliases {
    private ReplacementAliases() {}

    public record Snapshot(Map<Identifier, Replacement> byVanilla, Map<Identifier, Replacement> byReplacement) {
        public static final Snapshot EMPTY = new Snapshot(Map.of(), Map.of());

        public boolean hasAny() {
            return !this.byVanilla.isEmpty();
        }

        public Optional<Replacement> forVanilla(Identifier vanillaStructure) {
            return Optional.ofNullable(this.byVanilla.get(vanillaStructure));
        }

        public Optional<Replacement> forReplacement(Identifier replacementStructure) {
            return Optional.ofNullable(this.byReplacement.get(replacementStructure));
        }
    }

    private static volatile Snapshot current = Snapshot.EMPTY;

    public static void rebuild() {
        Map<Identifier, Replacement> byVanilla = new HashMap<>();
        Map<Identifier, Replacement> byReplacement = new HashMap<>();

        for (Replacement replacement : ReplaceVanillaManager.getReplacements()) {
            if (replacement.replacementStructure() == null || !ReplaceVanillaManager.isActive(replacement)) continue;
            byVanilla.put(replacement.vanillaStructure(), replacement);
            byReplacement.put(replacement.replacementStructure(), replacement);
        }

        current = new Snapshot(Map.copyOf(byVanilla), Map.copyOf(byReplacement));

        if (MoogsStructuresCommon.LOGGER.isDebugEnabled()) {
            MoogsStructuresCommon.LOGGER.debug("Moogs Structures: active structure aliases: {}",
                    byVanilla.isEmpty() ? "none" : byVanilla.values().stream()
                            .map(r -> r.vanillaStructure() + " -> " + r.replacementStructure())
                            .collect(Collectors.joining(", ")));
        }
    }

    public static Snapshot snapshot() {
        return current;
    }

    public static boolean hasAny() {
        return current.hasAny();
    }

    public static Optional<Replacement> forVanilla(Identifier vanillaStructure) {
        return current.forVanilla(vanillaStructure);
    }

    public static Optional<Replacement> forReplacement(Identifier replacementStructure) {
        return current.forReplacement(replacementStructure);
    }
}
