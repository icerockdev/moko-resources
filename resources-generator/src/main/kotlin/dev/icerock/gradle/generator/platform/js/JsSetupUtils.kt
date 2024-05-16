/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.platform.js

import dev.icerock.gradle.actions.js.CopyResourcesToExecutableAction
import dev.icerock.gradle.actions.js.CopyResourcesToKLibAction
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.targets.js.ir.JsIrBinary
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrCompilation
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrLink
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.io.File

internal fun setupJsKLibResources(
    compileTask: Kotlin2JsCompile,
    resourcesGenerationDir: Provider<File>,
) {
    val copyResourcesToKLibAction = CopyResourcesToKLibAction(resourcesGenerationDir)
    @Suppress("UNCHECKED_CAST")
    compileTask.doLast(copyResourcesToKLibAction as Action<in Task>)
}

internal fun setupJsExecutableResources(
    linkTask: KotlinJsIrLink,
    projectDir: Provider<File>,
) {
    val copyResourcesAction = CopyResourcesToExecutableAction(
        outputDir = linkTask.destinationDirectory.asFile,
        projectDir = projectDir
    )

    @Suppress("UNCHECKED_CAST")
    linkTask.doLast(copyResourcesAction as Action<in Task>)
}

internal fun setupJsResourcesWithLinkTask(
    target: KotlinJsIrTarget,
    project: Project,
) {
    target.compilations.withType<KotlinJsIrCompilation>().configureEach { compilation ->
        compilation.binaries.withType<JsIrBinary>().configureEach { binary: JsIrBinary ->
            binary.linkTask.configure { linkTask ->
                setupJsExecutableResources(
                    linkTask = linkTask,
                    projectDir = project.provider { project.projectDir }
                )
            }
        }
    }
}
