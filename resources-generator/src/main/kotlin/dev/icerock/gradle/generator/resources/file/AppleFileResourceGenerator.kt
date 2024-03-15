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
import dev.icerock.gradle.generator.addAppleContainerBundleInitializerProperty
import dev.icerock.gradle.generator.addValuesFunction
import dev.icerock.gradle.metadata.resource.FileMetadata
import java.io.File

internal class AppleFileResourceGenerator(
    private val resourcesGenerationDir: File,
) : PlatformResourceGenerator<FileMetadata> {
    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: FileMetadata): CodeBlock {
        return CodeBlock.of(
            "FileResource(fileName = %S, extension = %S, bundle = %L)",
            metadata.filePath.nameWithoutExtension,
            metadata.filePath.extension,
            Constants.Apple.platformContainerBundlePropertyName
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
        parentObjectName: String,
        builder: Builder,
        metadata: List<FileMetadata>,
        modifier: KModifier?,
    ) {
        builder.addAppleContainerBundleInitializerProperty(modifier)
    }

    override fun generateAfterProperties(
        builder: Builder,
        metadata: List<FileMetadata>,
        modifier: KModifier?,
    ) {
        builder.addValuesFunction(
            modifier = modifier,
            metadata = metadata,
            classType = Constants.fileResourceName
        )
    }
}
