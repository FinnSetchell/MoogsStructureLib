//? if <1.21.6 {
package com.finndog.moogs_structures.mixins.structures;

import com.finndog.moogs_structures.world.processors.RailRotationProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * While one of MSL's own structures is placing, adds {@link RailRotationProcessor} as the last processor of
 * each template, so its flat rails don't come out sideways when a piece is turned 180 degrees.
 */
@Mixin(StructureTemplate.class)
public class StructureTemplateRailRotationMixin {

    @Inject(method = "placeInWorld", at = @At("HEAD"))
    private void moogs_structures$turnRailsRightInMslStructures(ServerLevelAccessor serverLevelAccessor, BlockPos structurePiecePos, BlockPos structurePiecePivotPos,
                                                              StructurePlaceSettings structurePlaceSettings, RandomSource randomSource, int flags, CallbackInfoReturnable<Boolean> cir) {
        RailRotationProcessor.addWhilePlacingMslStructure(structurePlaceSettings);
    }
}
//?}
