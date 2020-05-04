/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.ios

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.FilesGenerator
import org.gradle.api.file.FileTree
import java.io.File

class IosFilesGenerator(
    inputFileTree: FileTree
) : FilesGenerator(
    inputFileTree = inputFileTree
) {
    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(fileName: String): CodeBlock? {
        return CodeBlock.of(
            "FileResource(fileName = %S, bundle = ${IosMRGenerator.BUNDLE_PROPERTY_NAME})",
            fileName
        )
    }

    override fun generateResources(
        resourcesGenerationDir: File,
        files: List<FileSpec>
    ) {
        val targetDir = File(resourcesGenerationDir, "files")
        targetDir.mkdirs()

        files.forEach { (_, file) ->
            file.copyTo(File(targetDir, file.name))
        }
    }
}
