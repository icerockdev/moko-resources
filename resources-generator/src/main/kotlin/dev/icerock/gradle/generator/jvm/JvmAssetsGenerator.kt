/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.jvm

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.AssetsGenerator
import dev.icerock.gradle.generator.ObjectBodyExtendable
import org.gradle.api.file.SourceDirectorySet
import java.io.File

class JvmAssetsGenerator(
    sourceDirectorySet: SourceDirectorySet
) : AssetsGenerator(sourceDirectorySet), ObjectBodyExtendable by ClassLoaderExtender() {

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(fileSpec: AssetSpecFile) = CodeBlock.of(
        "AssetResource(resourcesClassLoader = resourcesClassLoader, filePath = %S)",
        File(FILES_DIR, fileSpec.pathRelativeToBase).path
    )

    override fun generateResources(
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        files: List<AssetSpec>
    ) {
        val fileResDir = File(resourcesGenerationDir, FILES_DIR).apply { mkdirs() }
        generateResourcesInner(files, fileResDir)
    }

    private fun generateResourcesInner(files: List<AssetSpec>, fileResDir: File) {
        files.forEach {
            if (it is AssetSpecFile)
                it.file.copyTo(File(fileResDir, it.pathRelativeToBase))
            else if (it is AssetSpecDirectory) {
                generateResourcesInner(it.assets, fileResDir)
            }
        }
    }

    companion object {
        private const val FILES_DIR = "files"
    }
}
