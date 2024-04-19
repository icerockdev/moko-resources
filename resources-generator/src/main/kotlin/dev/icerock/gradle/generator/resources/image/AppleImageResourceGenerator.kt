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

            validItems.forEach { it.filePath.copyTo(File(assetDir, it.filePath.name)) }

            val imagesContent: JsonArray = buildJsonArray {
                validItems.map { item ->
                    buildJsonObject {
                        put("idiom", JsonPrimitive("universal"))
                        put("filename", JsonPrimitive(item.filePath.name))
                        item.quality?.let { quality ->
                            put("scale", JsonPrimitive(quality + "x"))
                        }
                        item.appearance?.let { appearance ->
                            put("appearances", buildJsonArray {
                                add(buildJsonObject {
                                    put("appearance", JsonPrimitive("luminosity"))
                                    put("value", JsonPrimitive(appearance.name.lowercase()))
                                })
                            })
                        }
                    }
                }.forEach { add(it) }
            }

            val content: String = buildJsonObject {
                put("images", imagesContent)
                put(
                    "info",
                    buildJsonObject {
                        put("version", JsonPrimitive(1))
                        put("author", JsonPrimitive("xcode"))
                    }
                )

                if (validItems.any { it.quality == null }) {
                    put(
                        "properties",
                        buildJsonObject {
                            put("preserves-vector-representation", JsonPrimitive(true))
                        }
                    )
                }
            }.toString()

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

    private companion object {
        val VALID_SIZES: IntRange = 1..3
    }
}
