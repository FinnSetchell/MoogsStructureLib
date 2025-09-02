# Moog's Structure Lib

A library mod for Minecraft that provides flexible, data-driven structure generation systems. Other mods can use this to create custom structures without writing Java code.

## Quick Start

### Add as Dependency
```gradle
dependencies {
    modImplementation "com.finndog:moogs_structures:${version}"
}
```

### Basic Usage
1. Copy the `.example` files to your mod's data folder
2. Rename them to `.json`
3. Remove comment markers and customize settings

## Available Features

### 🏗️ Structure Types
- **`moogs_structures:moogs_structures_generic_jigsaw_structure`** - Main jigsaw structure for overworld and end
- **`moogs_structures:moogs_structures_generic_nether_jigsaw_structure`** - Specialized for Nether structures

### 🧩 Pool Elements
- **`moogs_structures:mirroring_single_pool_element`** - Single element with mirroring
- **`moogs_structures:legacy_ocean_bottom_single_pool_element`** - Ocean bottom placement

### 📍 Placement Modifiers
- **`moogs_structures:minus_eight_placement`** - Places 8 blocks lower
- **`moogs_structures:unlimited_count`** - Bypasses count limits
- **`moogs_structures:snap_to_lower_non_air_placement`** - Snaps to lowest solid block

### 🎯 Structure Placement
- **`moogs_structures:advanced_random_spread`** - Enhanced random spread with advanced options

### ⚙️ Processors
- **`moogs_structures:waterlogging_fix_processor`** - Fixes waterlogging issues

### 🏷️ Biome Tags
- **`moogs_structures:has_structure/overworld_biomes`** - Comprehensive overworld biome collection

## Example Structure Configuration

### Basic Structure
```json
{
  "type": "moogs_structures:moogs_structures_generic_jigsaw_structure",
  "start_pool": "your_mod:your_structure/start_pool",
  "size": 1,
  "biomes": "#moogs_structures:has_structure/overworld_biomes",
  "project_start_to_heightmap": "WORLD_SURFACE_WG",
  "cannot_spawn_in_liquid": true,
  "step": "surface_structures",
  "terrain_adaptation": "beard_thin"
}
```

### Advanced Placement
```json
{
  "type": "moogs_structures:advanced_random_spread",
  "salt": 123456789,
  "spacing": 31,
  "separation": 26
}
```

### Template Pool
```json
{
  "name": "your_mod:your_structure/start_pool",
  "fallback": "minecraft:empty",
  "elements": [
    {
      "weight": 1,
      "element": {
        "location": "your_mod:your_piece",
        "processors": "moogs_structures:waterlogging_fix_processor",
        "projection": "rigid",
        "element_type": "minecraft:single_pool_element"
      }
    }
  ]
}
```

## Structure Options

### Burying Types
- **`LOWEST_CORNER`** - Buries to lowest corner
- **`AVERAGE_LAND`** - Buries to average land height  
- **`LOWEST_SIDE`** - Buries to lowest side

### Heightmap Types
- **`WORLD_SURFACE_WG`** - World surface with generation
- **`OCEAN_FLOOR_WG`** - Ocean floor with generation
- **`MOTION_BLOCKING_NO_LEAVES`** - Motion blocking without leaves

## Example Structure: Barn

The mod includes a complete barn structure example (disabled by default) showing:
- Structure definition
- Template pools
- Structure set configuration
- Biome targeting

## Benefits

✅ **No Java Code Required** - Fully data-driven  
✅ **Advanced Placement** - Sophisticated algorithms  
✅ **Biome Integration** - Comprehensive biome system  
✅ **Cross-Platform** - Works on Fabric and Forge  
✅ **Extensible** - Easy to add custom features

## License

GNU Lesser General Public License v3.0

## Credits

- **Special Thanks**: 
  - TelepathicGrunt for structure mod template inspiration and Repurposed Structures
