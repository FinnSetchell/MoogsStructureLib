# Building Moog's Structure Lib

One source tree builds every Minecraft version and loader MSL supports, through
[Stonecutter](https://stonecutter.kikugie.dev/). Each version × loader is a Gradle subproject
("node") under `versions/`, declared in `settings.gradle.kts`. Gradle runs on Java 25; each node
compiles on its own toolchain (17 for 1.20-1.20.4, 21 up to 1.21.11, 25 for 26.x).

| Range | Node | Fabric | Forge | NeoForge |
| --- | --- | --- | --- | --- |
| 1.20-1.20.4 | `1.20` | yes | yes (legacy plugin, reobfuscated) | - |
| 1.20.5-1.20.6 | `1.20.6` | yes | - | yes |
| 1.21-1.21.1 | `1.21.1` | yes | yes | yes |
| 1.21.2-1.21.3 | `1.21.2` | yes | yes (built on 1.21.3) | yes |
| 1.21.4 | `1.21.4` | yes | yes | yes |
| 1.21.5-1.21.8 | `1.21.5` | yes | not yet (Trello card 537) | yes |
| 1.21.10 | `1.21.10` | yes | yes | yes |
| 1.21.11 | `1.21.11` | yes | yes | yes |
| 26.1.0-26.1.2 | `26.1.2` | yes | yes (built on 26.1.1) | yes |
| 26.2 | `26.2` | yes | yes | yes |
| 26.3 | `26.3` | yes | yes | yes |

## Building

```bash
./gradlew :1.21.1-fabric:buildAndCollect
```

```bash
./gradlew buildAndCollect
```

The first builds one node, the second every node. Jars land in `build/libs/<mod version>/`.

## Where things live

- `src/main/java` - all Java. Loader code sits in `fabric`, `forge` and `neoforge` packages; each
  loader's build excludes the other two.
- `src/main/resources` - shared resources. `src/<loader>/resources` - loader metadata, service files
  and the loader mixin configs.
- `src/main/access` - access widener / transformer variants, picked per node by `mod.access`.
- `stonecutter.properties.toml` - per-node versions, ranges and dependencies.
- `build.<loader>.gradle.kts` - one build script per loader; `build.forge-legacy.gradle.kts` builds
  Forge before 1.20.5.

## Version-specific code

The committed source is the `1.21.1-fabric` node. Other versions differ through Stonecutter
comments:

```java
//? if >=1.21.2 {
/*public static boolean isFullCube(BlockState state) {
*///?} else {
public static boolean isFullCube(BlockGetter world, BlockPos pos, BlockState state) {
//?}
```

- Switch the active version with the Stonecutter IntelliJ plugin, or run
  `./gradlew "Set active project to 26.3-fabric"`. Switch back with `./gradlew "Reset active project"`
  before committing.
- `ResourceLocation` is written everywhere; Stonecutter rewrites it to `Identifier` for 1.21.11 and
  later. So no other name may contain either word.
- A block must not open with a comment line: Stonecutter reads it as already commented out.
- A mixin that only exists on some versions just needs its class gated. Every mixin config lists
  every mixin, and each node ships only the ones it compiled with `@Mixin`.
