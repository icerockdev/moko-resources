/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.file

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.metadata.resource.FileMetadata
import java.io.File
import java.util.Locale

internal class AndroidFileResourceGenerator(
    private val androidRClassPackage: String,
    private val resourcesGenerationDir: File
) : PlatformResourceGenerator<FileMetadata> {
    override fun imports(): List<ClassName> = listOf(
        ClassName(androidRClassPackage, "R")
    )

    override fun generateInitializer(metadata: FileMetadata): CodeBlock {
        return CodeBlock.of("FileResource(R.raw.%L)", keyToResourceId(metadata.key))
    }

    override fun generateResourceFiles(data: List<FileMetadata>) {
        val targetDir = File(resourcesGenerationDir, "raw")
        targetDir.mkdirs()

        data.forEach { metadata ->
            val fileName: String = keyToResourceId(metadata.key) + "." + metadata.filePath.extension
            metadata.filePath.copyTo(File(targetDir, fileName))
        }
    }

    private fun keyToResourceId(key: String) = key.lowercase(Locale.ROOT)
}
