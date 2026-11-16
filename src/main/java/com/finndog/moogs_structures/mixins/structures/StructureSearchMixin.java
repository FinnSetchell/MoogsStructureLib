//? if <1.20.6 {
/*package com.finndog.moogs_structures.mixins.structures;

import com.finndog.moogs_structures.world.structures.placements.AdvancedRandomSpread;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/^*
 * Before 1.20.5 a structure search (/locate, explorer maps, dolphins, eyes of ender) never asks the placement
 * whether it would start anything in a chunk: it asks the structure, which works out its start, and then loads the
 * chunk to look. A set turned off in the config, or with no structure on this Minecraft version, then costs that for
 * every candidate and finds nothing, so the search runs out to its full radius, tens of thousands of candidates.
 * While such a set's chunk is checked, this marks the search so {@link StructureCheckMixin} answers "no start" where
 * 1.20.5 and later ask {@link AdvancedRandomSpread#isDisabled()} through applyAdditionalChunkRestrictions: after
 * looking for a start already there, before the structure is asked or its answer cached.
 ^/
@Mixin(ChunkGenerator.class)
public class StructureSearchMixin {

    @WrapOperation(
            method = "getStructureGeneratingAt",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/StructureManager;checkStructurePresence(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/levelgen/structure/Structure;Z)Lnet/minecraft/world/level/levelgen/structure/StructureCheckResult;")
    )
    private static StructureCheckResult moogs_structures$markTurnedOffSets(StructureManager structureManager, ChunkPos chunkPos, Structure structure, boolean skipKnown,
                                                                           Operation<StructureCheckResult> original, @Local(argsOnly = true) StructurePlacement placement) {
        if (!(placement instanceof AdvancedRandomSpread spread) || !spread.isDisabled()) {
            return original.call(structureManager, chunkPos, structure, skipKnown);
        }
        AdvancedRandomSpread.SEARCHING_TURNED_OFF_SET.set(Boolean.TRUE);
        try {
            return original.call(structureManager, chunkPos, structure, skipKnown);
        } finally {
            AdvancedRandomSpread.SEARCHING_TURNED_OFF_SET.remove();
        }
    }
}
*///?}
