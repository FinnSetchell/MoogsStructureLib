# Advanced Topics

This page covers advanced features and techniques for Moogs Structure Lib, including version-aware structures, advanced placement modifiers, and complex configuration options.

## Version-Aware Pool Elements

For structures that need different NBT files per Minecraft version, use version-aware pool elements. This allows a single structure definition to work across multiple Minecraft versions.

### When to Use

Use version-aware elements when:
- Block IDs or properties change between versions
- Structure format requirements differ
- You want to maintain compatibility across versions

### How It Works

Version-aware elements automatically select the correct NBT file based on the running Minecraft version. If no version match is found, it falls back to a default file.

### Syntax

**Template Pool Element:**
```json
{
  "weight": 1,
  "element": {
    "element_type": "moogs_structures:version_aware_single_pool_element",
    "location": "namespace:structure/default",
    "locations": {
      "1.21.5-1.21.10": "namespace:structure/new_version",
      "1.21-1.21.4": "namespace:structure/old_version"
    },
    "processors": "minecraft:empty",
    "projection": "rigid"
  }
}
```

### Fields

- `element_type`: Must be `"moogs_structures:version_aware_single_pool_element"`
- `location`: Default/fallback NBT file path
- `locations`: Object mapping version ranges to NBT files
  - Key: Version range (e.g., `"1.21.5-1.21.10"`)
  - Value: NBT file path for that version range
- `processors`: Processor list (same as regular elements)
- `projection`: Projection type (same as regular elements)

### Version Range Format

Version ranges use format: `"<start>-<end>"`

**Examples:**
- `"1.21.5-1.21.10"` - Versions 1.21.5 through 1.21.10
- `"1.21-1.21.4"` - Versions 1.21 through 1.21.4
- `"1.21.5"` - Single version

### Example

```json
{
  "name": "mvs:versioned_structure/start_pool",
  "fallback": "minecraft:empty",
  "elements": [
    {
      "weight": 1,
      "element": {
        "element_type": "moogs_structures:version_aware_single_pool_element",
        "location": "mvs:structure/default",
        "locations": {
          "1.21.5-1.21.10": "mvs:structure/v1_21_5",
          "1.21-1.21.4": "mvs:structure/v1_21"
        },
        "processors": "minecraft:empty",
        "projection": "rigid"
      }
    }
  ]
}
```

**Behavior:**
- Minecraft 1.21.5-1.21.10: Uses `mvs:structure/v1_21_5`
- Minecraft 1.21-1.21.4: Uses `mvs:structure/v1_21`
- Other versions: Uses `mvs:structure/default` (fallback)

## Advanced Structure Features

### Burying Types

Burying types control how structures are positioned relative to terrain:

#### LOWEST_CORNER
Places structure so its lowest corner aligns with terrain height.

**Use case:** Structures that should sit on the ground.

```json
{
  "burying_type": "LOWEST_CORNER"
}
```

#### AVERAGE_LAND
Places structure at average terrain height in the area.

**Use case:** Structures that should adapt to terrain variations.

```json
{
  "burying_type": "AVERAGE_LAND"
}
```

#### LOWEST_SIDE
Places structure so its lowest side aligns with terrain.

**Use case:** Structures that should follow terrain contours.

```json
{
  "burying_type": "LOWEST_SIDE"
}
```

### Y Allowance

Restricts structure placement to specific Y coordinate ranges:

```json
{
  "y_allowance": {
    "min_y_allowed": 50,
    "max_y_allowed": 100
  }
}
```

**Use cases:**
- Ocean structures (restrict to ocean depth)
- Sky structures (restrict to high altitudes)
- Cave structures (restrict to underground)

**Important:** `max_y_allowed` cannot be less than `min_y_allowed`.

### Pools That Ignore Boundaries

Allows specific pools to extend beyond normal structure boundaries:

```json
{
  "pools_that_ignore_boundaries": [
    "mvs:cathedral/special_pool",
    "mvs:cathedral/decorative_pool"
  ]
}
```

**Use case:** Decorative elements or special features that can extend beyond main structure limits.

### Max Distance From Center

Limits how far jigsaw pieces can extend from the structure center:

```json
{
  "max_distance_from_center": 35
}
```

**Use case:** Large jigsaw structures where you want to limit total size.

**Example:** Cathedral structure with `max_distance_from_center: 35` means no piece can be more than 35 blocks from the center.

## Terrain Adaptation Modes

Controls how structures adapt to terrain:

### none
No terrain adaptation. Structure places exactly as built.

```json
{
  "terrain_adaptation": "none"
}
```

**Use case:** Underground structures, floating structures.

### beard_thin
Thin beard adaptation. Structure blends slightly with terrain.

```json
{
  "terrain_adaptation": "beard_thin"
}
```

**Use case:** Most surface structures (most common).

### beard_box
Box beard adaptation. More aggressive terrain blending.

```json
{
  "terrain_adaptation": "beard_box"
}
```

**Use case:** Structures that should blend heavily with terrain.

