/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.image

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.IosMRGenerator
import org.gradle.api.file.FileTree
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

class IosImagesGenerator(
    sourceSet: KotlinSourceSet,
    inputFileTree: FileTree
) : ImagesGenerator(
    sourceSet = sourceSet,
    inputFileTree = inputFileTree
) {
    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)


    override fun getPropertyInitializer(key: String): CodeBlock? {
        return CodeBlock.of(
            "ImageResource(resourceId = %S, bundle = ${IosMRGenerator.BUNDLE_PROPERTY_NAME})",
            key
        )
    }

    override fun generateResources(
        resourcesGenerationDir: File,
        keyFileMap: Map<String, List<File>>
    ) {
//        val resDirName = when (language) {
//            null -> "Base.lproj"
//            else -> "$language.lproj"
//        }
//
//        val resDir = File(resourcesGenerationDir, resDirName)
//        val localizableFile = File(resDir, "Localizable.strings")
//        resDir.mkdirs()
//
//        val content = strings.map { (key, value) ->
//            "\"$key\" = \"$value\";"
//        }.joinToString("\n")
//
//        localizableFile.writeText(content)
    }
}