/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.asset

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addAppleContainerBundleInitializerProperty
import dev.icerock.gradle.generator.addValuesFunction
import dev.icerock.gradle.metadata.resource.AssetMetadata
import java.io.File

internal class AppleAssetResourceGenerator(
    private val resourcesGenerationDir: File,
) : PlatformResourceGenerator<AssetMetadata> {
    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: AssetMetadata): CodeBlock {
        return CodeBlock.of(
            "AssetResource(originalPath = %S, fileName = %S, extension = %S, bundle = %L)",
            metadata.pathRelativeToBase.path,
            metadata.pathRelativeToBase.path
                .replace('/', PATH_DELIMITER)
                .substringBeforeLast('.'),
            metadata.filePath.extension,
            Constants.Apple.platformContainerBundlePropertyName
        )
    }

    override fun generateResourceFiles(data: List<AssetMetadata>) {
        data.forEach { metadata ->
            val newName: String = metadata.pathRelativeToBase.path.replace('/', PATH_DELIMITER)
            metadata.filePath.copyTo(File(resourcesGenerationDir, newName))
        }
    }

    override fun generateBeforeProperties(
        parentObjectName: String,
        builder: Builder,
        metadata: List<AssetMetadata>,
        modifier: KModifier?,
    ) {
        builder.addAppleContainerBundleInitializerProperty(modifier)
    }

    override fun generateAfterProperties(
        builder: Builder,
        metadata: List<AssetMetadata>,
        modifier: KModifier?,
    ) {
        builder.addValuesFunction(
            modifier = modifier,
            metadata = metadata,
            classType = Constants.assetResourceName
        )
    }

    private companion object {
        const val PATH_DELIMITER = '+'
    }
}
