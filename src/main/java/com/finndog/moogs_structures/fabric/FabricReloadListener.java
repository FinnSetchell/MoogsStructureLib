package com.finndog.moogs_structures.fabric;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
//? if <1.21.10 {
import net.minecraft.server.packs.resources.ResourceManager;
//?}
//? if <1.21.2 {
import net.minecraft.util.profiling.ProfilerFiller;
//?}

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FabricReloadListener implements IdentifiableResourceReloadListener {

    private final ResourceLocation id;
    private final PreparableReloadListener listener;

    public FabricReloadListener(ResourceLocation id, PreparableReloadListener listener) {
        this.id = id;
        this.listener = listener;
    }


    @Override
    public ResourceLocation getFabricId() {
        return id;
    }

    @Override
    //? if <1.21.2 {
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, ProfilerFiller profiler, ProfilerFiller profiler2, Executor executor, Executor executor2) {
        return listener.reload(barrier, manager, profiler, profiler2, executor, executor2);
    //?}
    //? if >=1.21.2 <1.21.5 {
    /*public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, Executor executor, Executor executor2) {
        return listener.reload(barrier, manager, executor, executor2);
    *///?}
    //? if >=1.21.5 <1.21.10 {
    /*public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, Executor executor, Executor executor2) {
        return listener.reload(preparationBarrier, resourceManager, executor, executor2);
    *///?}
    //? if >=1.21.10 {
    /*public CompletableFuture<Void> reload(SharedState sharedState, Executor executor, PreparationBarrier preparationBarrier, Executor executor2) {
        return listener.reload(sharedState, executor, preparationBarrier, executor2);
    *///?}
    }
}
