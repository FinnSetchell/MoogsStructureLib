# Changelog

## [3.1.0] - 2026-08-13

### Added
- **Replace-vanilla presets**: consumer mods declare replacement presets in `data/<namespace>/moogs_structures/replace_vanilla.json`; MSL aggregates them from every loaded mod into `config/moogs_structures.json` as per-preset on/off toggles, so adding a new replacement needs no lib update. The config re-reads on world load.
  - `vanilla_loot_swap_processor` - rewrites a container's loot table to a vanilla equivalent while its preset is enabled, so mods that inject into the vanilla loot table still fill the replacing structure's chests. No-op when the preset is off.
  - `DisableVanillaStructureMixin` - cancels generation of a replaced vanilla structure while its preset is enabled.
  - `/locate` on a replaced vanilla structure now reports which structure replaced it and points at the config instead of running the vanilla search.
  - `conditional_concentric_rings` structure placement - a concentric-rings placement whose ring count switches on a replacement preset (e.g. full density when replacing, a reduced density when coexisting). Extends the vanilla type so the special ring handling still applies.
- **In-game config screen**: an optional Cloth Config screen listing every mod's replacement presets as toggles, reached from Mod Menu on Fabric and the mod-list config button on Forge. Cloth Config (and Mod Menu on Fabric) are soft dependencies - absent them, the mod still runs without a screen.
- **Structure controls in the config screen** (a "Structures" tab, driven by a per-mod `structures` block in `replace_vanilla.json`):
  - **Spacing multipliers** - a universal rarity slider plus per-mod and per-structure sliders that scale a structure's spacing/separation (effective = universal x per_mod x per_structure). Applied by `advanced_random_spread` (via an optional `spacing_key`) and read once per world load.
  - **Disable** - a per-structure toggle that stops any Moogs structure from generating, enforced by structure id at `tryGenerateStructure` (works for every placement type). `/locate` on a disabled structure reports it instead of searching.
  - **Preview** - a per-structure button opening the online structure preview (built from a per-mod `preview_url_template`) via the vanilla confirm-link screen.
- **Datapack-declared structures**: the config screen reads its `structures` blocks from loaded datapacks as well as mod jars (rescanned on datapack reload), so datapacks can expose their structures to the spacing/disable/preview controls.
- Config changes now also re-read on `/reload`, not only on world load (they still affect newly generated chunks only).

### Fixed
- `always_false` json condition returned true instead of false.

## [3.0.2] - 2026-07-12

### Fixed
- version-mapping fallback warnings are now silent by default; enable with `/moogs_structures debug on` to see them (issue #10).

## [3.0.1] - 2026-07-09

### Fixed
- worldgen hang from beardifier mixin method names colliding with yung's api. renamed the methods so both mods coexist.

## [3.0.0] - 2026-06-20

### Added
- **Structure processors:**
  - `pillar_processor` - extends a pillar up or down from a trigger block until it reaches solid ground, with an optional altitude-aware block-state randomizer. Recognises legacy vanilla block ids (e.g. `minecraft:chain` from before the 1.21.9 rename) and rewrites them to their renamed modern equivalents so older authored structures keep generating correctly.
  - `spawner_randomizing_processor` - sets a mob spawner's mob from an inline weighted entity list (no external dependency). Each weighted entry may carry an optional `nbt` field so spawners can ship pre-equipped or otherwise pre-configured mobs without an extra processor.
  - `equip_armor_stand_processor` - equips armor stands from a weighted-random list of armor sets, with per-item enchantments and trims expressed in the vanilla item-component format.
  - `close_off_fluid_sources_processor`, `remove_floating_blocks_processor`, `random_replace_with_properties_processor`, `super_gravity_processor`, `flood_with_water_processor`.
- **Per-piece spawn counts** (`data/<namespace>/msl_pieces_spawn_counts/` and `..._additions/`): a datapack-driven cap on how many times each jigsaw piece may appear in a generated structure, so rare pieces stay rare without rebuilding the pool. The `_additions` variant lets downstream datapacks extend or override another mod's counts without forking the source file.
- **Entity processor framework** (`StructureEntityProcessor`): lets processors modify or equip entities as a structure is placed. On Fabric a mixin runs the entity processors during `StructureTemplate.placeEntities` (which vanilla never invokes the entity hook for); Forge/NeoForge use their native entity processing.
- **Enhanced terrain adaptation** (beardifier): carves or buries terrain around pieces, with a configurable vertical `band` to confine carving to matching-height rows.
- **Basalt & delta suppression**: prevents basalt columns and basalt deltas from generating within structure piece bounds (structure tags `no_basalt`, `no_delta`).
- **Nether jigsaw structures**: raised size cap to 128 pieces, added a `FIXED_HEIGHT` land search direction for structures that must place at a specific Y, and stricter `y_allowance` enforcement on final placement.
- **Debug command**: `/moogs_structures debug keepjigsaws on|off|status` keeps jigsaw blocks in placed structures so their name/target/pool can be inspected in-world.

### Fixed
- dependent mods (e.g. Moog's Nether Structures) failing to recognise this build as version 3.0.0.
- structures with enchanted armor on armor stands failing to load.

---

## 2.0.1 (2026-04-13)
- Updated to mc 26.1.2
- Ported build system from Architectury to Jared's MultiLoader template
