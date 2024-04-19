/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.image

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addEmptyPlatformResourceProperty
import dev.icerock.gradle.metadata.resource.ImageMetadata
import java.io.File

internal class JsImageResourceGenerator(
    private val resourcesGenerationDir: File,
) : PlatformResourceGenerator<ImageMetadata> {

    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: ImageMetadata): CodeBlock {
        val item: ImageMetadata.ImageItem = metadata.getHighestQualityItem()
        val fileName = "${metadata.key}.${item.filePath.extension}"
        val requireDeclaration = """require("$IMAGES_DIR/$fileName")"""
        return CodeBlock.of(
            "ImageResource(fileUrl = js(%S) as String, fileName = %S)",
            requireDeclaration,
            fileName
        )
    }

    override fun generateBeforeProperties(
        builder: Builder,
        metadata: List<ImageMetadata>,
        modifier: KModifier?,
    ) {
        builder.addEmptyPlatformResourceProperty(modifier)
    }

    override fun generateResourceFiles(data: List<ImageMetadata>) {
        generateHighestQualityImageResources(
            resourcesGenerationDir = resourcesGenerationDir,
            data = data,
            imagesDirName = IMAGES_DIR
        )
    }

    override fun generateAfterProperties(
        builder: Builder,
        metadata: List<ImageMetadata>,
        modifier: KModifier?
    ) {
        val languageKeysList: String = metadata.joinToString { it.key }

        val valuesFun: FunSpec = FunSpec.builder("values")
            .also {
                if (modifier != null) {
                    it.addModifiers(modifier)
                }
            }
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return listOf($languageKeysList)")
            .returns(
                ClassName("kotlin.collections", "List")
                    .parameterizedBy(Constants.imageResourceName)
            )
            .build()

        builder.addFunction(valuesFun)
    }

    private companion object {
        const val IMAGES_DIR = "images"
    }
}
