# Complex Structure Example: Cathedral

This walkthrough demonstrates creating a large, complex jigsaw structure using the Cathedral structure from MoogsVoyagerStructures as an example. Complex structures use multiple pools, jigsaw expansion, and advanced features.

## Overview

The Cathedral structure demonstrates:
- Large jigsaw expansion (`size: 17`)
- Maximum distance limits (`max_distance_from_center: 35`)
- Multiple template pools
- Spawn overrides for custom entities
- Rare structure placement

## File Structure

```
data/mvs/
├── structure/
│   └── cathedral/
│       ├── base/
│       │   ├── base.nbt
│       │   └── bottom.nbt
│       ├── corridors/
│       │   └── (multiple .nbt files)
│       ├── special/
│       │   └── (multiple .nbt files)
│       └── ...
└── worldgen/
    ├── structure/
    │   └── cathedral.json
    ├── structure_set/
    │   └── cathedral.json
    └── template_pool/
        └── cathedral/
            └── temp.json
```

## Step 1: Create Multiple NBT Files

Complex structures require many pieces:

### Main Pieces
- `cathedral/base/base.nbt` - Main base piece
- `cathedral/base/bottom.nbt` - Bottom section
- `cathedral/cathedral_start.nbt` - Starting piece

### Corridors
- `cathedral/corridors/corridor_1.nbt` through `corridor_8.nbt`
- Various corridor configurations

### Special Rooms
- `cathedral/special/room_1.nbt` through `room_13.nbt`
- Special rooms and features

### Jigsaw Connections

Each piece contains jigsaw blocks that connect to other pieces:
- Base connects to corridors
- Corridors connect to special rooms
- Corridors connect to other corridors
- Special rooms connect back to corridors

**Jigsaw Block Setup:**
- **Name**: Pool to connect to (e.g., `mvs:cathedral/corridor`)
- **Target**: Jigsaw block name in target pieces (e.g., `cathedral_corridor`)

## Step 2: Create Multiple Template Pools

### Start Pool

**File:** `data/mvs/worldgen/template_pool/cathedral/temp.json`

```json
{
  "name": "temp",
  "fallback": "minecraft:empty",
  "elements": [
    {
      "weight": 1,
      "element": {
        "element_type": "minecraft:single_pool_element",
        "projection": "rigid",
        "location": "mvs:cathedral/base/base",
        "processors": "minecraft:empty"
      }
    }
  ]
}
```

### Corridor Pool

**File:** `data/mvs/worldgen/template_pool/cathedral/corridor.json`

```json
{
  "name": "mvs:cathedral/corridor",
  "fallback": "minecraft:empty",
  "elements": [
    {
      "weight": 1,
      "element": {
        "location": "mvs:cathedral/corridors/corridor_1",
        "processors": "minecraft:empty",
        "projection": "rigid",
        "element_type": "minecraft:single_pool_element"
      }
    },
    {
      "weight": 1,
      "element": {
        "location": "mvs:cathedral/corridors/corridor_2",
        "processors": "minecraft:empty",
        "projection": "rigid",
        "element_type": "minecraft:single_pool_element"
      }
    }
    // ... more corridor variants
  ]
}
```

### Special Room Pool

**File:** `data/mvs/worldgen/template_pool/cathedral/special.json`

```json
{
  "name": "mvs:cathedral/special",
  "fallback": "minecraft:empty",
  "elements": [
    {
      "weight": 1,
      "element": {
        "location": "mvs:cathedral/special/room_1",
        "processors": "minecraft:empty",
        "projection": "rigid",
        "element_type": "minecraft:single_pool_element"
      }
    }
    // ... more special room variants
  ]
}
```

## Step 3: Create the Structure JSON

**File:** `data/mvs/worldgen/structure/cathedral.json`

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

### Key Features Explained

#### Large Size

```json
{
  "size": 17
}
```

**What this does:**
- Allows structure to expand up to 17 jigsaw connections
- Creates large, complex structures
- Higher values = larger structures (max 30)

#### Maximum Distance Limit

```json
{
  "max_distance_from_center": 35
}
```

**What this does:**
- Limits how far pieces can extend from center
- Prevents structures from growing too large
- 35 blocks = pieces must be within 35 blocks of center

**Why:** Controls total structure size even with high `size` value.

#### Terrain Height Range

```json
{
  "terrain_height_radius_check": 1,
  "allowed_terrain_height_range": 5
}
```

**What this does:**
- Checks terrain in 1 chunk radius
- Allows up to 5 block height difference
- More lenient than simple structures (cathedrals are large)

#### Liquid Settings

```json
{
  "liquid_settings": "ignore_waterlogging"
}
```

**What this does:**
- Handles waterlogging in structure blocks
- Important for large structures near water

#### Spawn Overrides

