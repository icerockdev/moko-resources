/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.android

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.AssetsGenerator
import dev.icerock.gradle.generator.NOPObjectBodyExtendable
import dev.icerock.gradle.generator.ObjectBodyExtendable
import org.gradle.api.file.SourceDirectorySet
import java.io.File

class AndroidAssetsGenerator(
    sourceDirectorySet: SourceDirectorySet
) : AssetsGenerator(sourceDirectorySet), ObjectBodyExtendable by NOPObjectBodyExtendable() {

    override fun generateResources(
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        files: List<AssetSpec>
    ) {
        files.forEach {
            if (it is AssetSpecFile) {
                it.file.copyTo(File(assetsGenerationDir, it.pathRelativeToBase))
            } else if (it is AssetSpecDirectory) {
                generateResources(assetsGenerationDir, resourcesGenerationDir, it.assets)
            }
        }
    }

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(fileSpec: AssetSpecFile) =
        CodeBlock.of("AssetResource(path = %S)", fileSpec.pathRelativeToBase)
}
