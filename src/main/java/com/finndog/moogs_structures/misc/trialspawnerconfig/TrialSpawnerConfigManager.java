//? if <26.1.2 {
package com.finndog.moogs_structures.misc.trialspawnerconfig;

import com.finndog.moogs_structures.MoogsStructuresCommon;
//? if <1.21.2 {
import com.google.gson.Gson;
//?}
import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
//? if >=1.21.4 {
/*import net.minecraft.resources.FileToIdConverter;
*///?}
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
//? if >=1.21.2 {
/*import net.minecraft.util.ExtraCodecs;
*///?}
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads {@code data/<ns>/trial_spawner/<path>.json} files at datapack reload and exposes them as
 * raw NBT compounds. On MC 1.21.0&ndash;1.21.4 vanilla has no {@code minecraft:trial_spawner}
 * registry, so MSL mirrors it here. {@link com.finndog.moogs_structures.world.processors.TrialSpawnerRandomizingProcessor}
 * resolves a config ResourceLocation to its NBT at placement time and writes it inline into
 * the block entity, since the pre-1.21.5 trial-spawner codec only accepts inline configs.
 */
//? if <1.21.2 {
public class TrialSpawnerConfigManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
//?} else {
/*public class TrialSpawnerConfigManager extends SimpleJsonResourceReloadListener<JsonElement> {
*///?}
    public static final TrialSpawnerConfigManager INSTANCE = new TrialSpawnerConfigManager();

    private Map<ResourceLocation, CompoundTag> configs = new HashMap<>();

    public TrialSpawnerConfigManager() {
        //? if <1.21.2 {
        super(GSON, "trial_spawner");
        //?}
        //? if >=1.21.2 <1.21.4 {
        /*super(ExtraCodecs.JSON, "trial_spawner");
        *///?}
        //? if >=1.21.4 <1.21.5 {
        /*super(ExtraCodecs.JSON, FileToIdConverter.json("trial_spawner"));
        *///?}
        // 1.21.5: SimpleJsonResourceReloadListener takes a FileToIdConverter instead of a String directory.
        //? if >=1.21.5 {
        /*super(ExtraCodecs.JSON, new FileToIdConverter("trial_spawner", ".json"));
        *///?}
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> loader, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, CompoundTag> builder = new HashMap<>();
        loader.forEach((id, json) -> {
            try {
                Tag asNbt = new Dynamic<>(JsonOps.INSTANCE, json).convert(NbtOps.INSTANCE).getValue();
                if (asNbt instanceof CompoundTag compound) {
                    builder.put(id, compound);
                } else {
                    MoogsStructuresCommon.LOGGER.error("Moog's Structure Lib Error: trial_spawner config {} is not a JSON object", id);
                }
            } catch (Exception e) {
                MoogsStructuresCommon.LOGGER.error("Moog's Structure Lib Error: couldn't parse trial_spawner config {} - JSON: {}", id, json, e);
            }
        });
        this.configs = builder;
    }

    @Nullable
    public CompoundTag get(ResourceLocation id) {
        return configs.get(id);
    }
}

//?}