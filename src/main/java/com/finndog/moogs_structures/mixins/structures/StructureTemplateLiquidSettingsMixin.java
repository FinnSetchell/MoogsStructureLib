//? if <1.21.1 {
/*package com.finndog.moogs_structures.mixins.structures;

import com.finndog.moogs_structures.world.structures.GenericJigsawStructure;
import com.finndog.moogs_structures.world.structures.PlacingStructure;
import com.finndog.moogs_structures.world.structures.codecs.LiquidSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/^*
 * Before 1.21 the game has no liquid settings: a template always takes on the water it is placed into. While an
 * MSL structure with {@code liquid_settings: ignore_waterlogging} places its pieces, this turns off keepLiquids,
 * the 1.20 switch that 1.21's IGNORE_WATERLOGGING replaced, so its blocks stay dry as they do from 1.21 on.
 ^/
@Mixin(StructureTemplate.class)
public class StructureTemplateLiquidSettingsMixin {

    @Inject(method = "placeInWorld", at = @At("HEAD"))
    private void moogs_structures$applyLiquidSettings(ServerLevelAccessor serverLevelAccessor, BlockPos structurePiecePos, BlockPos structurePiecePivotPos,
                                                      StructurePlaceSettings structurePlaceSettings, RandomSource randomSource, int flags, CallbackInfoReturnable<Boolean> cir) {
        GenericJigsawStructure structure = PlacingStructure.get();
        if (structure != null && structure.liquidSettings == LiquidSettings.IGNORE_WATERLOGGING) {
            structurePlaceSettings.setKeepLiquids(false);
        }
    }
}
*///?}
