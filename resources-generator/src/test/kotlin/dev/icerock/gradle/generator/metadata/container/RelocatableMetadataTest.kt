/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.metadata.container

import dev.icerock.gradle.metadata.container.ContainerMetadata
import dev.icerock.gradle.metadata.container.ResourceType
import dev.icerock.gradle.metadata.resource.AssetMetadata
import dev.icerock.gradle.metadata.resource.FileMetadata
import dev.icerock.gradle.metadata.resource.FontMetadata
import dev.icerock.gradle.metadata.resource.ImageMetadata
import dev.icerock.gradle.metadata.resource.ResourceMetadata
import dev.icerock.gradle.serialization.RelativeFileSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RelocatableMetadataTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val serializer: KSerializer<List<ContainerMetadata>> =
        ListSerializer(ContainerMetadata.serializer())

    @Test
    fun metadataWrittenInOneCheckoutResolvesInAnother() {
        val checkoutA: File = tempFolder.newFolder("checkout-a")
        val checkoutB: File = tempFolder.newFolder("checkout-b")

        val encoded: String = jsonFor(checkoutA).encodeToString(serializer, metadataIn(checkoutA))

        assertFalse(
            actual = encoded.contains(checkoutA.path),
            message = "metadata must not contain the absolute checkout path:\n$encoded"
        )
        assertTrue(
            actual = encoded.contains("\"shared/src/commonMain/moko-resources/images/logo@2x.png\""),
            message = "metadata must contain paths relative to the root directory:\n$encoded"
        )

        val decoded: List<ContainerMetadata> = jsonFor(checkoutB).decodeFromString(serializer, encoded)

        assertEquals(expected = metadataIn(checkoutB), actual = decoded)
    }

    @Test
    fun assetPathRelativeToBaseSurvivesRelocation() {
        val checkoutA: File = tempFolder.newFolder("checkout-a")
        val checkoutB: File = tempFolder.newFolder("checkout-b")

        val encoded: String = jsonFor(checkoutA).encodeToString(serializer, metadataIn(checkoutA))
        val asset: AssetMetadata = jsonFor(checkoutB).decodeFromString(serializer, encoded)
            .flatMap { it.resources }
            .filterIsInstance<AssetMetadata>()
            .single()

        assertEquals(
            expected = "data/config.json",
            actual = asset.pathRelativeToBase.invariantSeparatorsPath
        )
    }

    @Test
    fun fileOutsideOfRootDirectoryIsStoredAsAbsolutePath() {
        val rootDir: File = tempFolder.newFolder("checkout")
        val font = File(tempFolder.newFolder("external-resources"), "fonts/inter.ttf")
        val metadata: List<ContainerMetadata> = listOf(
            container(ResourceType.FONTS, FontMetadata(key = "inter", filePath = font))
        )

        val encoded: String = jsonFor(rootDir).encodeToString(serializer, metadata)

        assertTrue(
            actual = encoded.contains(Json.encodeToString(String.serializer(), font.path)),
            message = "metadata must keep the absolute path of a file outside of the root directory:\n$encoded"
        )
        assertFalse(
            actual = encoded.contains(".."),
            message = "metadata must not contain parent directory references:\n$encoded"
        )
        assertEquals(
            expected = metadata,
            actual = jsonFor(tempFolder.newFolder("other-checkout")).decodeFromString(serializer, encoded)
        )
    }

    private fun jsonFor(rootDir: File): Json = Json {
        serializersModule = SerializersModule {
            contextual(File::class, RelativeFileSerializer(rootDir))
        }
    }

    private fun metadataIn(rootDir: File): List<ContainerMetadata> {
        val resourcesDir = File(rootDir, "shared/src/commonMain/moko-resources")
        return listOf(
            container(
                ResourceType.IMAGES,
                ImageMetadata(
                    key = "logo",
                    values = listOf(
                        ImageMetadata.ImageItem(
                            quality = "2",
                            appearance = ImageMetadata.Appearance.LIGHT,
                            filePath = File(resourcesDir, "images/logo@2x.png")
                        )
                    )
                )
            ),
            container(
                ResourceType.FONTS,
                FontMetadata(
                    key = "inter_regular",
                    filePath = File(resourcesDir, "fonts/Inter-Regular.ttf")
                )
            ),
            container(
                ResourceType.FILES,
                FileMetadata(
                    key = "licenses",
                    relativePath = File(resourcesDir, "files"),
                    filePath = File(resourcesDir, "files/licenses.txt")
                )
            ),
            container(
                ResourceType.ASSETS,
                AssetMetadata(
                    key = "data_config",
                    relativePath = File(resourcesDir, "assets"),
                    filePath = File(resourcesDir, "assets/data/config.json")
                )
            )
        )
    }

    private fun container(type: ResourceType, resource: ResourceMetadata): ContainerMetadata {
        return ContainerMetadata(
            name = type.name.lowercase(),
            parentObjectName = "MR",
            resourceType = type,
            resources = listOf(resource)
        )
    }
}
