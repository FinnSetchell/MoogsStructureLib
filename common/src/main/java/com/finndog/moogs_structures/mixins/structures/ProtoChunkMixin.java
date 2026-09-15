package com.finndog.moogs_structures.mixins.structures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProtoChunk.class)
public class ProtoChunkMixin {
    // Saving a merchant rolls its trades, and an explorer map trade would then search for a structure from the worldgen thread.
    @Inject(
            method = "addEntity(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(value = "HEAD")
    )
    private void moogs_structures_deferTradeRolls(Entity entity, CallbackInfo ci) {
        entity.getSelfAndPassengers().forEach(rider -> {
            if (rider instanceof AbstractVillager villager) {
                AbstractVillagerAccessor accessor = (AbstractVillagerAccessor) villager;
                if (accessor.moogs_structures_getOffers() == null) {
                    accessor.moogs_structures_setOffers(new MerchantOffers());
                }
            }
        });
    }
}
