/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.apple

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.AssetsGenerator
import dev.icerock.gradle.generator.ObjectBodyExtendable
import org.gradle.api.file.SourceDirectorySet
import java.io.File

class AppleAssetsGenerator(sourceDirectorySet: SourceDirectorySet) :
    AssetsGenerator(sourceDirectorySet),
    ObjectBodyExtendable by AppleGeneratorHelper() {

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(fileSpec: AssetSpecFile): CodeBlock {
        val ext = fileSpec.file.extension

        val relativePathWithoutExt = fileSpec
            .pathRelativeToBase
            .replace('/', PATH_DELIMITER)
            .substringBeforeLast('.')

        return CodeBlock.of(
            "AssetResource(originalPath = %S, fileName = %S, extension = %S, bundle = " +
                    AppleMRGenerator.BUNDLE_PROPERTY_NAME + ")",
            fileSpec.pathRelativeToBase,
            relativePathWithoutExt,
            ext
        )
    }

    override fun generateResources(
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        files: List<AssetSpec>
    ) {
        files.forEach {
            if (it is AssetSpecFile) {
                val newName = it.pathRelativeToBase
                    .replace('/', PATH_DELIMITER)
                it.file.copyTo(File(resourcesGenerationDir, newName))
            } else if (it is AssetSpecDirectory) {
                generateResources(assetsGenerationDir, resourcesGenerationDir, it.assets)
            }
        }
    }
}
