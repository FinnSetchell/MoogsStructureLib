# Simple Structure Example: Cart

This walkthrough demonstrates creating a simple, single-piece structure using the Cart structure from MoogsVoyagerStructures as an example.

## Overview

The Cart structure is a simple decorative structure that:
- Spawns on the surface
- Uses a single NBT file (no jigsaw connections)
- Has minimal configuration
- Perfect for learning the basics

## File Structure

```
data/mvs/
├── structure/
│   └── carts/
│       └── cart.nbt
└── worldgen/
    ├── structure/
    │   └── cart.json
    ├── structure_set/
    │   └── cart.json
    └── template_pool/
        └── carts/
            └── cart/
                └── start_pool.json
```

## Step 1: Create the NBT File

1. Build your cart structure in Minecraft using structure blocks
2. Save it as `cart.nbt`
3. Place it at: `data/mvs/structure/carts/cart.nbt`

**Note:** The structure should be a simple, single-piece design without jigsaw blocks.

## Step 2: Create the Template Pool

**File:** `data/mvs/worldgen/template_pool/carts/cart/start_pool.json`

```json
{
  "name": "mvs:cart/start_pool",
  "fallback": "minecraft:empty",
  "elements": [
    {
      "weight": 1,
      "element": {
        "location": "mvs:carts/cart",
        "processors": "minecraft:empty",
        "projection": "rigid",
        "element_type": "minecraft:single_pool_element"
      }
    }
  ]
}
```

### Explanation

- **`name`**: `"mvs:cart/start_pool"` - Unique identifier for this pool
- **`fallback`**: `"minecraft:empty"` - Use nothing if pool fails
- **`elements`**: Array with one element
  - **`weight: 1`**: Only one option, so weight doesn't matter
  - **`location`**: `"mvs:carts/cart"` - References `data/mvs/structure/carts/cart.nbt`
  - **`processors`**: `"minecraft:empty"` - No block processing
  - **`projection`**: `"rigid"` - Structure stays as-built
  - **`element_type`**: `"minecraft:single_pool_element"` - Standard single piece

## Step 3: Create the Structure JSON

**File:** `data/mvs/worldgen/structure/cart.json`

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

### Field-by-Field Explanation

- **`type`**: `"moogs_structures:moogs_structures_generic_jigsaw_structure"` - Uses generic jigsaw structure type
- **`start_pool`**: `"mvs:carts/cart/start_pool"` - References the template pool we created
- **`size`**: `1` - Simple structure, no jigsaw expansion needed
- **`biomes`**: `"#mvs:has_structure/overworld_biomes"` - Spawns in overworld biomes (using a tag)
- **`project_start_to_heightmap`**: `"WORLD_SURFACE_WG"` - Places on world surface
- **`cannot_spawn_in_liquid`**: `true` - Won't spawn in water
- **`step`**: `"surface_structures"` - Generates during surface structures phase
- **`terrain_adaptation`**: `"beard_thin"` - Slight terrain blending
- **`start_height`**: `{ "absolute": 0 }` - Starting height (adjusted by heightmap)
- **`spawn_overrides`**: `{}` - No custom entity spawning

## Step 4: Create the Structure Set

**File:** `data/mvs/worldgen/structure_set/cart.json`

```json
{
  "structures": [
    {
      "structure": "mvs:cart",
      "weight": 1
    }
  ],
  "placement": {
    "type": "moogs_structures:advanced_random_spread",
    "salt": 203698201,
    "spacing": 34,
    "separation": 26
  }
}
```

### Field-by-Field Explanation

- **`structures`**: Array with one structure entry
  - **`structure`**: `"mvs:cart"` - References `data/mvs/worldgen/structure/cart.json`
  - **`weight`**: `1` - Only one structure, so weight doesn't matter
- **`placement`**: Placement configuration
  - **`type`**: `"moogs_structures:advanced_random_spread"` - Uses advanced random spread
  - **`salt`**: `203698201` - Unique identifier (use random large number)
  - **`spacing`**: `34` - Average 34 chunks (~544 blocks) between attempts
  - **`separation`**: `26` - Minimum 26 chunks (~416 blocks) between structures

**Important:** `spacing` (34) must be greater than `separation` (26)!

## How It All Works Together

1. **World Generation** checks structure sets to see if a structure should spawn
2. **Structure Set** (`cart.json`) determines if this chunk is valid:
   - Checks spacing/separation rules
   - Uses salt to determine chunk selection
3. **Structure JSON** (`cart.json`) validates the location:
   - Checks biome matches
   - Verifies not in liquid
   - Projects to world surface
4. **Template Pool** (`start_pool.json`) selects the structure piece:
   - Only one element, so always selects cart
5. **NBT File** (`cart.nbt`) provides the structure data
6. **Structure is placed** in the world!

## Testing Your Structure

1. Create a new world with your datapack/mod installed
2. Use `/locate structure mvs:cart` to find cart structures
3. Teleport to found locations: `/tp @s <coordinates>`
4. Verify structure spawned correctly:
   - On surface
   - Not in water
   - In correct biomes
   - Proper spacing from other carts

## Common Issues and Solutions

### Structure Not Spawning

**Check:**
- File paths match exactly (case-sensitive)
- JSON syntax is valid
- Biome tags exist
- `spacing > separation`

### Structure Spawning in Wrong Places

**Check:**
- Biome tags are correct
- `cannot_spawn_in_liquid` is working
- Terrain is suitable

### Structure Not Found with /locate

**Check:**
- Structure set JSON exists
- Structure JSON exists
- Datapack/mod is loaded
- World was generated after adding structure

## Variations

### Multiple Variants

To have multiple cart variants, modify the template pool:

```json
{
  "elements": [
    {
      "weight": 2,
      "element": {
        "location": "mvs:carts/cart",
        ...
      }
    },
    {
      "weight": 1,
      "element": {
        "location": "mvs:carts/cart_golden",
        ...
      }
    }
  ]
}
```

Golden carts spawn half as often as regular carts.

### Different Spacing

To make carts more/less common:

```json
{
  "placement": {
    "spacing": 20,    // More common (was 34)
    "separation": 15   // Closer together (was 26)
  }
}
```

## Next Steps

Now that you understand simple structures:

1. **[Jigsaw Structure Example](Jigsaw-Structure)** - Learn about connecting multiple pieces
2. **[Nether Structure Example](Nether-Structure)** - Create Nether-specific structures
3. **[Complex Structure Example](Complex-Structure)** - Build large, expanding structures

---

**See Also:**
- [Getting Started](Getting-Started) - Basics overview
- [Structure Files](Structure-Files) - Complete structure reference
- [Template Pools](Template-Pools) - Pool system details

