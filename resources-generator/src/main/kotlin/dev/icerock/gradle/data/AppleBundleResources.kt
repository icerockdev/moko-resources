/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.data

import org.gradle.api.logging.Logger
import java.io.File

/**
 * Searches for Apple resource bundles in all filesystem shapes exposed by Kotlin/Native tasks.
 */
internal fun getAppleBundlesFromKlibSources(
    sourceFiles: Iterable<File>,
    logger: Logger,
): List<File> = sourceFiles.flatMap { sourceFile ->
    val isPackedKlib = sourceFile.isFile && sourceFile.extension == "klib"
    val isUnpackedKlib = sourceFile.isDirectory

    when {
        isPackedKlib || isUnpackedKlib -> {
            logger.info("found klib {}", sourceFile)
            getAppleBundlesFromKotlinLibrary(sourceFile)
        }

        sourceFile.name == "manifest" && sourceFile.parentFile?.name == "default" -> {
            // Kotlin/Native may expose the content files instead of the unpacked KLib directory.
            logger.info("found manifest of klib {}", sourceFile)
            getAppleBundlesFromKotlinLibrary(sourceFile.parentFile.parentFile)
        }

        else -> {
            logger.debug("found some file {}", sourceFile)
            emptyList()
        }
    }
}

private fun getAppleBundlesFromKotlinLibrary(klibFile: File): List<File> {
    val resourcesDir: File = getKlibResourcesDir(klibFile) ?: return emptyList()
    return resourcesDir.listFiles()
        ?.filter { it.isDirectory && it.extension == "bundle" }
        ?: emptyList()
}
