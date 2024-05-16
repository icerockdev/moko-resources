/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.actions.apple

import org.gradle.api.Action
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask
import java.io.File

internal class CopyAppleResourcesFromFrameworkToFatAction : Action<FatFrameworkTask> {
    override fun execute(task: FatFrameworkTask) {
        val fatFrameworkDir: File = task.fatFramework
        val frameworkFile: File = task.frameworks.first().files.rootDir

        executeWithFramework(fatFrameworkDir, frameworkFile)
    }

    private fun executeWithFramework(
        fatFrameworkDir: File,
        frameworkFile: File,
    ) {
        frameworkFile.listFiles().orEmpty()
            .filter { it.extension == "bundle" }
            .forEach { bundleFile ->
                val destinationDir = File(fatFrameworkDir, bundleFile.name)

                destinationDir.deleteRecursively()

                bundleFile.copyRecursively(
                    target = destinationDir,
                    overwrite = false
                )
            }
    }
}
