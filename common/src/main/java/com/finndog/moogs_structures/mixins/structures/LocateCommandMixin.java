package com.finndog.moogs_structures.mixins.structures;

import com.finndog.moogs_structures.config.ReplaceVanillaManager;
import com.finndog.moogs_structures.modinit.MoogsStructuresTags;
import com.google.common.base.Stopwatch;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LocateCommand.class)
public class LocateCommandMixin {

    @Final
    @Shadow
    private static DynamicCommandExceptionType ERROR_STRUCTURE_NOT_FOUND;

    /**
     * When a vanilla structure has been replaced by a Moogs one, tell the player instead of
     * running the vanilla search (which has nothing left to find).
     */
    @Inject(
            method = "locateStructure(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/commands/arguments/ResourceOrTagKeyArgument$Result;)I",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void moogs_structures_interceptReplacedLocate(CommandSourceStack source,
                                                                ResourceOrTagKeyArgument.Result<Structure> result,
                                                                CallbackInfoReturnable<Integer> cir) {
        result.unwrap().ifLeft(key -> ReplaceVanillaManager.getActiveReplacement(key.location()).ifPresent(replacement -> {
            ResourceLocation replacementId = replacement.replacementStructure();
            String replacementText = replacementId != null ? replacementId.toString() : "a Moogs structure";
            source.sendSuccess(() -> Component.literal(
                    key.location() + " has been replaced with " + replacementText +
                    ". You can change this in the Moogs Structures config (config/moogs_structures.json)."), false);
            cir.setReturnValue(1);
        }));
    }

    /**
     * Increases the radius that locate command works with
     * @author - TelepathicGrunt
     */
    @Inject(
            method = "locateStructure(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/commands/arguments/ResourceOrTagKeyArgument$Result;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;findNearestMapStructure(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/HolderSet;Lnet/minecraft/core/BlockPos;IZ)Lcom/mojang/datafixers/util/Pair;", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true,
            require = 0
    )
    private static void moogs_structures_increaseLocateRadius(CommandSourceStack commandSourceStack,
                                                                  ResourceOrTagKeyArgument.Result<Structure> result,
                                                                  CallbackInfoReturnable<Integer> cir,
                                                                  Registry<Structure> registry,
                                                                  HolderSet<Structure> holderSet,
                                                                  BlockPos blockPos,
                                                                  ServerLevel serverLevel) throws CommandSyntaxException {
        if(holderSet.stream().anyMatch(configuredStructureFeatureHolder -> configuredStructureFeatureHolder.is(MoogsStructuresTags.LARGER_LOCATE_SEARCH))) {
            int increasedSearchRadius = 2000;
            Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
            Pair<BlockPos, Holder<Structure>> pair = serverLevel.getChunkSource().getGenerator().findNearestMapStructure(serverLevel, holderSet, blockPos, increasedSearchRadius, false);
            stopwatch.stop();
            if (pair == null) {
                throw ERROR_STRUCTURE_NOT_FOUND.create(result.asPrintable());
            }
            else {
                cir.setReturnValue(LocateCommand.showLocateResult(commandSourceStack, result, blockPos, pair, "commands.locate.structure.success", false, stopwatch.elapsed()));
            }
        }
    }
}