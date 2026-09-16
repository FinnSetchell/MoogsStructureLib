package com.finndog.moogs_structures.mixins.structures;

import com.finndog.moogs_structures.config.ReplaceVanillaManager;
import com.finndog.moogs_structures.replacement.AliasedStructurePredicate;
import com.finndog.moogs_structures.replacement.ReplacementAliases;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

/**
 * Makes a "is this position inside structure X" question answer yes when X has been replaced and the
 * position is inside the replacement, so vanilla and third-party code keyed on the vanilla structure
 * (fortress mob rules, advancement location predicates) keeps working inside the replacement.
 */
@Mixin(StructureManager.class)
public class StructureManagerMixin {

    @Inject(
            method = "getStructureAt(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/Structure;)Lnet/minecraft/world/level/levelgen/structure/StructureStart;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void moogs_structures_aliasStructureAt(BlockPos blockPos, Structure structure, CallbackInfoReturnable<StructureStart> cir) {
        StructureManager self = (StructureManager) (Object) this;
        Structure replacement = moogs_structures_aliasFor(self, structure, cir.getReturnValue());
        if (replacement == null) return;

        // Safe to re-enter: the replacement's own id has no alias, so the second call runs vanilla.
        StructureStart start = self.getStructureAt(blockPos, replacement);
        if (start.isValid()) cir.setReturnValue(start);
    }

    // 26.3: the single-structure overload takes raw coordinates; the BlockPos one is gone.
    @Inject(
            method = "getStructureWithPieceAt(IIILnet/minecraft/world/level/levelgen/structure/Structure;)Lnet/minecraft/world/level/levelgen/structure/StructureStart;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void moogs_structures_aliasStructureWithPieceAt(int x, int y, int z, Structure structure, CallbackInfoReturnable<StructureStart> cir) {
        StructureManager self = (StructureManager) (Object) this;
        Structure replacement = moogs_structures_aliasFor(self, structure, cir.getReturnValue());
        if (replacement == null) return;

        StructureStart start = self.getStructureWithPieceAt(x, y, z, replacement);
        if (start.isValid()) cir.setReturnValue(start);
    }

    /**
     * The tag and holder-set overloads both narrow to this one, so location predicates never reach
     * the single-structure aliasing above.
     */
    @Inject(
            method = "getStructureWithPieceAt(Lnet/minecraft/core/BlockPos;Ljava/util/function/Predicate;)Lnet/minecraft/world/level/levelgen/structure/StructureStart;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void moogs_structures_aliasStructureWithPieceMatching(BlockPos blockPos, Predicate<Holder<Structure>> predicate, CallbackInfoReturnable<StructureStart> cir) {
        if (!ReplacementAliases.hasAny() || predicate instanceof AliasedStructurePredicate) return;

        StructureManager self = (StructureManager) (Object) this;
        Registry<Structure> registry = self.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        cir.setReturnValue(self.getStructureWithPieceAt(blockPos, new AliasedStructurePredicate(predicate, registry)));
    }

    private static Structure moogs_structures_aliasFor(StructureManager manager, Structure asked, StructureStart found) {
        if (!ReplacementAliases.hasAny()) return null;
        if (found != null && found.isValid()) return null;

        Registry<Structure> registry = manager.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        Identifier askedId = registry.getKey(asked);
        if (askedId == null) return null;

        ReplaceVanillaManager.Replacement replacement = ReplacementAliases.forVanilla(askedId)
                .filter(r -> r.options().aliasLookups())
                .orElse(null);
        return replacement == null ? null : registry.getValue(replacement.replacementStructure());
    }
}
