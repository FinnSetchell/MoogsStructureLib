# Placement Systems

Placement systems control where structures spawn in the world. Moogs Structure Lib provides `AdvancedRandomSpread`, an enhanced version of Minecraft's random spread placement with additional features.

## Overview

Moogs Structure Lib uses `moogs_structures:advanced_random_spread` as its primary placement type. It extends vanilla random spread with:
- Super exclusion zones
- Minimum distance from spawn
- Enhanced spacing control
- Frequency reduction methods

## AdvancedRandomSpread

### Required Fields

#### `type`
**Type:** String  
**Example:** `"moogs_structures:advanced_random_spread"`

Must always be `"moogs_structures:advanced_random_spread"` for Moogs Structure Lib structures.

#### `salt`
**Type:** Integer  
**Example:** `203698201`

Unique identifier for this structure set. Affects which chunks are selected for structure placement. Each structure set should have a unique salt.

**Tip:** Use large random numbers. Avoid reusing salts across different structure sets.

#### `spacing`
**Type:** Integer (0 to Integer.MAX_VALUE)  
**Example:** `34`

Average number of chunks between structure attempts. Higher values mean rarer structures.

**Important:** 
- Must be greater than `separation`
- Internally multiplied by 1.65 by Moogs Structure Lib
- 1 chunk = 16 blocks

**Example:** `spacing: 34` means structures attempt to spawn approximately every 34 chunks (544 blocks), but internally becomes ~56 chunks (896 blocks).

#### `separation`
**Type:** Integer (0 to Integer.MAX_VALUE)  
**Example:** `26`

Minimum number of chunks between structures. Ensures structures don't spawn too close together.

**Important:**
- Must be less than `spacing`
- Internally multiplied by 1.65 by Moogs Structure Lib
- If `spacing <= separation`, structures cannot spawn properly!

**Example:** `separation: 26` means structures must be at least 26 chunks (416 blocks) apart, internally ~43 chunks (688 blocks).

### Optional Fields

#### `locate_offset`
**Type:** Vec3i (Array of 3 integers)  
**Example:** `[0, 0, 0]` or `[8, 0, 8]`

Offset for the `/locate` command. Adjusts where the locate command points when finding this structure.

**Default:** `[0, 0, 0]`

**Example:**
```json
{
  "locate_offset": [8, 0, 8]
}
```
This offsets the locate command by 8 blocks in X and Z.

#### `frequency`
**Type:** Float (0.0 to 1.0)  
**Example:** `1.0`

Spawn frequency multiplier. Lower values reduce spawn chance.

**Default:** `1.0` (100% spawn chance)

**Example:**
- `1.0` - Normal spawn rate
- `0.5` - 50% spawn rate (half as common)
- `0.1` - 10% spawn rate (very rare)

#### `frequency_reduction_method`
**Type:** String  
**Example:** `"DEFAULT"`

Algorithm used for frequency reduction. Inherited from vanilla Minecraft's RandomSpreadStructurePlacement.

**Default:** `"DEFAULT"`

**Note:** This is an advanced feature. Most users should use the default value.

#### `exclusion_zone`
**Type:** Exclusion Zone Object  
**Example:** See below

Standard exclusion zone from vanilla Minecraft. Prevents structures from spawning near other structure sets.

**Example:**
```json
{
  "exclusion_zone": {
    "chunk_count": 10,
    "other_set": "minecraft:villages"
  }
}
```

Prevents spawning within 10 chunks of villages.

#### `super_exclusion_zone`
**Type:** Super Exclusion Zone Object  
**Example:** See below

Enhanced exclusion zone unique to Moogs Structure Lib. More powerful than standard exclusion zones.

**Structure:**
```json
{
  "super_exclusion_zone": {
    "other_set": "#namespace:tag_name",
    "chunk_count": 5,
    "allowed_chunk_count": 10
  }
}
```

**Fields:**
- `other_set` - Structure set tag or array of structure sets to avoid
- `chunk_count` - Exclusion radius in chunks
- `allowed_chunk_count` - Optional override for allowed distance

**Example from MVS:**
```json
{
  "super_exclusion_zone": {
    "chunk_count": 3,
    "other_set": "#mvs:common_avoid"
  }
}
```

Prevents spawning within 3 chunks of any structure in the `#mvs:common_avoid` tag.

**How it works:**
- Checks if any structure from `other_set` exists within `chunk_count` chunks
- If found, prevents placement
- If `allowed_chunk_count` is specified and larger than `chunk_count`, allows placement only if structures exist within `allowed_chunk_count` range

#### `spread_type`
**Type:** String  
**Example:** `"LINEAR"`

Distribution type for structure placement.

**Options:**
- `"LINEAR"` - Linear distribution (default)
- `"TRIANGULAR"` - Triangular distribution

**Default:** `"LINEAR"`

**Note:** Most users should use `"LINEAR"`. `"TRIANGULAR"` creates a different distribution pattern but is rarely needed.

#### `min_distance_from_world_origin`
**Type:** Integer (0 to Integer.MAX_VALUE)  
**Example:** `800`

Minimum distance in blocks from world spawn (0, 0) before structures can spawn. Useful for rare structures that should only appear far from spawn.

