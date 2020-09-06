/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import java.io.File
import java.io.FileFilter

open class CopyFrameworkResourcesToAppTask : DefaultTask() {
    init {
        group = "moko-resources"
    }

    @Internal
    lateinit var framework: Framework

    @TaskAction
    fun copyResources() {
        val buildProductsDir =
            project.property("moko.resources.BUILT_PRODUCTS_DIR") as String
        val contentsFolderPath =
            project.property("moko.resources.CONTENTS_FOLDER_PATH") as String
        val outputDir = File("$buildProductsDir/$contentsFolderPath")

        val inputDir = framework.outputFile
        inputDir.listFiles(FileFilter { it.extension == "bundle" })?.forEach {
            project.logger.info("copy resources bundle $it to $outputDir")
            it.copyRecursively(File(outputDir, it.name), overwrite = true)
        }
    }
}
