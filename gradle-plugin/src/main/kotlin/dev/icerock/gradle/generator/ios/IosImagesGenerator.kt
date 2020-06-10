/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.ios

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.ImagesGenerator
import dev.icerock.gradle.generator.ios.IosMRGenerator.Companion.BUNDLE_PROPERTY_NAME
import org.gradle.api.file.FileTree
import java.io.File

class IosImagesGenerator(
    inputFileTree: FileTree
) : ImagesGenerator(
    inputFileTree = inputFileTree
) {
    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(key: String): CodeBlock? {
        return CodeBlock.of(
            "ImageResource(assetImageName = %S, bundle = ${IosMRGenerator.BUNDLE_PROPERTY_NAME})",
            key
        )
    }

    override fun generateResources(
        resourcesGenerationDir: File,
        keyFileMap: Map<String, List<File>>
    ) {
        val assetsDirectory = File(resourcesGenerationDir, "Assets.xcassets")

        keyFileMap.forEach { (key, files) ->
            val assetDir = File(assetsDirectory, "$key.imageset")
            val contentsFile = File(assetDir, "Contents.json")

            val validFiles = files.filter { file ->
                VALID_SIZES.map { "@${it}x" }.any { file.nameWithoutExtension.endsWith(it) }
            }

            validFiles.forEach { it.copyTo(File(assetDir, it.name)) }

            val imagesContent = validFiles.joinToString(separator = ",\n") { file ->
                val scale = file.nameWithoutExtension.substringAfter("@")
                """    {
      "idiom" : "universal",
      "filename" : "${file.name}",
      "scale" : "$scale"
    }"""
            }

            val content = """{
  "images" : [
$imagesContent
  ],
  "info" : {
    "version" : 1,
    "author" : "xcode"
  }
}"""

            contentsFile.writeText(content)
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

    override fun buildGetByFileNameMethod(argName: String, builder: FunSpec.Builder): FunSpec.Builder {
        val methodBody = CodeBlock.of(
"""
return ImageResource($argName, $BUNDLE_PROPERTY_NAME).let { imgRes ->
    if (imgRes.toUIImage() != null) {
        imgRes
    } else {
        null
    }
}
"""
        )

        return builder
            .addModifiers(listOf(KModifier.ACTUAL))
            .addCode(methodBody)
    }

    private companion object {
        val VALID_SIZES: IntRange = 0..3
    }
}
