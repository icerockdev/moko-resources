/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.jvm

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.FontsGenerator
import org.gradle.api.file.FileTree
import java.io.File

class JvmFontsGenerator(inputFileTree: FileTree) : FontsGenerator(inputFileTree) {

    override fun getClassModifiers() = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers() = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(fontFileName: String) =
        CodeBlock.of("FontResource(fontPath = %S)", "$FONTS_DIR/$fontFileName.ttf")

    override fun generateResources(resourcesGenerationDir: File, files: List<FontFile>) {
        val fontsDir = File(resourcesGenerationDir, FONTS_DIR).apply { mkdirs() }
        files.forEach { (_, file) ->
            file.copyTo(File(fontsDir, file.name))
        }
    }

    companion object {
        private const val FONTS_DIR = "fonts"
    }
}
