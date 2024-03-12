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

internal class AndroidAssetResourceGenerator(
    private val androidRClassPackage: String,
    private val assetsGenerationDir: File
) : PlatformResourceGenerator<AssetMetadata> {
    override fun imports(): List<ClassName> = listOf(
        ClassName(androidRClassPackage, "R")
    )

    override fun generateInitializer(metadata: AssetMetadata): CodeBlock {
        return CodeBlock.of("AssetResource(path = %S)", metadata.pathRelativeToBase.path)
    }

    override fun generateBeforeProperties(
        builder: Builder,
        metadata: List<AssetMetadata>,
        modifiers: List<KModifier>
    ) {
        builder.addEmptyPlatformResourceProperty(modifiers)
    }

    override fun generateAfterProperties(
        builder: Builder,
        metadata: List<AssetMetadata>,
        modifiers: List<KModifier>
    ) {
        builder.addValuesFunction(
            modifiers = modifiers,
            metadata = metadata,
            classType = Constants.assetResourceName
        )
    }

    override fun generateResourceFiles(data: List<AssetMetadata>) {
        data.forEach { metadata ->
            metadata.filePath.copyTo(File(assetsGenerationDir, metadata.pathRelativeToBase.path))
        }
    }
}
