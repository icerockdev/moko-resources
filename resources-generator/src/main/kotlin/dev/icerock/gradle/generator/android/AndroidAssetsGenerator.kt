/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.android

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.AssetsGenerator
import dev.icerock.gradle.generator.NOPObjectBodyExtendable
import dev.icerock.gradle.generator.ObjectBodyExtendable
import org.gradle.api.file.FileTree
import java.io.File

class AndroidAssetsGenerator(
    inputFile: FileTree,
    private val androidRClassPackage: String
) : AssetsGenerator(inputFile), ObjectBodyExtendable by NOPObjectBodyExtendable() {

    override fun generateResources(
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        files: List<AssetSpec>
    ) {
        files.forEach {
            it.file.copyTo(File(assetsGenerationDir, it.pathRelativeToBase))
        }
    }

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(fileSpec: AssetSpec) =
        CodeBlock.of("AssetResource(path = %S)", fileSpec.key)

    override fun getImports() = listOf(
        ClassName(androidRClassPackage, "R")
    )
}
