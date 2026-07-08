/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import dev.icerock.gradle.metadata.resource.PluralMetadata
import dev.icerock.gradle.metadata.resource.PluralMetadata.PluralItem
import dev.icerock.gradle.metadata.resource.PluralMetadata.PluralItem.Quantity
import dev.icerock.gradle.metadata.resource.StringMetadata
import dev.icerock.gradle.utils.calculateResourcesHash
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class DeterministicOutputTest {

    private val plural = PluralMetadata(
        key = "apples",
        values = listOf(
            PluralMetadata.LocaleItem(
                locale = "base",
                values = listOf(
                    PluralItem(Quantity.ONE, "one apple"),
                    PluralItem(Quantity.OTHER, "%d apples"),
                ),
            ),
            PluralMetadata.LocaleItem(
                locale = "ru",
                values = listOf(
                    PluralItem(Quantity.ONE, "яблоко"),
                    PluralItem(Quantity.FEW, "яблока"),
                    PluralItem(Quantity.MANY, "яблок"),
                ),
            ),
        ),
    )

    private val string = StringMetadata(
        key = "hello",
        values = listOf(
            StringMetadata.LocaleItem(locale = "base", value = "Hello, world"),
            StringMetadata.LocaleItem(locale = "ru", value = "Привет, мир"),
        ),
    )

    @Test
    fun pluralContentHashIsStable() {
        assertEquals(expected = "-9167ca0", actual = plural.contentHash())
    }

    @Test
    fun stringContentHashIsStable() {
        assertEquals(expected = "367b4e74", actual = string.contentHash())
    }

    @Test
    fun equivalentMetadataProduceEqualContentHash() {
        assertEquals(expected = plural.contentHash(), actual = plural.copy().contentHash())
    }

    @Test
    fun directoryHashIgnoresFilesystemOrder() {
        val forward = createTree(listOf("a.txt" to "A", "b.txt" to "B", "c.txt" to "C"))
        val reverse = createTree(listOf("c.txt" to "C", "b.txt" to "B", "a.txt" to "A"))
        try {
            assertEquals(expected = forward.calculateResourcesHash(), actual = reverse.calculateResourcesHash())
        } finally {
            forward.deleteRecursively()
            reverse.deleteRecursively()
        }
    }

    private fun createTree(entries: List<Pair<String, String>>): File {
        val dir = Files.createTempDirectory("moko-hash-test").toFile()
        entries.forEach { (name, content) -> File(dir, name).writeText(content) }
        return dir
    }
}
