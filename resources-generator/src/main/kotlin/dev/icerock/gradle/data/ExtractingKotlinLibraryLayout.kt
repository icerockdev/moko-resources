/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.data

import org.jetbrains.kotlin.konan.file.File as KonanFile
import org.jetbrains.kotlin.konan.file.createTempDir
import org.jetbrains.kotlin.konan.file.unzipTo
import java.io.File

private const val DEFAULT_COMPONENT = "default"
private const val RESOURCES_DIR_NAME = "resources"

/**
 * Gets the resources directory from a klib file (packed or unpacked).
 *
 * For a packed (zipped) klib (single .klib file), extracts the resources
 * directory to a temporary location and returns it.
 *
 * For an unpacked klib (directory), returns the direct path to the
 * resources directory.
 *
 * The structure of a klib is:
 * - Packed: a .klib zip containing `default/resources/`
 * - Unpacked: a directory with `default/resources/` subdirectory
 */
internal fun getKlibResourcesDir(klibFile: File): File {
    return if (klibFile.isFile) {
        // Packed (zipped) klib - extract resources to temp
        val konanFile = KonanFile(klibFile.path)
        val resourcesSubdir = konanFile.child(DEFAULT_COMPONENT).child(RESOURCES_DIR_NAME)
        val temporary = createTempDir(RESOURCES_DIR_NAME)
        temporary.deleteOnExitRecursively()
        konanFile.unzipTo(temporary, fromSubdirectory = resourcesSubdir)
        File(temporary.path)
    } else {
        // Unpacked klib directory - navigate to resources
        File(klibFile, "$DEFAULT_COMPONENT/$RESOURCES_DIR_NAME")
    }
}
