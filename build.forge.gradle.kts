plugins {
    id("net.minecraftforge.gradle") version "[7.0.29,8.0)"
    id("net.minecraftforge.jarjar") version "0.2.3"
    id("minecraft-mutex")
    id("mixin-config-filter")
}

fun prop(key: String): String = sc.properties.get<String>(key)

val modId = property("mod_id").toString()
val modName = property("mod_name").toString()
val modAuthor = property("mod_author").toString()
val requiredJava: JavaVersion = JavaVersion.toVersion(prop("mod.java"))
// The Minecraft version this node compiles against. Usually the node version, but a range can build
// one loader against a different patch release (Forge 1.21.3 for the 1.21.2-1.21.3 range).
val mcBuild: String = prop("mod.mc_build")
// Which access widener / transformer this node uses; the classes they open moved between versions.
val awFile = rootProject.file("src/main/access/${prop("mod.access")}.accesswidener")
val atFile = rootProject.file("src/main/access/${prop("mod.access")}.cfg")
val mixinConfigs = "$modId-common.mixins.json,$modId-forge.mixins.json"
// Dev-only gametests are compiled into main (Forge only scans main) but never shipped.
val gametestFiles = listOf(
    "com/finndog/moogs_structures/gametest/**",
    "data/minecraft/structure/moogs_structures.armor_stand_processor_test_empty.nbt",
    "data/moogs_structures/structure/armor_stand_processor_test_empty.nbt",
    "data/moogs_structures/structure/equiparmorstandprocessortest.armor_stand_processor_test_empty.nbt",
    "data/moogs_structures/test_instance/**",
    "$modId-forge-gametest.mixins.json",
)
// From 1.21.5 a GameTest runs from a test instance in data, which names a test function registered in code. MSL's
// Forge GameTest is compiled up to 26.1.2, and a test instance whose function is missing fails the run.
val hasTestInstances = sc.current.parsed >= "1.21.5" && sc.current.parsed < "26.1.2"

version = property("mod_version").toString()
base.archivesName = "${property("archives_base_name")}-forge-$mcBuild"

sourceSets.main {
    java.exclude("**/fabric/**", "**/neoforge/**")
    resources.srcDir(rootProject.file("src/forge/resources"))
}

// Forge's dev launcher makes each folder on the run classpath its own module. With classes and resources in
// two folders, MSL's classes end up in two modules and runServer, runClient and runGameTestServer fail to
// start, so each source set gets one output folder, as in Forge's MDK.
sourceSets.configureEach {
    val dir = layout.buildDirectory.dir("sourcesSets/$name")
    output.setResourcesDir(dir)
    java.destinationDirectory.set(dir)
}

// Forge 52 (1.21.1) publishes accesstransformers and modlauncher asking for jopt-simple 6.0-alpha-3, which wins
// the version conflict, but its modlauncher needs 5.0.4 (module jopt.simple), the version Forge's installer
// ships, so the 1.21.1 dev runs fail to start. Every other node resolves 5.0.4 already.
configurations.runtimeClasspath {
    resolutionStrategy.force("net.sf.jopt-simple:jopt-simple:5.0.4")
}

minecraft {
    mappings("official", mcBuild)
    accessTransformers.from(atFile)

    runs {
        // Per-node game directory, so worlds are never opened by a different Minecraft version.
        configureEach {
            workingDir.set(rootProject.file("run/${project.name}"))
            // A jar names its mixin configs in its manifest; a dev run loads MSL from a folder, which has none,
            // so without these MSL's mixins are never applied in dev runs and GameTests.
            mixinConfigs.split(',').forEach { args("--mixin.config", it) }
        }
        register("client")
        register("server") { args("--nogui") }
        register("gameTestServer") {
            systemProperty("forge.enabledGameTestNamespaces", modId)
            systemProperty("forge.enableGameTest", "true")
            systemProperty("forge.gameTestServer", "true")
            // On Forge 55 to 58 this starts the GameTest server the way Forge 59 does; elsewhere the config is empty.
            args("--mixin.config", "$modId-forge-gametest.mixins.json")
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
    implementation(minecraft.dependency("net.minecraftforge:forge:$mcBuild-${prop("deps.forge")}"))

    compileOnly("io.github.llamalad7:mixinextras-common:${prop("deps.mixinextras")}")
    implementation("io.github.llamalad7:mixinextras-forge:${prop("deps.mixinextras")}")
    "jarJar"("io.github.llamalad7:mixinextras-forge:${prop("deps.mixinextras")}")

    // In-game config screen: compiled against, never bundled or required at runtime.
    compileOnly("me.shedaniel.cloth:cloth-config-${prop("deps.cloth_config_loader")}:${prop("deps.cloth_config")}") { isTransitive = false }
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
        filesMatching(listOf("META-INF/mods.toml", "*.mixins.json", "pack.mcmeta")) { expand(props) }
        from(awFile) { rename { "$modId.accesswidener" } }
        from(atFile) { into("META-INF"); rename { "accesstransformer.cfg" } }
        if (!hasTestInstances) exclude("data/moogs_structures/test_instance/**")
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
                "Built-On-Minecraft" to mcBuild,
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

    // Shared classes a Forge node leaves out, e.g. config-screen widgets on a range where Forge's
    // config is file-only and the GUI API differs from the version the shared code targets.
    compileJava { exclude(prop("mod.forge_compile_excludes").split(',').filter { it.isNotBlank() }) }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds the mod jar and copies it to build/libs/{mod version}/"
        from(named<Jar>("jarJar").flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("libs/$version"))
    }
}
