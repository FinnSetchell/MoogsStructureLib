# Getting Started

This guide will walk you through the basics of creating structures with Moogs Structure Lib. By the end, you'll understand the four required files and have created a simple structure.

## Prerequisites

**Minecraft 1.21 or higher** is required
Moogs Structure Lib supports Minecraft 1.20 through 1.21.10. This wiki will walk through how to use the lib in 1.21+ however most of the steps are the same for 1.20-1.20.6. Feel free to look at the data for our mods like MVS to figure out how to use the lib with older mc versions.

For mod development, add Moogs Structure Lib as a dependency in your `fabric.mod.json` or `mods.toml`.

### File Structure Overview

Structures are defined using Minecraft's datapack system. Your files should be organized like this:

```
data/
└── <namespace>/
    ├── structure/              # NBT structure files (assets)
    │   └── your_structure.nbt
    └── worldgen/
        ├── structure/           # Structure definitions
        │   └── your_structure.json
        ├── structure_set/       # Structure placement rules
        │   └── your_structure.json
        └── template_pool/       # Template pools
            └── your_structure/
                └── start_pool.json
```

**Note:** The `structure/` folder for NBT files is separate from `worldgen/structure/` for JSON definitions.

## The Four Required Files

To generate a structure, you need exactly four files:

### 1. Structure JSON
**Location:** `data/<namespace>/worldgen/structure/<name>.json`

Defines what type of structure it is, which biomes it spawns in, height settings, and other properties.

### 2. Template Pool JSON
**Location:** `data/<namespace>/worldgen/template_pool/<name>/start_pool.json`

Defines which NBT structure files to use and how they connect together (for jigsaw structures).

### 3. Structure Set JSON
**Location:** `data/<namespace>/worldgen/structure_set/<name>.json`

Defines where structures spawn in the world, how often, and spacing between them.

### 4. NBT Structure Files
**Location:** `data/<namespace>/structure/<path>/<name>.nbt`

The actual structure data files created with structure blocks in Minecraft.

## Quick Example: Cart Structure

Let's look at a complete, simple example from MoogsVoyagerStructures - the Cart structure.

### 1. Structure JSON
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

**What this does:**
- `type`: Uses the generic jigsaw structure type from MSL (moogs structure lib)
- `start_pool`: References the template pool
- `size`: 1 means it's a simple structure (no jigsaw expansion)
- `biomes`: Spawns in overworld biomes (using a tag)
- `project_start_to_heightmap`: Places on world surface
- `cannot_spawn_in_liquid`: Won't spawn in water
- `step`: Generates during surface structures phase
- `start_height`: Uses absolute height 0 (will be adjusted by heightmap)

### 2. Template Pool JSON
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

**What this does:**
- `name`: Unique identifier for this pool
- `fallback`: What to use if pool fails (usually `minecraft:empty`)
- `elements`: Array of structure pieces
  - `weight`: Selection weight (higher = more likely)
  - `location`: Path to NBT file (without `.nbt` extension)
  - `processors`: Processor list (use `minecraft:empty` for none)
  - `projection`: `rigid` keeps structure as-is, `terrain_matching` adapts to terrain
  - `element_type`: Type of pool element

### 3. Structure Set JSON
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

**What this does:**
- `structures`: List of structures in this set
  - `structure`: Reference to structure JSON
  - `weight`: Selection weight if multiple structures
- `placement`: How structures are placed
  - `type`: Uses advanced random spread placement
  - `salt`: Unique number for this structure (affects spawn locations)
  - `spacing`: Average space between structures
  - `separation`: Minimum space between structures

### 4. NBT Structure File
**File:** `data/mvs/structure/carts/cart.nbt`

This is the actual structure file created with a structure block. The path `mvs:carts/cart` in the template pool references this file.

## The Complete Flow

Here's how these files work together:

1. **World Generation** checks structure sets to see if a structure should spawn in a chunk
2. **Structure Set** (`cart.json`) determines if this chunk is valid based on spacing/separation
3. **Structure JSON** (`cart.json`) checks biomes, terrain, and other conditions
4. **Template Pool** (`start_pool.json`) selects which structure piece to use
5. **NBT File** (`cart.nbt`) provides the actual structure data
6. **Structure is placed** in the world!

## Next Steps

Now that you understand the basics:

1. **[Structure Files](Structure-Files)** - Learn all available options for structure JSON files
2. **[Template Pools](Template-Pools)** - Understand how pools work and connect structures
3. **[Structure Sets](Structure-Sets)** - Configure where structures spawn
4. **[Placement Systems](Placement-Systems)** - Advanced placement options
5. **[NBT Files](NBT-Files)** - How to create and organize structure files

Or jump straight to examples:
- **[Simple Structure Example](Simple-Structure)** - Detailed walkthrough of the Cart structure

## Common Mistakes to Avoid

- **Spacing must be greater than separation** - Otherwise structures can't spawn properly
- **NBT file paths don't include `.nbt` extension** - Use `mvs:carts/cart` not `mvs:carts/cart.nbt`
- **Structure names must match** - The structure set references the structure JSON by name

---

Ready to dive deeper? Check out the [Structure Files](Structure-Files) page for a complete reference!

