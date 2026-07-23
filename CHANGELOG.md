## [3.0.6] - Unreleased

### Fixed
- worldgen crash (`NoSuchElementException` in `computeDensity`) when many chunks generate at once near a large enhanced-adaptation structure. this is the real cause of the crash 3.0.5 tried to fix; no other mod is involved. the enhanced beardifier kept single-use fastutil iterators as mutable state on the vanilla `Beardifier`, draining them per noise cell and rewinding with `back(Integer.MAX_VALUE)` to reuse them. that only works if one thread evaluates the beardifier at a time, so under parallel chunk generation the shared cursor advanced between `hasNext()` and `next()`. msl now stores the piece and junction lists and iterates them locally per call, the way modern vanilla does.
- enhanced terrain adaptation no longer writes per-chunk data onto vanilla's shared `Beardifier.EMPTY` singleton. vanilla returns `EMPTY` whenever no *vanilla* terrain-adapting structure touches a chunk, which is the usual case for msl structures, so every chunk generating at the same time was reading and overwriting one another's structure pieces and affected bounding box. this is what made the iterator shared across threads in the first place, and on its own it could produce terrain that differed between runs. msl now uses a private beardifier instance for those chunks and leaves the singleton alone.

---

## [3.0.5] - 2026-07-19

### Fixed
- worldgen crash on chunk generation when another mod's beardifier / density function mixin invalidates the enhanced-beardifier iterator between hasNext() and next(). fastutil returns a null slot instead of throwing, and computeDensity NPE'd on the next line. reported on 26.2 fabric with terralith installed (issue #12).

---

## [3.0.4] - 2026-07-12

### Fixed
- version-mapping fallback warnings are now silent by default; enable with `/moogs_structures debug on` to see them (issue #10).

---

# Changelog

## [3.0.3] - 2026-07-09

### Fixed
- worldgen crash ("parent chunk missing") when yung's api is installed on fabric. the 3.0.2 rename fix wasn't enough: both mods were swapping out the beardifier at the same mixin point, so whichever ran first lost its terrain adaptation data and chunk generation died. msl now modifies the vanilla beardifier in place and applies its mixin after yung's, so both mods' terrain adaptation works at the same time.

## [3.0.2] - 2026-07-09

### Fixed
- worldgen hang from beardifier mixin method names colliding with yung's api. renamed the methods so both mods coexist.

## [3.0.1] - 2026-06-22

### Fixed
- mixin compatibility level bumped to java 25 on fabric and neoforge configs (was java 21), matching the mc 26.2 class version. previously the neoforge `StructurePoolMixin` and fabric `EntityProcessorMixin` were silently skipped at load.

## [3.0.0] - 2026-06-21 — MC 26.2

### Changed
- ported to mc 26.2 (chaos cubed). no api changes.
- vulkan renderer backend (experimental, opt-in via game settings): msl has no rendering code; confirmed no impact on structure or processor behaviour.

## [3.0.0] - 2026-06-20

### Added
- **Structure processors:**
  - `pillar_processor` - extends a pillar up or down from a trigger block until it reaches solid ground, with an optional altitude-aware block-state randomizer. Recognises legacy vanilla block ids (e.g. `minecraft:chain` from before the 1.21.9 rename) and rewrites them to their renamed modern equivalents so older authored structures keep generating correctly.
  - `spawner_randomizing_processor` - sets a mob spawner's mob from an inline weighted entity list (no external dependency). Each weighted entry may carry an optional `nbt` field so spawners can ship pre-equipped or otherwise pre-configured mobs without an extra processor.
  - `trial_spawner_randomizing_processor` - writes a chosen trial-spawner configuration into placed trial spawners, with an optional ominous variant. Uses inline configs on MC 1.21 - 1.21.4 and references the vanilla `minecraft:trial_spawner` registry on 1.21.5+.
  - `vault_randomizing_processor` - assigns a loot table and key item to a placed vault, automatically picking the ominous variant for blocks with the ominous blockstate set.
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
