# Changelog

---

## [2.0.0] - 2026-05-10

### Added
- Forge 1.21.4 support
- Version-aware `SinglePoolElement`, letting structure packs pick the right NBT per Minecraft version
- `/moogs_structures debug` command to toggle runtime diagnostics

### Changed
- Migrated build system from Architectury to MultiLoader (jaredlll08) template
- Replaced `@ExpectPlatform` with `ServiceLoader` pattern for platform abstraction
