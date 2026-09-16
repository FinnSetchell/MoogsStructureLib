package com.finndog.moogs_structures.mixins.resources;

import com.finndog.moogs_structures.compat.LegacyDataUpgrader;
import com.google.gson.JsonElement;
import com.mojang.serialization.Decoder;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Every data-driven registry element - worldgen at world load, loot tables and predicates on reload -
 * comes through {@code PendingRegistration.loadFromResource}, which parses the file to a JsonElement
 * and hands it to the codec. Rewriting the element between those two steps is the one place that
 * covers all of them, with the registry key in hand to scope the rewrite.
 */
@Mixin(targets = "net.minecraft.resources.RegistryLoadTask$PendingRegistration")
public class RegistryLoadTaskMixin {

    @ModifyVariable(
            method = "loadFromResource",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/StrictJsonParser;parse(Ljava/io/Reader;)Lcom/google/gson/JsonElement;"),
            ordinal = 0
    )
    private static JsonElement moogs_structures_upgradeLegacyData(JsonElement json, Decoder<?> decoder, RegistryOps<JsonElement> ops,
                                                                  ResourceKey<?> elementKey, Resource resource) {
        return LegacyDataUpgrader.upgrade(elementKey, json);
    }
}