```json
{
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

**What this does:**
- Spawns custom entities in the structure
- `monster` category spawns hostile mobs
- `bounding_box: "full"` spawns throughout entire structure
- Weighted random selection:
  - Skeletons: 1/3 chance, 1-2 spawn
  - Zombies: 2/3 chance, 2-5 spawn

## Step 4: Create the Structure Set

**File:** `data/mvs/worldgen/structure_set/cathedral.json`

```json
{
  "structures": [
    {
      "structure": "mvs:cathedral",
      "weight": 1
    }
  ],
  "placement": {
    "type": "moogs_structures:advanced_random_spread",
    "salt": 303685798,
    "spacing": 105,
    "separation": 98,
    "min_distance_from_world_origin": 1000
  }
}
```

### Rare Structure Placement

**Spacing:** `105` chunks (~1680 blocks)
- Very rare structure
- High spacing = uncommon spawns

**Separation:** `98` chunks (~1568 blocks)
- High separation = far apart
- Prevents multiple cathedrals nearby

**Minimum Distance:** `1000` blocks
- Only spawns far from world spawn
- Creates exploration goal
- End-game content

## How Complex Expansion Works

### Expansion Flow

```
1. Start Pool places base piece
   ↓
2. Base jigsaw blocks reference corridor pool
   ↓
3. Corridor pool selects random corridor piece
   ↓
4. Corridor connects to base
   ↓
5. Corridor jigsaw blocks reference:
   - More corridors (continues expansion)
   - Special rooms (branches off)
   ↓
6. Process repeats until:
   - size limit (17) reached
   - max_distance_from_center (35) reached
   - No more jigsaw connections available
```

### Jigsaw Connection Example

**Base Piece** (`base.nbt`):
- Contains jigsaw blocks:
  - Name: `mvs:cathedral/corridor`
  - Target: `cathedral_corridor`

**Corridor Piece** (`corridor_1.nbt`):
- Contains jigsaw blocks:
  - Name: `mvs:cathedral/corridor` (continues corridor)
  - Target: `cathedral_corridor`
  - Name: `mvs:cathedral/special` (branches to rooms)
  - Target: `cathedral_special`

**Special Room** (`room_1.nbt`):
- Contains jigsaw blocks:
  - Name: `mvs:cathedral/corridor` (returns to corridor)
  - Target: `cathedral_corridor`

## Testing Your Structure

1. Create a new world with your datapack/mod
2. Travel far from spawn (1000+ blocks)
3. Use `/locate structure mvs:cathedral` to find cathedrals
4. Verify:
   - Structure expands correctly
   - Stays within size limits
   - Custom entities spawn
   - Proper spacing from other cathedrals
   - Terrain is suitable

## Common Issues

### Structure Too Large

**Solutions:**
- Reduce `size` value
- Lower `max_distance_from_center`
- Limit jigsaw connections in NBT files

### Structure Not Expanding

**Check:**
- Jigsaw blocks are placed correctly
- Pool names match jigsaw block names
- Target names match between pieces
- `size` is high enough

### Entities Not Spawning

**Check:**
- Spawn override syntax is correct
- Entity types are valid
- `bounding_box` is set correctly
- Structure has valid spawn locations

## Advanced: Pools That Ignore Boundaries

For decorative elements that can extend beyond limits:

```json
{
  "pools_that_ignore_boundaries": [
    "mvs:cathedral/decorative_pool"
  ]
}
```

Allows decorative pools to extend beyond `max_distance_from_center`.

## Advanced: Multiple Structure Variants

To have multiple cathedral variants:

**Structure Set:**
```json
{
  "structures": [
    {
      "structure": "mvs:cathedral",
      "weight": 3
    },
    {
      "structure": "mvs:cathedral_ruined",
      "weight": 1
    }
  ]
}
```

Ruined cathedrals spawn 1/3 as often as regular cathedrals.

## Design Tips for Complex Structures

1. **Plan Your Layout** - Sketch structure before building
2. **Modular Pieces** - Create reusable corridor/room pieces
3. **Clear Connections** - Use consistent jigsaw naming
4. **Test Incrementally** - Test each pool separately
5. **Limit Growth** - Use `max_distance_from_center` to control size
6. **Variety** - Multiple variants keep structures interesting
7. **Documentation** - Note jigsaw connections for reference

## Comparison: Simple vs Complex

| Feature | Simple (Cart) | Complex (Cathedral) |
|---------|---------------|---------------------|
| Size | 1 | 17 |
| Max Distance | Not set | 35 |
| Pools | 1 | Multiple |
| Jigsaw | None | Extensive |
| Spawn Overrides | None | Custom entities |
| Spacing | 34 chunks | 105 chunks |
| Min Distance | None | 1000 blocks |

## Next Steps

Now that you understand complex structures:

1. **[Advanced Topics](Advanced-Topics)** - More advanced features
2. **[Structure Files](Structure-Files)** - Complete reference
3. **[Template Pools](Template-Pools)** - Advanced pool techniques

---

**See Also:**
- [Structure Files](Structure-Files) - All structure options
- [Template Pools](Template-Pools) - Jigsaw connections
- [Placement Systems](Placement-Systems) - Rare structure placement
- [Advanced Topics](Advanced-Topics) - Advanced features

