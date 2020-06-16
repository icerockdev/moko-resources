/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.android

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.FilesGenerator
import org.gradle.api.file.FileTree
import java.io.File
import java.util.Locale

class AndroidFilesGenerator(
    inputFileTree: FileTree,
    private val androidRClassPackage: String
) : FilesGenerator(
    inputFileTree = inputFileTree
) {
    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(fileSpec: FileSpec): CodeBlock? {
        return CodeBlock.of("FileResource(rawResId = R.raw.%L)", keyToResourceId(fileSpec.key))
    }

    override fun getImports(): List<ClassName> = listOf(
        ClassName(androidRClassPackage, "R")
    )

    override fun generateResources(
        resourcesGenerationDir: File,
        files: List<FileSpec>
    ) {
        val targetDir = File(resourcesGenerationDir, "raw")
        targetDir.mkdirs()

        files.forEach { (key, file) ->
            val fileName = keyToResourceId(key) + "." + file.extension
            file.copyTo(File(targetDir, fileName))
        }
    }

    override fun extendObjectBody(classBuilder: TypeSpec.Builder) = Unit

    private fun keyToResourceId(key: String): String {
        return key.toLowerCase(Locale.ROOT)
    }
}
