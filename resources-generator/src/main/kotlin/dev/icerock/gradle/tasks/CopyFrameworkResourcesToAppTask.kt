/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileFilter

abstract class CopyFrameworkResourcesToAppTask : DefaultTask() {
    init {
        group = "moko-resources"
    }

    @get:InputDirectory
    abstract val inputFrameworkDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    abstract val frameworkIsStatic: Property<Boolean>

    @TaskAction
    fun copyResources() {
        if (!frameworkIsStatic.get()) {
            logger.info("Framework is not static. Copy resources task is skipped.")
            return
        }

        val outputDir: File = outputDirectory.get().asFile

        val inputDir: File = inputFrameworkDirectory.get().asFile

        inputDir.listFiles(FileFilter { it.extension == "bundle" })?.forEach {
            logger.info("copy resources bundle $it to $outputDir")
            it.copyRecursively(File(outputDir, it.name), overwrite = true)
        }
    }
}
