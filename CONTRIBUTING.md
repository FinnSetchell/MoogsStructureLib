# Contributing to Moog's Structure Lib

## How the repo is laid out

Everything happens on `main`. One source tree builds every Minecraft version and every loader, using
[Stonecutter](https://stonecutter.kikugie.dev/). The old per-version branches are kept under
`archive/` so you can look back at them, but they are no longer updated.

Each Minecraft version and loader pair is its own Gradle project, called a node, for example
`1.21.1-fabric`. The nodes are listed in `settings.gradle.kts` and Gradle creates them under `versions/`.
A node is compiled against one Minecraft version and published for a range of them:

| Range | Node | Fabric | Forge | NeoForge |
| --- | --- | --- | --- | --- |
| 1.20-1.20.4 | `1.20` | yes | yes | no |
| 1.20.5-1.20.6 | `1.20.6` | yes | no | yes |
| 1.21-1.21.1 | `1.21.1` | yes | yes | yes |
| 1.21.2-1.21.3 | `1.21.2` | yes | yes | yes |
| 1.21.4 | `1.21.4` | yes | yes | yes |
| 1.21.5-1.21.8 | `1.21.5` | yes | not yet | yes |
| 1.21.10 | `1.21.10` | yes | yes | yes |
| 1.21.11 | `1.21.11` | yes | yes | yes |
| 26.1-26.1.2 | `26.1.2` | yes | yes | yes |
| 26.2 | `26.2` | yes | yes | yes |
| 26.3 | `26.3` | yes | yes | yes |

## Setup

You need Java 25 to run Gradle. The Java 17 and 21 toolchains that the older nodes use are
downloaded for you.

In IntelliJ, install the Stonecutter Dev plugin. It adds a dropdown for picking which version the
code is shown as.

## Building

Build one node:

```bash
./gradlew :1.21.1-fabric:build
```

Build every node and copy the jars to `build/libs/<mod version>/`:

```bash
./gradlew buildAndCollect
```

The first full build takes a while, because every node sets up its own copy of Minecraft.

## Where things are

- `src/main/java` - all the Java. Loader code goes in a `fabric`, `forge` or `neoforge` package, and
  each loader's build leaves out the other two.
- `src/main/resources` - resources every loader uses.
- `src/<loader>/resources` - loader metadata, service files and that loader's mixin config.
- `src/main/access` - the access widener and access transformer. There are a few versions of them,
  because the classes they open moved between Minecraft versions.
- `stonecutter.properties.toml` - settings for each node: the Minecraft version it compiles against,
  the range it supports and its dependency versions.
- `build.fabric.gradle.kts`, `build.forge.gradle.kts`, `build.neoforge.gradle.kts` - one build script
  per loader. `build.forge-legacy.gradle.kts` builds Forge for 1.20-1.20.4.

## Writing code for different versions

The code in the repo is written for 1.21.1 Fabric. When something is different on other versions,
wrap it in a Stonecutter comment:

```java
//? if >=1.21.2 {
/*public static boolean isFullCube(BlockState state) {
*///?} else {
public static boolean isFullCube(BlockGetter world, BlockPos pos, BlockState state) {
//?}
```

The code for the versions you're not looking at stays commented out. Switch versions with the
IntelliJ plugin, or with `./gradlew "Set active project to 26.3-fabric"`. Run
`./gradlew "Reset active project"` before you commit, so the repo goes back to 1.21.1.

Some things to know:

- Write `ResourceLocation`. Stonecutter changes it to `Identifier` on 1.21.11 and later, so don't use
  either word inside any other name.
- Don't start a version block with a comment line. Stonecutter thinks the block is already commented
  out and breaks it.
- If a mixin only exists on some versions, just wrap its class. The mixin configs list every mixin,
  and each build only keeps the ones it actually compiled.
- If a lot of a method changes between versions, a small helper is usually easier to read than lots
  of version blocks.

## Adding a Minecraft version

1. Add the node to `settings.gradle.kts`.
2. Add its settings to `stonecutter.properties.toml`.
3. Switch to it and fix whatever doesn't compile, using version blocks.
4. Add a build for it to each target in `.github/moogs-publish.yml`.

## Releasing

Update `mod_version` in `gradle.properties` and add a section for it to `CHANGELOG.md`. Then tag `main`
once for each loader, for example `3.5.0-fabric`, `3.5.0-forge` and `3.5.0-neoforge`.

Each tag builds every version for that loader and posts a review card in Discord. Which Minecraft
versions each jar is published for is set in `.github/moogs-publish.yml`.
