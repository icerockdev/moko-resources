/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.image

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.CodeConst
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.metadata.resource.ImageMetadata
import java.io.File

internal class JvmImageResourceGenerator(
    private val className: String,
    private val resourcesGenerationDir: File
) : PlatformResourceGenerator<ImageMetadata> {
    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: ImageMetadata): CodeBlock {
        val item: ImageMetadata.ImageQualityItem = metadata.getHighestQualityItem()
        val fileName = "${metadata.key}.${item.filePath.extension}"
        return CodeBlock.of(
            "ImageResource(resourcesClassLoader = %L, filePath = %S)",
            CodeConst.Jvm.resourcesClassLoaderPropertyName,
            "$IMAGES_DIR/${fileName}"
        )
    }

    override fun generateResourceFiles(data: List<ImageMetadata>) {
        generateHighestQualityImageResources(resourcesGenerationDir, data, IMAGES_DIR)
    }

    override fun generateBeforeProperties(
        builder: TypeSpec.Builder,
        metadata: List<ImageMetadata>
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
        const val IMAGES_DIR = "images"
    }
}
