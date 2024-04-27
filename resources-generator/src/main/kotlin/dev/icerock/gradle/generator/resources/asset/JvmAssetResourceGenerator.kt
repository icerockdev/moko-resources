/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.asset

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
import dev.icerock.gradle.metadata.resource.AssetMetadata
import java.io.File

internal class JvmAssetResourceGenerator(
    private val resourcesGenerationDir: File,
) : PlatformResourceGenerator<AssetMetadata> {
    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: AssetMetadata): CodeBlock {
        return CodeBlock.of(
            "AssetResource(resourcesClassLoader = %L, originalPath = %S, path = %S)",
            "${PlatformDetails.platformDetailsPropertyName}.${Jvm.resourcesClassLoaderPropertyName}",
            metadata.pathRelativeToBase.path,
            buildAssetPath(metadata)
        )
    }

    override fun generateResourceFiles(data: List<AssetMetadata>) {
        data.forEach { metadata ->
            metadata.filePath.copyTo(File(resourcesGenerationDir, buildAssetPath(metadata)))
        }
    }

    override fun generateBeforeProperties(
        builder: Builder,
        metadata: List<AssetMetadata>,
        modifier: KModifier?,
    ) {
        builder.addJvmPlatformResourceClassLoaderProperty(modifier = modifier)
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

    private fun buildAssetPath(metadata: AssetMetadata): String {
        return File(ASSETS_DIR, metadata.pathRelativeToBase.path).path
            .replace('\\', '/')
    }

    private companion object {
        const val ASSETS_DIR = "assets"
    }
}
