# Changelog

---

## [3.0.0] - 2026-05-27

Major feature release: a full structure-processor and pool-element toolkit, version-aware templates, terrain adaptation, and entity (armor stand) equipping.

### Added
- **Version-aware pool element** (`moogs_structures:versioned_single_pool_element`): resolves a different structure template per Minecraft version, via a `locations` map of version ranges (e.g. `"1.21-1.21.4"`, `"1.21.5-26.1.2"`) with a fallback `location`.
- **Mirroring pool element** (`moogs_structures:mirroring_single_pool_element`): a single pool element that can mirror its piece, with optional per-element enhanced terrain adaptation.
- **Structure processors:**
  - `pillar_processor` — extends a pillar up or down from a trigger block until it reaches solid ground, with an optional altitude-aware block-state randomizer.
  - `spawner_randomizing_processor` — sets a mob spawner's mob from an inline weighted entity list (no external dependency).
  - `equip_armor_stand_processor` — equips armor stands from a weighted-random list of armor sets, with per-item enchantments and trims expressed in the vanilla item-component format.
  - `close_off_fluid_sources_processor`, `remove_floating_blocks_processor`, `random_replace_with_properties_processor`, `super_gravity_processor`, `flood_with_water_processor`.
- **Entity processor framework** (`StructureEntityProcessor`): lets processors modify or equip entities as a structure is placed. On Fabric a mixin runs the entity processors during `StructureTemplate.placeEntities` (which vanilla never invokes the entity hook for); Forge/NeoForge use their native entity processing.
- **Enhanced terrain adaptation** (beardifier): carves or buries terrain around pieces, with a configurable vertical `band` to confine carving to matching-height rows.
- **Basalt & delta suppression**: prevents basalt columns and basalt deltas from generating within structure piece bounds (structure tags `no_basalt`, `no_delta`).
- **Debug command**: `/moogs_structures debug keepjigsaws on|off|status` keeps jigsaw blocks in placed structures so their name/target/pool can be inspected in-world.

---

## [2.0.2] - 2026-04-17

### Added
- added Forge 1.21.5-1.21.10 support

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
- reduced piece placement retry limit, improving generation speed



### Changed
- migrated build system from Architectury to Multiloader template
- replaced `@ExpectPlatform` with ServiceLoader pattern for platform abstraction