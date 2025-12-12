# Structure Files

Structure files define what your structure is, where it can spawn, and how it behaves. This page covers all available options for structure JSON files.

## Structure Types

Moogs Structure Lib provides two main structure types:

### 1. Generic Jigsaw Structure
**Type:** `moogs_structures:moogs_structures_generic_jigsaw_structure`

Used for overworld structures. Supports all standard features plus advanced terrain checks, burying types, and height restrictions.

### 2. Generic Nether Jigsaw Structure
**Type:** `moogs_structures:moogs_structures_generic_nether_jigsaw_structure`

Designed specifically for Nether structures. Includes special land search algorithms optimized for Nether terrain.

## Required Fields

Every structure JSON must include these fields:

### `type`
**Type:** String  
**Example:** `"moogs_structures:moogs_structures_generic_jigsaw_structure"`

The structure type identifier. Determines which structure system to use.

### `start_pool`
**Type:** Resource Location (String)  
**Example:** `"mvs:carts/cart/start_pool"`

Reference to the template pool that defines which structure pieces to use. Must match a pool defined in `worldgen/template_pool/`.

### `size`
**Type:** Integer (0-30)  
**Example:** `1`

The jigsaw generation size. Controls how many times the structure can expand

**Note:** For simple structures without jigsaw connections, use `1`.

### `start_height`
**Type:** Height Provider Object  
**Example:** `{ "absolute": 0 }`

Defines the starting height for structure placement. Common options:

```json
{ "absolute": 0 }           // Absolute Y coordinate
{ "uniform": { "min_inclusive": 60, "max_inclusive": 80 } }  // Random range
```

The actual placement will be adjusted by `project_start_to_heightmap` if specified.

### `biomes`
**Type:** Biome Tag or Array  
**Example:** `"#mvs:has_structure/overworld_biomes"` or `["minecraft:plains", "minecraft:forest"]`

Which biomes the structure can spawn in. Can be:
- A biome tag: `"#namespace:tag_name"`
- An array of biome IDs: `["minecraft:plains", "minecraft:forest"]`

