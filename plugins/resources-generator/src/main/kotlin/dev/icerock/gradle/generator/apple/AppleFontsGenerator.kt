/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.apple

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.FontsGenerator
import dev.icerock.gradle.generator.ObjectBodyExtendable
import org.gradle.api.file.FileTree
import java.io.File

class AppleFontsGenerator(
    inputFileTree: FileTree
) : FontsGenerator(inputFileTree), ObjectBodyExtendable by AppleGeneratorHelper() {

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(fontFileName: String): CodeBlock? {
        return CodeBlock.of(
            "FontResource(fontName = %S, bundle = ${AppleMRGenerator.BUNDLE_PROPERTY_NAME})",
            fontFileName
        )
    }

    override fun generateResources(
        resourcesGenerationDir: File,
        files: List<FontFile>
    ) {
        files.forEach { (_, file) ->
            file.copyTo(File(resourcesGenerationDir, file.name))
        }
    }
}
