/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.image

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
import dev.icerock.gradle.metadata.resource.ImageMetadata
import dev.icerock.gradle.metadata.resource.ImageMetadata.Appearance
import java.io.File

internal class JvmImageResourceGenerator(
    private val resourcesGenerationDir: File,
) : PlatformResourceGenerator<ImageMetadata> {
    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: ImageMetadata): CodeBlock {
        var fileName: String = ""
        var darkFileName: String? = null

        metadata.values.groupBy { it.appearance }.forEach { (theme, resources) ->
            val item: ImageMetadata.ImageItem = resources.getHighestQualityItem(theme)

            if (theme == Appearance.DARK) {
                darkFileName = "${metadata.key}${theme.suffix}.${item.filePath.extension}"
            } else {
                fileName = "${metadata.key}.${item.filePath.extension}"
            }
        }

        val darkFilePath: String = if (darkFileName != null) {
            "$IMAGES_DIR/$darkFileName"
        } else {
            "null"
        }

        return CodeBlock.of(
            "ImageResource(resourcesClassLoader = %L, filePath = %S, darkFilePath = $darkFilePath)",
            "${PlatformDetails.platformDetailsPropertyName}.${Jvm.resourcesClassLoaderPropertyName}",
            "$IMAGES_DIR/$fileName"
        )
    }

    override fun generateResourceFiles(data: List<ImageMetadata>) {
        generateHighestQualityImageResources(
            resourcesGenerationDir = resourcesGenerationDir,
            data = data,
            imagesDirName = IMAGES_DIR
        )
    }

    override fun generateBeforeProperties(
        builder: Builder,
        metadata: List<ImageMetadata>,
        modifier: KModifier?,
    ) {
        builder.addJvmPlatformResourceClassLoaderProperty(modifier = modifier)
    }

    override fun generateAfterProperties(
        builder: Builder,
        metadata: List<ImageMetadata>,
        modifier: KModifier?,
    ) {
        builder.addValuesFunction(
            modifier = modifier,
            metadata = metadata,
            classType = Constants.imageResourceName
        )
    }

    private companion object {
        const val IMAGES_DIR = "images"
    }
}
