/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.font

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addEmptyPlatformResourceProperty
import dev.icerock.gradle.generator.addValuesFunction
import dev.icerock.gradle.metadata.resource.FontMetadata
import java.io.File

internal class AndroidFontResourceGenerator(
    private val androidRClassPackage: String,
    private val resourcesGenerationDir: File,
) : PlatformResourceGenerator<FontMetadata> {
    override fun imports(): List<ClassName> = listOf(
        ClassName(androidRClassPackage, "R")
    )

    override fun generateInitializer(metadata: FontMetadata): CodeBlock {
        return CodeBlock.of("FontResource(R.font.%L)", metadata.key)
    }

    override fun generateBeforeProperties(
        builder: Builder,
        metadata: List<FontMetadata>,
        modifiers: List<KModifier>,
    ) {
        builder.addEmptyPlatformResourceProperty(modifiers)
    }

    override fun generateResourceFiles(data: List<FontMetadata>) {
        val fontResDir = File(resourcesGenerationDir, "font")
        fontResDir.mkdirs()

        data.forEach { metadata ->
            val fileName: String = metadata.key + "." + metadata.filePath.extension
            metadata.filePath.copyTo(File(fontResDir, fileName))
        }
    }

    override fun generateAfterProperties(
        builder: Builder,
        metadata: List<FontMetadata>,
        modifiers: List<KModifier>,
    ) {
        builder.addValuesFunction(
            modifiers = modifiers,
            metadata = metadata,
            classType = Constants.fontResourceName
        )
    }
}
