package com.finndog.moogs_structures.mixins.structures;

import com.finndog.moogs_structures.world.processors.FollowFloorProcessor;
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
 * Tells {@code follow_ceiling_processor} how tall the template it is placing is: a processor only learns
 * where a piece starts, and a hanging piece is measured from its top. Nothing is kept for templates
 * without that processor.
 */
@Mixin(StructureTemplate.class)
public class StructureTemplatePlacingMixin {

    @Inject(method = "placeInWorld", at = @At("HEAD"))
    private void moogs_structures$startPlacing(ServerLevelAccessor serverLevelAccessor, BlockPos structurePiecePos, BlockPos structurePiecePivotPos,
                                               StructurePlaceSettings structurePlaceSettings, RandomSource randomSource, int flags, CallbackInfoReturnable<Boolean> cir) {
        FollowFloorProcessor.startPlacing(structurePlaceSettings, ((StructureTemplate) (Object) this).getSize());
    }

    @Inject(method = "placeInWorld", at = @At("RETURN"))
    private void moogs_structures$stopPlacing(ServerLevelAccessor serverLevelAccessor, BlockPos structurePiecePos, BlockPos structurePiecePivotPos,
                                              StructurePlaceSettings structurePlaceSettings, RandomSource randomSource, int flags, CallbackInfoReturnable<Boolean> cir) {
        FollowFloorProcessor.stopPlacing(structurePlaceSettings);
    }
}
