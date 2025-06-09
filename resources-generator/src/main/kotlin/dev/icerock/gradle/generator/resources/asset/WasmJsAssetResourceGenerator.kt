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
import dev.icerock.gradle.generator.addEmptyPlatformResourceProperty
import dev.icerock.gradle.generator.addValuesFunction
import dev.icerock.gradle.metadata.resource.AssetMetadata
import java.io.File

internal class WasmJsAssetResourceGenerator(
    private val resourcesGenerationDir: File,
) : PlatformResourceGenerator<AssetMetadata> {

    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: AssetMetadata): CodeBlock {
        val filePath: String = File(ASSETS_DIR, metadata.pathRelativeToBase.path)
            .invariantSeparatorsPath

        val requireDeclaration = "./$filePath"
        return CodeBlock.of(
            "AssetResource(originalPath = %S as String, rawPath = %S)",
            requireDeclaration,
            metadata.pathRelativeToBase.invariantSeparatorsPath
        )
    }

    override fun generateBeforeProperties(
        builder: Builder,
        metadata: List<AssetMetadata>,
        modifier: KModifier?,
    ) {
        builder.addEmptyPlatformResourceProperty(modifier)
    }

    override fun generateResourceFiles(data: List<AssetMetadata>) {
        val targetDir = File(resourcesGenerationDir, ASSETS_DIR)
        targetDir.mkdirs()

        data.forEach { metadata ->
            metadata.filePath.copyTo(File(targetDir, metadata.pathRelativeToBase.path))
        }
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
        const val ASSETS_DIR = "assets"
    }
}