For more info and examples of biome tags, read [this](https://gist.github.com/TelepathicGrunt/b768ce904baa4598b21c3ca42f137f23).

### `step`
**Type:** String  
**Example:** `"surface_structures"`

The generation step when structures spawn. Possible values:
- `"raw_generation"`
- `"lakes"`
- `"local_modifications"`
- `"underground_structures"`
- `"surface_structures"` - (most common)
- `"strongholds"`
- `"underground_ores"`
- `"underground_decoration"`
- `"vegetal_decoration"`
- `"top_layer_modification"`

### `spawn_overrides`
**Type:** Object  
**Example:** `{}` or see below

Defines custom entity spawning for the structure. Empty object `{}` means no custom spawning.

Example with custom spawning:
```json
{
  "monster": {
    "bounding_box": "full",
    "spawns": [
      {
        "type": "minecraft:skeleton",
        "weight": 1,
        "minCount": 1,
        "maxCount": 2
      }
    ]
  }
}
```

## Optional Fields

### `project_start_to_heightmap`
**Type:** String  
**Example:** `"WORLD_SURFACE_WG"`

Projects the structure start position to a heightmap. Common values:
- `"WORLD_SURFACE_WG"` - World surface (most common)
- `"OCEAN_FLOOR_WG"` - Ocean floor (for underwater structures)
- `"WORLD_SURFACE"` - World surface (non-worldgen)
- `"OCEAN_FLOOR"` - Ocean floor (non-worldgen)
- `"MOTION_BLOCKING"` - Motion blocking blocks
- `"MOTION_BLOCKING_NO_LEAVES"` - Motion blocking excluding leaves

If not specified, uses the `start_height` value directly.

### `cannot_spawn_in_liquid`
**Type:** Boolean  
**Default:** `false`  
**Example:** `true`

Prevents the structure from spawning if the center chunk contains liquid (water/lava).

### `terrain_height_radius_check`
**Type:** Integer (1-100)  
**Example:** `1`

Checks terrain height in a radius around the structure. Used with `allowed_terrain_height_range` or `y_allowance`.

- `1`: Checks only the center chunk
- Higher values: Checks more chunks around the structure

### `allowed_terrain_height_range`
**Type:** Integer (1-1000)  
**Example:** `3`

Maximum allowed height difference in the checked area. If terrain varies more than this, structure won't spawn.

**Example:** With `terrain_height_radius_check: 1` and `allowed_terrain_height_range: 3`, the structure will only spawn if the terrain height in the checked area varies by 3 blocks or less.

### `valid_biome_radius_check`
**Type:** Integer (1-100)  
**Example:** `5`

Checks that all biomes in a radius match the required biome. Ensures structures don't spawn on biome borders.

### `y_allowance`
**Type:** Object  
**Example:** `{ "min_y_allowed": 50, "max_y_allowed": 100 }`

Restricts structure placement to a specific Y range. Useful for structures that must spawn at certain heights.

```json
{
  "min_y_allowed": 50,    // Minimum Y coordinate
  "max_y_allowed": 100    // Maximum Y coordinate
}
```

Both fields are optional. You can specify just `min_y_allowed` or just `max_y_allowed`.

**Note:** `max_y_allowed` cannot be less than `min_y_allowed`.

### `max_distance_from_center`
**Type:** Integer (1-128)  
**Example:** `35`

Maximum distance (in blocks) that jigsaw pieces can extend from the center. Prevents structures from growing too large.

Useful for large jigsaw structures like mineshafts or cathedrals.

### `pools_that_ignore_boundaries`
**Type:** Array of Resource Locations  
**Example:** `["mvs:cathedral/special_pool"]`

List of template pools that can ignore structure boundaries. Allows certain pools to extend beyond normal limits.

### `burying_type`
**Type:** String  
**Example:** `"LOWEST_CORNER"`

How the structure should be buried/positioned relative to terrain. Options:
- `"LOWEST_CORNER"` - Places structure so lowest corner aligns with terrain
- `"AVERAGE_LAND"` - Places structure at average terrain height
- `"LOWEST_SIDE"` - Places structure so lowest side aligns with terrain

Useful for structures that should be partially buried or adapt to terrain.

### `liquid_settings`
**Type:** String  
**Example:** `"ignore_waterlogging"`

How to handle liquids in the structure. Options:
- `"ignore_waterlogging"` - Ignores waterlogging (default)
- Other options depend on Minecraft version

### `terrain_adaptation`
**Type:** String  
**Example:** `"beard_thin"`

How the structure adapts to terrain. Common values:
- `"none"` - No adaptation
- `"beard_thin"` - Thin beard adaptation (most common)
- `"beard_box"` - Box beard adaptation
- `"encapsulate"` - Encapsulation adaptation
- `"bury"` - Bury adaptation

### `dimension_padding`
**Type:** Integer  
**Example:** `10`

Padding around the structure in blocks. Prevents other structures from spawning too close.

## Nether-Specific Fields

These fields are only available for `moogs_structures:moogs_structures_generic_nether_jigsaw_structure`:

### `land_search_direction`
**Type:** String  
**Required for Nether structures  
**Example:** `"HIGHEST_LAND"`

Which direction to search for valid placement:
- `"HIGHEST_LAND"` - Places on highest solid block
- `"LOWEST_LAND"` - Places on lowest solid block

Optimized for Nether's complex terrain.

### `ledge_offset_y`
**Type:** Integer (0-100)  
**Example:** `5`

Y offset when placing on ledges. Adjusts structure position relative to found land.

## Complete Examples

### Simple Structure (Cart)
```json
{
  "type": "moogs_structures:moogs_structures_generic_jigsaw_structure",
  "start_pool": "mvs:carts/cart/start_pool",
  "size": 1,
  "biomes": "#mvs:has_structure/overworld_biomes",
  "project_start_to_heightmap": "WORLD_SURFACE_WG",
  "cannot_spawn_in_liquid": true,
  "step": "surface_structures",
  "terrain_adaptation": "beard_thin",
  "start_height": {
    "absolute": 0
  },
  "spawn_overrides": {}
}
```

### Medium Complexity (Barn)
```json
{
  "type": "moogs_structures:moogs_structures_generic_jigsaw_structure",
  "start_pool": "mvs:houses/barn/start_pool",
  "size": 1,
  "biomes": "#mvs:has_structure/overworld_biomes",
  "project_start_to_heightmap": "WORLD_SURFACE_WG",
  "cannot_spawn_in_liquid": true,
  "terrain_height_radius_check": 1,
  "allowed_terrain_height_range": 3,
  "step": "surface_structures",
  "terrain_adaptation": "beard_thin",
  "start_height": {
    "absolute": 0
  },
  "spawn_overrides": {}
}
```

### Complex Structure (Cathedral)
```json
{
  "type": "moogs_structures:moogs_structures_generic_jigsaw_structure",
  "start_pool": "mvs:cathedral/temp",
  "size": 17,
  "max_distance_from_center": 35,
  "project_start_to_heightmap": "WORLD_SURFACE_WG",
  "cannot_spawn_in_liquid": true,
  "terrain_height_radius_check": 1,
  "allowed_terrain_height_range": 5,
  "liquid_settings": "ignore_waterlogging",
  "biomes": "#mvs:has_structure/overworld_biomes",
  "step": "surface_structures",
  "terrain_adaptation": "beard_thin",
  "start_height": {
    "absolute": 0
  },
  "spawn_overrides": {
    "monster": {
      "bounding_box": "full",
      "spawns": [
        {
          "type": "minecraft:skeleton",
          "weight": 1,
          "minCount": 1,
          "maxCount": 2
        },
        {
          "type": "minecraft:zombie",
          "weight": 2,
          "minCount": 2,
          "maxCount": 5
        }
      ]
    }
  }
}
```

### Nether Structure (Nether Devil)
```json
{
  "type": "moogs_structures:moogs_structures_generic_nether_jigsaw_structure",
  "start_pool": "mvs:other_decoration/nether_devil/start_pool",
  "size": 1,
  "biomes": "#mvs:has_structure/nether_biomes",
  "land_search_direction": "HIGHEST_LAND",
  "project_start_to_heightmap": "WORLD_SURFACE_WG",
  "cannot_spawn_in_liquid": true,
  "terrain_height_radius_check": 2,
  "allowed_terrain_height_range": 1,
  "step": "surface_structures",
  "terrain_adaptation": "beard_thin",
  "start_height": {
    "absolute": 0
  },
  "spawn_overrides": {}
}
```

### Ocean Structure (Ocean Tower)
```json
{
  "type": "moogs_structures:moogs_structures_generic_jigsaw_structure",
  "start_pool": "mvs:ocean_tower_start_pool",
  "size": 1,
  "project_start_to_heightmap": "OCEAN_FLOOR_WG",
  "biomes": "#mvs:has_structure/deep_ocean_biomes",
  "step": "surface_structures",
  "terrain_adaptation": "beard_thin",
  "y_allowance": {
    "max_y_allowed": 25
  },
  "start_height": {
    "absolute": 0
  },
  "spawn_overrides": {}
}
```

### Underground Structure (Mineshaft)
```json
{
  "type": "moogs_structures:moogs_structures_generic_jigsaw_structure",
  "start_pool": "mvs:mineshaft/mineshaft",
  "size": 10,
  "max_distance_from_center": 90,
  "project_start_to_heightmap": "WORLD_SURFACE_WG",
  "cannot_spawn_in_liquid": true,
  "terrain_height_radius_check": 1,
  "allowed_terrain_height_range": 2,
  "liquid_settings": "ignore_waterlogging",
  "dimension_padding": 10,
  "biomes": "#mvs:has_structure/taiga_biomes",
  "step": "underground_structures",
  "terrain_adaptation": "none",
  "start_height": {
    "absolute": 0
  },
  "spawn_overrides": {}
}
```

## Common Patterns

### Surface Structures
Most common pattern for overworld structures:
- `step: "surface_structures"`
- `project_start_to_heightmap: "WORLD_SURFACE_WG"`
- `cannot_spawn_in_liquid: true`
- `terrain_adaptation: "beard_thin"`

### Underwater Structures
For ocean structures:
- `project_start_to_heightmap: "OCEAN_FLOOR_WG"`
- `y_allowance` to restrict depth
- Appropriate biome tags for ocean biomes

### Large Jigsaw Structures
For expanding structures:
- `size: 10` or higher
- `max_distance_from_center` to limit growth
- Multiple template pools for different pieces

### Nether Structures
Always use:
- `type: "moogs_structures:moogs_structures_generic_nether_jigsaw_structure"`
- `land_search_direction: "HIGHEST_LAND"` or `"LOWEST_LAND"`
- Nether biome tags

## Troubleshooting

**Structure not spawning?**
- Check biome tags exist and are correct
- Verify `spacing > separation` in structure set
- Check terrain height restrictions aren't too strict
- Ensure `cannot_spawn_in_liquid` isn't blocking valid locations

**Structure spawning in wrong places?**
- Adjust `terrain_height_radius_check` and `allowed_terrain_height_range`
- Use `valid_biome_radius_check` to avoid biome borders
- Check `y_allowance` restrictions

**Structure too large/small?**
- Adjust `size` for jigsaw expansion
- Use `max_distance_from_center` to limit growth
- Check template pool connections

---

**Next:** Learn about [Template Pools](Template-Pools.md) to understand how structure pieces connect!

