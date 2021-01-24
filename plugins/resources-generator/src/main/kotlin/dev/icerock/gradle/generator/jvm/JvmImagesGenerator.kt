/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.jvm

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.ImagesGenerator
import org.gradle.api.file.FileTree
import java.io.File

class JvmImagesGenerator(inputFileTree: FileTree) : ImagesGenerator(inputFileTree) {

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    // TODO replace .png with getting extension
    override fun getPropertyInitializer(key: String) =
        CodeBlock.of("ImageResource(imagePath = %S)", "$IMAGES_DIR/$key.png")

    override fun generateResources(
        resourcesGenerationDir: File,
        keyFileMap: Map<String, List<File>>
    ) {
        val imagesDir = File(resourcesGenerationDir, IMAGES_DIR).apply { mkdirs() }

        keyFileMap.forEach { (key, files) ->
            // We copy the only highest quality image to jvm
            val hqFile = files.maxByOrNull {
                it.nameWithoutExtension.substringAfter("@").substringBefore("x").toDouble()
            } ?: return
            hqFile.copyTo(File(imagesDir, "$key.${hqFile.extension}"))
        }
    }

    companion object {
        private const val IMAGES_DIR = "images"
    }
}
