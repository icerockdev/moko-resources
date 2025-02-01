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
import org.jetbrains.kotlin.library.impl.javaFile
import java.io.File

internal abstract class CopyResourcesFromKLibsAction : Action<Task> {

    protected fun copyResourcesFromLibraries(
        linkTask: KotlinNativeLink,
        outputDir: File
    ) {
        val logger: Logger = linkTask.logger

        linkTask.klibs
            .onEach { file ->
                logger.debug("found klib dependency {}", file)
            }
            .flatMap { file ->
                getBundlesFromKlib(file, logger)
            }.forEach { bundle ->
                logger.info("copy $bundle to $outputDir")
                bundle.copyRecursively(File(outputDir, bundle.name), overwrite = true)
            }
    }

    /**
     * Search bundles in klib different types
     */
    private fun getBundlesFromKlib(klibFile: File, logger: Logger): List<File> {
        val isPackedKlib = klibFile.isFile && klibFile.extension == "klib"
        val isUnpackedKlib = klibFile.isDirectory

        return if (isPackedKlib || isUnpackedKlib) {
            logger.info("found klib $klibFile")
            getBundlesFromKotlinLibrary(klibFile, logger)
        } else if (klibFile.name == "manifest" && klibFile.parentFile.name == "default") {
            // for unpacked klibs we can see content files instead of klib directory.
            // try to check this case
            logger.info("found manifest of klib $klibFile")
            val unpackedKlibRoot: File = klibFile.parentFile.parentFile
            getBundlesFromKotlinLibrary(unpackedKlibRoot, logger)
        } else {
            logger.info("found some file $klibFile")
            emptyList()
        }
    }

    private fun getBundlesFromKotlinLibrary(
        klibFile: File,
        logger: Logger
    ): List<File> {
        val layout: KotlinLibraryLayout = getKotlinLibraryLayout(klibFile, logger)
        return layout.resourcesDir.listFilesOrEmpty
            .filter { it.isDirectory && it.extension == "bundle" }
            .map { it.javaFile() }
    }

    private fun getKotlinLibraryLayout(file: File, logger: Logger): KotlinLibraryLayout {
        val klibKonan = org.jetbrains.kotlin.konan.file.File(file.path)
        val klib = KotlinLibraryLayoutImpl(klib = klibKonan, component = "default")

        logger.warn("klib zipped ${klib.isZipped}, resources count ${klib.resourcesDir.listFilesOrEmpty.size}")
        klib.resourcesDir.listFilesOrEmpty.forEach {
            logger.warn("i see $it")
        }

        return if (klib.isZipped) klib.extractingToTemp else klib
    }
}
