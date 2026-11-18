package com.finndog.moogs_structures.utils;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Brings mob NBT that a processor writes from JSON up to the running Minecraft's format.
 *
 * <p>A structure file carries the data version it was saved in, and the game upgrades its blocks and mobs when it
 * loads it. NBT that a processor puts into a spawner comes from a datapack's JSON instead, which has no data
 * version, so the game loads it as it is: gear written as {@code ArmorItems} is ignored from 1.21.5, items written
 * with {@code Count} and {@code tag} from 1.20.5, and so on. A processor that declares the version its NBT is
 * written for ({@code "nbt_version": "1.21"}) has each mob run through the game's own upgrade, the same one a mob
 * saved in a structure file goes through, so a mod writes its mobs once, in its oldest version's format.
 */
public final class MobNbtUpgrader {

    /** The Minecraft version mob NBT is written for, and that version's data version. */
    public record NbtVersion(String name, int dataVersion) {}

    /**
     * Each release's data version, read from its own {@code version.json}. A node for a newer Minecraft needs its
     * version added here before a mod can declare it (the running version itself always works).
     */
    private static final Map<String, Integer> DATA_VERSIONS = new LinkedHashMap<>();

    static {
        DATA_VERSIONS.put("1.20", 3463);
        DATA_VERSIONS.put("1.20.1", 3465);
        DATA_VERSIONS.put("1.20.2", 3578);
        DATA_VERSIONS.put("1.20.3", 3698);
        DATA_VERSIONS.put("1.20.4", 3700);
        DATA_VERSIONS.put("1.20.5", 3837);
        DATA_VERSIONS.put("1.20.6", 3839);
        DATA_VERSIONS.put("1.21", 3953);
        DATA_VERSIONS.put("1.21.1", 3955);
        DATA_VERSIONS.put("1.21.2", 4080);
        DATA_VERSIONS.put("1.21.3", 4082);
        DATA_VERSIONS.put("1.21.4", 4189);
        DATA_VERSIONS.put("1.21.5", 4325);
        DATA_VERSIONS.put("1.21.6", 4435);
        DATA_VERSIONS.put("1.21.7", 4438);
        DATA_VERSIONS.put("1.21.8", 4440);
        DATA_VERSIONS.put("1.21.9", 4554);
        DATA_VERSIONS.put("1.21.10", 4556);
        DATA_VERSIONS.put("1.21.11", 4671);
        DATA_VERSIONS.put("26.1", 4786);
        DATA_VERSIONS.put("26.1.1", 4788);
        DATA_VERSIONS.put("26.1.2", 4790);
        DATA_VERSIONS.put("26.2", 4903);
        DATA_VERSIONS.put("26.3", 5023);
    }

    /** {@code "nbt_version"}: a Minecraft version such as {@code "1.20"} or {@code "1.21"}. */
    public static final Codec<NbtVersion> VERSION_CODEC = Codec.STRING.comapFlatMap(MobNbtUpgrader::parse, NbtVersion::name);

    private MobNbtUpgrader() {}

    private static DataResult<NbtVersion> parse(String name) {
        Integer dataVersion = DATA_VERSIONS.get(name);
        if (dataVersion != null) {
            return DataResult.success(new NbtVersion(name, dataVersion));
        }
        if (name.equals(VersionResolver.getCurrentVersionString())) {
            // Already in the running version's format: nothing to upgrade.
            return DataResult.success(new NbtVersion(name, Integer.MAX_VALUE));
        }
        return DataResult.error(() -> "Unknown nbt_version '" + name + "', expected one of " + DATA_VERSIONS.keySet());
    }

    /**
     * The mob (with its passengers) in the running version's format. Returns the mob as it was when it is already
     * in that format, or when the game can't upgrade it (logged).
     */
    public static CompoundTag upgrade(CompoundTag mob, NbtVersion from) {
        if (from.dataVersion() == Integer.MAX_VALUE) {
            return mob;
        }
        // The game upgrades structure files to whatever version is running, which a node built for an earlier
        // patch can't know at compile time, so the mob goes through as the one mob of a structure file.
        CompoundTag holder = new CompoundTag();
        holder.put("nbt", mob);
        ListTag entities = new ListTag();
        entities.add(holder);
        CompoundTag structure = new CompoundTag();
        structure.put("entities", entities);
        try {
            CompoundTag upgraded = DataFixTypes.STRUCTURE.updateToCurrentVersion(DataFixers.getDataFixer(), structure, from.dataVersion());
            if (upgraded.get("entities") instanceof ListTag list && !list.isEmpty()
                    && list.get(0) instanceof CompoundTag upgradedHolder && upgradedHolder.get("nbt") instanceof CompoundTag upgradedMob) {
                return upgradedMob;
            }
            MoogsStructuresCommon.LOGGER.error("Moog's Structure Lib: upgrading mob NBT from {} lost the mob, keeping {}", from.name(), mob);
        } catch (RuntimeException e) {
            MoogsStructuresCommon.LOGGER.error("Moog's Structure Lib: couldn't upgrade mob NBT from {}, keeping {}", from.name(), mob, e);
        }
        return mob;
    }

    /** Upgrades each mob a trial spawner config spawns ({@code spawn_potentials[].data.entity}), in place. */
    public static void upgradeSpawnPotentials(CompoundTag config, NbtVersion from) {
        if (!(config.get("spawn_potentials") instanceof ListTag potentials)) {
            return;
        }
        for (Tag potential : potentials) {
            if (potential instanceof CompoundTag entry && entry.get("data") instanceof CompoundTag data
                    && data.get("entity") instanceof CompoundTag mob) {
                data.put("entity", upgrade(mob, from));
            }
        }
    }
}
