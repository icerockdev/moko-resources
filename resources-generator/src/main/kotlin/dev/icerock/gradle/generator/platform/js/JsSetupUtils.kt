/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.platform.js

import dev.icerock.gradle.actions.js.CopyResourcesToExecutableAction
import dev.icerock.gradle.actions.js.CopyResourcesToKLibAction
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.io.File

internal fun setupJsKLibResources(
    compileTask: Kotlin2JsCompile,
    resourcesGenerationDir: Provider<File>
) {
    val copyResourcesToKLibAction = CopyResourcesToKLibAction(resourcesGenerationDir)
    @Suppress("UNCHECKED_CAST")
    compileTask.doLast(copyResourcesToKLibAction as Action<in Task>)
}

internal fun setupJsResources(
    compileTask: Kotlin2JsCompile,
    resourcesGenerationDir: Provider<File>,
    projectDir: Provider<File>
) {
    val copyResourcesAction = CopyResourcesToExecutableAction(
        resourcesGeneratedDir = resourcesGenerationDir,
        projectDir = projectDir
    )
    @Suppress("UNCHECKED_CAST")
    compileTask.doLast(copyResourcesAction as Action<in Task>)
}
