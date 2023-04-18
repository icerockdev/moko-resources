/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.android

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.ImagesGenerator
import dev.icerock.gradle.generator.NOPObjectBodyExtendable
import dev.icerock.gradle.generator.ObjectBodyExtendable
import dev.icerock.gradle.utils.svg
import org.gradle.api.file.FileTree
import java.io.File
import com.android.ide.common.vectordrawable.Svg2Vector
import java.io.FileOutputStream
import java.io.IOException

class AndroidImagesGenerator(
    inputFileTree: FileTree,
    private val getAndroidRClassPackage: () -> String
) : ImagesGenerator(inputFileTree), ObjectBodyExtendable by NOPObjectBodyExtendable() {
    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(fileName: String): CodeBlock? {
        val processedKey = processKey(fileName.substringBefore("."))
        return CodeBlock.of("ImageResource(R.drawable.%L)", processedKey)
    }

    override fun getImports(): List<ClassName> = listOf(
        ClassName(getAndroidRClassPackage(), "R")
    )

    override fun generateResources(
        resourcesGenerationDir: File,
        keyFileMap: Map<String, List<File>>
    ) {
        keyFileMap.flatMap { (key, files) ->
            files.map { key to it }
        }.forEach { (key, file) ->
            val scale = file.nameWithoutExtension.substringAfter("@").substringBefore("x")
            val drawableDirName = "drawable" + when (scale) {
                "0.75" -> "-ldpi"
                "1" -> "-mdpi"
                "1.5" -> "-hdpi"
                "2" -> "-xhdpi"
                "3" -> "-xxhdpi"
                "4" -> "-xxxhdpi"
                else -> {
                    if (file.svg) {
                        ""
                    } else {
                        println("ignore $file - unknown scale ($scale)")
                        return@forEach
                    }
                }
            }

            val drawableDir = File(resourcesGenerationDir, drawableDirName)
            val processedKey = processKey(key)

            val resourceExtension = if (file.svg) "xml" else file.extension
            val resourceFile = File(drawableDir, "$processedKey.$resourceExtension")
            if (file.svg) {
                parseSvgToVectorDrawable(file, resourceFile)
            } else {
                file.copyTo(resourceFile)
            }
        }
    }

    private fun parseSvgToVectorDrawable(svgFile: File, vectorDrawableFile: File) {
        try {
            vectorDrawableFile.parentFile.mkdirs()
            vectorDrawableFile.createNewFile()
            val os = FileOutputStream(vectorDrawableFile, false)
            try {
                Svg2Vector.parseSvgToXml(svgFile, os)
                    .takeIf { it.isNotEmpty() }
                    ?.let { error -> println("parse from $svgFile to xml error: $error") }
            } finally {
                os.flush()
            }
        } catch (e: IOException) {
            println("parse from $svgFile to xml error: $e")
        }
    }

    private fun processKey(key: String): String {
        return key.lowercase()
    }
}
