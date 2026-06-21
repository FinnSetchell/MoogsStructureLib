package com.finndog.moogs_structures.mixins.structures;

import com.finndog.moogs_structures.utils.DebugFlags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Debug aid: when {@link DebugFlags#isKeepJigsawBlocks()} is on, jigsaw blocks are left
 * intact in placed structures instead of being converted to their final_state. This lets
 * structure designers inspect each piece's jigsaw name/target/pool wiring directly in-world.
 *
 * JigsawReplacementProcessor.INSTANCE is the single chokepoint through which both vanilla
 * {@code minecraft:single_pool_element} and Moog's custom pool elements convert jigsaw blocks,
 * so intercepting it here covers every element type at once.
 */
@Mixin(JigsawReplacementProcessor.class)
public class JigsawReplacementProcessorMixin {

    @Inject(method = "processBlock", at = @At("HEAD"), cancellable = true)
    private void moogs_structures_keepJigsawBlocks(LevelReader level,
                                                   BlockPos targetPosition,
                                                   BlockPos referencePos,
                                                   BlockPos templateRelativePos,
                                                   StructureTemplate.StructureBlockInfo processedBlockInfo,
                                                   StructurePlaceSettings settings,
                                                   CallbackInfoReturnable<StructureTemplate.StructureBlockInfo> cir) {
        if (DebugFlags.isKeepJigsawBlocks()) {
            // Return the block unchanged so the jigsaw block survives into the world.
            cir.setReturnValue(processedBlockInfo);
        }
    }
}
