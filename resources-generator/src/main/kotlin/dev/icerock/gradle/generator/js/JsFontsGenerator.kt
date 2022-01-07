/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.js

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.FontsGenerator
import org.gradle.api.file.FileTree
import java.io.File

class JsFontsGenerator(
    inputFileTree: FileTree
) : FontsGenerator(inputFileTree) {
    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(fontFile: File): CodeBlock {
        return CodeBlock.of("FontResource(fileUrl = js(\"require(\\\"${FONTS_DIR}/${fontFile.name}\\\")\") as String)")
    }

    override fun generateResources(resourcesGenerationDir: File, files: List<FontFile>) {
        val fontsDir = File(resourcesGenerationDir, FONTS_DIR).apply { mkdirs() }
        files.forEach { (_, file) ->
            file.copyTo(File(fontsDir, file.name))
        }
    }

    override fun extendObjectBodyAtStart(classBuilder: TypeSpec.Builder) = Unit

    override fun extendObjectBodyAtEnd(classBuilder: TypeSpec.Builder) = Unit

    companion object {
        const val FONTS_DIR = "fonts"
    }
}