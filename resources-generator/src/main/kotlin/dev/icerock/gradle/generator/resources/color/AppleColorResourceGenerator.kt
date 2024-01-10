/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.color

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addAppleContainerBundleProperty
import dev.icerock.gradle.metadata.resource.ColorMetadata
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File

internal class AppleColorResourceGenerator(
    private val assetsGenerationDir: File
) : PlatformResourceGenerator<ColorMetadata> {
    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: ColorMetadata): CodeBlock {
        return CodeBlock.of(
            "ColorResource(name = %S, bundle = %L)",
            metadata.key,
            Constants.Apple.containerBundlePropertyName
        )
    }

    override fun generateResourceFiles(data: List<ColorMetadata>) {
        val assetsDirectory = File(assetsGenerationDir, Constants.Apple.assetsDirectoryName)

        data.forEach { imageMetadata ->
            val assetDir = File(assetsDirectory, "${imageMetadata.key}.colorset")
            assetDir.mkdirs()

            val contentsFile = File(assetDir, "Contents.json")
            contentsFile.createNewFile()

            val colorContentObj: JsonArray = when (imageMetadata.value) {
                is ColorMetadata.ColorItem.Single -> {
                    buildJsonArray {
                        add(buildColorIdiomJson(imageMetadata.value.color))
                    }
                }

                is ColorMetadata.ColorItem.Themed -> {
                    /*
                    Sets any color as light. Check about colors in the docs:
                    https://developer.apple.com/documentation/xcode/supporting_dark_mode_in_your_interface
                     */
                    val anyColor = buildColorIdiomJson(imageMetadata.value.light)
                    val lightColorObj = buildAppearancesIdiomJson(
                        valueTag = "light",
                        color = imageMetadata.value.light
                    )
                    val darkColorObj = buildAppearancesIdiomJson(
                        valueTag = "dark",
                        color = imageMetadata.value.dark
                    )

                    buildJsonArray {
                        add(anyColor)
                        add(lightColorObj)
                        add(darkColorObj)
                    }
                }
            }

            val resultObj: JsonObject = buildJsonObject {
                put("colors", colorContentObj)
                put(
                    "info",
                    buildJsonObject {
                        put("author", "xcode")
                        put("version", 1)
                    }
                )
            }
            contentsFile.writeText(resultObj.toString())
        }
    }

    override fun generateBeforeProperties(
        builder: TypeSpec.Builder,
        metadata: List<ColorMetadata>
    ) {
        builder.addAppleContainerBundleProperty()
    }

    private fun buildAppearancesIdiomJson(
        valueTag: String,
        color: ColorMetadata.Color
    ): JsonObject {
        return buildJsonObject {
            put(
                "appearances",
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put("appearance", "luminosity")
                            put("value", valueTag)
                        }
                    )
                }
            )
            put("color", buildColorJson(color))
            put("idiom", "universal")
        }
    }

    private fun buildColorIdiomJson(color: ColorMetadata.Color): JsonObject = buildJsonObject {
        put("color", buildColorJson(color))
        put("idiom", "universal")
    }

    private fun buildColorJson(color: ColorMetadata.Color): JsonObject = buildJsonObject {
        put("color-space", "srgb")
        put(
            "components",
            @Suppress("MagicNumber")
            buildJsonObject {
                put("alpha", color.alpha / 255.0f)
                put("red", color.red / 255.0f)
                put("green", color.green / 255.0f)
                put("blue", color.blue / 255.0f)
            }
        )
    }
}
