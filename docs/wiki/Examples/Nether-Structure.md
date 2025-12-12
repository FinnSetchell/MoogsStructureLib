# Nether Structure Example: Nether Devil

This walkthrough demonstrates creating a Nether-specific structure using the Nether Devil structure from MoogsVoyagerStructures. Nether structures use special placement algorithms optimized for Nether terrain.

## Overview

The Nether Devil structure demonstrates:
- Nether-specific structure type
- Land search direction
- Nether biome configuration
- Terrain height checks for Nether

## File Structure

```
data/mvs/
├── structure/
│   └── other_decoration/
│       └── nether_devil.nbt
└── worldgen/
    ├── structure/
    │   └── nether_devil.json
    ├── structure_set/
    │   └── nether_devil.json
    └── template_pool/
        └── other_decoration/
            └── nether_devil/
                └── start_pool.json
```

## Step 1: Create the NBT File

1. Build your Nether Devil structure in Minecraft
2. Design for Nether terrain (ledges, lava, open spaces)
3. Save as `nether_devil.nbt`
4. Place at: `data/mvs/structure/other_decoration/nether_devil.nbt`

**Design Tips:**
- Nether structures often spawn on ledges or floating platforms
- Account for lava lakes and open spaces
- Consider Nether's vertical nature

## Step 2: Create the Template Pool

**File:** `data/mvs/worldgen/template_pool/other_decoration/nether_devil/start_pool.json`

```json
{
  "name": "mvs:nether_devil/start_pool",
  "fallback": "minecraft:empty",
  "elements": [
    {
      "weight": 1,
      "element": {
        "location": "mvs:other_decoration/nether_devil",
        "processors": "minecraft:empty",
        "projection": "rigid",
        "element_type": "minecraft:single_pool_element"
      }
    }
  ]
}
```

### Explanation

Same as overworld structures - no special requirements for Nether.

## Step 3: Create the Structure JSON

**File:** `data/mvs/worldgen/structure/nether_devil.json`

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

### Key Differences from Overworld Structures

#### Nether Structure Type

```json
{
  "type": "moogs_structures:moogs_structures_generic_nether_jigsaw_structure"
}
```

**Important:** Must use `moogs_structures_generic_nether_jigsaw_structure` for Nether structures!

#### Land Search Direction

```json
{
  "land_search_direction": "HIGHEST_LAND"
}
```

**Options:**
- `"HIGHEST_LAND"` - Places on highest solid block (most common)
- `"LOWEST_LAND"` - Places on lowest solid block

**Why:** Nether terrain is complex with many ledges and platforms. Land search finds the best placement location.

#### Nether Biomes

```json
{
  "biomes": "#mvs:has_structure/nether_biomes"
}
```

**Nether Biome Tag:** `data/mvs/tags/worldgen/biome/has_structure/nether_biomes.json`

```json
{
  "values": [
    "minecraft:nether_wastes",
    "minecraft:crimson_forest",
    "minecraft:warped_forest",
    "minecraft:soul_sand_valley",
    "minecraft:basalt_deltas"
  ]
}
```

#### Terrain Height Checks

```json
{
  "terrain_height_radius_check": 2,
  "allowed_terrain_height_range": 1
}
```

**Nether-specific:**
- Checks 2 chunks radius (Nether is more variable)
- Only 1 block height difference allowed (very strict)
- Prevents spawning on uneven Nether terrain

### Optional: Ledge Offset

You can add a Y offset for ledge placement:

```json
{
  "ledge_offset_y": 5
}
```

Adjusts structure position by 5 blocks when placing on ledges.

### Field-by-Field Explanation

- **`type`**: Nether-specific structure type
- **`start_pool`**: References Nether Devil pool
- **`size`**: `1` - Simple structure
- **`biomes`**: Nether biome tag
- **`land_search_direction`**: `"HIGHEST_LAND"` - Finds highest solid block
- **`terrain_height_radius_check`**: `2` - Check 2 chunks
- **`allowed_terrain_height_range`**: `1` - Very flat terrain only
- Other fields similar to overworld

## Step 4: Create the Structure Set

**File:** `data/mvs/worldgen/structure_set/nether_devil.json`

```json
{
  "structures": [
    {
      "structure": "mvs:nether_devil",
      "weight": 1
    }
  ],
  "placement": {
    "type": "moogs_structures:advanced_random_spread",
    "salt": 513238798,
    "spacing": 64,
    "separation": 16
  }
}
```

