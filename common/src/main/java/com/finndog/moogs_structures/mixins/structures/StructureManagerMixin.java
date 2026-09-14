package com.finndog.moogs_structures.mixins.structures;

import com.finndog.moogs_structures.config.ReplaceVanillaManager;
import com.finndog.moogs_structures.replacement.ReplacementAliases;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(
            method = "getStructureWithPieceAt(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/Structure;)Lnet/minecraft/world/level/levelgen/structure/StructureStart;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void moogs_structures_aliasStructureWithPieceAt(BlockPos blockPos, Structure structure, CallbackInfoReturnable<StructureStart> cir) {
        StructureManager self = (StructureManager) (Object) this;
        Structure replacement = moogs_structures_aliasFor(self, structure, cir.getReturnValue());
        if (replacement == null) return;

        StructureStart start = self.getStructureWithPieceAt(blockPos, replacement);
        if (start.isValid()) cir.setReturnValue(start);
    }

    private static Structure moogs_structures_aliasFor(StructureManager manager, Structure asked, StructureStart found) {
        if (!ReplacementAliases.hasAny()) return null;
        if (found != null && found.isValid()) return null;

        Registry<Structure> registry = manager.registryAccess().registryOrThrow(Registries.STRUCTURE);
        ResourceLocation askedId = registry.getKey(asked);
        if (askedId == null) return null;

        ReplaceVanillaManager.Replacement replacement = ReplacementAliases.forVanilla(askedId)
                .filter(r -> r.options().aliasLookups())
                .orElse(null);
        return replacement == null ? null : registry.get(replacement.replacementStructure());
    }
}
