/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.ios

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.ImagesGenerator
import dev.icerock.gradle.generator.ObjectBodyExtendable
import dev.icerock.gradle.generator.ios.AppleMRGenerator.Companion.ASSETS_DIR_NAME
import org.gradle.api.file.FileTree
import java.io.File

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class IosImagesGenerator(
    inputFileTree: FileTree
) : ImagesGenerator(
    inputFileTree = inputFileTree
), ObjectBodyExtendable by IosGeneratorHelper() {

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(key: String): CodeBlock? {
        return CodeBlock.of(
            "ImageResource(assetImageName = %S, bundle = ${AppleMRGenerator.BUNDLE_PROPERTY_NAME})",
            key
        )
    }

    override fun generateResources(
        resourcesGenerationDir: File,
        keyFileMap: Map<String, List<File>>
    ) {
        val assetsDirectory = File(resourcesGenerationDir, ASSETS_DIR_NAME)

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
    }

    private companion object {
        val VALID_SIZES: IntRange = 0..3
    }
}
