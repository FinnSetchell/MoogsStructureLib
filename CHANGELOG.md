# Changelog

---

## [2.0.3] - 2026-04-16

### Fixed
- fixed mixin refmap not being bundled in the fabric jar, causing a crash on launch

---

## [2.0.2] - 2026-04-14

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

## [2.0.1] - 2026-04-14

### Changed
- migrated build system from Architectury to Multiloader template
- replaced `@ExpectPlatform` with ServiceLoader pattern for platform abstraction

### Added
- Forge 1.21.5 support