### Nether Placement Characteristics

**Spacing:** `64` chunks (~1024 blocks)
- Nether structures often use moderate spacing
- Nether is less crowded than overworld

**Separation:** `16` chunks (~256 blocks)
- Lower separation than overworld
- Nether has more space

**No Exclusion Zone:**
- Nether structures typically don't need exclusion zones
- Less structure density in Nether

### Field-by-Field Explanation

- **`structures`**: Single Nether Devil structure
- **`placement`**: Standard advanced random spread
  - **`salt`**: Unique identifier
  - **`spacing`**: 64 chunks (moderate for Nether)
  - **`separation`**: 16 chunks (lower than overworld)

## How Nether Placement Works

Nether structures use special algorithms:

1. **Land Search** finds highest/lowest solid block
2. **Terrain Check** verifies flat enough terrain
3. **Placement** adjusts for Nether's vertical nature
4. **Structure Spawns** on suitable ledge/platform

### Land Search Process

```
1. Structure attempts to spawn at chunk
2. Land search algorithm scans area:
   - HIGHEST_LAND: Finds highest solid block
   - LOWEST_LAND: Finds lowest solid block
3. Structure placed at found location
4. Ledge offset applied if specified
```

## Testing Your Structure

1. Create a new Nether world with your datapack/mod
2. Use `/locate structure mvs:nether_devil` in Nether
3. Verify:
   - Spawns in Nether biomes only
   - Places on suitable terrain (ledges/platforms)
   - Proper spacing between structures
   - Not in lava

## Common Issues

### Structure Not Spawning in Nether

**Check:**
- Using correct structure type (`moogs_structures_generic_nether_jigsaw_structure`)
- Nether biome tags are correct
- Terrain height restrictions aren't too strict
- Structure set is configured correctly

### Spawning in Wrong Locations

**Solutions:**
- Adjust `land_search_direction` (try LOWEST_LAND)
- Modify `terrain_height_radius_check` and `allowed_terrain_height_range`
- Add `ledge_offset_y` for better positioning

### Too Strict Terrain Checks

**Solution:** Increase `allowed_terrain_height_range`:

```json
{
  "allowed_terrain_height_range": 3
}
```

## Advanced: Nether Jigsaw Structures

For expanding Nether structures:

**Structure JSON:**
```json
{
  "type": "moogs_structures:moogs_structures_generic_nether_jigsaw_structure",
  "size": 5,
  "max_distance_from_center": 30,
  "land_search_direction": "HIGHEST_LAND",
  "ledge_offset_y": 3
}
```

**Create Multiple Pools:**
- Start pool
- Extension pools
- Branch pools

**Add Jigsaw Blocks** in NBT files to connect pieces.

## Comparison: Overworld vs Nether

| Feature | Overworld | Nether |
|---------|-----------|--------|
| Structure Type | `moogs_structures_generic_jigsaw_structure` | `moogs_structures_generic_nether_jigsaw_structure` |
| Land Search | Not available | `HIGHEST_LAND` or `LOWEST_LAND` |
| Ledge Offset | Not available | `ledge_offset_y` (optional) |
| Terrain Checks | Standard | Often stricter |
| Spacing | Varies | Often moderate |
| Exclusion Zones | Common | Rare |

## Best Practices for Nether Structures

1. **Use Nether Structure Type** - Always use `moogs_structures_generic_nether_jigsaw_structure`
2. **Choose Land Search** - `HIGHEST_LAND` for platforms, `LOWEST_LAND` for ground
3. **Strict Terrain Checks** - Nether terrain is variable, be selective
4. **Test Thoroughly** - Nether placement can be unpredictable
5. **Account for Lava** - Use `cannot_spawn_in_liquid: true`
6. **Consider Vertical Space** - Nether is tall, structures can be vertical

## Next Steps

Now that you understand Nether structures:

1. **[Complex Structure Example](Complex-Structure.md)** - Large expanding structures
2. **[Advanced Topics](../Advanced-Topics.md)** - More advanced features
3. **[Structure Files](../Structure-Files.md)** - Complete reference

---

**See Also:**
- [Structure Files](../Structure-Files.md) - Nether-specific fields
- [Template Pools](../Template-Pools.md) - Pool system (same for Nether)
- [Placement Systems](../Placement-Systems.md) - Placement configuration

