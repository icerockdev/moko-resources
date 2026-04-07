/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.data

import java.io.File
import java.nio.file.Files
import java.util.zip.ZipFile

private const val DEFAULT_COMPONENT = "default"
private const val RESOURCES_DIR_NAME = "resources"
private const val RESOURCES_PREFIX = "$DEFAULT_COMPONENT/$RESOURCES_DIR_NAME/"

/**
 * Gets the resources directory from a klib file (packed or unpacked).
 *
 * For a packed (zipped) klib (single .klib file), extracts the resources
 * directory to a temporary location and returns it. Returns null if the
 * klib does not contain a resources directory.
 *
 * For an unpacked klib (directory), returns the direct path to the
 * resources directory (which may not exist if the klib has no resources).
 *
 * The structure of a klib is:
 * - Packed: a .klib zip containing `default/resources/`
 * - Unpacked: a directory with `default/resources/` subdirectory
 */
internal fun getKlibResourcesDir(klibFile: File): File? {
    return if (klibFile.isFile) {
        // Packed (zipped) klib - extract resources to temp if present
        extractResourcesFromPackedKlib(klibFile)
    } else {
        // Unpacked klib directory - navigate to resources (may not exist)
        File(klibFile, "$DEFAULT_COMPONENT/$RESOURCES_DIR_NAME")
    }
}

private fun extractResourcesFromPackedKlib(klibFile: File): File? {
    ZipFile(klibFile).use { zip ->
        val hasResources = zip.entries().asSequence().any { it.name.startsWith(RESOURCES_PREFIX) }
        if (!hasResources) return null

        val temporary = Files.createTempDirectory(RESOURCES_DIR_NAME).toFile().also {
            it.deleteOnExit()
        }
        zip.entries().asSequence()
            .filter { it.name.startsWith(RESOURCES_PREFIX) }
            .forEach { entry ->
                val relativeName = entry.name.removePrefix(RESOURCES_PREFIX)
                if (relativeName.isEmpty()) return@forEach
                val output = File(temporary, relativeName)
                if (entry.isDirectory) {
                    output.mkdirs()
                } else {
                    output.parentFile?.mkdirs()
                    zip.getInputStream(entry).use { it.copyTo(output.outputStream()) }
                }
            }
        return temporary
    }
}