**Example:**
```json
{
  "min_distance_from_world_origin": 1000
}
```

Structures will only spawn at least 1000 blocks from spawn.

**Common use cases:**
- Rare structures (crystals, cathedrals)
- End-game content
- Structures that should be discovered through exploration

## Complete Examples

### Basic Placement
```json
{
  "type": "moogs_structures:advanced_random_spread",
  "salt": 203698201,
  "spacing": 34,
  "separation": 26
}
```

Simple placement with no special features.

### With Exclusion Zone
```json
{
  "type": "moogs_structures:advanced_random_spread",
  "salt": 766544243,
  "spacing": 31,
  "separation": 26,
  "super_exclusion_zone": {
    "chunk_count": 3,
    "other_set": "#mvs:common_avoid"
  }
}
```

Prevents spawning near common structures.

### Rare Structure
```json
{
  "type": "moogs_structures:advanced_random_spread",
  "salt": 469982354,
  "spacing": 112,
  "separation": 62,
  "min_distance_from_world_origin": 800
}
```

Very rare structure that only spawns far from spawn.

### Reduced Frequency
```json
{
  "type": "moogs_structures:advanced_random_spread",
  "salt": 123456789,
  "spacing": 50,
  "separation": 35,
  "frequency": 0.5
}
```

Structure spawns at 50% of normal rate.

### With Locate Offset
```json
{
  "type": "moogs_structures:advanced_random_spread",
  "salt": 987654321,
  "spacing": 40,
  "separation": 30,
  "locate_offset": [8, 0, 8]
}
```

Locate command points to structure center offset by 8 blocks.

## Understanding Spacing and Separation

### Spacing vs Separation

**Spacing** controls average distance:
- Low spacing (20-30) = Common structures
- Medium spacing (50-70) = Uncommon structures  
- High spacing (100+) = Rare structures

**Separation** controls minimum distance:
- Should be 60-80% of spacing value
- Prevents structures from spawning too close
- Too high = structures become too rare

### The 1.65x Multiplier

Moogs Structure Lib multiplies both spacing and separation by 1.65 internally. This means:

**Your JSON:**
```json
{
  "spacing": 34,
  "separation": 26
}
```

**Internal values:**
- Spacing: 34 × 1.65 = 56.1 chunks
- Separation: 26 × 1.65 = 42.9 chunks

**Why?** This provides better structure distribution and prevents clustering. Also in the early days of MVS i wanted to make all the structures rarer without having to individually change all of the values... and its too late to go back now xD

## Exclusion Zones Explained

### Standard Exclusion Zone

Vanilla Minecraft exclusion zone:
```json
{
  "exclusion_zone": {
    "chunk_count": 10,
    "other_set": "minecraft:villages"
  }
}
```

Prevents spawning within 10 chunks of villages.

### Super Exclusion Zone

Moogs Structure Lib enhanced exclusion:
```json
{
  "super_exclusion_zone": {
    "chunk_count": 5,
    "other_set": "#mvs:common_avoid",
    "allowed_chunk_count": 10
  }
}
```

**Behavior:**
1. Checks for structures in `other_set` within `chunk_count` chunks
2. If found, prevents placement
3. If `allowed_chunk_count` is specified:
   - If structures exist within `allowed_chunk_count` range, allows placement
   - Otherwise, prevents placement

**Use case:** Allow structures only when other structures are nearby (or prevent when nearby).

## Common Patterns

### Common Structures
```json
{
  "spacing": 20,
  "separation": 15
}
```
Spawns frequently, close together.

### Uncommon Structures
```json
{
  "spacing": 50,
  "separation": 35
}
```
Moderate rarity, reasonable spacing.

### Rare Structures
```json
{
  "spacing": 100,
  "separation": 80,
  "min_distance_from_world_origin": 1000
}
```
Very rare, far from spawn.

### Structures Avoiding Others
```json
{
  "spacing": 30,
  "separation": 25,
  "super_exclusion_zone": {
    "chunk_count": 5,
    "other_set": "#namespace:avoid_tag"
  }
}
```
Prevents overlap with tagged structures.

## Troubleshooting

**Structures not spawning?**
- Verify `spacing > separation` (critical!)
- Check salt is unique
- Verify structure set JSON syntax
- Check exclusion zones aren't too restrictive

**Structures too common/rare?**
- Adjust spacing (higher = rarer)
- Remember the 1.65x multiplier
- Test in new worlds

**Structures spawning too close?**
- Increase separation
- Use exclusion zones
- Check other structure sets

**Exclusion zones not working?**
- Verify structure set tags exist
- Check tag syntax
- Ensure chunk_count is appropriate
- Test exclusion zone logic

## Best Practices

1. **Unique Salts** - Never reuse salts across structure sets
2. **Spacing > Separation** - Always ensure spacing is greater
3. **Appropriate Values** - Match spacing to structure rarity
4. **Exclusion Zones** - Use them to prevent overlap
5. **Testing** - Always test structure placement in new worlds
6. **Documentation** - Comment your placement values for future reference

---

**Next:** Learn about [NBT Files](NBT-Files) to understand where structure files go!

