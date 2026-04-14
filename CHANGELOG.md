# Changelog

---

## [1.0.4] - 2026-04-14

### Fixed
- fixed structures sometimes always generating the same layout instead of varying
- fixed structures occasionally spawning in or near water/lava when they shouldn't
- improved spawn height accuracy, reducing structures floating or sinking into terrain
- fixed a precision bug that could cause structures to clip into each other or fail to place
- improved structure overlap detection to better prevent intersecting structures
- optimized structure collision checks, improving world generation performance
- fixed a potential crash with unrecognized structure connection types
- reduced piece placement retry limit, improving generation speed

---

## [1.0.3] - 2026-04-14

### Changed
- compressed and removed duplicate icon.png
- migrated build system from Architectury to Multiloader template
- replaced `@ExpectPlatform` with ServiceLoader pattern for platform abstraction

### Added
- Forge 1.21.2 support
