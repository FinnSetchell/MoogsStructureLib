# Jigsaw Structure Example: Barn

This walkthrough demonstrates creating a jigsaw structure using the Barn structure from MoogsVoyagerStructures. Jigsaw structures can expand and connect multiple pieces together.

## Overview

The Barn structure demonstrates:
- Simple jigsaw connections
- Terrain height checks
- Exclusion zones
- Multiple template pools

While the barn itself is simple, it shows how jigsaw systems work.

## File Structure

```
data/mvs/
├── structure/
│   └── houses/
│       └── barn.nbt
└── worldgen/
    ├── structure/
    │   └── barn.json
    ├── structure_set/
    │   └── barn.json
    └── template_pool/
        └── houses/
            └── barn/
                └── start_pool.json
```

## Step 1: Create the NBT File

1. Build your barn structure in Minecraft
2. **Add Jigsaw Blocks** where you want connections:
   - Place jigsaw blocks at connection points
   - Set jigsaw block properties:
     - **Name**: Pool to connect to (e.g., `mvs:barn/side_pool`)
     - **Target**: Jigsaw block name in target pieces (e.g., `barn_side`)
     - **Joint**: `rollable` (usually)
3. Save as `barn.nbt`
4. Place at: `data/mvs/structure/houses/barn.nbt`

**Note:** For this example, the barn is simple and may not have side pools, but the structure supports jigsaw expansion.

## Step 2: Create the Template Pool

**File:** `data/mvs/worldgen/template_pool/houses/barn/start_pool.json`

```json
{
  "name": "mvs:barn/start_pool",
  "fallback": "minecraft:empty",
  "elements": [
    {
      "weight": 1,
      "element": {
        "location": "mvs:houses/barn",
        "processors": "minecraft:empty",
        "projection": "rigid",
        "element_type": "minecraft:single_pool_element"
      }
    }
  ]
}
```

### Explanation

- **`name`**: `"mvs:barn/start_pool"` - Pool identifier
- **`location`**: `"mvs:houses/barn"` - References `data/mvs/structure/houses/barn.nbt`
- If the barn NBT has jigsaw blocks, they reference other pools (like side pools)

### Optional: Side Pool Example

If your barn has side pieces, create additional pools:

**File:** `data/mvs/worldgen/template_pool/houses/barn/side_pool.json`

```json
{
  "name": "mvs:barn/side_pool",
  "fallback": "minecraft:empty",
  "elements": [
    {
      "weight": 1,
      "element": {
        "location": "mvs:houses/barn_side_left",
        "processors": "minecraft:empty",
        "projection": "rigid",
        "element_type": "minecraft:single_pool_element"
      }
    },
    {
      "weight": 1,
      "element": {
        "location": "mvs:houses/barn_side_right",
        "processors": "minecraft:empty",
        "projection": "rigid",
        "element_type": "minecraft:single_pool_element"
      }
    }
  ]
}
```

Jigsaw blocks in the main barn would reference `mvs:barn/side_pool` to connect these pieces.

## Step 3: Create the Structure JSON

**File:** `data/mvs/worldgen/structure/barn.json`

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

### Key Differences from Simple Structure

#### Terrain Height Checks

```json
{
  "terrain_height_radius_check": 1,
  "allowed_terrain_height_range": 3
}
```

**What this does:**
- Checks terrain height in a 1-chunk radius around the structure
- Only spawns if terrain height varies by 3 blocks or less
- Prevents barns from spawning on steep hills or cliffs

**Why:** Barns should spawn on relatively flat terrain.

### Field-by-Field Explanation

- **`type`**: Generic jigsaw structure (supports expansion)
- **`start_pool`**: References the barn start pool
- **`size`**: `1` - Minimal expansion (barn is simple)
- **`terrain_height_radius_check`**: `1` - Check 1 chunk radius
- **`allowed_terrain_height_range`**: `3` - Max 3 block height difference
- Other fields same as simple structure

## Step 4: Create the Structure Set

**File:** `data/mvs/worldgen/structure_set/barn.json`

