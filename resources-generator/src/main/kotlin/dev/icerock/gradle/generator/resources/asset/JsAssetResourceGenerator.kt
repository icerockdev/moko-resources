/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.asset

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addEmptyPlatformResourceProperty
import dev.icerock.gradle.metadata.resource.AssetMetadata
import java.io.File

internal class JsAssetResourceGenerator(
    private val resourcesGenerationDir: File,
) : PlatformResourceGenerator<AssetMetadata> {

    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: AssetMetadata): CodeBlock {
        val filePath: String = File(ASSETS_DIR, metadata.pathRelativeToBase.path).path
            .replace("\\", "/")

        val requireDeclaration = """require("$filePath")"""
        return CodeBlock.of(
            "AssetResource(originalPath = js(%S) as String, rawPath = %S)",
            requireDeclaration,
            metadata.pathRelativeToBase
        )
    }

    override fun generateBeforeProperties(
        parentObjectName: String,
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
        // FIXME duplicate
        val values: String = metadata.joinToString { it.key }

        val valuesFun: FunSpec = FunSpec.builder("values")
            .also {
                if (modifier != null) {
                    it.addModifiers(modifier)
                }
            }
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return listOf($values)")
            .returns(
                ClassName("kotlin.collections", "List")
                    .parameterizedBy(Constants.assetResourceName)
            )
            .build()

        builder.addFunction(valuesFun)
    }

    private companion object {
        const val ASSETS_DIR = "assets"
    }
}
