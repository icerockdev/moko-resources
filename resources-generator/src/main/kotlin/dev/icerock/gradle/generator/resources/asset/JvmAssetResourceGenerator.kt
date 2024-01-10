/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.asset

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.CodeConst
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.metadata.resource.AssetMetadata
import java.io.File

internal class JvmAssetResourceGenerator(
    private val className: String,
    private val resourcesGenerationDir: File
) : PlatformResourceGenerator<AssetMetadata> {
    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: AssetMetadata): CodeBlock {
        return CodeBlock.of(
            "AssetResource(resourcesClassLoader = %L, originalPath = %S, path = %S)",
            CodeConst.Jvm.resourcesClassLoaderPropertyName,
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
        builder: TypeSpec.Builder,
        metadata: List<AssetMetadata>
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

    private fun buildAssetPath(metadata: AssetMetadata): String {
        return File(ASSETS_DIR, metadata.pathRelativeToBase.path).path
            .replace('\\', '/')
    }

    private companion object {
        const val ASSETS_DIR = "assets"
    }
}
