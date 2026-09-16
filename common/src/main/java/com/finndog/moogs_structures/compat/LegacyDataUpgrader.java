package com.finndog.moogs_structures.compat;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.world.processors.BlockAliasCompatCodec;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rewrites pre-26.3 datapack JSON into the 26.3 shapes as each registry element is read, so a
 * universal 1.21 jar keeps loading here without a per-version branch.
 *
 * Minecraft datafixes world saves but not datapack JSON, and 26.3 changed four things the structure
 * mods ship a lot of:
 * <ul>
 *   <li>block states: {@code {"Name": ..., "Properties": ...}} -> {@code {"id": ..., "properties": ...}}</li>
 *   <li>structure spawn overrides: {@code minCount}/{@code maxCount} -> a {@code count} int provider</li>
 *   <li>loot: the function key {@code function} -> {@code type}, the {@code conditions} list -> one
 *       {@code condition} (wrapped in {@code all_of} when there are several), and inside those
 *       conditions the type key {@code condition} -> {@code type}. A string {@code condition} anywhere
 *       else is a 26.3 predicate reference and is not touched</li>
 *   <li>loot number providers: a bare {@code {"min", "max"}} range now needs {@code "type": "uniform"}</li>
 *   <li>renamed block ids: the old {@code Name} path quietly turned an unknown block into air; the
 *       {@code id} codec rejects it, so {@link BlockAliasCompatCodec}'s renames are applied here for
 *       every processor, not only the pillar processor that wraps its codec</li>
 * </ul>
 * Each rewrite only fires on the old key, so data already in 26.3 form passes through untouched,
 * and each is scoped to the registries where the shape can occur.
 */
public final class LegacyDataUpgrader {
    private static final Set<String> BLOCK_STATE_REGISTRIES = Set.of(
            Registries.PROCESSOR_LIST.identifier().toString(),
            Registries.TEMPLATE_POOL.identifier().toString(),
            Registries.STRUCTURE.identifier().toString());
    private static final Set<String> LOOT_REGISTRIES = Set.of(
            Registries.LOOT_TABLE.identifier().toString(),
            Registries.PREDICATE.identifier().toString(),
            Registries.ITEM_MODIFIER.identifier().toString());
    // Loot fields that take a number provider; a typeless {min,max} object under one of these is the
    // old implicit-uniform form. Kept to these names so IntRange fields (limit_count's "limit") and
    // MinMaxBounds elsewhere are left alone.
    private static final Set<String> NUMBER_FIELDS = Set.of(
            "rolls", "bonus_rolls", "count", "levels", "damage", "amplifier", "duration", "amount", "chance");

    private static final Set<String> REPORTED_NAMESPACES = ConcurrentHashMap.newKeySet();

    private LegacyDataUpgrader() {}

    public static JsonElement upgrade(ResourceKey<?> elementKey, JsonElement json) {
        if (!(json instanceof JsonObject root)) return json;
        String registry = elementKey.registry().toString();
        int changes = 0;

        if (BLOCK_STATE_REGISTRIES.contains(registry)) {
            changes += renameBlockStates(root);
            if (registry.equals(Registries.STRUCTURE.identifier().toString())) {
                changes += upgradeSpawnOverrides(root);
            }
        } else if (LOOT_REGISTRIES.contains(registry)) {
            changes += upgradeLoot(root, registry);
        }

        if (changes > 0) {
            String namespace = elementKey.identifier().getNamespace();
            if (REPORTED_NAMESPACES.add(namespace)) {
                MoogsStructuresCommon.LOGGER.info("Upgrading pre-26.3 data from '{}' on the fly (first: {} {}). It still works, but the mod should ship 26.3-format data.",
                        namespace, registry, elementKey.identifier());
            }
        }
        return json;
    }

    // --- block states -------------------------------------------------------------------------

