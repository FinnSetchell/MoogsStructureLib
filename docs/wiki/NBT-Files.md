# NBT Files

NBT files contain the actual structure data - the blocks, entities, and layout of your structure. This page explains where to place them and how to reference them.

## File Location

NBT structure files are stored in a **different location** than structure JSON files:

```
data/<namespace>/structure/<path>/<name>.nbt
```

**Important:** This is `structure/`, NOT `worldgen/structure/`!

**Example:** `data/mvs/structure/carts/cart.nbt`

## Creating NBT Files

### Using Structure Blocks

1. **Build your structure** in Minecraft (creative mode recommended)
2. **Place a Structure Block** (obtain with `/give @s structure_block`)
3. **Set the structure block:**
   - Mode: **Save**
   - Structure Name: Your structure name (e.g., `cart`)
   - Relative Position: Set corner positions
   - Size: Adjust to fit your structure
4. **Save the structure** by clicking "Save"
5. **Export the structure:**
   - The NBT file is saved to your world's `generated/structures/` folder
   - Copy it to your datapack's `data/<namespace>/structure/` folder

### Structure Block Settings

**Structure Name:**
- Use lowercase with underscores: `cart`, `barn`, `mineshaft_entrance`
- This becomes part of the resource location

**Relative Position:**
- Set the two opposite corners of your structure
- Structure block shows a preview outline

**Size Limits:**
- Maximum 48×48×48 blocks per structure
- For larger structures, use jigsaw connections

### Exporting Structures

After saving in a structure block:

1. Navigate to your world save folder
2. Go to `generated/structures/<namespace>/<name>.nbt`
3. Copy the file to your datapack:
   ```
   data/<namespace>/structure/<name>.nbt
   ```

**Example:**
- World save: `saves/MyWorld/generated/structures/mvs/cart.nbt`
- Datapack: `datapacks/my_pack/data/mvs/structure/carts/cart.nbt`

## Referencing NBT Files in Template Pools

In template pool JSON files, reference NBT files using resource locations:

```json
{
  "location": "namespace:path/to/structure"
}
```

**Important Rules:**
1. **No `.nbt` extension** - Don't include `.nbt` in the path
2. **Use forward slashes** - Use `/` not `\`
3. **Match directory structure** - Path should match folder structure

### Examples

**File:** `data/mvs/structure/carts/cart.nbt`  
**Reference:** `"mvs:carts/cart"`

**File:** `data/mvs/structure/houses/barn.nbt`  
**Reference:** `"mvs:houses/barn"`

**File:** `data/mvs/structure/mineshaft/entrance.nbt`  
**Reference:** `"mvs:mineshaft/entrance"`

### Complete Example

**Template Pool:** `data/mvs/worldgen/template_pool/carts/cart/start_pool.json`

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

The `"location": "mvs:carts/cart"` references `data/mvs/structure/carts/cart.nbt`.

## Naming Conventions

### Good Naming
- `cart.nbt` - Simple, clear
- `barn.nbt` - Descriptive
- `mineshaft_entrance.nbt` - Descriptive with context
- `large_cart_1.nbt` - Variant with number

### Avoid
- `Cart.nbt` - Use lowercase
- `cart structure.nbt` - No spaces
- `cart.nbt.nbt` - No double extension
- `cart_v2_final.nbt` - Keep names simple

## Jigsaw Structures

For jigsaw structures, you need multiple NBT files that connect together:

### Jigsaw Blocks

In your NBT files, place **Jigsaw Blocks** to define connection points:

1. **Place a Jigsaw Block** where you want connections
2. **Set Jigsaw Block properties:**
   - **Name:** The pool to connect to (e.g., `mvs:mineshaft/straight`)
   - **Target:** The jigsaw block name in target pieces (e.g., `mineshaft_entrance`)
   - **Joint:** `rollable` or `aligned` (usually `rollable`)

### Example: Mineshaft

**Entrance piece** (`entrance.nbt`):
- Contains jigsaw blocks with:
  - Name: `mvs:mineshaft/straight`
  - Target: `mineshaft_entrance`

**Straight piece** (`straight_1.nbt`):
- Contains jigsaw blocks with:
  - Name: `mvs:mineshaft/straight` (connects to more straights)
  - Target: `mineshaft_entrance` (connects back to entrance)
- Also has jigsaw blocks for intersections, dead ends, etc.

### Jigsaw Block Naming

**Name field:** References the template pool
- `mvs:mineshaft/straight` → Pool at `data/mvs/worldgen/template_pool/mineshaft/straight.json`

**Target field:** Matches jigsaw block names in other pieces
- `mineshaft_entrance` → Jigsaw blocks in other pieces with this name

## Version-Aware Structures

For structures that need different NBT files per Minecraft version, use version-aware pool elements. See [Advanced Topics](Advanced-Topics.md) for details.

## Common Issues

### Structure Not Found
**Problem:** Template pool can't find NBT file

**Solutions:**
- Check file path matches reference exactly
- Verify no `.nbt` extension in reference
- Ensure file exists in `structure/` folder (not `worldgen/structure/`)
- Check namespace matches

### Wrong Structure Loading
**Problem:** Wrong NBT file loads

**Solutions:**
- Verify `location` field matches file path
- Check file names are correct
- Ensure no typos in paths

### Jigsaw Not Connecting
**Problem:** Jigsaw pieces don't connect

**Solutions:**
- Verify jigsaw block names match pool names
- Check jigsaw block targets match
- Ensure target pools exist
- Verify jigsaw block properties are set correctly

---

**Next:** Learn about [Advanced Topics](Advanced-Topics.md) including version-aware structures!

