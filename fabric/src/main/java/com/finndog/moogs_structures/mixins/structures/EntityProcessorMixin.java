package com.finndog.moogs_structures.mixins.structures;

import com.finndog.moogs_structures.world.processors.StructureEntityProcessor;
import com.finndog.moogs_structures.world.processors.StructureProcessingContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Allows {@link StructureEntityProcessor}s to process entities in jigsaw/template structures on Fabric.
 * Vanilla's {@code StructureTemplate.placeEntities} never invokes the {@code processEntity} hook on Fabric,
 * so we hook it ourselves: capture the placement context, then at {@code placeEntities} HEAD run the
 * entity processors and spawn the processed entities (cancelling vanilla placement to avoid duplicates).
 *
 * <p>Only activates when the structure being placed actually uses a {@link StructureEntityProcessor},
 * so other mods' entity placement is unaffected. Ported/adapted from YUNG's API.
 */
@Mixin(StructureTemplate.class)
public class EntityProcessorMixin {
    @Shadow
    @Final
    private List<StructureTemplate.StructureEntityInfo> entityInfoList;

    @Unique
    private static final ThreadLocal<StructureProcessingContext> moogs_structures$context = new ThreadLocal<>();

    @Inject(
            method = "placeInWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;placeEntities(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Mirror;Lnet/minecraft/world/level/block/Rotation;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;Z)V"))
    private void moogs_structures$captureContext(ServerLevelAccessor serverLevelAccessor, BlockPos structurePiecePos, BlockPos structurePiecePivotPos,
                                                 StructurePlaceSettings structurePlaceSettings, RandomSource randomSource, int i, CallbackInfoReturnable<Boolean> cir) {
        moogs_structures$context.set(new StructureProcessingContext(
                serverLevelAccessor,
                structurePlaceSettings,
                structurePiecePos,
                structurePiecePivotPos,
                entityInfoList
        ));
    }

    @Inject(
            method = "placeInWorld",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;placeEntities(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Mirror;Lnet/minecraft/world/level/block/Rotation;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;Z)V"))
    private void moogs_structures$clearContext(ServerLevelAccessor serverLevelAccessor, BlockPos structurePiecePos, BlockPos structurePiecePivotPos,
                                               StructurePlaceSettings structurePlaceSettings, RandomSource randomSource, int i, CallbackInfoReturnable<Boolean> cir) {
        moogs_structures$context.remove();
    }

    @Inject(
            method = "placeEntities",
            at = @At(value = "HEAD"),
            cancellable = true)
    private void moogs_structures$processAndPlaceEntities(ServerLevelAccessor serverLevelAccessor, BlockPos structurePiecePos, Mirror mirror, Rotation rotation, BlockPos pivot, BoundingBox boundingBox, boolean bl, CallbackInfo ci) {
        StructureProcessingContext ctx = moogs_structures$context.get();
        if (ctx == null) {
            return;
        }

        // If the structure isn't using any MSL entity processors, leave vanilla placement alone.
        if (ctx.structurePlaceSettings().getProcessors().stream().noneMatch(p -> p instanceof StructureEntityProcessor)) {
            return;
        }

        List<StructureTemplate.StructureEntityInfo> processedEntities = moogs_structures$processEntityInfoList(ctx);

        for (StructureTemplate.StructureEntityInfo entityInfo : processedEntities) {
            BlockPos entityBlockPos = entityInfo.blockPos;
            if (ctx.structurePlaceSettings().getBoundingBox() == null || ctx.structurePlaceSettings().getBoundingBox().isInside(entityBlockPos)) {
                CompoundTag entityNbt = entityInfo.nbt.copy();
                Vec3 entityPos = entityInfo.pos;
                ListTag listTag = new ListTag();
                listTag.add(DoubleTag.valueOf(entityPos.x));
                listTag.add(DoubleTag.valueOf(entityPos.y));
                listTag.add(DoubleTag.valueOf(entityPos.z));
                entityNbt.put("Pos", listTag);
                entityNbt.remove("UUID");
                moogs_structures$tryCreateEntity(serverLevelAccessor, entityNbt).ifPresent((entity) -> {
                    float f = entity.mirror(ctx.structurePlaceSettings().getMirror());
                    f += entity.getYRot() - entity.rotate(ctx.structurePlaceSettings().getRotation());
                    entity.moveTo(entityPos.x, entityPos.y, entityPos.z, f, entity.getXRot());
                    if (ctx.structurePlaceSettings().shouldFinalizeEntities() && entity instanceof Mob) {
                        ((Mob) entity).finalizeSpawn(serverLevelAccessor, serverLevelAccessor.getCurrentDifficultyAt(BlockPos.containing(entityPos)), MobSpawnType.STRUCTURE, null, null);
                    }
                    serverLevelAccessor.addFreshEntityWithPassengers(entity);
                });
            }
        }

        // Cancel vanilla entity placement to prevent double-spawning.
        ci.cancel();
    }

    @Unique
    private List<StructureTemplate.StructureEntityInfo> moogs_structures$processEntityInfoList(StructureProcessingContext ctx) {
        List<StructureTemplate.StructureEntityInfo> processedEntities = new ArrayList<>();

        ServerLevelAccessor serverLevelAccessor = ctx.serverLevelAccessor();
        BlockPos structurePiecePos = ctx.structurePiecePos();
        BlockPos structurePiecePivotPos = ctx.structurePiecePivotPos();
        StructurePlaceSettings structurePlaceSettings = ctx.structurePlaceSettings();
        List<StructureTemplate.StructureEntityInfo> rawEntityInfos = ctx.rawEntityInfos();

        for (StructureTemplate.StructureEntityInfo rawEntityInfo : rawEntityInfos) {
            Vec3 globalPos = StructureTemplate
                    .transform(rawEntityInfo.pos,
                            structurePlaceSettings.getMirror(),
                            structurePlaceSettings.getRotation(),
                            structurePlaceSettings.getRotationPivot())
                    .add(Vec3.atLowerCornerOf(structurePiecePos));
            BlockPos globalBlockPos = StructureTemplate
                    .transform(rawEntityInfo.blockPos,
                            structurePlaceSettings.getMirror(),
                            structurePlaceSettings.getRotation(),
                            structurePlaceSettings.getRotationPivot())
                    .offset(structurePiecePos);
            StructureTemplate.StructureEntityInfo globalEntityInfo = new StructureTemplate.StructureEntityInfo(globalPos, globalBlockPos, rawEntityInfo.nbt);

            for (StructureProcessor processor : structurePlaceSettings.getProcessors()) {
                if (processor instanceof StructureEntityProcessor) {
                    globalEntityInfo = ((StructureEntityProcessor) processor).processEntity(serverLevelAccessor, structurePiecePos, structurePiecePivotPos, rawEntityInfo, globalEntityInfo, structurePlaceSettings);
                    if (globalEntityInfo == null) break;
                }
            }

            if (globalEntityInfo != null) {
                processedEntities.add(globalEntityInfo);
            }
        }

        return processedEntities;
    }

    @Unique
    private static Optional<Entity> moogs_structures$tryCreateEntity(ServerLevelAccessor serverLevelAccessor, CompoundTag compoundTag) {
        try {
            return EntityType.create(compoundTag, serverLevelAccessor.getLevel());
        } catch (Exception exception) {
            return Optional.empty();
        }
    }
}
