# Armor-stand processor smoke test

Catches silent NBT-key drift in `EquipArmorStandProcessor` before it reaches players.

## Background

MC 1.21.5 changed the armor-stand entity format: it stopped reading the legacy `ArmorItems`
list and started reading an `equipment` compound (`equipment.chest`, `equipment.head`, etc.).
The processor still ran without errors on 1.21.5+, but armor stands spawned naked because the
key it wrote (`ArmorItems`) was silently ignored.

The test directly calls `EquipArmorStandProcessor.processEntity()` and asserts that the
output NBT contains the slot the running MC version actually reads.

## Running the test

**Single branch (current checkout):**
```
./gradlew :neoforge:runGameTestServer
```
The task exits with code 0 on pass, non-zero on fail. Game-test output goes to the console
and to `neoforge/runs/gameTestServer/`.

**All branches (outer harness):**
```powershell
.\scripts\test-armor-stand.ps1
```
The script creates a temporary git worktree for each branch, runs the game-test server in
that worktree, tears it down, and prints a markdown matrix. Exit code is non-zero if any
branch fails. Test a subset with `-Only`:
```powershell
.\scripts\test-armor-stand.ps1 -Only "26.1.0-26.1.2","1.21.4"
```

## What it catches

- **NBT-key drift in the processor.** The write key changing (`ArmorItems` -> `equipment`,
  or any future rename) shows up immediately as a missing key in the assertion.
- **Write-key removal.** If someone removes the `equipment.put(...)` call entirely, the
  assertion on `equipment.contains("chest")` fails.
- **Codec decode failure.** If the `armor_sets` JSON shape changes incompatibly, the
  `processor.result().orElseThrow()` call fails before any NBT is checked.

## What it does NOT catch

- **Worldgen pool failures.** The test calls `processEntity()` directly; it does not place
  a jigsaw structure in the world. A broken pool element or fallback pool would not be caught
  here.
- **Biome-restriction bugs.** No worldgen runs during the test.
- **Processor ordering issues.** Only one processor in the test; multi-processor interactions
  are not covered.
- **MSL's entity-processor mixin hookup.** The mixin that routes entity processors during
  structure template placement is not exercised. If the mixin were accidentally removed, this
  test would still pass (nothing would route the call in production, but the processor logic
  itself is correct). The mixin has been stable across all MSL versions.

## Adding a new MC version

1. Create the new branch in the usual way.
2. Cherry-pick or propagate `feat/armor-stand-smoketest` to it:
   ```
   git cherry-pick <commit-sha-of-test-class>
   ```
3. Update the assertion key if the armor-stand entity format changed for this MC version.
   - Pre-1.21.5: assert `ArmorItems` (index 2 = chest)
   - 1.21.5+: assert `equipment.chest`
4. Add the branch to the `$BRANCHES` table in `scripts/test-armor-stand.ps1`.
5. Verify locally with `./gradlew :neoforge:runGameTestServer` on the new branch.

## Branch propagation status

The test class lives in `neoforge/src/main/java/.../gametest/EquipArmorStandProcessorTest.java`
(and `forge/...` for the `1.20-1.20.4` branch which has no NeoForge).

The harness skips branches where the test class is absent and prints a reminder to propagate.

Current status:

| Branch | Propagated |
|---|---|
| 26.1.0-26.1.2 | yes (origin branch) |
| 1.21.11 | no |
| 1.21.5-1.21.10 | no |
| 1.21.4 | no |
| 1.21.2-1.21.3 | no |
| 1.21-1.21.1 | no |
| 1.20.5-1.20.6 | no |
| 1.20-1.20.4 | no (needs forge/ placement) |

## Test structure resource

`neoforge/src/main/resources/data/moogs_structures/structure/armor_stand_processor_test_empty.nbt`

A minimal 1x1x1 gzip-compressed NBT structure with no blocks and no entities. Required by
the NeoForge GameTest framework (every `@GameTest` method needs a template). The template
exists only to satisfy the framework -- the test does not interact with the world.

To regenerate it for a different MC version (DataVersion change), run:
```powershell
.\scripts\gen-empty-structure.ps1 -DataVersion <n> -Out neoforge\src\main\resources\data\moogs_structures\structure\armor_stand_processor_test_empty.nbt
```
(See `scripts/gen-empty-structure.ps1`. For an empty structure the DataVersion value does
not matter since there are no blocks or entities to migrate through DFU.)
