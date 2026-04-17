# Changelog

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