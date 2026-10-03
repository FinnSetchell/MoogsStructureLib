plugins {
    // Forge before 1.20.5 runs on SRG-named Minecraft, so the jar has to be reobfuscated out of the
    // official names the source is written in. ForgeGradle 7 cannot reobfuscate; ModDevGradle's legacy
    // Forge plugin can, and wires it into the jar. Everything else matches build.forge.gradle.kts.
    id("net.neoforged.moddev.legacyforge") version "2.0.147"
    id("minecraft-mutex")
    id("mixin-config-filter")
}

fun prop(key: String): String = sc.properties.get<String>(key)

val modId = property("mod_id").toString()
val modName = property("mod_name").toString()
val modAuthor = property("mod_author").toString()
val requiredJava: JavaVersion = JavaVersion.toVersion(prop("mod.java"))
val mcBuild: String = prop("mod.mc_build")
val awFile = rootProject.file("src/main/access/${prop("mod.access")}.accesswidener")
val atFile = rootProject.file("src/main/access/${prop("mod.access")}.cfg")
val mixinConfigs = "$modId-common.mixins.json,$modId-forge.mixins.json"
// Dev-only gametests are compiled into main (Forge only scans main) but never shipped.
val gametestFiles = listOf(
    "com/finndog/moogs_structures/gametest/**",
    "data/minecraft/structure/moogs_structures.armor_stand_processor_test_empty.nbt",
    "data/moogs_structures/structure/armor_stand_processor_test_empty.nbt",
    "data/moogs_structures/structure/equiparmorstandprocessortest.armor_stand_processor_test_empty.nbt",
)

version = property("mod_version").toString()
base.archivesName = "${property("archives_base_name")}-forge-$mcBuild"

sourceSets.main {
    java.exclude("**/fabric/**", "**/neoforge/**")
    resources.srcDir(rootProject.file("src/forge/resources"))
}

repositories {
    maven("https://maven.shedaniel.me") { name = "Shedaniel" }
}

legacyForge {
    version = "$mcBuild-${prop("deps.forge")}"
    accessTransformers.from(atFile)

    mods {
        register(modId) { sourceSet(sourceSets.main.get()) }
    }

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
    }
}

// Mixin targets need a refmap on SRG-named Forge; only this script points the configs at one.
mixin {
    add(sourceSets.main.get(), "$modId.refmap.json")
    config("$modId-common.mixins.json")
    config("$modId-forge.mixins.json")
}

// The mixin processor can't map ScreenInvoker's generic target, so its entry is added by hand.
val refmapPatches = mapOf(
    "com/finndog/moogs_structures/mixins/client/ScreenInvoker" to mapOf(
        "addRenderableWidget" to "m_142416_(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;",
    ),
)

tasks.named<JavaCompile>("compileJava") {
    doLast {
        val refmapFile = layout.buildDirectory.file("mixin/$modId.refmap.json").get().asFile
        @Suppress("UNCHECKED_CAST")
        val refmap = groovy.json.JsonSlurper().parse(refmapFile) as MutableMap<String, Any?>
        @Suppress("UNCHECKED_CAST")
        fun section(map: MutableMap<String, Any?>, key: String) = map.getOrPut(key) { mutableMapOf<String, Any?>() } as MutableMap<String, Any?>
        val mappings = section(refmap, "mappings")
        val searge = section(section(refmap, "data"), "searge")
        for ((owner, members) in refmapPatches) {
            section(mappings, owner).putAll(members)
            section(searge, owner).putAll(members)
        }
        refmapFile.writeText(groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(refmap)))

        val classes = destinationDirectory.get().asFile
        val unmapped = classes.walk()
            .filter { it.extension == "class" && "/mixins/" in it.invariantSeparatorsPath }
            .filter { f ->
                val bytes = f.readText(Charsets.ISO_8859_1)
                "Lorg/spongepowered/asm/mixin/gen/Invoker;" in bytes || "Lorg/spongepowered/asm/mixin/gen/Accessor;" in bytes
            }
            .map { it.relativeTo(classes).invariantSeparatorsPath.removeSuffix(".class") }
            .filter { it !in mappings }
            .toList()
        if (unmapped.isNotEmpty()) {
            throw GradleException("No refmap entry for $unmapped, so SRG-named Forge can't find their targets.")
        }
    }
}

dependencies {
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    // In-game config screen: compiled against, never bundled or required at runtime. Cloth's Forge jar
    // is SRG-named here, so it goes through the remapping configuration.
    modCompileOnly("me.shedaniel.cloth:cloth-config-forge:${prop("deps.cloth_config")}") { isTransitive = false }
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
            "java_version" to requiredJava.majorVersion,
        )
        props.forEach { (k, v) -> inputs.property(k, v) }
        filesMatching(listOf("META-INF/mods.toml", "*.mixins.json", "pack.mcmeta")) { expand(props) }
        filesMatching("*.mixins.json") {
            filter { line -> line.replace("\"required\": true,", "\"required\": true,\n    \"refmap\": \"$modId.refmap.json\",") }
        }
        from(awFile) { rename { "$modId.accesswidener" } }
        from(atFile) { into("META-INF"); rename { "accesstransformer.cfg" } }
    }

    jar {
        exclude(gametestFiles)
        from(rootProject.file("LICENSE")) { rename { "${it}_${modName}" } }
        manifest {
            attributes(
                // Forge only reads mixin configs named here; the legacy plugin does not add them.
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

    named("createMinecraftArtifacts") { dependsOn("stonecutterGenerate") }
    withType<JavaCompile>().configureEach { dependsOn("stonecutterGenerate") }
    compileJava { exclude(prop("mod.forge_compile_excludes").split(',').filter { it.isNotBlank() }) }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds the mod jar and copies it to build/libs/{mod version}/"
        // The reobfuscated jar, not `jar`: that one keeps official names and dies on a real server.
        val reobf = named("reobfJar")
        dependsOn(reobf)
        from(reobf.map { it.outputs.files })
        into(rootProject.layout.buildDirectory.dir("libs/$version"))
    }
}
