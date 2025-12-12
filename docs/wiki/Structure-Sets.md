# Structure Sets

Structure sets group structures together and define where and how often they spawn in the world. They're the final piece that connects your structure to world generation.

## What are Structure Sets?

Structure sets are collections of structures with shared placement rules. They tell Minecraft:
- Which structures can spawn
- How often they spawn
- Where they spawn (spacing, exclusion zones, etc.)

## File Location

Structure sets are stored in:
```
data/<namespace>/worldgen/structure_set/<name>.json
```

**Example:** `data/mvs/worldgen/structure_set/cart.json`

## Basic Structure Set

Every structure set JSON has this structure:

```json
{
  "structures": [
    {
      "structure": "namespace:structure_name",
      "weight": 1
    }
  ],
  "placement": {
    "type": "moogs_structures:advanced_random_spread",
    "salt": 123456789,
    "spacing": 34,
    "separation": 26
  }
}
```

## Required Fields

### `structures`
**Type:** Array of Structure Objects  
**Example:** See below

List of structures that belong to this set. Each structure has:

#### `structure`
**Type:** Resource Location (String)  
**Example:** `"mvs:cart"`

Reference to a structure JSON file. Must match a structure defined in `worldgen/structure/`.

#### `weight`
**Type:** Integer  
**Example:** `1`

Selection weight if multiple structures are in the set. Higher weights are more likely to be selected.

**Example:** With weights `[1, 2]`, the second structure is twice as likely to spawn.

### `placement`
**Type:** Placement Object  
**Example:** See Placement Systems page

Defines how structures are placed in the world. For Moogs Structure Lib, use `moogs_structures:advanced_random_spread`.

See [Placement Systems](Placement-Systems.md) for complete placement documentation.

## Simple Example: Cart

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

This structure set:
- Contains one structure (`mvs:cart`)
- Uses advanced random spread placement
- Spawns approximately every 34 chunks
- Keeps at least 26 chunks between instances

## Multiple Structures Example

You can include multiple structures in one set:

```json
{
  "structures": [
    {
      "structure": "mvs:house_variant_1",
      "weight": 3
    },
    {
      "structure": "mvs:house_variant_2",
      "weight": 2
    },
    {
      "structure": "mvs:house_variant_3",
      "weight": 1
    }
  ],
  "placement": {
    "type": "moogs_structures:advanced_random_spread",
    "salt": 123456789,
    "spacing": 20,
    "separation": 15
  }
}
```

When a structure spawns, variant 1 is 3x more likely than variant 3.

## With Exclusion Zone: Barn

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

The `super_exclusion_zone` prevents barns from spawning within 3 chunks of any structure in the `#mvs:common_avoid` tag.

## Rare Structure: Crystal

**File:** `data/mvs/worldgen/structure_set/crystal.json`

```json
{
  "structures": [
    {
      "structure": "mvs:crystal",
      "weight": 1
    }
  ],
  "placement": {
    "type": "moogs_structures:advanced_random_spread",
    "salt": 469982354,
    "spacing": 112,
    "separation": 62,
    "min_distance_from_world_origin": 800
  }
}
```

This structure set:
- Spawns very rarely (112 chunks spacing ≈ 1792 blocks)
- Requires at least 800 blocks from world spawn
- Creates a rare, distant structure

## Complex Structure: Cathedral

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

Large, rare structures often use:
- High spacing values (105 chunks ≈ 1680 blocks)
- High separation (98 chunks ≈ 1568 blocks)
- Minimum distance from spawn (1000 blocks)

## Nether Structure: Nether Devil

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

Nether structures typically use:
- Moderate spacing (64 chunks ≈ 1024 blocks)
- Lower separation (16 chunks ≈ 256 blocks)
- No exclusion zones (Nether is less crowded)

## Understanding Placement Values

### Spacing
**Example:** `"spacing": 34`

Average chunks between structure attempts. Higher = rarer structures.

**Important:** Moogs Structure Lib multiplies spacing by **1.65** internally. So your `spacing: 34` becomes `56.1` chunks internally (~897 blocks).

**Conversion:** 1 chunk = 16 blocks. Your specified spacing 34 = ~544 blocks, but internally becomes ~897 blocks after the 1.65x multiplier.

### Separation
**Example:** `"separation": 26`

Minimum chunks between structures. Must be less than spacing.

**Important:** 
- `spacing` must be greater than `separation`, otherwise structures can't spawn!
- Moogs Structure Lib multiplies separation by **1.65** internally. So your `separation: 26` becomes `42.9` chunks internally (~686 blocks).

**Conversion:** 1 chunk = 16 blocks. Your specified separation 26 = ~416 blocks, but internally becomes ~686 blocks after the 1.65x multiplier.

### Salt
**Example:** `"salt": 203698201`

Unique number for this structure set. Affects which chunks are selected for spawning.

**Tip:** Use random large numbers. Each structure set should have a unique salt.

## Common Patterns

### Common Structures
For structures that should spawn frequently:
```json
{
  "placement": {
    "spacing": 20,
    "separation": 15
  }
}
```

### Uncommon Structures
For moderately rare structures:
```json
{
  "placement": {
    "spacing": 50,
    "separation": 35
  }
}
```

### Rare Structures
For very rare structures:
```json
{
  "placement": {
    "spacing": 100,
    "separation": 80,
    "min_distance_from_world_origin": 1000
  }
}
```

### Structures with Exclusion
For structures that avoid other structures:
```json
{
  "placement": {
    "spacing": 30,
    "separation": 25,
    "super_exclusion_zone": {
      "chunk_count": 5,
      "other_set": "#namespace:avoid_tag"
    }
  }
}
```

## Structure Set Tags

You can create tags that group structure sets together. Useful for exclusion zones:

**File:** `data/mvs/tags/worldgen/structure_set/common_avoid.json`

```json
{
  "values": [
    "mvs:house",
    "mvs:barn",
    "mvs:cart"
  ]
}
```

Then reference it in exclusion zones:
```json
{
  "super_exclusion_zone": {
    "chunk_count": 3,
    "other_set": "#mvs:common_avoid"
  }
}
```

## Troubleshooting

**Structures not spawning?**
- Check `spacing > separation` (must be true!)
- Verify structure JSON exists and is valid
- Check biome tags/biomes in structure JSON
- Ensure structure set JSON syntax is correct

**Structures too common/rare?**
- Adjust `spacing` (higher = rarer)
- Adjust `separation` (affects minimum distance)
- Remember the 1.65x multiplier!

**Structures spawning too close together?**
- Increase `separation` value
- Use exclusion zones
- Check other structure sets aren't conflicting

**Exclusion zones not working?**
- Verify structure set tags exist
- Check tag syntax is correct
- Ensure `chunk_count` is appropriate
- Verify referenced structure sets exist

## Best Practices

1. **Unique Salts** - Each structure set should have a unique salt value
2. **Appropriate Spacing** - Match spacing to structure rarity
3. **Exclusion Zones** - Use them to prevent structure overlap
4. **Structure Set Tags** - Group related structures for easier management
5. **Testing** - Test structure spawning in new worlds to verify placement

---

**Next:** Learn about [Placement Systems](Placement-Systems.md) for advanced placement options!

