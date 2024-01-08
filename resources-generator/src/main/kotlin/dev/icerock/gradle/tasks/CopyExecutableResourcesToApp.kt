/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.tasks

import dev.icerock.gradle.utils.klibs
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import org.jetbrains.kotlin.library.impl.KotlinLibraryLayoutImpl
import java.io.File
import java.io.FileFilter

// TODO register tasks
abstract class CopyExecutableResourcesToApp : DefaultTask() {
    @get:Internal
    abstract var linkTask: KotlinNativeLink

    init {
        group = "moko-resources"
    }

    @TaskAction
    fun copyResources() {
        val buildProductsDir =
            File(project.property("moko.resources.BUILT_PRODUCTS_DIR") as String)
        val contentsFolderPath =
            project.property("moko.resources.CONTENTS_FOLDER_PATH") as String

        val outputDir = File(buildProductsDir, contentsFolderPath)

        linkTask.klibs
            .filter { library -> library.extension == "klib" }
            .filter(File::exists)
            .forEach { inputFile ->
                val klibKonan = org.jetbrains.kotlin.konan.file.File(inputFile.path)
                val klib = KotlinLibraryLayoutImpl(klib = klibKonan, component = "default")
                val layout = klib.extractingToTemp

                // extracting bundles
                layout
                    .resourcesDir
                    .absolutePath
                    .let(::File)
                    .listFiles(FileFilter { it.extension == "bundle" })
                    // copying bundles to app
                    ?.forEach {
                        logger.info("${it.absolutePath} copying to $outputDir")
                        it.copyRecursively(target = File(outputDir, it.name), overwrite = true)
                    }
            }
    }
}
