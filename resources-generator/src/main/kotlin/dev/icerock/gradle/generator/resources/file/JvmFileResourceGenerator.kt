/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.file

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.Constants.Jvm
import dev.icerock.gradle.generator.Constants.PlatformDetails
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addJvmPlatformResourceClassLoaderProperty
import dev.icerock.gradle.generator.addValuesFunction
import dev.icerock.gradle.metadata.resource.FileMetadata
import java.io.File

internal class JvmFileResourceGenerator(
    private val resourcesGenerationDir: File,
) : PlatformResourceGenerator<FileMetadata> {
    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: FileMetadata): CodeBlock {
        return CodeBlock.of(
            "FileResource(resourcesClassLoader = %L, filePath = %S)",
            "${PlatformDetails.platformDetailsPropertyName}.${Jvm.resourcesClassLoaderPropertyName}",
            "$FILES_DIR/${metadata.filePath.name}"
        )
    }

    override fun generateResourceFiles(data: List<FileMetadata>) {
        val fontsDir = File(resourcesGenerationDir, FILES_DIR)
        fontsDir.mkdirs()

        data.map { it.filePath }.forEach { file ->
            file.copyTo(File(fontsDir, file.name))
        }
    }

    override fun generateBeforeProperties(
        parentObjectName: String,
        builder: Builder,
        metadata: List<FileMetadata>,
        modifier: KModifier?,
    ) {
        builder.addJvmPlatformResourceClassLoaderProperty(
            parentObjectName = parentObjectName,
            modifier = modifier,
        )
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

    private companion object {
        const val FILES_DIR = "files"
    }
}
