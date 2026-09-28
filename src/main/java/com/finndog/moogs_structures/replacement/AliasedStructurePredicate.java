package com.finndog.moogs_structures.replacement;

import com.finndog.moogs_structures.config.ReplaceVanillaManager;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.function.Predicate;

/**
 * Widens a structure-matching predicate so a replacement also matches wherever the vanilla structure
 * it stands in for would have. Its own type is what tells the aliasing mixin an already-widened
 * predicate has come back round, so it stops after one pass.
 */
public final class AliasedStructurePredicate implements Predicate<Holder<Structure>> {
    private final Predicate<Holder<Structure>> original;
    private final Registry<Structure> registry;

    public AliasedStructurePredicate(Predicate<Holder<Structure>> original, Registry<Structure> registry) {
        this.original = original;
        this.registry = registry;
    }

    @Override
    public boolean test(Holder<Structure> holder) {
        if (this.original.test(holder)) return true;

        ResourceLocation id = holder.unwrapKey().map(ResourceKey::location).orElse(null);
        if (id == null) return false;

        ReplaceVanillaManager.Replacement replacement = ReplacementAliases.forReplacement(id)
                .filter(r -> r.options().aliasLookups())
                .orElse(null);
        if (replacement == null) return false;

        return this.registry.getHolder(ResourceKey.create(Registries.STRUCTURE, replacement.vanillaStructure()))
                .map(vanillaHolder -> this.original.test(vanillaHolder))
                .orElse(false);
    }
}
