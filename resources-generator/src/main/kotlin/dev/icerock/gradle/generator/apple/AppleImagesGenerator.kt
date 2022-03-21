/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.apple

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.ImagesGenerator
import dev.icerock.gradle.generator.ObjectBodyExtendable
import dev.icerock.gradle.generator.apple.AppleMRGenerator.Companion.ASSETS_DIR_NAME
import org.gradle.api.file.FileTree
import java.io.File

class AppleImagesGenerator(
    inputFileTree: FileTree
) : ImagesGenerator(
    inputFileTree = inputFileTree
), ObjectBodyExtendable by AppleGeneratorHelper() {

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(fileName: String): CodeBlock? {
        return CodeBlock.of(
            "ImageResource(assetImageName = %S, bundle = ${AppleMRGenerator.BUNDLE_PROPERTY_NAME})",
            fileName.substringBefore(".")
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
                VALID_SIZES.any { size -> file.scale == "${size}x" }
            }

            val uniqueNames = files.map { it.nameWithoutScale }.distinct()
            uniqueNames.forEach { name ->
                require(validFiles.any { it.nameWithoutScale == name }) {
                    "Apple Generator cannot find a valid scale for file with name \"$name\".\n" +
                            "Note: Apple resources can have only 1x, 2x and 3x scale factors " +
                            "(https://developer.apple.com/design/human-interface-guidelines/ios/" +
                            "icons-and-images/image-size-and-resolution/).\n" +
                            "It is still possible to use 4x images for android, but you need to " +
                            "add a valid iOS variant."
                }
            }

            validFiles.forEach { it.copyTo(File(assetDir, it.name)) }

            val imagesContent = validFiles.joinToString(separator = ",\n") { file ->
                val scale = file.scale

                // language=js
                return@joinToString """
                    {
                        "idiom" : "universal",
                        "filename" : "${file.name}",
                        "scale" : "$scale"
                    }
                """.trimIndent()
            }

            // language=js
            val content = """
                {
                    "images" : [
                        $imagesContent
                    ],
                    "info" : {
                        "version" : 1,
                        "author" : "xcode"
                    }
                }
            """.trimIndent()

            contentsFile.writeText(content)
        }
    }

    private companion object {
        val VALID_SIZES: IntRange = 1..3

        private val File.scale: String get() =
            nameWithoutExtension.substringAfter("@")
        private val File.nameWithoutScale: String get() =
            nameWithoutExtension.substringBefore("@")
    }
}
