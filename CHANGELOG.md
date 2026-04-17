# Changelog

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
