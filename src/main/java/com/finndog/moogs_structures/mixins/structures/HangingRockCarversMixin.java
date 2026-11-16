package com.finndog.moogs_structures.mixins.structures;

import com.finndog.moogs_structures.world.structures.HangingRockGuard;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
//? if <26.3 {
import net.minecraft.world.level.StructureManager;
//?} else {
/*import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.CarverOutput;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import org.spongepowered.asm.mixin.injection.At;
*///?}
//? if <1.21.2 {
import net.minecraft.world.level.levelgen.GenerationStep;
//?}
import org.spongepowered.asm.mixin.Mixin;

/**
 * Carves each chunk with the {@link HangingRockGuard} of the hanging structures that reach into it, so the carvers leave
 * the rock right above their pieces. A chunk no hanging structure reaches into carves exactly as before.
 * Up to 26.2 the carvers ask the guard themselves ({@code HangingRockWorldCarverMixin}); from 26.3 they write into a
 * carving record, and the blocks the guard keeps are left out of it here.
 */
@Mixin(NoiseBasedChunkGenerator.class)
public class HangingRockCarversMixin {

    //? if <1.21.2 {
    @WrapMethod(method = "applyCarvers")
    private void moogs_structures_guardHangingRock(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager,
                                                   StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step,
                                                   Operation<Void> original) {
        HangingRockGuard.startCarving(HangingRockGuard.of(structureManager, chunk.getPos()));
        try {
            original.call(region, seed, randomState, biomeManager, structureManager, chunk, step);
        } finally {
            HangingRockGuard.stopCarving();
        }
    }
    //?}
    //? if >=1.21.2 <26.3 {
    /*@WrapMethod(method = "applyCarvers")
    private void moogs_structures_guardHangingRock(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager,
                                                   StructureManager structureManager, ChunkAccess chunk, Operation<Void> original) {
        HangingRockGuard.startCarving(HangingRockGuard.of(structureManager, chunk.getPos()));
        try {
            original.call(region, seed, randomState, biomeManager, structureManager, chunk);
        } finally {
            HangingRockGuard.stopCarving();
        }
    }
    *///?}
    //? if >=26.3 {
    /*@WrapMethod(method = "generateCarvers")
    private void moogs_structures_guardHangingRock(ChunkAccess chunk, Blender blender, NoiseChunk noiseChunk, RandomState randomState,
                                                   BiomeManager biomeManager, WorldGenRegion region, MaterialRule materialRule,
                                                   Operation<Void> original) {
        HangingRockGuard.startCarving(HangingRockGuard.of(region, chunk));
        try {
            original.call(chunk, blender, noiseChunk, randomState, biomeManager, region, materialRule);
        } finally {
            HangingRockGuard.stopCarving();
        }
    }

    @WrapOperation(
            method = "generateCarvers",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/carver/WorldCarver;carve(Lnet/minecraft/world/level/levelgen/WorldGenerationContext;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/CarverOutput;)Z")
    )
    private boolean moogs_structures_carveAroundHangingRock(WorldCarver carver, WorldGenerationContext context, RandomSource random,
                                                            ChunkPos pos, ChunkPos source, CarverOutput output, Operation<Boolean> original) {
        HangingRockGuard guard = HangingRockGuard.carving();
        return original.call(carver, context, random, pos, source, guard == null ? output : guard.guarding(output));
    }
    *///?}
}
