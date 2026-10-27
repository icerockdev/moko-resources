/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources

import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.ResourceGenerationNamespace
import dev.icerock.gradle.generator.container.AppleContainerGenerator
import dev.icerock.gradle.generator.resources.asset.AppleAssetResourceGenerator
import dev.icerock.gradle.generator.resources.color.AppleColorResourceGenerator
import dev.icerock.gradle.generator.resources.file.AppleFileResourceGenerator
import dev.icerock.gradle.generator.resources.font.AppleFontResourceGenerator
import dev.icerock.gradle.generator.resources.image.AppleImageResourceGenerator
import dev.icerock.gradle.generator.resources.plural.ApplePluralResourceGenerator
import dev.icerock.gradle.generator.resources.string.AppleStringResourceGenerator
import dev.icerock.gradle.metadata.resource.AssetMetadata
import dev.icerock.gradle.metadata.resource.ColorMetadata
import dev.icerock.gradle.metadata.resource.FileMetadata
import dev.icerock.gradle.metadata.resource.FontMetadata
import dev.icerock.gradle.metadata.resource.ImageMetadata
import dev.icerock.gradle.metadata.resource.PluralMetadata
import dev.icerock.gradle.metadata.resource.ResourceMetadata
import dev.icerock.gradle.metadata.resource.StringMetadata
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class AppleResourceNamespaceIsolationTest {

    @Test
    fun everyAppleResourceTypeUsesTheIsolatedPlatformDetailsProvider() {
        withTemporaryDirectory { directory ->
            val resourceFile = File(directory, "test-resource.dat").apply {
                writeText("test")
            }
            val resourceGenerationNamespace = ResourceGenerationNamespace.isolated("iosTest")
            val providerReference =
                resourceGenerationNamespace.applePlatformDetailsProviderReference

            assertProviderReference(
                generator = AppleStringResourceGenerator(
                    baseLocalizationRegion = "en",
                    resourcesGenerationDir = directory,
                    resourceGenerationNamespace = resourceGenerationNamespace,
                ),
                metadata = StringMetadata(
                    key = "test_string",
                    values = listOf(
                        StringMetadata.LocaleItem(locale = "base", value = "Test"),
                    ),
                ),
                providerReference = providerReference,
            )
            assertProviderReference(
                generator = ApplePluralResourceGenerator(
                    baseLocalizationRegion = "en",
                    resourcesGenerationDir = directory,
                    resourceGenerationNamespace = resourceGenerationNamespace,
                ),
                metadata = PluralMetadata(
                    key = "test_plural",
                    values = listOf(
                        PluralMetadata.LocaleItem(
                            locale = "base",
                            values = listOf(
                                PluralMetadata.PluralItem(
                                    quantity = PluralMetadata.PluralItem.Quantity.OTHER,
                                    value = "Tests",
                                ),
                            ),
                        ),
                    ),
                ),
                providerReference = providerReference,
            )
            assertProviderReference(
                generator = AppleImageResourceGenerator(
                    assetsGenerationDir = directory,
                    resourceGenerationNamespace = resourceGenerationNamespace,
                ),
                metadata = ImageMetadata(
                    key = "test_image",
                    values = listOf(
                        ImageMetadata.ImageItem(
                            quality = null,
                            appearance = ImageMetadata.Appearance.LIGHT,
                            filePath = resourceFile,
                        ),
                    ),
                ),
                providerReference = providerReference,
            )
            assertProviderReference(
                generator = AppleColorResourceGenerator(
                    assetsGenerationDir = directory,
                    resourceGenerationNamespace = resourceGenerationNamespace,
                ),
                metadata = ColorMetadata(
                    key = "test_color",
                    value = ColorMetadata.ColorItem.Single(
                        color = ColorMetadata.Color(0, 0, 0, 255),
                    ),
                ),
                providerReference = providerReference,
            )
            assertProviderReference(
                generator = AppleFontResourceGenerator(
                    resourcesGenerationDir = directory,
                    resourceGenerationNamespace = resourceGenerationNamespace,
                ),
                metadata = FontMetadata(key = "test_font", filePath = resourceFile),
                providerReference = providerReference,
            )
            assertProviderReference(
                generator = AppleFileResourceGenerator(
                    resourcesGenerationDir = directory,
                    resourceGenerationNamespace = resourceGenerationNamespace,
                ),
                metadata = FileMetadata(
                    key = "test_file",
                    relativePath = directory,
                    filePath = resourceFile,
                ),
                providerReference = providerReference,
            )
            assertProviderReference(
                generator = AppleAssetResourceGenerator(
                    resourcesGenerationDir = directory,
                    resourceGenerationNamespace = resourceGenerationNamespace,
                ),
                metadata = AssetMetadata(
                    key = "test_asset",
                    relativePath = directory,
                    filePath = resourceFile,
                ),
                providerReference = providerReference,
            )
        }
    }

    @Test
    fun appleContainerGeneratesTheProviderReferencedByResourceGenerators() {
        val resourceGenerationNamespace = ResourceGenerationNamespace.isolated("iosTest")
        val providerFile = AppleContainerGenerator(
            bundleIdentifier = "com.example.test",
            resourceGenerationNamespace = resourceGenerationNamespace,
        ).generateAdditionalFiles(packageName = "com.example").single()

        assertEquals(
            resourceGenerationNamespace.applePlatformDetailsProviderName,
            providerFile.name,
        )
        assertContains(
            providerFile.toString(),
            "object ${resourceGenerationNamespace.applePlatformDetailsProviderName}",
        )
        assertEquals(
            "${providerFile.name}.details",
            resourceGenerationNamespace.applePlatformDetailsProviderReference,
        )
    }

    private fun <T : ResourceMetadata> assertProviderReference(
        generator: PlatformResourceGenerator<T>,
        metadata: T,
        providerReference: String,
    ) {
        val container = TypeSpec.objectBuilder("resources")
        generator.generateContainerProperties(container, listOf(metadata))

        assertContains(container.build().toString(), providerReference)
        assertContains(
            generator.generateAccessorInitializer(metadata).toString(),
            "$providerReference.nsBundle",
        )
    }

    private fun withTemporaryDirectory(block: (File) -> Unit) {
        val directory = Files.createTempDirectory("moko-apple-test-resources").toFile()
        try {
            block(directory)
        } finally {
            directory.deleteRecursively()
        }
    }
}
