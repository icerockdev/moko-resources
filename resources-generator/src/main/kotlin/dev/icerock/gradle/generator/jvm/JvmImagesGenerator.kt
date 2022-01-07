/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.jvm

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.ImagesGenerator
import dev.icerock.gradle.generator.ObjectBodyExtendable
import dev.icerock.gradle.generator.js_jvm_common.generateHighestQualityImageResources
import org.gradle.api.file.FileTree
import java.io.File

class JvmImagesGenerator(
    inputFileTree: FileTree
) : ImagesGenerator(inputFileTree), ObjectBodyExtendable by ClassLoaderExtender() {

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(fileName: String) =
        CodeBlock.of(
            "ImageResource(resourcesClassLoader = resourcesClassLoader, filePath = %S)",
            "$IMAGES_DIR/$fileName"
        )

    override fun generateResources(
        resourcesGenerationDir: File,
        keyFileMap: Map<String, List<File>>
    ) {
        generateHighestQualityImageResources(resourcesGenerationDir, keyFileMap, IMAGES_DIR)
    }

    companion object {
        private const val IMAGES_DIR = "images"
    }
}
