/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources

import dev.icerock.gradle.generator.platform.js.JsFilePathMode
import dev.icerock.gradle.generator.ResourceGenerationNamespace
import dev.icerock.gradle.generator.resources.plural.JsPluralResourceGenerator
import dev.icerock.gradle.generator.resources.plural.JvmPluralResourceGenerator
import dev.icerock.gradle.generator.resources.string.JsStringResourceGenerator
import dev.icerock.gradle.generator.resources.string.JvmStringResourceGenerator
import dev.icerock.gradle.metadata.resource.PluralMetadata
import dev.icerock.gradle.metadata.resource.StringMetadata
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

class LocalizationResourceNamespaceIsolationTest {

    private val resourceGenerationNamespace = ResourceGenerationNamespace.isolated("jvmTest")

    private val stringMetadata = StringMetadata(
        key = "test_value",
        values = listOf(
            StringMetadata.LocaleItem(locale = "base", value = "Test value"),
        ),
    )
    private val pluralMetadata = PluralMetadata(
        key = "test_plural",
        values = listOf(
            PluralMetadata.LocaleItem(
                locale = "base",
                values = listOf(
                    PluralMetadata.PluralItem(
                        quantity = PluralMetadata.PluralItem.Quantity.OTHER,
                        value = "Test values",
                    ),
                ),
            ),
        ),
    )

    @Test
    fun jvmTestLocalizationFilesIncludeSourceSetNamespace() = withTemporaryDirectory { outputDir ->
        JvmStringResourceGenerator(
            flattenClassPackage = "comexample",
            resourcesGenerationDir = outputDir,
            resourceGenerationNamespace = resourceGenerationNamespace,
        ).generateResourceFiles(listOf(stringMetadata))
        JvmPluralResourceGenerator(
            flattenClassPackage = "comexample",
            resourcesGenerationDir = outputDir,
            resourceGenerationNamespace = resourceGenerationNamespace,
        ).generateResourceFiles(listOf(pluralMetadata))

        assertTrue(
            File(outputDir, "localization/comexample_mokoBundle_jvmTest.properties").isFile
        )
        assertTrue(
            File(outputDir, "localization/comexample_mokoPluralsBundle_jvmTest.properties").isFile
        )
    }

    @Test
    fun jsTestLocalizationFilesIncludeSourceSetNamespace() = withTemporaryDirectory { outputDir ->
        JsStringResourceGenerator(
            resourcesPackageName = "com.example",
            resourcesGenerationDir = outputDir,
            filePathMode = JsFilePathMode.require,
            resourceGenerationNamespace = ResourceGenerationNamespace.isolated("jsTest"),
        ).generateResourceFiles(listOf(stringMetadata))
        JsPluralResourceGenerator(
            resourcesPackageName = "com.example",
            resourcesGenerationDir = outputDir,
            filePathMode = JsFilePathMode.require,
            resourceGenerationNamespace = ResourceGenerationNamespace.isolated("jsTest"),
        ).generateResourceFiles(listOf(pluralMetadata))

        assertTrue(
            File(outputDir, "localization/comexample_stringsJson_jsTest.json").isFile
        )
        assertTrue(
            File(outputDir, "localization/comexample_pluralsJson_jsTest.json").isFile
        )
    }

    private fun withTemporaryDirectory(block: (File) -> Unit) {
        val directory = Files.createTempDirectory("moko-test-resources").toFile()
        try {
            block(directory)
        } finally {
            directory.deleteRecursively()
        }
    }
}
