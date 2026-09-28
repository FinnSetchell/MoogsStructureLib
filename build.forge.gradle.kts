plugins {
    id("net.minecraftforge.gradle") version "[7.0.29,8.0)"
    id("net.minecraftforge.jarjar") version "0.2.3"
    id("minecraft-mutex")
}

fun prop(key: String): String = sc.properties.get<String>(key)

val modId = property("mod_id").toString()
val modName = property("mod_name").toString()
val modAuthor = property("mod_author").toString()
val requiredJava: JavaVersion = JavaVersion.toVersion(prop("mod.java"))
val mixinConfigs = "$modId-common.mixins.json,$modId-forge.mixins.json"
// Dev-only gametests are compiled into main (Forge only scans main) but never shipped.
val gametestFiles = listOf(
    "com/finndog/moogs_structures/gametest/**",
    "data/minecraft/structure/moogs_structures.armor_stand_processor_test_empty.nbt",
)

version = property("mod_version").toString()
base.archivesName = "${property("archives_base_name")}-forge-${sc.current.version}"

sourceSets.main {
    java.exclude("**/fabric/**", "**/neoforge/**")
    resources.srcDir(rootProject.file("src/forge/resources"))
}

minecraft {
    mappings("official", sc.current.version)
    accessTransformers.from(rootProject.file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs {
        // Per-node game directory, so worlds are never opened by a different Minecraft version.
        configureEach {
            workingDir.set(rootProject.file("run/${project.name}"))
        }
        register("client")
        register("server") { args("--nogui") }
        register("gameTestServer") {
            systemProperty("forge.enabledGameTestNamespaces", modId)
            systemProperty("forge.enableGameTest", "true")
            systemProperty("forge.gameTestServer", "true")
        }
    }
}

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
    mavenCentral()
    maven("https://maven.shedaniel.me") { name = "Shedaniel" }
}

jarJar.register()

dependencies {
    implementation(minecraft.dependency("net.minecraftforge:forge:${sc.current.version}-${prop("deps.forge")}"))

    compileOnly("io.github.llamalad7:mixinextras-common:${prop("deps.mixinextras")}")
    implementation("io.github.llamalad7:mixinextras-forge:${prop("deps.mixinextras")}")
    "jarJar"("io.github.llamalad7:mixinextras-forge:${prop("deps.mixinextras")}")

    // In-game config screen: compiled against, never bundled or required at runtime.
    compileOnly("me.shedaniel.cloth:cloth-config-forge:${prop("deps.cloth_config")}") { isTransitive = false }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
    toolchain { languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion) }
}

tasks {
    processResources {
        val props = mapOf(
            "version" to version.toString(),
            "mod_id" to modId,
            "mod_name" to modName,
            "mod_author" to modAuthor,
            "license" to project.property("license").toString(),
            "description" to project.property("description").toString(),
            "credits" to project.property("credits").toString(),
            "forge_loader_version_range" to prop("deps.forge_range"),
            "mc_compat" to prop("mod.mc_compat"),
            // Forge's bundled Mixin tops out at JAVA_21, even on Java 25 Minecraft.
            "java_version" to minOf(requiredJava.majorVersion.toInt(), 21).toString(),
        )
        props.forEach { (k, v) -> inputs.property(k, v) }
        filesMatching(listOf("META-INF/mods.toml", "*.mixins.json")) { expand(props) }
    }

    jar {
        archiveClassifier = "slim"
        exclude(gametestFiles)
        from(rootProject.file("LICENSE")) { rename { "${it}_${modName}" } }
        manifest {
            attributes(
                "MixinConfigs" to mixinConfigs,
                "Specification-Title" to modName,
                "Specification-Vendor" to modAuthor,
                "Specification-Version" to version,
                "Implementation-Title" to "forge",
                "Implementation-Version" to version,
                "Implementation-Vendor" to modAuthor,
                "Built-On-Minecraft" to sc.current.version,
            )
        }
    }

    // The shipped jar is the one with MixinExtras bundled inside it.
    named<Jar>("jarJar") {
        archiveClassifier = null as String?
        exclude(gametestFiles)
    }
    assemble { dependsOn("jarJar") }

    // Forge's Minecraft setup must not run before Stonecutter has written the processed sources.
    withType<JavaCompile>().configureEach { dependsOn("stonecutterGenerate") }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds the mod jar and copies it to build/libs/{mod version}/"
        from(named<Jar>("jarJar").flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("libs/$version"))
    }
}
