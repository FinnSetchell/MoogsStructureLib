plugins {
    id("dev.kikugie.stonecutter")
}

// Must stay a string literal: Stonecutter rewrites this line when switching versions.
stonecutter active "1.21.1-fabric"

stonecutter parameters {
    val (version, loader) = current.project.split('-', limit = 2)

    // Lets `[fabric."1.21.1"]` tables in stonecutter.properties.toml resolve for this node.
    properties {
        tags(version, loader)
    }

    // `//? if fabric {` and friends, for the few places loader code shares a file with common code.
    constants {
        match(loader, "fabric", "forge", "neoforge")
    }

    replacements {
        // 1.21.11 renamed ResourceLocation to Identifier across the whole codebase.
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }
    }
}
