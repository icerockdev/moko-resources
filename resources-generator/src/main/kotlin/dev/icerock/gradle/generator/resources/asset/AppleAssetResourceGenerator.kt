/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.asset

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addAppleContainerBundleInitializerProperty
import dev.icerock.gradle.metadata.resource.AssetMetadata
import java.io.File

internal class AppleAssetResourceGenerator(
    private val resourcesGenerationDir: File,
) : PlatformResourceGenerator<AssetMetadata> {
    override fun imports(): List<ClassName> = listOf(
        Constants.Apple.nsBundleName,
        Constants.Apple.loadableBundleName
    )

    override fun generateInitializer(metadata: AssetMetadata): CodeBlock {
        return CodeBlock.of(
            "AssetResource(originalPath = %S, fileName = %S, extension = %S, bundle = %L)",
            metadata.pathRelativeToBase.invariantSeparatorsPath,
            processedFilePath(metadata).substringBeforeLast('.'),
            metadata.filePath.extension,
            Constants.Apple.platformContainerBundlePropertyName
        )
    }

    override fun generateAccessorInitializer(metadata: AssetMetadata): CodeBlock {
        return CodeBlock.of(
            "AssetResource(originalPath = %S, fileName = %S, extension = %S, bundle = %L)",
            metadata.pathRelativeToBase.invariantSeparatorsPath,
            processedFilePath(metadata).substringBeforeLast('.'),
            metadata.filePath.extension,
            Constants.Apple.providerBundleReference
        )
    }

    override fun generateResourceFiles(data: List<AssetMetadata>) {
        data.forEach { metadata ->
            val newName: String = processedFilePath(metadata)
            metadata.filePath.copyTo(File(resourcesGenerationDir, newName))
        }
    }

    private fun processedFilePath(metadata: AssetMetadata): String {
        return metadata.pathRelativeToBase.invariantSeparatorsPath
            .replace('/', PATH_DELIMITER)
    }

    override fun generateContainerProperties(
        builder: Builder,
        metadata: List<AssetMetadata>,
        modifier: KModifier?,
    ) {
        builder.addAppleContainerBundleInitializerProperty(modifier)
    }

    override fun generateAccessorFilePreamble(
        builder: FileSpec.Builder,
        metadata: List<AssetMetadata>,
        objectName: String,
    ) = Unit

    private companion object {
        const val PATH_DELIMITER = '+'
    }
}
