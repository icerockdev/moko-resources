package dev.icerock.gradle.generator.js.action

import org.gradle.api.Action
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.io.File

class CopyResourcesToKLibAction(
    private val resourcesDirProvider: Provider<File>,
) : Action<Kotlin2JsCompile> {
    override fun execute(task: Kotlin2JsCompile) {
        val unpackedKLibDir: File = task.destinationDirectory.asFile.get()
        val defaultDir = File(unpackedKLibDir, "default")
        val resRepackDir = File(defaultDir, "resources")
        if (resRepackDir.exists().not()) return

        val resDir = File(resRepackDir, "moko-resources-js")

        resourcesDirProvider.get().copyRecursively(
            target = resDir,
            overwrite = true
        )
    }
}
