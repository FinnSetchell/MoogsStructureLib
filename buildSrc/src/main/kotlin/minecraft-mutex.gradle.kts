import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

// Serialises NeoForm's (NeoForge) Minecraft setup across nodes. Without this, building several
// NeoForge nodes at once starts a full Minecraft decompile per node in parallel and will bring the
// machine to its knees.
//
// Forge (ForgeGradle 7) does NOT need a task-level mutex here, and deliberately doesn't get one:
// its Minecraft/Forge artifact setup (the "Minecraft Mavenizer" step) runs eagerly during Gradle's
// project *configuration* phase, not as a lazy execution-phase task — confirmed by watching it run
// for all eight Forge nodes strictly one after another while syncing this project, never
// interleaved, even though configuration itself isn't something org.gradle.parallel affects. By
// the time task *execution* starts (where parallel=true actually parallelises work, and where
// NeoForge's createMinecraftArtifacts lives), every Forge node's setup is already done — so a
// Forge node's decompile can never race a NeoForge node's, or another Forge node's, regardless of
// this mutex.
interface MinecraftSetupMutex : BuildService<BuildServiceParameters.None>

val mutex = gradle.sharedServices.registerIfAbsent("createMinecraftArtifactsMutex", MinecraftSetupMutex::class.java) {
    maxParallelUsages.set(1)
}

tasks.named { it == "createMinecraftArtifacts" }.configureEach {
    usesService(mutex)
}
