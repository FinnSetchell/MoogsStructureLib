# Changelog

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

### Fixed
- Structures with enchanted armor on armor stands failing to load.
- Pillar structures that place chains failing to generate (chains were renamed to iron chains in Minecraft 1.21.9).

---

## [2.0.3] - 2026-04-18

### Fixed
- Fixed Forge build failing due to 1.21.11 API changes (eventbus 7.0.1 rewrite, `ResourceKey.identifier()`, `Identifier` rename)

---

## [2.0.2] - 2026-04-17

### Added
- Forge 1.21.11 support

### Changed
- Migrated build system from Architectury to Multiloader template
- Replaced `@ExpectPlatform` with ServiceLoader pattern for platform abstraction
- Updated fabric-loom to 1.9

### Fixed
- Fixed compatibility with 1.21.11 API changes (StringTag, FabricRegistryBuilder)
- Fixed structures sometimes always generating the same layout instead of varying
- Fixed structures occasionally spawning in or near water/lava when they shouldn't
- Improved spawn height accuracy, reducing structures floating or sinking into terrain
- Fixed a precision bug that could cause structures to clip into each other or fail to place
- Improved structure overlap detection to better prevent intersecting structures
- Optimized structure collision checks, improving world generation performance
- Fixed a potential crash with unrecognized structure connection types
- Reduced piece placement retry limit, improving generation speed
- Fixed sourcesJar duplicate AT entry in Forge

---

## [1.1.0]

### Updated
Updated to mc 1.21.11
- Credits to [Acuadragon100](https://github.com/Acuadragon100)
