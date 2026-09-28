import groovy.json.JsonOutput
import groovy.json.JsonSlurper

// Each mixin config lists every mixin MSL has on any Minecraft version. A mixin class that doesn't
// exist on a version is gated out of the source by Stonecutter, so this drops every entry whose class
// was not compiled for this node, or was compiled without its @Mixin annotation. The version
// conditions in the Java are then the only list to keep.

plugins {
    java
}

val MIXIN = "Lorg/spongepowered/asm/mixin/Mixin;"
val mainClasses: FileCollection = the<SourceSetContainer>()["main"].output.classesDirs

tasks.named<ProcessResources>("processResources") {
    dependsOn(tasks.named("compileJava"))
    inputs.files(mainClasses).withPropertyName("mixinClasses")
    doLast {
        destinationDir.listFiles { f -> f.name.endsWith(".mixins.json") }?.forEach { file ->
            @Suppress("UNCHECKED_CAST")
            val config = JsonSlurper().parse(file) as MutableMap<String, Any?>
            val pkg = (config["package"] as String).replace('.', '/')
            for (key in listOf("mixins", "client", "server")) {
                val entries = config[key] as? List<*> ?: continue
                config[key] = entries.filter { entry ->
                    val path = "$pkg/${(entry as String).replace('.', '/')}.class"
                    // It must still be a mixin: some are kept as empty interfaces on versions that
                    // need no mixin, and Mixin refuses a listed class without @Mixin.
                    mainClasses.map { File(it, path) }.any { it.exists() && MIXIN in it.readText(Charsets.ISO_8859_1) }
                }
            }
            file.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(config)))
        }
    }
}
