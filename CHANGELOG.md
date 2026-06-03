# Changelog

---

## 3.0.0-alpha.2 — 2026-06-03

### Crash fixes
- Added missing `trial_spawner_randomizing_processor` and `vault_randomizing_processor`. These existed on every other 1.21+ MSL branch but the 1.21.2-1.21.3 propagation was missed in the initial cycle. Resolves a server crash when MNS 3.0.0-alpha tried to load `dragon_arena_pillars` / `large_arena_pillars` processor lists.

### Added
- Optional `nbt` field on `SpawnerRandomizingProcessor.weighted_entities`, bringing 1.21.2-1.21.3 into parity with other branches. Enables pre-equipped mob spawns from regular spawners.

---

## [3.0.0] - 2026-06-02

Major feature release: a full structure-processor and pool-element toolkit, version-aware templates, terrain adaptation, and entity (armor stand) equipping.

### Added
- **Version-aware pool element** (`moogs_structures:versioned_single_pool_element`): resolves a different structure template per Minecraft version, via a `locations` map of version ranges (e.g. `"1.21-1.21.4"`, `"1.21.5-26.1.2"`) with a fallback `location`.
- **Mirroring pool element** (`moogs_structures:mirroring_single_pool_element`): a single pool element that can mirror its piece, with optional per-element enhanced terrain adaptation.
- **Structure processors:**
  - `pillar_processor` - extends a pillar up or down from a trigger block until it reaches solid ground, with an optional altitude-aware block-state randomizer. Recognises legacy vanilla block ids (e.g. `minecraft:chain` from before the 1.21.9 rename) and rewrites them to their renamed modern equivalents so older authored structures keep generating correctly.
  - `spawner_randomizing_processor` - sets a mob spawner's mob from an inline weighted entity list (no external dependency).
  - `equip_armor_stand_processor` - equips armor stands from a weighted-random list of armor sets, with per-item enchantments and trims expressed in the vanilla item-component format.
  - `close_off_fluid_sources_processor`, `remove_floating_blocks_processor`, `random_replace_with_properties_processor`, `super_gravity_processor`, `flood_with_water_processor`.
- **Per-piece spawn counts** (`data/<namespace>/msl_pieces_spawn_counts/` and `..._additions/`): a datapack-driven cap on how many times each jigsaw piece may appear in a generated structure, so rare pieces stay rare without rebuilding the pool. The `_additions` variant lets downstream datapacks extend or override another mod's counts without forking the source file.
- **Entity processor framework** (`StructureEntityProcessor`): lets processors modify or equip entities as a structure is placed. On Fabric a mixin runs the entity processors during `StructureTemplate.placeEntities` (which vanilla never invokes the entity hook for); Forge/NeoForge use their native entity processing.
- **Enhanced terrain adaptation** (beardifier): carves or buries terrain around pieces, with a configurable vertical `band` to confine carving to matching-height rows.
- **Basalt & delta suppression**: prevents basalt columns and basalt deltas from generating within structure piece bounds (structure tags `no_basalt`, `no_delta`).
- **Debug command**: `/moogs_structures debug keepjigsaws on|off|status` keeps jigsaw blocks in placed structures so their name/target/pool can be inspected in-world.

---

## [2.0.2] - 2026-04-17

### Added
- added `/moogs_structures debug` command (on/off/toggle/status) for runtime debug logging, requires permission level 2
- added `versioned_single_pool_element` — a new jigsaw pool element type that lets datapacks specify different structure templates per Minecraft version range
- added Forge 1.21.2-1.21.3 support

### Fixed
- fixed a potential stack overflow crash when two structure sets reference each other via `super_exclusion_zone`, now detected and skipped per-thread
- fixed piece spawn count cache not clearing on datapack reload, which could cause stale max-count limits to persist across reloads
- fixed structures sometimes always generating the same layout instead of varying
- fixed structures occasionally spawning in or near water/lava when they shouldn't
- improved spawn height accuracy, reducing structures floating or sinking into terrain
- fixed a precision bug that could cause structures to clip into each other or fail to place
- improved structure overlap detection to better prevent intersecting structures
- optimized structure collision checks, improving world generation performance
- fixed a potential crash with unrecognized structure connection types

### Changed
- migrated build system from Architectury to Multiloader template
- replaced `@ExpectPlatform` with ServiceLoader pattern for platform abstraction
