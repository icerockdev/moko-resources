/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.js

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.AssetsGenerator
import dev.icerock.gradle.generator.NOPObjectBodyExtendable
import dev.icerock.gradle.generator.ObjectBodyExtendable
import org.gradle.api.file.SourceDirectorySet
import java.io.File

class JsAssetsGenerator(
    sourceDirectorySet: SourceDirectorySet
) : AssetsGenerator(sourceDirectorySet), ObjectBodyExtendable by NOPObjectBodyExtendable() {

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(fileSpec: AssetSpecFile): CodeBlock {
        val filePath = File(FILES_DIR, fileSpec.pathRelativeToBase).path
        return CodeBlock.of("""
            AssetResource(
                originalPath = js("require(\"$filePath\")") as String, 
                rawPath = "${fileSpec.pathRelativeToBase}"
            )
        """.trimIndent())
    }

    override fun beforeGenerate(objectBuilder: TypeSpec.Builder, files: List<AssetSpec>) {
        val languageKeysList = flattenAssets(files).joinToString()

        objectBuilder.addFunction(
            FunSpec.builder("values")
                .addModifiers(KModifier.OVERRIDE)
                .addStatement("return listOf($languageKeysList)")
                .returns(
                    ClassName("kotlin.collections", "List")
                        .parameterizedBy(resourceClassName)
                )
                .build()
        )
    }

    private fun flattenAssets(assets: List<AssetSpec>, prefix: String? = null): List<String> {
        return assets.flatMap { spec ->
            when (spec) {
                is AssetSpecDirectory -> {
                    val key = spec.name.replace('-', '_')
                    val nextPrefix = when (prefix) {
                        null -> key
                        else -> "$prefix.$key"
                    }
                    return@flatMap flattenAssets(spec.assets, nextPrefix)
                }
                is AssetSpecFile -> {
                    val key = spec.file.nameWithoutExtension.replace('-', '_')
                    val name = when (prefix) {
                        null -> key
                        else -> "$prefix.$key"
                    }
                    return@flatMap listOf(name)
                }
            }
        }
    }

    override fun generateResources(
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        files: List<AssetSpec>
    ) {
        val fileResDir = File(resourcesGenerationDir, FILES_DIR).apply { mkdirs() }
        generateResourcesInner(files, fileResDir)
    }

    private fun generateResourcesInner(files: List<AssetSpec>, fileResDir: File) {
        files.forEach { assetSpec ->
            when (assetSpec) {
                is AssetSpecDirectory ->
                    generateResourcesInner(assetSpec.assets, fileResDir)
                is AssetSpecFile ->
                    assetSpec.file.copyTo(File(fileResDir, assetSpec.pathRelativeToBase))
            }
        }
    }

    private companion object {
        const val FILES_DIR = "files"
    }
}
