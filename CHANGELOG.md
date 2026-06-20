# Changelog

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
