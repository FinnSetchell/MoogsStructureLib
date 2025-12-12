# Template Pools

Template pools define which structure pieces are used and how they connect together. They're the bridge between your structure JSON and your NBT files.

## What are Template Pools?

Template pools are collections of structure pieces (NBT files) that can be randomly selected during structure generation. For jigsaw structures, pools can connect to other pools, allowing structures to expand and create complex layouts.

Check out [this cool video](https://www.youtube.com/watch?v=5IDJEEDEQNs) explaining jigsaw blocks.

## File Location

Template pools are stored in:
```
data/<namespace>/worldgen/template_pool/<path>/<name>.json
```

**Example:** `data/mvs/worldgen/template_pool/carts/cart/start_pool.json`

## Basic Pool Structure

Every template pool JSON has this structure:

```json
{
  "name": "namespace:pool_name",
  "fallback": "minecraft:empty",
  "elements": [
    {
      "weight": 1,
      "element": {
        "location": "namespace:path/to/structure",
        "processors": "minecraft:empty",
        "projection": "rigid",
        "element_type": "minecraft:single_pool_element"
      }
    }
  ]
}
```

## Required Fields

### `name`
**Type:** Resource Location (String)  
**Example:** `"mvs:cart/start_pool"`

Unique identifier for this pool. Must match the reference used in structure JSON files.

**Important:** The `name` field should match the file path. For `data/mvs/worldgen/template_pool/carts/cart/start_pool.json`, use `"mvs:cart/start_pool"` or `"mvs:carts/cart/start_pool"` depending on your naming convention.

### `fallback`
**Type:** Resource Location (String)  
**Example:** `"minecraft:empty"`

What to use if this pool fails or has no valid elements. Usually `"minecraft:empty"` to place nothing.

### `elements`
**Type:** Array of Element Objects  
**Example:** See below

Array of structure pieces that can be selected. Each element has a weight that determines selection probability.

## Element Structure

Each element in the `elements` array contains:

### `weight`
**Type:** Integer  
**Example:** `1`

Selection weight. Higher weights are more likely to be selected. If all elements have weight `1`, they're equally likely.

**Example:** With weights `[1, 2, 3]`, the third element is 3x more likely than the first.

### `element`
**Type:** Object  
Contains the element configuration:

#### `location`
**Type:** Resource Location (String)  
**Example:** `"mvs:carts/cart"`

Path to the NBT structure file **without** the `.nbt` extension. The file should be at `data/<namespace>/structure/<path>.nbt`.

#### `processors`
**Type:** Resource Location (String)  
**Example:** `"minecraft:empty"` or `"mvs:cathedral"`

Reference to a processor list that modifies blocks during placement. Use `"minecraft:empty"` for no processing.

Processor lists are defined in `data/<namespace>/worldgen/processor_list/`.

[Here](https://misode.github.io/worldgen/processor-list/) is a website which helps generate custom processors

#### `projection`
**Type:** String  
**Example:** `"rigid"`

How the structure aligns with terrain:
- `"rigid"` - Structure stays as-is, doesn't adapt to terrain (most common)
- `"terrain_matching"` - Structure adapts to match terrain height

#### `element_type`
**Type:** String  
**Example:** `"minecraft:single_pool_element"`

Type of pool element:
- `"minecraft:single_pool_element"` - Standard single structure piece
- `"moogs_structures:version_aware_single_pool_element"` - Version-aware element (see Advanced Topics)

## Simple Pool Example

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

This pool has a single element that always selects the cart structure.

## Multiple Elements Example

**File:** `data/mvs/worldgen/template_pool/houses/barn/example_side_pool.json`

```json
{
  "name": "mvs:run_down_house/side_pool",
  "fallback": "minecraft:empty",
  "elements": [
    {
      "weight": 1,
      "element": {
        "location": "mvs:houses/run_down_house_right_side",
        "processors": "minecraft:empty",
        "projection": "rigid",
        "element_type": "minecraft:single_pool_element"
      }
    },
    {
      "weight": 1,
      "element": {
        "location": "mvs:run_down_house_right_side_golden",
        "processors": "minecraft:empty",
        "projection": "rigid",
        "element_type": "minecraft:single_pool_element"
      }
    }
  ]
}
```

This pool randomly selects between two different side pieces, each with equal probability.

## Jigsaw Connections

For jigsaw structures, pools connect via **jigsaw blocks** in the NBT files. Here's how it works:

1. **Start Pool** - The initial pool referenced in the structure JSON
2. **Jigsaw Blocks** - Special blocks in NBT files that define connection points
3. **Target Pools** - Pools that can connect to jigsaw blocks

### How Jigsaw Works

1. Structure generation starts with the start pool
2. When a jigsaw block is encountered, it looks for a matching pool
3. A piece from the target pool is selected and placed
4. The process continues until `size` limit is reached or no more connections

### Jigsaw Block Format

Jigsaw blocks in your NBT files have two properties:
- **Name** - The pool to connect to (e.g., `"mvs:mineshaft/straight"`)
- **Target** - The jigsaw block name in the target piece (e.g., `"mineshaft_entrance"`)

**Example:** A jigsaw block with name `"mvs:mineshaft/straight"` and target `"mineshaft_entrance"` will:
1. Look for pool `mvs:mineshaft/straight`
2. Select a piece from that pool
3. Connect it to any jigsaw block named `"mineshaft_entrance"` in the selected piece

### Start Pool Example

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

The barn NBT file contains jigsaw blocks that reference other pools (like side pools), allowing the structure to expand.

### Side Pool Example

**File:** `data/mvs/worldgen/template_pool/houses/barn/example_side_pool.json`

```json
{
  "name": "mvs:run_down_house/side_pool",
  "fallback": "minecraft:empty",
  "elements": [
    {
      "weight": 1,
      "element": {
        "location": "mvs:houses/run_down_house_right_side",
        "processors": "minecraft:empty",
        "projection": "rigid",
        "element_type": "minecraft:single_pool_element"
      }
    },
    {
      "weight": 1,
      "element": {
        "location": "mvs:run_down_house_right_side_golden",
        "processors": "minecraft:empty",
        "projection": "rigid",
        "element_type": "minecraft:single_pool_element"
      }
    }
  ]
}
```

This pool provides pieces that can connect to jigsaw blocks in the main structure.

## Complex Jigsaw Example: Mineshaft

Mineshafts use multiple interconnected pools. Here's a simplified example:

### Start Pool
**File:** `data/mvs/worldgen/template_pool/mineshaft/mineshaft.json`

```json
{
  "name": "mvs:mineshaft/mineshaft",
  "fallback": "minecraft:empty",
  "elements": [
    {
      "weight": 1,
      "element": {
        "location": "mvs:mineshaft/entrance",
        "processors": "minecraft:empty",
        "projection": "rigid",
        "element_type": "minecraft:single_pool_element"
      }
    }
  ]
}
```

The entrance piece contains jigsaw blocks that connect to:
- Straight corridors
- Intersections
- Dead ends
- Staircases

### Intersection Pool
**File:** `data/mvs/worldgen/template_pool/mineshaft/intersection_1.json`

```json
{
  "name": "mvs:mineshaft/intersection_1",
  "fallback": "minecraft:empty",
  "elements": [
    {
      "weight": 1,
      "element": {
        "location": "mvs:mineshaft/intersection_1",
        "processors": "minecraft:empty",
        "projection": "rigid",
        "element_type": "minecraft:single_pool_element"
      }
    }
  ]
}
```

This pool provides intersection pieces that can branch in multiple directions.

## Crystal Structure Example

The crystal structure uses multiple pools for different parts:

### Main Pool
**File:** `data/mvs/worldgen/template_pool/crystal/temp.json`

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
        "location": "mvs:crystal/base",
        "processors": "minecraft:empty"
      }
    }
  ]
}
```

### Lower Pool
**File:** `data/mvs/worldgen/template_pool/crystal/lower.json`

```json
{
  "name": "lower",
  "fallback": "minecraft:empty",
  "elements": [
    {
      "weight": 1,
      "element": {
        "element_type": "minecraft:single_pool_element",
        "projection": "rigid",
        "location": "mvs:crystal/lower",
        "processors": "minecraft:empty"
      }
    }
  ]
}
```

The base piece connects to the lower piece via jigsaw blocks.

## Version-Aware Pool Elements

For structures that need different NBT files per Minecraft version, use:

```json
{
  "element_type": "moogs_structures:version_aware_single_pool_element",
  "location": "mvs:structure/default",
  "locations": {
    "1.21.5-1.21.10": "mvs:structure/new_version",
    "1.21-1.21.4": "mvs:structure/old_version"
  }
}
```

See [Advanced Topics](Advanced-Topics) for more details.

## Pool Naming Conventions

Good naming makes pools easier to manage:

- **Start pools:** `namespace:structure_name/start_pool` or `namespace:structure_name`
- **Side pools:** `namespace:structure_name/side_pool` or `namespace:structure_name/sides`
- **Feature pools:** `namespace:structure_name/feature_name` (e.g., `mineshaft/intersection_1`)

## Common Patterns

### Single Piece Structure
One pool, one element:
```json
{
  "name": "namespace:structure/start_pool",
  "fallback": "minecraft:empty",
  "elements": [
    {
      "weight": 1,
      "element": {
        "location": "namespace:structure/piece",
        "processors": "minecraft:empty",
        "projection": "rigid",
        "element_type": "minecraft:single_pool_element"
      }
    }
  ]
}
```

### Variant Structure
Multiple elements with equal weights:
```json
{
  "elements": [
    { "weight": 1, "element": { "location": "namespace:variant1", ... } },
    { "weight": 1, "element": { "location": "namespace:variant2", ... } },
    { "weight": 1, "element": { "location": "namespace:variant3", ... } }
  ]
}
```

### Weighted Variants
Some variants more common than others:
```json
{
  "elements": [
    { "weight": 5, "element": { "location": "namespace:common", ... } },
    { "weight": 2, "element": { "location": "namespace:uncommon", ... } },
    { "weight": 1, "element": { "location": "namespace:rare", ... } }
  ]
}
```

## Troubleshooting

**Pool not found?**
- Check the `name` field matches the reference in structure JSON
- Verify file path matches namespace convention
- Ensure JSON syntax is valid

**Wrong piece selected?**
- Check element weights (higher = more likely)
- Verify all elements are valid
- Check fallback pool exists

**Jigsaw not connecting?**
- Verify jigsaw block names match pool names
- Check jigsaw block targets match
- Ensure target pools exist
- Check `size` limit hasn't been reached

**Structure not expanding?**
- Increase `size` in structure JSON
- Verify jigsaw blocks are placed correctly in NBT
- Check pools are properly named and referenced

---

**Next:** Learn about [Structure Sets](Structure-Sets) to configure where structures spawn!