    private static int renameBlockStates(JsonElement element) {
        int changes = 0;
        if (element instanceof JsonObject obj) {
            if (obj.has("Name") && obj.get("Name").isJsonPrimitive() && !obj.has("id")) {
                obj.add("id", obj.remove("Name"));
                if (obj.has("Properties") && !obj.has("properties")) {
                    obj.add("properties", obj.remove("Properties"));
                }
                changes++;
            }
            if (obj.get("id") instanceof JsonPrimitive id && id.isString()) {
                String renamed = BlockAliasCompatCodec.aliasIfRegistered(id.getAsString());
                if (!renamed.equals(id.getAsString())) {
                    obj.addProperty("id", renamed);
                    changes++;
                }
            }
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                changes += renameBlockStates(entry.getValue());
            }
        } else if (element instanceof JsonArray array) {
            for (JsonElement child : array) changes += renameBlockStates(child);
        }
        return changes;
    }

    // --- structure spawn overrides -------------------------------------------------------------

    private static int upgradeSpawnOverrides(JsonObject structure) {
        if (!(structure.get("spawn_overrides") instanceof JsonObject overrides)) return 0;
        int changes = 0;
        for (Map.Entry<String, JsonElement> category : overrides.entrySet()) {
            if (!(category.getValue() instanceof JsonObject override)) continue;
            if (!(override.get("spawns") instanceof JsonArray spawns)) continue;
            for (JsonElement spawnElement : spawns) {
                if (!(spawnElement instanceof JsonObject spawn) || spawn.has("count")) continue;
                if (!spawn.has("minCount") && !spawn.has("maxCount")) continue;
                int min = spawn.has("minCount") ? spawn.get("minCount").getAsInt() : 1;
                int max = spawn.has("maxCount") ? spawn.get("maxCount").getAsInt() : min;
                spawn.remove("minCount");
                spawn.remove("maxCount");
                spawn.add("count", intProvider(min, max));
                changes++;
            }
        }
        return changes;
    }

    private static JsonElement intProvider(int min, int max) {
        if (min == max) return new JsonPrimitive(min);
        JsonObject uniform = new JsonObject();
        uniform.addProperty("type", "minecraft:uniform");
        uniform.addProperty("min_inclusive", min);
        uniform.addProperty("max_inclusive", max);
        return uniform;
    }

    // --- loot tables, predicates, item modifiers ----------------------------------------------

    private static int upgradeLoot(JsonObject root, String registry) {
        int changes = 0;
        // A predicate file is itself a condition object.
        if (registry.equals(Registries.PREDICATE.identifier().toString())) {
            changes += upgradeCondition(root);
        }
        return changes + upgradeLootTree(root, null);
    }

    private static int upgradeLootTree(JsonElement element, String parentKey) {
        int changes = 0;
        if (element instanceof JsonObject obj) {
            // Loot function: "function": "minecraft:set_count" -> "type": ...
            if (obj.has("function") && obj.get("function").isJsonPrimitive() && !obj.has("type")) {
                obj.add("type", obj.remove("function"));
                changes++;
            }
            // "conditions": [...] -> "condition": {...} (all_of when several). Only the members of
            // that list are legacy condition objects; a string "condition" elsewhere is a 26.3
            // predicate reference and is left alone.
            if (obj.get("conditions") instanceof JsonArray conditions && !obj.has("condition")) {
                obj.remove("conditions");
                for (JsonElement term : conditions) changes += upgradeCondition(term);
                if (conditions.size() == 1) {
                    obj.add("condition", conditions.get(0));
                } else if (conditions.size() > 1) {
                    JsonObject allOf = new JsonObject();
                    allOf.addProperty("type", "minecraft:all_of");
                    allOf.add("terms", conditions);
                    obj.add("condition", allOf);
                }
                changes++;
            }
            // Implicit uniform number provider: {"min": 1, "max": 3} -> add "type": "minecraft:uniform"
            if (parentKey != null && NUMBER_FIELDS.contains(parentKey)
                    && obj.has("min") && obj.has("max") && !obj.has("type")) {
                obj.addProperty("type", "minecraft:uniform");
                changes++;
            }
            // Snapshot the keys: the rewrites above may have replaced entries.
            List<Map.Entry<String, JsonElement>> entries = new ArrayList<>(obj.entrySet());
            for (Map.Entry<String, JsonElement> entry : entries) {
                changes += upgradeLootTree(entry.getValue(), entry.getKey());
            }
        } else if (element instanceof JsonArray array) {
            for (JsonElement child : array) changes += upgradeLootTree(child, parentKey);
        }
        return changes;
    }

    /** A legacy condition object: "condition": "minecraft:random_chance" -> "type": ..., recursing into composites. */
    private static int upgradeCondition(JsonElement element) {
        if (!(element instanceof JsonObject condition)) return 0;
        int changes = 0;
        if (condition.has("condition") && condition.get("condition").isJsonPrimitive() && !condition.has("type")) {
            condition.add("type", condition.remove("condition"));
            changes++;
        }
        if (condition.get("terms") instanceof JsonArray terms) {
            for (JsonElement term : terms) changes += upgradeCondition(term);
        }
        if (condition.has("term")) changes += upgradeCondition(condition.get("term"));
        return changes;
    }
}
