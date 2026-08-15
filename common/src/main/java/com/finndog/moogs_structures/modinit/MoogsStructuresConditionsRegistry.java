package com.finndog.moogs_structures.modinit;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.modinit.registry.CustomRegistry;
import com.finndog.moogs_structures.modinit.registry.RegistryEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public final class MoogsStructuresConditionsRegistry {
    private MoogsStructuresConditionsRegistry() {}

    public static final ResourceKey<Registry<Supplier<Boolean>>> MoogsStructures_JSON_CONDITIONS_KEY = ResourceKey.createRegistryKey(new ResourceLocation(MoogsStructuresCommon.MODID, "json_conditions"));
    public static final CustomRegistry<Supplier<Boolean>> MoogsStructures_JSON_CONDITIONS_REGISTRY = CustomRegistry.of(MoogsStructuresCommon.MODID, MoogsStructures_JSON_CONDITIONS_KEY, false, false, true);
    public static final RegistryEntry<Supplier<Boolean>> ALWAYS_TRUE = MoogsStructures_JSON_CONDITIONS_REGISTRY.register("always_true", () -> () -> true);
    public static final RegistryEntry<Supplier<Boolean>> ALWAYS_FALSE = MoogsStructures_JSON_CONDITIONS_REGISTRY.register("always_false", () -> () -> false);

    /**
     * Whether a json_conditions id (e.g. "moogs_structures:always_false") is currently met, for the
     * optional "condition" field on datapack entries. Absent/blank/unparseable, or an id no loaded mod
     * registered, defaults to met - a typo or missing optional dependency never silently deletes
     * content. The value is a live Supplier, so it reflects the config at the moment it is evaluated
     * (for datapack entries that is each reload, since the result is baked in when the pack loads).
     */
    public static boolean isConditionMet(String conditionId) {
        if (conditionId == null || conditionId.isBlank()) return true;
        ResourceLocation id = ResourceLocation.tryParse(conditionId);
        if (id == null) return true;
        Supplier<Boolean> condition = MoogsStructures_JSON_CONDITIONS_REGISTRY.lookup().get(id);
        return condition == null || Boolean.TRUE.equals(condition.get());
    }

    /*
     * This registry is for hooking up the pool_additions json files to a code base config to enable/disable it.
     * Best for direct mod compat where a mod wants to add houses to Repurposed Structures by the pool_additions
     * json files like the many Repurposed Structures datapacks works but want a code config to control it.
     *
     * Add "condition" to the individual entries in the template pool in pool_additions folder and give it the
     * ResourceLocation of the condition you registered. The msl_pieces_spawn_counts folder files can also take
     * a "condition" field for its entries as well.
     *
     * You can register what the condition is to this registry by doing the below in your mod so now your config can control the json files.
     * NOTE: DO THIS CODE ONLY AT MOD INIT. Do not run it when a world is being made! The registry will be frozen after mod init.

     * FABRIC/QUILT:
         BuiltInRegistries.REGISTRY.getOptional(new ResourceLocation("repurposed_structures", "json_conditions"))
             .ifPresent(registry -> Registry.register(
                 (Registry<Supplier<Boolean>>)registry,
                 new ResourceLocation("repurposed_structures", "test"),
                 () -> SomeConfig.EnableJson()));

     * FORGE:
        public static final DeferredRegister<Supplier<Boolean>> RS_CONDITIONS_REGISTRY = DeferredRegister.createOptional(
                new ResourceLocation("repurposed_structures", "json_conditions"), "modid");

        // If the typing here doesn't work, make a helper method that takes a Supplier<Boolean> and returns a Supplier<Boolean>
        public static final RegistryObject<Supplier<Boolean>> CUSTOM_MOD_CONFIG_CONDITION = RS_CONDITIONS_REGISTRY.register(
                "test", () -> () -> SomeConfig.EnableJson());
     */
}
