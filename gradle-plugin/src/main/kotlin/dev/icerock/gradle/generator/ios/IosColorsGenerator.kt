/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.ios

import com.lectra.koson.ObjectType
import com.lectra.koson.arr
import com.lectra.koson.obj
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.ColorNode
import dev.icerock.gradle.generator.ColorsGenerator
import dev.icerock.gradle.generator.ObjectBodyExtendable
import dev.icerock.gradle.utils.ArgbColor
import dev.icerock.gradle.utils.parseArgbColor
import org.gradle.api.file.FileTree
import java.io.File

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class IosColorsGenerator(
    colorsFileTree: FileTree
) : ColorsGenerator(colorsFileTree), ObjectBodyExtendable by IosGeneratorHelper() {
    override fun getImports(): List<ClassName> {
        return listOf(
            ClassName("dev.icerock.moko.graphics", "Color")
        )
    }

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)
    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun generateResources(resourcesGenerationDir: File, colors: List<ColorNode>) {
        val assetsDirectory = File(resourcesGenerationDir, "Assets.xcassets")
        assetsDirectory.mkdir()

        colors.forEach { colorNode ->
            val assetDir = File(assetsDirectory, "${colorNode.name}.colorset")
            assetDir.mkdir()
            val contentsFile = File(assetDir, "Contents.json")
            contentsFile.createNewFile()

            val colorContentObj = if (colorNode.isThemed()) {
                val lightColor = parseArgbColor(colorNode.lightColor!!.toLong(16))
                val darkColor = parseArgbColor(colorNode.darkColor!!.toLong(16))

                val anyColor = buildColorIdiomJsonObj(lightColor) // set any color as light
                val lightColorObj = buildAppearancesIdiomJsonBlock("light", lightColor)
                val darkColorObj = buildAppearancesIdiomJsonBlock("dark", darkColor)

                arr[anyColor, lightColorObj, darkColorObj]
            } else {
                val singleColor = parseArgbColor(colorNode.singleColor!!.toLong(16))

                arr[buildColorIdiomJsonObj(singleColor)]
            }

            val resultObj = obj {
                "colors" to colorContentObj
                "info" to obj {
                    "author" to "xcode"
                    "version" to 1
                }
            }
            contentsFile.writeText(resultObj.pretty())
        }

        val process = Runtime.getRuntime().exec(
            "xcrun actool Assets.xcassets --compile . --platform iphoneos --minimum-deployment-target 9.0",
            emptyArray(),
            assetsDirectory.parentFile
        )
        val errors = process.errorStream.bufferedReader().readText()
        val input = process.inputStream.bufferedReader().readText()
        val result = process.waitFor()
        if (result != 0) {
            println("can't compile assets - $result")
            println(input)
            println(errors)
        } else {
            assetsDirectory.deleteRecursively()
        }
    }

    private fun buildColorJsonObj(argbColor: ArgbColor): ObjectType = obj {
        "color-space" to "srgb"
        "components" to obj {
            "alpha" to argbColor.a
            "red" to argbColor.r
            "green" to argbColor.g
            "blue" to argbColor.b
        }
    }

    private fun buildColorIdiomJsonObj(argbColor: ArgbColor): ObjectType = obj {
        "color" to buildColorJsonObj(argbColor)
        "idiom" to "universal"
    }

    private fun buildAppearancesIdiomJsonBlock(valueTag: String, argbColor: ArgbColor): ObjectType {
        return obj {
            "appearances" to arr[
                    obj {
                        "appearance" to "luminosity"
                        "value" to valueTag
                    }
            ]
            "color" to buildColorJsonObj(argbColor)
            "idiom" to "universal"
        }
    }
}
