/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.font

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addAppleContainerBundleInitializerProperty
import dev.icerock.gradle.generator.addValuesFunction
import dev.icerock.gradle.metadata.resource.FontMetadata
import java.io.File

internal class AppleFontResourceGenerator(
    private val resourcesGenerationDir: File,
) : PlatformResourceGenerator<FontMetadata> {
    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: FontMetadata): CodeBlock {
        return CodeBlock.of(
            "FontResource(fontName = %S, bundle = %L)",
            metadata.filePath.name,
            Constants.Apple.platformContainerBundlePropertyName
        )
    }

    override fun generateResourceFiles(data: List<FontMetadata>) {
        data.map { it.filePath }.forEach { file ->
            file.copyTo(File(resourcesGenerationDir, file.name))
        }
    }

    override fun generateBeforeProperties(
        parentObjectName: String,
        builder: Builder,
        metadata: List<FontMetadata>,
        modifier: KModifier?,
    ) {
        builder.addAppleContainerBundleInitializerProperty(modifier)
    }

    override fun generateAfterProperties(
        builder: Builder,
        metadata: List<FontMetadata>,
        modifier: KModifier?,
    ) {
        builder.addValuesFunction(
            modifier = modifier,
            metadata = metadata,
            classType = Constants.fontResourceName
        )
    }
}
