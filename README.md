# Moog's Structure Lib

A library mod for Minecraft that provides flexible, data-driven structure generation systems. Other mods can use this to create custom structures without writing Java code.

## Example Structure: Barn

The mod includes a complete barn structure example (disabled by default).

## Usage for Mod Developers
1. Copy the `.example` files to your mod's data folder
2. Rename them to `.json`
3. Remove comment markers and customize settings

### Add as Dependency
```gradle
dependencies {
    modImplementation "com.finndog:moogs_structures:${version}"
}
```

## License

GNU Lesser General Public License v3.0

## Credits

- **Special Thanks**: 
  - TelepathicGrunt for structure mod template inspiration and Repurposed Structures
