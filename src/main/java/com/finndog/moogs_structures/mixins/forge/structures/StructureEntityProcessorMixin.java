package com.finndog.moogs_structures.mixins.forge.structures;

import com.finndog.moogs_structures.world.processors.StructureEntityProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Forge bridge for MSL's entity processors. Forge's structure placement invokes
 * {@code StructureProcessor#processEntity(LevelReader, BlockPos, SEI, SEI, StructurePlaceSettings,
 * StructureTemplate)} on every processor, but MSL's own {@code processEntity} has a different
 * descriptor (an overload, not an override), so without this Forge runs its no-op default and MSL
 * entity processors like {@code equip_armor_stand} never fire. This adds the missing override and
 * forwards to MSL's method. (Fabric does the equivalent via {@code EntityProcessorMixin} on
 * {@code placeEntities}.)
 */
@Mixin(StructureEntityProcessor.class)
public abstract class StructureEntityProcessorMixin {

    public StructureTemplate.StructureEntityInfo processEntity(LevelReader worldReader, BlockPos piecePos,
                                                               StructureTemplate.StructureEntityInfo localEntityInfo,
                                                               StructureTemplate.StructureEntityInfo globalEntityInfo,
                                                               StructurePlaceSettings settings, StructureTemplate template) {
        // Placement always passes a ServerLevelAccessor; guard the cast so an unexpected caller
        // gets the entity back untouched rather than a crash.
        if (!(worldReader instanceof ServerLevelAccessor serverLevelAccessor)) {
            return globalEntityInfo;
        }
        // Forge gives no pivot, so reuse piecePos for the bottom-center arg (equip does not use it).
        return ((StructureEntityProcessor) (Object) this).processEntity(
                serverLevelAccessor, piecePos, piecePos, localEntityInfo, globalEntityInfo, settings);
    }
}
