/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.image

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addAppleContainerBundleInitializerProperty
import dev.icerock.gradle.generator.addValuesFunction
import dev.icerock.gradle.metadata.resource.ImageMetadata
import dev.icerock.gradle.metadata.resource.ImageMetadata.ImageItem
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import org.gradle.api.InvalidUserDataException
import java.io.File

internal class AppleImageResourceGenerator(
    private val assetsGenerationDir: File,
) : PlatformResourceGenerator<ImageMetadata> {
    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: ImageMetadata): CodeBlock {
        return CodeBlock.of(
            "ImageResource(assetImageName = %S, bundle = %L)",
            metadata.key,
            Constants.Apple.platformContainerBundlePropertyName
        )
    }

    @Suppress("LongMethod")
    override fun generateResourceFiles(data: List<ImageMetadata>) {
        val assetsDirectory = File(assetsGenerationDir, Constants.Apple.assetsDirectoryName)

        data.forEach { imageMetadata ->
            val assetDir = File(assetsDirectory, "${imageMetadata.key}.imageset")
            assetDir.mkdirs()
            val contentsFile = File(assetDir, "Contents.json")

            val validItems: List<ImageMetadata.ImageItem> =
                imageMetadata.values.filter { item ->
                    item.quality == null || VALID_SIZES.any { item.quality == it.toString() }
                }

            resourceIsValidOrError(validItems, imageMetadata)

            validItems.forEach { it.filePath.copyTo(File(assetDir, it.filePath.name)) }

            val imagesContent: JsonArray = getImagesContent(validItems, imageMetadata)
            val content: String = prepareContentInfo(imagesContent, validItems)

            contentsFile.writeText(content)
        }
    }

    override fun generateBeforeProperties(
        builder: Builder,
        metadata: List<ImageMetadata>,
        modifier: KModifier?,
    ) {
        builder.addAppleContainerBundleInitializerProperty(modifier)
    }

    override fun generateAfterProperties(
        builder: Builder,
        metadata: List<ImageMetadata>,
        modifier: KModifier?,
    ) {
        builder.addValuesFunction(
            metadata = metadata,
            classType = Constants.imageResourceName,
            modifier = modifier
        )
    }

    private fun getImagesContent(
        validItems: List<ImageItem>,
        imageMetadata: ImageMetadata,
    ): JsonArray {
        return buildJsonArray {
            validItems.map { item ->
                buildJsonObject {
                    put(
                        key = "idiom",
                        element = JsonPrimitive("universal")
                    )
                    put(
                        key = "filename",
                        element = JsonPrimitive(item.filePath.name)
                    )
                    item.quality?.let { quality ->
                        put(
                            key = "scale",
                            element = JsonPrimitive(quality + "x")
                        )
                    }
                    if (imageMetadata.isThemed) {
                        put(
                            key = "appearances",
                            element = buildJsonArray {
                                add(
                                    buildJsonObject {
                                        put(
                                            key = "appearance",
                                            element = JsonPrimitive("luminosity")
                                        )
                                        put(
                                            key = "value",
                                            element = JsonPrimitive(item.appearance.name.lowercase())
                                        )
                                    }
                                )
                            }
                        )
                    }
                }
            }.forEach { add(it) }
        }
    }

    private fun resourceIsValidOrError(
        validItems: List<ImageItem>,
        imageMetadata: ImageMetadata,
    ) {
        if (validItems.isEmpty()) {
            val errorMessage: String = buildString {
                val name: String = imageMetadata.key
                appendLine("Apple Generator cannot find a valid scale for file with name \"${name}\".")
                append("Note: Apple resources can have only 1x, 2x and 3x scale factors ")
                append("(https://developer.apple.com/design/human-interface-guidelines/ios/")
                appendLine("icons-and-images/image-size-and-resolution/).")
                append("It is still possible to use 4x images for android, but you need to ")
                append("add a valid iOS variant.")
            }
            throw InvalidUserDataException(errorMessage)
        }
    }

    private fun prepareContentInfo(
        imagesContent: JsonArray,
        validItems: List<ImageItem>,
    ): String {
        return buildJsonObject {
            put(key = "images", element = imagesContent)
            put(
                key = "info",
                element = buildJsonObject {
                    put(key = "version", element = JsonPrimitive(1))
                    put(key = "author", element = JsonPrimitive("xcode"))
                }
            )

            if (validItems.any { it.quality == null }) {
                put(
                    key = "properties",
                    element = buildJsonObject {
                        put(
                            key = "preserves-vector-representation",
                            element = JsonPrimitive(true)
                        )
                    }
                )
            }
        }.toString()
    }

    private companion object {
        val VALID_SIZES: IntRange = 1..3
    }
}
