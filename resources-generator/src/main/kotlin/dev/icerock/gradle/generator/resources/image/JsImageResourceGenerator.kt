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
import dev.icerock.gradle.generator.platform.js.JsFilePathMode
import dev.icerock.gradle.metadata.resource.ImageMetadata
import dev.icerock.gradle.metadata.resource.ImageMetadata.Appearance
import java.io.File

internal class JsImageResourceGenerator(
    private val resourcesGenerationDir: File,
    private val filePathMode: JsFilePathMode
) : PlatformResourceGenerator<ImageMetadata> {

    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: ImageMetadata): CodeBlock {
        var fileName: String = ""
        var darkFileName: String? = null

        metadata.values.groupBy { it.appearance }.forEach { (theme, resources) ->
            val item: ImageMetadata.ImageItem = resources.getHighestQualityItem(theme)

            if (theme == Appearance.DARK) {
                darkFileName = "${metadata.key}${theme.themeSuffix}.${item.filePath.extension}"
            } else {
                fileName = "${metadata.key}.${item.filePath.extension}"
            }
        }

        val lightFilePath: String = filePathMode.argument("./$IMAGES_DIR/$fileName")
        val darkFilePath: String = filePathMode.argument("./$IMAGES_DIR/$darkFileName")
        val format: String = filePathMode.format

        return if (darkFileName != null) {
            CodeBlock.of(
                "ImageResource(fileUrl = $format as String, darkFileUrl = $format as String, fileName = %S)",
                lightFilePath,
                darkFilePath,
                fileName
            )
        } else {
            CodeBlock.of(
                "ImageResource(fileUrl = $format as String, fileName = %S)",
                lightFilePath,
                fileName
            )
        }
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
        modifier: KModifier?,
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
