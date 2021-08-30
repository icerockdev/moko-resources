/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.apple

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.AssetsGenerator
import dev.icerock.gradle.generator.NOPObjectBodyExtendable
import dev.icerock.gradle.generator.ObjectBodyExtendable
import org.gradle.api.file.FileTree
import java.io.File

class AppleAssetsGenerator(inputFileTree: FileTree) : AssetsGenerator(inputFileTree),
    ObjectBodyExtendable by NOPObjectBodyExtendable() {

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(fileSpec: AssetSpec) =
        CodeBlock.of(
            "AssetResource(path = %S, bundle = ${AppleMRGenerator.BUNDLE_PROPERTY_NAME})",
            fileSpec.key
        )

    override fun generateResources(
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        files: List<AssetSpec>
    ) {
        files.forEach {

            val extension = it.file.extension
            val newName = if (extension.isEmpty()) it.key else it.key + "." + extension
            it.file.copyTo(File(resourcesGenerationDir, newName))
        }
    }
}
