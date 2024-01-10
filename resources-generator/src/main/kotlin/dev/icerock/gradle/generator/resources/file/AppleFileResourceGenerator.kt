/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.file

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.CodeConst
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addAppleContainerBundleProperty
import dev.icerock.gradle.metadata.resource.FileMetadata
import java.io.File

internal class AppleFileResourceGenerator(
    private val resourcesGenerationDir: File
) : PlatformResourceGenerator<FileMetadata> {
    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: FileMetadata): CodeBlock {
        return CodeBlock.of(
            "FileResource(fileName = %S, extension = %S, bundle = %L)",
            metadata.filePath.nameWithoutExtension,
            metadata.filePath.extension,
            CodeConst.Apple.containerBundlePropertyName
        )
    }

    override fun generateResourceFiles(data: List<FileMetadata>) {
        val targetDir = File(resourcesGenerationDir, "files")
        targetDir.mkdirs()

        data.forEach { metadata ->
            metadata.filePath.copyTo(File(targetDir, metadata.filePath.name))
        }
    }

    override fun generateBeforeProperties(
        builder: TypeSpec.Builder,
        metadata: List<FileMetadata>
    ) {
        builder.addAppleContainerBundleProperty()
    }
}
