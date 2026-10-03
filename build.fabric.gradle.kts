plugins {
    // Applies the Loom variant matching this node's Minecraft version.
    id("dev.kikugie.loom-back-compat")
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
base.archivesName = "${property("archives_base_name")}-fabric-$mcBuild"

sourceSets.main {
    // Loader code sits in fabric/forge/neoforge packages; each loader compiles only its own.
    java.exclude("**/forge/**", "**/neoforge/**")
    resources.srcDir(rootProject.file("src/fabric/resources"))
}

repositories {
    maven("https://maven.shedaniel.me") { name = "Shedaniel" }
    maven("https://maven.terraformersmc.com/releases") { name = "TerraformersMC" }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcBuild")
    // No-op on the unobfuscated versions; applies Mojang mappings on the obfuscated ones.
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:${prop("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric_api")}")

    // @WrapMethod needs MixinExtras 0.4 or newer, which older loader versions don't ship.
    val mixinExtras = "io.github.llamalad7:mixinextras-fabric:${prop("deps.mixinextras")}"
    implementation(mixinExtras)
    annotationProcessor(mixinExtras)
    include(mixinExtras)

    // In-game config screen: compiled against, never bundled or required at runtime.
    modCompileOnly("me.shedaniel.cloth:cloth-config-fabric:${prop("deps.cloth_config")}")
    modCompileOnly("com.terraformersmc:modmenu:${prop("deps.modmenu")}")
}

loom {
    accessWidenerPath = awFile

    runs {
        // Per-node game directory, so worlds are never opened by a different Minecraft version.
        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir("../../run/${project.name}")
        }
        named("server") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir("../../run/${project.name}")
        }
    }
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
            "mc_compat" to prop("mod.mc_compat"),
            "fabric_loader_dep" to prop("mod.fabric_loader_dep"),
            "java_version" to requiredJava.majorVersion,
        )
        props.forEach { (k, v) -> inputs.property(k, v) }
        filesMatching(listOf("fabric.mod.json", "*.mixins.json", "pack.mcmeta")) { expand(props) }
        // The 1.20.x jars also apply the access widener at runtime, as their Architectury builds did.
        if (prop("mod.fabric_aw_entry").toBoolean()) {
            filesMatching("fabric.mod.json") {
                filter { line -> line.replace("\"environment\": \"*\",", "\"environment\": \"*\",\n  \"accessWidener\": \"$modId.accesswidener\",") }
            }
        }
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
                "Implementation-Title" to "fabric",
                "Implementation-Version" to version,
                "Implementation-Vendor" to modAuthor,
                "Built-On-Minecraft" to mcBuild,
            )
        }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds the mod jar and copies it to build/libs/{mod version}/"
        from(loomx.modJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("libs/$version"))
    }
}
