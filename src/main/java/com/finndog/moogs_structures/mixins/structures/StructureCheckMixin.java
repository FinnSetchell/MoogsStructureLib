//? if <1.20.6 {
/*package com.finndog.moogs_structures.mixins.structures;

import com.finndog.moogs_structures.world.structures.placements.AdvancedRandomSpread;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/^*
 * Before 1.20.5: while {@link StructureSearchMixin} has marked a search of a turned-off set, answer "no start" where
 * checkStart would next ask the structure (and cache its answer), that is after the start already in the world, if
 * any, has been looked for. This is where 1.20.5 and later ask the placement's applyAdditionalChunkRestrictions.
 ^/
@Mixin(StructureCheck.class)
public class StructureCheckMixin {

    @Inject(
            method = "checkStart",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/levelgen/structure/StructureCheck;featureChecks:Ljava/util/Map;", opcode = Opcodes.GETFIELD),
            cancellable = true
    )
    private void moogs_structures$skipTurnedOffSets(ChunkPos chunkPos, Structure structure, boolean skipKnown, CallbackInfoReturnable<StructureCheckResult> cir) {
        if (AdvancedRandomSpread.SEARCHING_TURNED_OFF_SET.get() == Boolean.TRUE) {
            cir.setReturnValue(StructureCheckResult.START_NOT_PRESENT);
        }
    }
}
*///?}