### encapsulate
Encapsulation adaptation. Structure is encapsulated by terrain.

```json
{
  "terrain_adaptation": "encapsulate"
}
```

**Use case:** Buried structures, underground features.

### bury
Bury adaptation. Structure is buried in terrain.

```json
{
  "terrain_adaptation": "bury"
}
```

**Use case:** Completely buried structures.

## Liquid Settings

Controls how liquids are handled in structures:

```json
{
  "liquid_settings": "ignore_waterlogging"
}
```

**Options:**
- `"ignore_waterlogging"` - Ignores waterlogging (default)
- Other options depend on Minecraft version

**Use case:** Underwater structures, structures near water.

## Biome Radius Checks

Ensures structures don't spawn on biome borders:

```json
{
  "valid_biome_radius_check": 5
}
```

Checks that all biomes within 5 chunks match the required biome.

**Use case:** Structures that need consistent biome context.

## Placement Modifiers

Moogs Structure Lib provides placement modifiers for different use cases. **Note:** These are for feature placement, not structure placement. They're used in different contexts than structure generation.

### Minus Eight Placement

Offsets placement position by -8 blocks in X and Z.

**Type:** `moogs_structures:minus_eight_placement`

**Use case:** Feature placement adjustments.

### Snap To Lower Non-Air Placement

Snaps placement to the lowest non-air block below.

**Type:** `moogs_structures:snap_to_lower_non_air_placement`

**Use case:** Features that need to sit on solid ground.

### Unlimited Count Placement

Allows unlimited count of feature placements.

**Type:** `moogs_structures:unlimited_count`

**Syntax:**
```json
{
  "type": "moogs_structures:unlimited_count",
  "count": 5
}
```

**Use case:** Features that should spawn multiple times.

**Note:** These placement modifiers are for feature generation (ores, vegetation, etc.), not structure generation. For structures, use `AdvancedRandomSpread` placement in structure sets.

## Processor Lists

Processor lists modify blocks during structure placement. They're referenced in template pools:

```json
{
  "processors": "namespace:processor_list_name"
}
```

**File location:** `data/<namespace>/worldgen/processor_list/<name>.json`

**Common processors:**
- `minecraft:empty` - No processing (most common)
- Custom processors for block replacement, randomization, etc.

**Example from MVS:**
```json
{
  "processors": "mvs:cathedral"
}
```

References `data/mvs/worldgen/processor_list/cathedral.json`.

**Note:** Processor lists are a Minecraft feature. See Minecraft documentation for details on creating custom processors.

## Complex Structure Patterns

### Large Jigsaw Structures

For large expanding structures like mineshafts:

```json
{
  "type": "moogs_structures:moogs_structures_generic_jigsaw_structure",
  "start_pool": "mvs:mineshaft/mineshaft",
  "size": 10,
  "max_distance_from_center": 90,
  "pools_that_ignore_boundaries": [],
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

**Key settings:**
- High `size` (10) for expansion
- `max_distance_from_center` to limit growth
- `dimension_padding` to prevent overlap
- `step: "underground_structures"` for underground
- `terrain_adaptation: "none"` for precise placement

### Nether Structures

Nether structures use special structure type:

```json
{
  "type": "moogs_structures:moogs_structures_generic_nether_jigsaw_structure",
  "start_pool": "mvs:nether_structure/start_pool",
  "size": 1,
  "biomes": "#mvs:has_structure/nether_biomes",
  "land_search_direction": "HIGHEST_LAND",
  "ledge_offset_y": 5,
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

**Key settings:**
- `moogs_structures_generic_nether_jigsaw_structure` type
- `land_search_direction: "HIGHEST_LAND"` or `"LOWEST_LAND"`
- `ledge_offset_y` for positioning
- Nether biome tags

## Best Practices

1. **Use Version-Aware Elements** - When supporting multiple versions
2. **Test Thoroughly** - Advanced features can have unexpected interactions
3. **Document Complex Structures** - Note jigsaw connections and pool relationships
4. **Start Simple** - Add advanced features incrementally
5. **Use Appropriate Settings** - Match terrain adaptation to structure type
6. **Limit Growth** - Use `max_distance_from_center` for large structures
7. **Test Exclusion Zones** - Verify they work as expected

## Troubleshooting Advanced Features

**Version-aware not working?**
- Check version range format
- Verify NBT files exist for all versions
- Check fallback file exists

**Burying type not working?**
- Verify terrain height checks are appropriate
- Check `y_allowance` restrictions
- Ensure terrain is suitable

**Structures too large?**
- Reduce `size` value
- Lower `max_distance_from_center`
- Check jigsaw connections aren't infinite

**Exclusion zones too restrictive?**
- Reduce `chunk_count`
- Check structure set tags are correct
- Verify `allowed_chunk_count` logic

---

**See Also:**
- [Structure Files](Structure-Files) - Complete structure JSON reference
- [Template Pools](Template-Pools) - Pool system details
- [Placement Systems](Placement-Systems) - Placement configuration

