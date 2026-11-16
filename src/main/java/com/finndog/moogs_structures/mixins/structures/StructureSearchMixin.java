//? if <1.20.6 {
/*package com.finndog.moogs_structures.mixins.structures;

import com.finndog.moogs_structures.world.structures.placements.AdvancedRandomSpread;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/^*
 * Before 1.20.5 a structure search (/locate, explorer maps, dolphins, eyes of ender) never asks the placement
 * whether it would start anything in a chunk: it asks the structure and then loads the chunk to look. A set
 * turned off in the config, or with no structure on this Minecraft version, then costs a chunk load for every
 * candidate and finds nothing, so the search runs out to its full radius. This gives the answer 1.20.5 and later
 * get from {@link AdvancedRandomSpread#isDisabled()} through applyAdditionalChunkRestrictions: no start there,
 * without loading the chunk. A start already in the world is still found, as it is there.
 ^/
@Mixin(ChunkGenerator.class)
public class StructureSearchMixin {

    @ModifyExpressionValue(
            method = "getStructureGeneratingAt",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/StructureManager;checkStructurePresence(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/levelgen/structure/Structure;Z)Lnet/minecraft/world/level/levelgen/structure/StructureCheckResult;")
    )
    private static StructureCheckResult moogs_structures$skipDisabledSets(StructureCheckResult result, @Local(argsOnly = true) StructurePlacement placement) {
        if (result == StructureCheckResult.CHUNK_LOAD_NEEDED && placement instanceof AdvancedRandomSpread spread && spread.isDisabled()) {
            return StructureCheckResult.START_NOT_PRESENT;
        }
        return result;
    }
}
*///?}
