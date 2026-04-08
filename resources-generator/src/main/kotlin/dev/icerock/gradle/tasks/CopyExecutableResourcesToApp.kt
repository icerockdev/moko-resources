/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.tasks

import dev.icerock.gradle.data.getKlibResourcesDir
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileFilter

abstract class CopyExecutableResourcesToApp : DefaultTask() {

    @get:InputFiles
    @get:Classpath
    abstract val klibs: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    init {
        group = "moko-resources"
    }

    @TaskAction
    fun copyResources() {
        val outputDir: File = outputDirectory.get().asFile

        klibs
            .filter { library -> library.extension == "klib" }
            .filter(File::exists)
            .forEach { inputFile ->
                val resourcesDir: File = getKlibResourcesDir(inputFile) ?: return@forEach

                // extracting bundles
                resourcesDir
                    .listFiles(FileFilter { it.extension == "bundle" })
                    // copying bundles to app
                    ?.forEach {
                        logger.info("${it.absolutePath} copying to $outputDir")
                        it.copyRecursively(target = File(outputDir, it.name), overwrite = true)
                    }
            }
    }
}
