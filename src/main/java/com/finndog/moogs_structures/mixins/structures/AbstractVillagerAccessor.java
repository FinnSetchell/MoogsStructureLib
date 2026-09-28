//? if <26.2 {
package com.finndog.moogs_structures.mixins.structures;

//? if <1.21.11 {
import net.minecraft.world.entity.npc.AbstractVillager;
//?} else {
/*import net.minecraft.world.entity.npc.villager.AbstractVillager;
*///?}
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractVillager.class)
public interface AbstractVillagerAccessor {
    @Accessor("offers")
    MerchantOffers moogs_structures_getOffers();

    @Accessor("offers")
    void moogs_structures_setOffers(MerchantOffers offers);
}

//?}