/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.font

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.CodeConst
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.metadata.resource.FontMetadata
import java.io.File

internal class JvmFontResourceGenerator(
    private val className: String,
    private val resourcesGenerationDir: File
) : PlatformResourceGenerator<FontMetadata> {
    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: FontMetadata): CodeBlock {
        return CodeBlock.of(
            "FontResource(resourcesClassLoader = %L, filePath = %S)",
            CodeConst.Jvm.resourcesClassLoaderPropertyName,
            "$FONTS_DIR/${metadata.filePath.name}"
        )
    }

    override fun generateResourceFiles(data: List<FontMetadata>) {
        val fontsDir = File(resourcesGenerationDir, FONTS_DIR)
        fontsDir.mkdirs()

        data.map { it.filePath }.forEach { file ->
            file.copyTo(File(fontsDir, file.name))
        }
    }

    override fun generateBeforeProperties(
        builder: TypeSpec.Builder,
        metadata: List<FontMetadata>
    ) {
        // FIXME duplication
        val classLoaderProperty: PropertySpec = PropertySpec.builder(
            CodeConst.Jvm.resourcesClassLoaderPropertyName,
            CodeConst.Jvm.classLoaderName,
            KModifier.OVERRIDE
        )
            .initializer(CodeBlock.of(className + "." + CodeConst.Jvm.resourcesClassLoaderPropertyName))
            .build()

        builder.addProperty(classLoaderProperty)
    }

    private companion object {
        const val FONTS_DIR = "fonts"
    }
}
