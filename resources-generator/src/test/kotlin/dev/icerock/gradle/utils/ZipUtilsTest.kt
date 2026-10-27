/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import java.nio.file.Files
import java.util.zip.ZipFile
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ZipUtilsTest {
    @Test
    fun `zip directory is deterministic and preserves its structure`() {
        val temporaryDirectory = Files.createTempDirectory("moko-resources-zip-test").toFile()
        try {
            val sourceDirectory = temporaryDirectory.resolve("source").apply { mkdirs() }
            sourceDirectory.resolve("b.txt").writeText("second")
            sourceDirectory.resolve("a.txt").writeText("first")
            sourceDirectory.resolve("nested").mkdirs()
            sourceDirectory.resolve("nested/value.txt").writeText("nested")
            sourceDirectory.resolve("empty").mkdirs()

            val firstZip = temporaryDirectory.resolve("first.zip")
            val secondZip = temporaryDirectory.resolve("second.zip")
            sourceDirectory.zipDirAs(firstZip)
            sourceDirectory.zipDirAs(secondZip)

            assertContentEquals(firstZip.readBytes(), secondZip.readBytes())
            ZipFile(firstZip).use { zip ->
                assertEquals(
                    listOf("a.txt", "b.txt", "empty/", "nested/", "nested/value.txt"),
                    zip.entries().asSequence().map { it.name }.toList()
                )
                assertEquals("first", zip.getInputStream(zip.getEntry("a.txt")).bufferedReader().readText())
                assertEquals("nested", zip.getInputStream(zip.getEntry("nested/value.txt")).bufferedReader().readText())
            }
        } finally {
            temporaryDirectory.deleteRecursively()
        }
    }
}
