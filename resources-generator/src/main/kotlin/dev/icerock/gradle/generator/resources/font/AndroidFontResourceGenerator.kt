/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.font

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.metadata.resource.FontMetadata
import java.io.File

internal class AndroidFontResourceGenerator(
    private val androidRClassPackage: String,
    private val resourcesGenerationDir: File
) : PlatformResourceGenerator<FontMetadata> {
    override fun imports(): List<ClassName> = listOf(
        ClassName(androidRClassPackage, "R")
    )

    override fun generateInitializer(metadata: FontMetadata): CodeBlock {
        return CodeBlock.of("FontResource(R.font.%L)", metadata.key)
    }

    override fun generateResourceFiles(data: List<FontMetadata>) {
        val fontResDir = File(resourcesGenerationDir, "font")
        fontResDir.mkdirs()

        data.forEach { metadata ->
            val fileName: String = metadata.key + "." + metadata.filePath.extension
            metadata.filePath.copyTo(File(fontResDir, fileName))
        }
    }
}
