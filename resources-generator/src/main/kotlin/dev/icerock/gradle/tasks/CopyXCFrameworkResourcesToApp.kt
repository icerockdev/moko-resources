/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileFilter

// TODO register tasks
open class CopyXCFrameworkResourcesToApp : DefaultTask() {
    init {
        group = "moko-resources"
    }

    @InputDirectory
    lateinit var xcFrameworkDir: File

    @TaskAction
    fun copyResources() {
        val buildProductsDir = project.property("moko.resources.BUILT_PRODUCTS_DIR") as String
        val contentsFolderPath = project.property("moko.resources.CONTENTS_FOLDER_PATH") as String
        val outputDir = File("$buildProductsDir/$contentsFolderPath")

        val frameworkDir: File = xcFrameworkDir.walkTopDown().first { it.extension == "framework" }
        frameworkDir.listFiles(FileFilter { it.extension == "bundle" })?.forEach {
            project.logger.info("copy resources bundle $it to $outputDir")
            it.copyRecursively(File(outputDir, it.name), overwrite = true)
        }
    }
}
