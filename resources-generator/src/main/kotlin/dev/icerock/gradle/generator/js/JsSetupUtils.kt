package dev.icerock.gradle.generator.js

import dev.icerock.gradle.generator.js.action.CopyResourcesToExecutableAction
import dev.icerock.gradle.generator.js.action.CopyResourcesToKLibAction
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.io.File

fun setupJsKLibResources(
    compileTask: Kotlin2JsCompile,
    resourcesGenerationDir: Provider<File>
) {
    val copyResourcesToKLibAction = CopyResourcesToKLibAction(resourcesGenerationDir)
    @Suppress("UNCHECKED_CAST")
    compileTask.doLast(copyResourcesToKLibAction as Action<in Task>)
}

fun setupJsResources(
    compileTask: Kotlin2JsCompile,
    resourcesGenerationDir: Provider<File>
) {
    val copyResourcesAction = CopyResourcesToExecutableAction(resourcesGenerationDir)
    @Suppress("UNCHECKED_CAST")
    compileTask.doLast(copyResourcesAction as Action<in Task>)
}