```json
{
  "structures": [
    {
      "structure": "mvs:barn",
      "weight": 1
    }
  ],
  "placement": {
    "type": "moogs_structures:advanced_random_spread",
    "salt": 766544243,
    "super_exclusion_zone": {
      "chunk_count": 3,
      "other_set": "#mvs:common_avoid"
    },
    "spacing": 31,
    "separation": 26
  }
}
```

### Key Feature: Exclusion Zone

```json
{
  "super_exclusion_zone": {
    "chunk_count": 3,
    "other_set": "#mvs:common_avoid"
  }
}
```

**What this does:**
- Prevents barns from spawning within 3 chunks of structures in `#mvs:common_avoid` tag
- Keeps barns away from common structures like houses and carts
- Creates better structure distribution

**Structure Set Tag:** `data/mvs/tags/worldgen/structure_set/common_avoid.json`

```json
{
  "values": [
    "mvs:house",
    "mvs:cart",
    "mvs:well"
  ]
}
```

### Field-by-Field Explanation

- **`structures`**: Single barn structure
- **`placement`**: Advanced random spread
  - **`salt`**: Unique identifier
  - **`super_exclusion_zone`**: Avoids common structures
  - **`spacing`**: 31 chunks average distance
  - **`separation`**: 26 chunks minimum distance

## How Jigsaw Expansion Works

Even though this barn uses `size: 1`, here's how jigsaw expansion would work:

1. **Start Pool** places the main barn piece
2. **Jigsaw Blocks** in the barn NBT file reference other pools
3. **Target Pools** (like side pools) provide pieces to connect
4. **Structure Expands** until:
   - `size` limit is reached
   - No more jigsaw connections available
   - `max_distance_from_center` limit reached

### Example Expansion Flow

```
Start Pool (barn.nbt)
  ↓
Jigsaw Block: "mvs:barn/side_pool" → "barn_side"
  ↓
Side Pool selects piece (barn_side_left.nbt or barn_side_right.nbt)
  ↓
New piece connects to jigsaw block
  ↓
Process repeats until size limit
```

## Testing Your Structure

1. Create a new world with your datapack/mod
2. Use `/locate structure mvs:barn` to find barns
3. Verify:
   - Spawns on relatively flat terrain
   - Not near other common structures (exclusion zone working)
   - Proper spacing between barns
   - Terrain height checks working

## Common Issues

### Structure Not Spawning

**Check:**
- Terrain height restrictions might be too strict
- Exclusion zone might be blocking all locations
- Biome tags correct

### Spawning on Steep Terrain

**Solution:** Increase `allowed_terrain_height_range` or adjust `terrain_height_radius_check`

### Too Close to Other Structures

**Solution:** Increase `chunk_count` in exclusion zone or adjust spacing/separation

## Advanced: Multiple Variants

To have multiple barn variants:

**Template Pool:**
```json
{
  "elements": [
    {
      "weight": 3,
      "element": {
        "location": "mvs:houses/barn",
        ...
      }
    },
    {
      "weight": 1,
      "element": {
        "location": "mvs:houses/barn_large",
        ...
      }
    }
  ]
}
```

Large barns spawn 1/3 as often as regular barns.

## Advanced: Jigsaw Expansion

To allow barn expansion:

**Structure JSON:**
```json
{
  "size": 5,
  "max_distance_from_center": 20
}
```

**Create Side Pools:**
- `barn/side_pool.json` - Side pieces
- `barn/corner_pool.json` - Corner pieces
- `barn/roof_pool.json` - Roof variations

**Add Jigsaw Blocks** in NBT files to connect pieces.

## Next Steps

Now that you understand jigsaw structures:

1. **[Nether Structure Example](Nether-Structure)** - Nether-specific features
2. **[Complex Structure Example](Complex-Structure)** - Large expanding structures
3. **[Advanced Topics](Advanced-Topics)** - Advanced features

---

**See Also:**
- [Template Pools](Template-Pools) - Jigsaw connections explained
- [Structure Files](Structure-Files) - Terrain checks and options
- [Placement Systems](Placement-Systems) - Exclusion zones

