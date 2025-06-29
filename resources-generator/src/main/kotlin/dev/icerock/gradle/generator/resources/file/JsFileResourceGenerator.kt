/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.file

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addEmptyPlatformResourceProperty
import dev.icerock.gradle.generator.addValuesFunction
import dev.icerock.gradle.generator.platform.js.JsFilePathMode
import dev.icerock.gradle.metadata.resource.FileMetadata
import java.io.File

internal class JsFileResourceGenerator(
    private val resourcesGenerationDir: File,
    private val filePathMode: JsFilePathMode
) : PlatformResourceGenerator<FileMetadata> {

    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: FileMetadata): CodeBlock {
        val fileUrl: String = filePathMode.argument("./$FILES_DIR/${metadata.filePath.name}")
        return CodeBlock.of(
            "FileResource(fileUrl = ${filePathMode.format} as String)",
            fileUrl
        )
    }

    override fun generateResourceFiles(data: List<FileMetadata>) {
        val targetDir = File(resourcesGenerationDir, FILES_DIR)
        targetDir.mkdirs()

        data.map { it.filePath }.forEach { file ->
            file.copyTo(File(targetDir, file.name))
        }
    }

    override fun generateBeforeProperties(
        builder: Builder,
        metadata: List<FileMetadata>,
        modifier: KModifier?,
    ) {
        builder.addEmptyPlatformResourceProperty(modifier)
    }

    override fun generateAfterProperties(
        builder: Builder,
        metadata: List<FileMetadata>,
        modifier: KModifier?
    ) {
        builder.addValuesFunction(
            modifier = modifier,
            metadata = metadata,
            classType = Constants.fileResourceName
        )
    }

    private companion object {
        const val FILES_DIR = "files"
    }
}
