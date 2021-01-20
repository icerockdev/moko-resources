/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.android

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.ImagesGenerator
import org.gradle.api.file.FileTree
import java.io.File

class AndroidImagesGenerator(
    inputFileTree: FileTree,
    private val androidRClassPackage: String
) : ImagesGenerator(inputFileTree) {
    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(key: String): CodeBlock? {
        val processedKey = processKey(key)
        return CodeBlock.of("ImageResource(R.drawable.%L)", processedKey)
    }

    override fun getImports(): List<ClassName> = listOf(
        ClassName(androidRClassPackage, "R")
    )

    override fun generateResources(
        resourcesGenerationDir: File,
        keyFileMap: Map<String, List<File>>
    ) {
        keyFileMap.flatMap { (key, files) ->
            files.map { key to it }
        }.forEach { (key, file) ->
            val scale = file.nameWithoutExtension.substringAfter("@").substringBefore("x")
            val drawableDirName = "drawable-" + when (scale) {
                "0.75" -> "ldpi"
                "1" -> "mdpi"
                "1.5" -> "hdpi"
                "2" -> "xhdpi"
                "3" -> "xxhdpi"
                "4" -> "xxxhdpi"
                else -> {
                    println("ignore $file - unknown scale ($scale)")
                    return@forEach
                }
            }

            val drawableDir = File(resourcesGenerationDir, drawableDirName)
            val processedKey = processKey(key)
            file.copyTo(File(drawableDir, "$processedKey.${file.extension}"))
        }
    }

    private fun processKey(key: String): String {
        return key.toLowerCase()
    }
}
