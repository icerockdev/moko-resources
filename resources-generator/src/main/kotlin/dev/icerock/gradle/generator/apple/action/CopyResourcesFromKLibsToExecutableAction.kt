/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.apple.action

import org.gradle.api.Task
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

internal class CopyResourcesFromKLibsToExecutableAction : CopyResourcesFromKLibsAction() {
    override fun execute(task: Task) {
        task as KotlinNativeLink

        val executable: TestExecutable = task.binary as TestExecutable

        copyResourcesFromLibraries(
            linkTask = task,
            project = task.project,
            outputDir = executable.outputFile
        )
    }
}
