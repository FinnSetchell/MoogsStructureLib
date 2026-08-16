package com.finndog.moogs_structures.mixins.structures;

import com.finndog.moogs_structures.config.MslConfig;
import com.finndog.moogs_structures.config.ReplaceVanillaManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkGenerator.class)
public class DisableVanillaStructureMixin {
    @Inject(method = "tryGenerateStructure", at = @At("HEAD"), cancellable = true)
    private void moogs_structures_disableReplacedVanilla(
            StructureSet.StructureSelectionEntry entry,
            StructureManager structureManager,
            RegistryAccess registryAccess,
            RandomState randomState,
            StructureTemplateManager structureTemplateManager,
            long seed,
            ChunkAccess chunk,
            ChunkPos chunkPos,
            SectionPos sectionPos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!ReplaceVanillaManager.hasAnyBindings() && !MslConfig.get().hasAnyDisabled()) return;
        entry.structure().unwrapKey().ifPresent(key -> {
            if (ReplaceVanillaManager.shouldCancelVanilla(key.identifier()) || MslConfig.get().isStructureDisabled(key.identifier())) {
                cir.setReturnValue(false);
            }
        });
    }
}
