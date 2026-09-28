plugins {
    id("net.neoforged.moddev") version "2.0.147"
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

version = property("mod_version").toString()
base.archivesName = "${property("archives_base_name")}-neoforge-$mcBuild"

sourceSets.main {
    java.exclude("**/fabric/**", "**/forge/**")
    resources.srcDir(rootProject.file("src/neoforge/resources"))
}

// NeoForge's gametests live in their own source set, so they never reach the shipped jar.
val gametest: SourceSet = sourceSets.create("gametest") {
    compileClasspath += sourceSets.main.get().compileClasspath + sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().runtimeClasspath + sourceSets.main.get().output
}

repositories {
    maven("https://maven.shedaniel.me") { name = "Shedaniel" }
}

neoForge {
    version = prop("deps.neoforge")
    accessTransformers.from(atFile)

    runs {
        // Per-node game directory, so worlds are never opened by a different Minecraft version.
        register("client") {
            client()
            gameDirectory = rootProject.file("run/${project.name}")
        }
        register("server") {
            server()
            gameDirectory = rootProject.file("run/${project.name}")
        }
        register("gameTestServer") {
            type = "gameTestServer"
            // Only this run enables gametests. Enabling them everywhere made the dev client register
            // test instances too, which broke registry sync for joining clients from 1.21.5.
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
            gameDirectory = rootProject.file("run/${project.name}-gametest")
        }
    }

    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
            sourceSet(gametest)
        }
    }
}

dependencies {
    // In-game config screen: compiled against, never bundled or required at runtime.
    compileOnly("me.shedaniel.cloth:cloth-config-neoforge:${prop("deps.cloth_config")}") { isTransitive = false }
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
            "neoforge_loader_version_range" to prop("deps.neoforge_range"),
            "neoforge_min" to "[${prop("deps.neoforge_min")},)",
            "mc_compat" to prop("mod.mc_compat"),
            "neo_icon_key" to prop("mod.neo_icon_key"),
            "java_version" to requiredJava.majorVersion,
        )
        props.forEach { (k, v) -> inputs.property(k, v) }
        filesMatching(listOf("META-INF/neoforge.mods.toml", "*.mixins.json", "pack.mcmeta")) { expand(props) }
        from(awFile) { rename { "$modId.accesswidener" } }
        from(atFile) { into("META-INF"); rename { "accesstransformer.cfg" } }
    }

    jar {
        from(rootProject.file("LICENSE")) { rename { "${it}_${modName}" } }
        manifest {
            attributes(
                "Specification-Title" to modName,
                "Specification-Vendor" to modAuthor,
                "Specification-Version" to version,
                "Implementation-Title" to "neoforge",
                "Implementation-Version" to version,
                "Implementation-Vendor" to modAuthor,
                "Built-On-Minecraft" to mcBuild,
            )
        }
    }

    // NeoForge's Minecraft artifacts must not be created before Stonecutter has written the sources.
    named("createMinecraftArtifacts") { dependsOn("stonecutterGenerate") }
    withType<JavaCompile>().configureEach { dependsOn("stonecutterGenerate") }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds the mod jar and copies it to build/libs/{mod version}/"
        from(jar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("libs/$version"))
    }
}
