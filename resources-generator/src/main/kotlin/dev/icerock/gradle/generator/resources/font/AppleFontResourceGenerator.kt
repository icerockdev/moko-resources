/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.font

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addAppleContainerBundleProperty
import dev.icerock.gradle.metadata.resource.FontMetadata
import java.io.File

internal class AppleFontResourceGenerator(
    private val resourcesGenerationDir: File
) : PlatformResourceGenerator<FontMetadata> {
    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: FontMetadata): CodeBlock {
        return CodeBlock.of(
            "FontResource(fontName = %S, bundle = %L)",
            metadata.filePath.name,
            Constants.Apple.containerBundlePropertyName
        )
    }

    override fun generateResourceFiles(data: List<FontMetadata>) {
        data.map { it.filePath }.forEach { file ->
            file.copyTo(File(resourcesGenerationDir, file.name))
        }
    }

    override fun generateBeforeProperties(
        builder: TypeSpec.Builder,
        metadata: List<FontMetadata>
    ) {
        builder.addAppleContainerBundleProperty()
    }
}
