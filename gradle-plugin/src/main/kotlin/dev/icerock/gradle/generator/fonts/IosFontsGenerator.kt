/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.fonts

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.ExtendsPlistDictionary
import dev.icerock.gradle.generator.IosMRGenerator
import org.gradle.api.file.FileTree
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilder

class IosFontsGenerator(
    sourceSet: KotlinSourceSet,
    inputFileTree: FileTree
) : FontsGenerator(
    sourceSet = sourceSet,
    inputFileTree = inputFileTree
) {
    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(fontFileName: String): CodeBlock? {
        return CodeBlock.of("FontResource(fontName = %S)", fontFileName)
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