/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.data

import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.file.file
import org.jetbrains.kotlin.konan.file.unzipTo
import org.jetbrains.kotlin.konan.file.withZipFileSystem
import org.jetbrains.kotlin.library.KotlinLibraryLayout
import org.jetbrains.kotlin.library.impl.KotlinLibraryLayoutImpl

/**
 * The code in this file is pulled from a previous version of Kotlin to replicate
 * removed functionality that MR relies on for extracting klibs.
 * https://github.com/JetBrains/kotlin/blob/00984f32ac1ebc2e7fb71b440c282be2a8b05f36/compiler/util-klib/src/org/jetbrains/kotlin/library/impl/KotlinLibraryLayoutImpl.kt
 */

internal open class ExtractingKotlinLibraryLayout(zipped: KotlinLibraryLayoutImpl) : KotlinLibraryLayout {
    override val libFile: File get() = error("Extracting layout doesn't extract its own root")
    override val libraryName = zipped.libraryName
    override val component = zipped.component
}

internal class ExtractingBaseLibraryImpl(zipped: KotlinLibraryLayoutImpl) : ExtractingKotlinLibraryLayout(zipped) {
    override val manifestFile: File by lazy { zipped.extract(zipped.manifestFile) }
    override val resourcesDir: File by lazy { zipped.extractDir(zipped.resourcesDir) }
}

private fun KotlinLibraryLayoutImpl.extract(file: File): File = extract(this.klib, file)

private fun extract(zipFile: File, file: File) = zipFile.withZipFileSystem { zipFileSystem ->
    val temporary = org.jetbrains.kotlin.konan.file.createTempFile(file.name)
    zipFileSystem.file(file).copyTo(temporary)
    temporary.deleteOnExit()
    temporary
}

private fun KotlinLibraryLayoutImpl.extractDir(directory: File): File = extractDir(this.klib, directory)

private fun extractDir(zipFile: File, directory: File): File {
    val temporary = org.jetbrains.kotlin.konan.file.createTempDir(directory.name)
    temporary.deleteOnExitRecursively()
    zipFile.unzipTo(temporary, fromSubdirectory = directory)
    return temporary
}
