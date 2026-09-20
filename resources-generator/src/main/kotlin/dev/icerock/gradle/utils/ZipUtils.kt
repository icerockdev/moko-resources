/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

private val zeroFileTimestamp: FileTime = FileTime.fromMillis(0)
private const val ZIP_COMPRESSION_LEVEL = 5

internal fun File.zipDirAs(zipFile: File) {
    val sourceRoot = toPath().toRealPath()

    zipFile.outputStream().use { output ->
        ZipOutputStream(output).use { zip ->
            zip.setLevel(ZIP_COMPRESSION_LEVEL)
            zip.addDirectoryEntries(sourceRoot)
        }
    }
}

private fun ZipOutputStream.addDirectoryEntries(sourceRoot: Path) {
    Files.walk(sourceRoot).use { paths ->
        paths.sorted().forEach { path ->
            addPathEntry(sourceRoot, path)
        }
    }
}

private fun ZipOutputStream.addPathEntry(
    sourceRoot: Path,
    path: Path,
) {
    val realPath = path.toRealPath()
    if (!realPath.startsWith(sourceRoot)) {
        throw ZipException(
            "An attempt to escape the source directory $sourceRoot in symlink $path"
        )
    }
    if (realPath == sourceRoot) return

    val entryName = sourceRoot.relativize(path)
        .joinToString(separator = "/") { it.toString() }
    val attributes = Files.readAttributes(
        realPath,
        BasicFileAttributes::class.java,
        LinkOption.NOFOLLOW_LINKS
    )

    when {
        attributes.isRegularFile -> addFileEntry(entryName, realPath.toFile())
        attributes.isDirectory -> addDirectoryEntry(entryName)
        else -> error("Unsupported file type encountered: $path")
    }
}

private fun ZipOutputStream.addFileEntry(name: String, file: File) {
    addEntry(ZipEntry(name).apply { method = ZipEntry.DEFLATED }) {
        file.inputStream().use { it.copyTo(this) }
    }
}

private fun ZipOutputStream.addDirectoryEntry(name: String) {
    addEntry(
        ZipEntry("$name/").apply {
            method = ZipEntry.STORED
            size = 0
            crc = 0
        }
    )
}

private inline fun ZipOutputStream.addEntry(entry: ZipEntry, writeContent: ZipOutputStream.() -> Unit = {}) {
    entry.creationTime = zeroFileTimestamp
    entry.lastModifiedTime = zeroFileTimestamp
    entry.lastAccessTime = zeroFileTimestamp
    entry.extra = null

    putNextEntry(entry)
    try {
        writeContent()
    } finally {
        closeEntry()
    }
}

internal fun unzipTo(outputDirectory: File, zipFile: File) {
    ZipFile(zipFile).use { zip ->
        val outputDirectoryCanonicalPath = outputDirectory.canonicalPath
        for (entry in zip.entries()) {
            unzipEntryTo(outputDirectory, outputDirectoryCanonicalPath, zip, entry)
        }
    }
}

private fun unzipEntryTo(
    outputDirectory: File,
    outputDirectoryCanonicalPath: String,
    zip: ZipFile,
    entry: ZipEntry
) {
    val output = outputDirectory.resolve(entry.name)
    if (!output.canonicalPath.startsWith(outputDirectoryCanonicalPath)) {
        throw ZipException("Zip entry '${entry.name}' is outside of the output directory")
    }
    if (entry.isDirectory) {
        output.mkdirs()
    } else {
        output.parentFile.mkdirs()
        zip.getInputStream(entry).use { it.copyTo(output) }
    }
}

private fun InputStream.copyTo(file: File): Long =
    file.outputStream().use { copyTo(it) }
