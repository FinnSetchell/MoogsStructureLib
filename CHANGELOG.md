# Changelog

---

## [3.0.0] - 2026-05-27

Major feature release: a full structure-processor and pool-element toolkit, version-aware templates, terrain adaptation, and entity (armor stand) equipping.

### Added
- **Version-aware pool element** (`moogs_structures:versioned_single_pool_element`): resolves a different structure template per Minecraft version.
- **Mirroring pool element** (`moogs_structures:mirroring_single_pool_element`): a single pool element that can mirror its piece, with optional per-element enhanced terrain adaptation.
- **Structure processors:** `pillar_processor`, `spawner_randomizing_processor`, `equip_armor_stand_processor`, `close_off_fluid_sources_processor`, `remove_floating_blocks_processor`, `random_replace_with_properties_processor`, `super_gravity_processor`, `flood_with_water_processor`.
- **Entity processor framework** (`StructureEntityProcessor`): lets processors modify or equip entities as a structure is placed. 
- **Enhanced terrain adaptation** (beardifier): carves or buries terrain around pieces, with a configurable vertical `band` to confine carving to matching-height rows.
- **Basalt & delta suppression**: prevents basalt columns and basalt deltas from generating within structure piece bounds (structure tags `no_basalt`, `no_delta`).
- **Debug command**: `/moogs_structures debug keepjigsaws on|off|status` keeps jigsaw blocks in placed structures so their name/target/pool can be inspected in-world.

### Fixed
- Dependent mods (e.g. Moog's Nether Structures) failing to recognise this build as version 3.0.0.

---

### Added
- Version-aware `SinglePoolElement`, letting structure packs pick the right NBT per Minecraft version. This allows for wider version compatibility
- `/moogs_structures debug` command to toggle runtime diagnostics.
