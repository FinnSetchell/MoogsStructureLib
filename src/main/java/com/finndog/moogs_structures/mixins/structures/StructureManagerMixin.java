package com.finndog.moogs_structures.mixins.structures;

import com.finndog.moogs_structures.config.ReplaceVanillaManager;
//? if >=1.20.6 {
import com.finndog.moogs_structures.replacement.AliasedStructurePredicate;
//?}
import com.finndog.moogs_structures.replacement.ReplacementAliases;
import net.minecraft.core.BlockPos;
//? if >=1.20.6 {
import net.minecraft.core.Holder;
//?}
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

import java.util.ArrayList;
import java.util.List;
//? if >=1.20.6 {
import java.util.function.Predicate;
//?}

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
        for (Structure replacement : moogs_structures_aliasesFor(self, structure, cir.getReturnValue())) {
            // Safe to re-enter: the replacement's own id has no alias, so the second call runs vanilla.
            StructureStart start = self.getStructureAt(blockPos, replacement);
            if (start.isValid()) {
                cir.setReturnValue(start);
                return;
            }
        }
    }

    @Inject(
            //? if <26.3 {
            method = "getStructureWithPieceAt(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/Structure;)Lnet/minecraft/world/level/levelgen/structure/StructureStart;",
            //?} else {
            /*method = "getStructureWithPieceAt(IIILnet/minecraft/world/level/levelgen/structure/Structure;)Lnet/minecraft/world/level/levelgen/structure/StructureStart;",
            *///?}
            at = @At("RETURN"),
            cancellable = true
    )
    //? if <26.3 {
    private void moogs_structures_aliasStructureWithPieceAt(BlockPos blockPos, Structure structure, CallbackInfoReturnable<StructureStart> cir) {
    //?} else {
    /*private void moogs_structures_aliasStructureWithPieceAt(int x, int y, int z, Structure structure, CallbackInfoReturnable<StructureStart> cir) {
    *///?}
        StructureManager self = (StructureManager) (Object) this;
        for (Structure replacement : moogs_structures_aliasesFor(self, structure, cir.getReturnValue())) {
            //? if <26.3 {
            StructureStart start = self.getStructureWithPieceAt(blockPos, replacement);
            //?} else {
            /*StructureStart start = self.getStructureWithPieceAt(x, y, z, replacement);
            *///?}
            if (start.isValid()) {
                cir.setReturnValue(start);
                return;
            }
        }
    }

    /**
     * The tag and holder-set overloads both narrow to this one, so location predicates never reach
     * the single-structure aliasing above.
     */
    //? if >=1.20.6 {
    @Inject(
            method = "getStructureWithPieceAt(Lnet/minecraft/core/BlockPos;Ljava/util/function/Predicate;)Lnet/minecraft/world/level/levelgen/structure/StructureStart;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void moogs_structures_aliasStructureWithPieceMatching(BlockPos blockPos, Predicate<Holder<Structure>> predicate, CallbackInfoReturnable<StructureStart> cir) {
        if (!ReplacementAliases.hasAny() || predicate instanceof AliasedStructurePredicate) return;

        StructureManager self = (StructureManager) (Object) this;
    //?}
        //? if >=1.20.6 <1.21.2 {
        Registry<Structure> registry = self.registryAccess().registryOrThrow(Registries.STRUCTURE);
        //?}
        //? if >=1.21.2 {
        /*Registry<Structure> registry = self.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        *///?}
        //? if >=1.20.6 {
        cir.setReturnValue(self.getStructureWithPieceAt(blockPos, new AliasedStructurePredicate(predicate, registry)));
    }

        //?}
    /** The structures standing in for the one asked about, in a fixed order, when it was not found itself. */
    private static List<Structure> moogs_structures_aliasesFor(StructureManager manager, Structure asked, StructureStart found) {
        if (!ReplacementAliases.hasAny()) return List.of();
        if (found != null && found.isValid()) return List.of();

        //? if <1.21.2 {
        Registry<Structure> registry = manager.registryAccess().registryOrThrow(Registries.STRUCTURE);
        //?} else {
        /*Registry<Structure> registry = manager.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        *///?}
        ResourceLocation askedId = registry.getKey(asked);
        if (askedId == null) return List.of();

        List<Structure> replacements = new ArrayList<>();
        for (ReplaceVanillaManager.Replacement replacement : ReplacementAliases.forVanilla(askedId)) {
            if (!replacement.options().aliasLookups()) continue;
            //? if <1.21.2 {
            Structure structure = registry.get(replacement.replacementStructure());
            //?} else {
            /*Structure structure = registry.getValue(replacement.replacementStructure());
            *///?}
            if (structure != null) replacements.add(structure);
        }
        return replacements;
    }
}
