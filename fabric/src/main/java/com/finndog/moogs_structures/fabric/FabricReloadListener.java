package com.finndog.moogs_structures.fabric;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;

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



    // 1.21.10 reworked PreparableReloadListener.reload to take a SharedState first (dropping the direct
    // ResourceManager arg); the wrapper must mirror the new signature or it fails to implement the interface.
    @Override
    public CompletableFuture<Void> reload(SharedState sharedState, Executor executor, PreparationBarrier preparationBarrier, Executor executor2) {
        return listener.reload(sharedState, executor, preparationBarrier, executor2);
    }
}
