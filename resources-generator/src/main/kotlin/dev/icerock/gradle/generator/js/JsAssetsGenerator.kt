/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.js

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
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
        return CodeBlock.of("""AssetResource(originalPath = js("require(\"$filePath\")") as String)""")
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
