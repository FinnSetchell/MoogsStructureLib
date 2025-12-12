# Moogs Structure Lib Wiki

Welcome to the Moogs Structure Lib documentation! This wiki will guide you through creating custom structures for Minecraft 1.21+ using Moogs Structure Lib.

## What is Moogs Structure Lib?

Moogs Structure Lib is a library mod that provides flexible, data-driven structure generation systems for Minecraft. It extends vanilla structure generation with advanced features like:

- Enhanced placement systems with exclusion zones
- Advanced terrain adaptation and height controls
- Nether-specific structure generation
- Version-aware structure templates
- And much more!

## Prerequisites

- Minecraft 1.21 or higher
- Moogs Structure Lib installed (as a mod dependency or in your modpack)
- Basic understanding of JSON files
- Familiarity with Minecraft's datapack structure (helpful but not required)

## Quick Navigation

### Getting Started
- **[Getting Started](Getting-Started)** - Start here! Learn the basics and create your first structure.

### Core Concepts
- **[Structure Files](Structure-Files)** - Complete reference for structure JSON files
- **[Template Pools](Template-Pools)** - Understanding how template pools work
- **[Structure Sets](Structure-Sets)** - Grouping structures with placement rules
- **[Placement Systems](Placement-Systems)** - Advanced placement configuration
- **[NBT Files](NBT-Files)** - Where and how to place your structure files

### Advanced Topics
- **[Advanced Topics](Advanced-Topics)** - Advanced features and placement modifiers

### Examples
- **[Simple Structure Example](Simple-Structure)** - Cart structure walkthrough
- **[Jigsaw Structure Example](Jigsaw-Structure)** - Barn structure with jigsaw connections
- **[Nether Structure Example](Nether-Structure)** - Nether Devil structure
- **[Complex Structure Example](Complex-Structure)** - Large structures like Cathedral

## Structure Generation Overview

To generate a structure in Minecraft, you need four main components:

1. **Structure JSON** (`data/<namespace>/worldgen/structure/`) - Defines the structure type and properties
2. **Template Pool JSON** (`data/<namespace>/worldgen/template_pool/`) - Defines which structure pieces to use
3. **Structure Set JSON** (`data/<namespace>/worldgen/structure_set/`) - Defines where and how often structures spawn
4. **NBT Structure Files** (`data/<namespace>/structure/`) - The actual structure data files

## Examples Used

Throughout this wiki, we use examples from **[MoogsVoyagerStructures](https://github.com/FinnSetchell/MoogsVanillaStructuresV2)** (MVS).

## Getting Help

- Join the [Discord server](https://discord.gg/S5nffJbuvA) for support
- Check the [GitHub repository](https://github.com/FinnSetchell/MoogsStructureLib) for issues and updates
- Review the examples in this wiki for common patterns

---

**Ready to start?** Head over to [Getting Started](Getting-Started) to create your first structure!

