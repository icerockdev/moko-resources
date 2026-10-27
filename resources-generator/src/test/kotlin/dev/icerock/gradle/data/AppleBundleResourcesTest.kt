/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.data

import org.gradle.api.logging.Logging
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.assertEquals

class AppleBundleResourcesTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `finds bundle in unpacked klib root`() {
        val klib = temporaryFolder.newFolder("library")
        createBundle(File(klib, "default/resources/example.main.bundle"))

        assertBundleNames(
            sources = listOf(klib),
            expected = setOf("example.main.bundle"),
        )
    }

    @Test
    fun `finds bundle from unpacked klib manifest`() {
        val klib = temporaryFolder.newFolder("library")
        val manifest = File(klib, "default/manifest").apply {
            parentFile.mkdirs()
            writeText("unique_name=example")
        }
        createBundle(File(klib, "default/resources/example.main.bundle"))

        assertBundleNames(
            sources = listOf(manifest),
            expected = setOf("example.main.bundle"),
        )
    }

    @Test
    fun `finds bundle in packed klib`() {
        val klib = temporaryFolder.newFile("library.klib")
        ZipOutputStream(klib.outputStream()).use { zip ->
            zip.putNextEntry(
                ZipEntry("default/resources/example.main.bundle/Contents/Info.plist")
            )
            zip.write("plist".toByteArray())
            zip.closeEntry()
        }

        assertBundleNames(
            sources = listOf(klib),
            expected = setOf("example.main.bundle"),
        )
    }

    private fun assertBundleNames(sources: List<File>, expected: Set<String>) {
        val actual = getAppleBundlesFromKlibSources(
            sourceFiles = sources,
            logger = Logging.getLogger(AppleBundleResourcesTest::class.java),
        ).mapTo(mutableSetOf()) { it.name }

        assertEquals(expected, actual)
    }

    private fun createBundle(bundleDirectory: File) {
        File(bundleDirectory, "Contents/Info.plist").apply {
            parentFile.mkdirs()
            writeText("plist")
        }
    }
}
