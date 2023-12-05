/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.apple

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.ColorNode
import dev.icerock.gradle.generator.ColorsGenerator
import dev.icerock.gradle.generator.ObjectBodyExtendable
import dev.icerock.gradle.generator.apple.AppleMRGenerator.Companion.ASSETS_DIR_NAME
import dev.icerock.gradle.utils.ArgbColor
import dev.icerock.gradle.utils.parseRgbaColor
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import java.io.File

class AppleColorsGenerator(
    project: Project,
    ownColorsFileTree: FileTree,
    lowerColorsFileTree: FileTree,
) : ColorsGenerator(project, ownColorsFileTree), ObjectBodyExtendable by AppleGeneratorHelper() {
    override fun getImports(): List<ClassName> {
        return listOf(
            ClassName("dev.icerock.moko.graphics", "Color")
        )
    }

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)
    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun generateResources(resourcesGenerationDir: File, colors: List<ColorNode>) {
        val assetsDirectory = File(resourcesGenerationDir, ASSETS_DIR_NAME)

        colors.forEach { colorNode ->
            val assetDir = File(assetsDirectory, "${colorNode.name}.colorset")
            assetDir.mkdir()
            val contentsFile = File(assetDir, "Contents.json")
            contentsFile.createNewFile()

            val colorContentObj = if (colorNode.isThemed()) {
                @Suppress("MagicNumber")
                val lightColor = parseRgbaColor(colorNode.lightColor!!.toLong(16))

                @Suppress("MagicNumber")
                val darkColor = parseRgbaColor(colorNode.darkColor!!.toLong(16))

                /*
                Sets any color as light. Check about colors in the docs:
                https://developer.apple.com/documentation/xcode/supporting_dark_mode_in_your_interface
                 */
                val anyColor = buildColorIdiomJsonObj(lightColor)
                val lightColorObj = buildAppearancesIdiomJsonBlock("light", lightColor)
                val darkColorObj = buildAppearancesIdiomJsonBlock("dark", darkColor)

                buildJsonArray {
                    add(anyColor)
                    add(lightColorObj)
                    add(darkColorObj)
                }
            } else {
                @Suppress("MagicNumber")
                val singleColor = parseRgbaColor(colorNode.singleColor!!.toLong(16))
                buildJsonArray {
                    add(buildColorIdiomJsonObj(singleColor))
                }
            }

            val resultObj = buildJsonObject {
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

    private fun buildColorJsonObj(argbColor: ArgbColor): JsonObject = buildJsonObject {
        put("color-space", "srgb")
        put(
            "components",
            buildJsonObject {
                put("alpha", argbColor.a)
                put("red", argbColor.r)
                put("green", argbColor.g)
                put("blue", argbColor.b)
            }
        )
    }

    private fun buildColorIdiomJsonObj(argbColor: ArgbColor): JsonObject = buildJsonObject {
        put("color", buildColorJsonObj(argbColor))
        put("idiom", "universal")
    }

    private fun buildAppearancesIdiomJsonBlock(valueTag: String, argbColor: ArgbColor): JsonObject {
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
            put("color", buildColorJsonObj(argbColor))
            put("idiom", "universal")
        }
    }

    override fun getPropertyInitializer(color: ColorNode): CodeBlock {
        return CodeBlock.of(
            "ColorResource(name = %S, bundle = ${AppleMRGenerator.BUNDLE_PROPERTY_NAME})",
            color.name
        )
    }
}
