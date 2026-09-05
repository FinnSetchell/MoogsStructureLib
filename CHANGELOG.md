# Changelog

---

## [3.2.0-alpha.1] - 2026-09-05

### Added
- Forge is now supported on Minecraft 26.1.1 and 26.1.2 (Forge itself does not exist for 26.1.0)

### Fixed
- Fixed a world generation crash when a structure sets `burying_type` alongside only one of
  `min_y_allowed` / `max_y_allowed`. The absent bound is now ignored rather than throwing,
  matching how the surrounding y-allowance checks already behave. Packs that set both bounds
  are unaffected.

---

## [3.1.2] - 2026-08-19

### Fixed
- Fixed a possible crash during world generation or `/reload` when a structure with piece-count limits was being placed on several threads at once.
- Armour stands and other structure entity processors now generate correctly on every loader, not only Fabric.
- Structures containing item frames, glow item frames, paintings or leash knots no
  longer spam "invalid position" errors into the log.
  - Applied automatically to any piece placed through one of MSL's pool element types
    (`versioned_single_pool_element`, `mirroring_single_pool_element`,
    `legacy_ocean_bottom_single_pool_element`).
  - Pieces that still use vanilla's `minecraft:single_pool_element` are unaffected and
    will keep logging. Moog's structures are moving onto the MSL element types over
    time, so these errors will thin out release by release.
  - A pack can opt a piece in early by adding
    `moogs_structures:hanging_entity_anchor_processor` to its processor list. This also
    covers modded entities that store a `block_pos` anchor, not just the vanilla four.

---

## [3.1.1] - 2026-08-18

### Fixed
- The mod-list config button did nothing when Cloth Config was not installed. It now opens a short screen telling the player the in-game config screen requires Cloth Config.

---

## [3.1.0] - 2026-08-13

### Added
- **Replace-vanilla presets**: consumer mods declare replacement presets in `data/<namespace>/moogs_structures/replace_vanilla.json`; MSL aggregates them from every loaded mod into `config/moogs_structures.json` as per-preset on/off toggles, so adding a new replacement needs no lib update. The config re-reads on world load.
  - `vanilla_loot_swap_processor` - rewrites a container's loot table to a vanilla equivalent while its preset is enabled, so mods that inject into the vanilla loot table still fill the replacing structure's chests. No-op when the preset is off.
  - `DisableVanillaStructureMixin` - cancels generation of a replaced vanilla structure while its preset is enabled.
  - `/locate` on a replaced vanilla structure now reports which structure replaced it and points at the config instead of running the vanilla search.
  - `conditional_concentric_rings` structure placement - a concentric-rings placement whose ring count switches on a replacement preset (e.g. full density when replacing, a reduced density when coexisting). Extends the vanilla type so the special ring handling still applies.
- **In-game config screen**: an optional Cloth Config screen listing every mod's replacement presets as toggles, reached from Mod Menu on Fabric and the mod-list config button on Forge. Cloth Config (and Mod Menu on Fabric) are soft dependencies - absent them, the mod still runs without a screen.
- **Structure controls in the config screen** (a "Structures" tab): every loaded mod and datapack that ships structures placed by an MSL placement type (`advanced_random_spread` / `conditional_concentric_rings`) is listed automatically, grouped by mod, with no opt-in file required (rescanned on datapack reload). Each structure gets:
  - **Spacing multipliers** - a universal rarity slider plus per-mod and per-structure sliders that scale a structure's spacing/separation (effective = universal x per_mod x per_structure), applied by `advanced_random_spread` and read once per world load. The owning structure_set id is stamped onto each placement at world load, so a set needs no `spacing_key`/`structure_id` in its JSON for the slider and disable toggle to work.
  - **Disable** - a per-structure toggle that stops a structure from generating, enforced both by structure id at `tryGenerateStructure` and at the placement, so a disabled structure also stops reporting placement positions (no longer influencing neighbours' exclusion zones or computing concentric rings). `/locate` on a disabled structure reports it instead of searching.
  - **Preview** - a per-structure button opening the online structure preview via the vanilla confirm-link screen. The URL is built from an optional per-mod `mod_slug` and the running game version, so the link tracks whatever Minecraft version the pack is played on. A mod that sets no `mod_slug` shows the button disabled with a note.
- **Config auto-derivation**: a mod exposes its structures to these controls by declaring a single `mod_slug` line in its `structures` block (or nothing at all, for spacing and disable without previews) rather than hand-listing every structure. Display names, spacing keys and preview paths are derived from each structure_set; an explicit `entries` array still overrides the derived rows.
- Config changes now also re-read on `/reload`, not only on world load (they still affect newly generated chunks only).
- **Config screen support links**: Discord and Ko-fi icon buttons in the top-right of the config screen, each with a close (×) that hides it for good.

### Fixed
- `msl_pieces_spawn_counts` per-piece spawn counts never applied: the reload listener was never registered, so the data was never loaded. It is now registered, and the per-piece max-count cache is cleared on reload so `/reload` updates it.
- `advanced_random_spread` could compute torn spacing/separation values under concurrent chunk generation (e.g. with C2ME), risking a divide-by-zero during worldgen or misplaced structures; the effective values are now read from a single immutable snapshot.
- `always_false` json condition returned true instead of false.

![structure_settings](https://pub-24a4e0e7ea8544a5b6f73c3a23512589.r2.dev/images/1e6791907ddb4befa6891dcf9e335ae5.png)

![replace_vanilla_config](https://pub-24a4e0e7ea8544a5b6f73c3a23512589.r2.dev/images/b3ae11317f2b45819bf8b002be2e4ff0.png)

---

## [3.0.6] - 2026-07-24

### Fixed
- fixed a world generation crash near some structures, usually when flying or teleporting into new terrain. 3.0.5 tried to fix this and didn't â€” this release does.

---

## [3.0.5] - 2026-07-19

### Fixed
- worldgen crash on chunk generation when another mod's beardifier / density function mixin invalidates the enhanced-beardifier iterator between hasNext() and next(). fastutil returns a null slot instead of throwing, and computeDensity NPE'd on the next line. reported on 26.2 fabric with terralith installed (issue #12).

---

## [3.0.4] - 2026-07-12

### Fixed
- version-mapping fallback warnings are now silent by default; enable with `/moogs_structures debug on` to see them (issue #10).

---

## [3.0.3] - 2026-07-09

### Fixed
- worldgen crash ("parent chunk missing") when yung's api is installed on fabric. the 3.0.2 rename fix wasn't enough: both mods were swapping out the beardifier at the same mixin point, so whichever ran first lost its terrain adaptation data and chunk generation died. msl now modifies the vanilla beardifier in place and applies its mixin after yung's, so both mods' terrain adaptation works at the same time.

## [3.0.2] - 2026-07-09

### Fixed
- worldgen hang from beardifier mixin method names colliding with yung's api. renamed the methods so both mods coexist.

## [3.0.1] - 2026-06-22

### Fixed
- mixin compatibility level bumped to java 25 on fabric and neoforge configs (was java 21), matching the mc 26.1 class version. previously the neoforge `StructurePoolMixin` and fabric `EntityProcessorMixin` were silently skipped at load.

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
