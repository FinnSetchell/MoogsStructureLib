plugins {
    id("net.neoforged.moddev") version "2.0.147"
    id("minecraft-mutex")
}

fun prop(key: String): String = sc.properties.get<String>(key)

val modId = property("mod_id").toString()
val modName = property("mod_name").toString()
val modAuthor = property("mod_author").toString()
val requiredJava: JavaVersion = JavaVersion.toVersion(prop("mod.java"))

version = property("mod_version").toString()
base.archivesName = "${property("archives_base_name")}-neoforge-${sc.current.version}"

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
    accessTransformers.from(rootProject.file("src/main/resources/META-INF/accesstransformer.cfg"))

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
            "neoforge_loader_version_range" to prop("deps.neoforge_range"),
            "mc_compat" to prop("mod.mc_compat"),
            "java_version" to requiredJava.majorVersion,
        )
        props.forEach { (k, v) -> inputs.property(k, v) }
        filesMatching(listOf("META-INF/neoforge.mods.toml", "*.mixins.json")) { expand(props) }
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
                "Built-On-Minecraft" to sc.current.version,
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
