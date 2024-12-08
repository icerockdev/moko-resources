/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.actions.apple

import dev.icerock.gradle.utils.klibs
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import org.jetbrains.kotlin.library.KotlinLibraryLayout
import org.jetbrains.kotlin.library.impl.KotlinLibraryLayoutImpl
import java.io.File

internal abstract class CopyResourcesFromKLibsAction : Action<Task> {

    protected fun copyResourcesFromLibraries(
        linkTask: KotlinNativeLink,
        outputDir: File
    ) {
        val packedKlibs: List<File> = linkTask.klibs
            .filter { it.exists() }
            .filter { it.extension == "klib" }
            .map { it }
        val unpackedKlibs: List<File> = linkTask.klibs
            .filter { it.exists() }
            // we need only unpacked klibs
            .filter { it.name == "manifest" && it.parentFile.name == "default" }
            // manifest stored in klib inside directory default
            .map { it.parentFile.parentFile }

        (packedKlibs + unpackedKlibs)
            .forEach { inputFile ->
                linkTask.logger.info("found dependency $inputFile, try to copy resources")

                val layout: KotlinLibraryLayout = getKotlinLibraryLayout(inputFile)

                copyResourcesFromKlib(
                    logger = linkTask.logger,
                    layout = layout,
                    outputDir = outputDir,
                )
            }
    }

    private fun copyResourcesFromKlib(logger: Logger, layout: KotlinLibraryLayout, outputDir: File) {
        logger.info("copy resources from $layout into $outputDir")

        try {
            File(layout.resourcesDir.path).copyRecursively(
                target = outputDir,
                overwrite = true
            )
        } catch (@Suppress("SwallowedException") exc: NoSuchFileException) {
            logger.info("resources in $layout not found")
        } catch (@Suppress("SwallowedException") exc: java.nio.file.NoSuchFileException) {
            logger.info("resources in $layout not found (empty lib)")
        }
    }

    private fun getKotlinLibraryLayout(file: File): KotlinLibraryLayout {
        val klibKonan = org.jetbrains.kotlin.konan.file.File(file.path)
        val klib = KotlinLibraryLayoutImpl(klib = klibKonan, component = "default")

        return if (klib.isZipped) klib.extractingToTemp else klib
    }
}
