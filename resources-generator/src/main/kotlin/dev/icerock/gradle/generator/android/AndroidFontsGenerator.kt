/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.android

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.FontsGenerator
import dev.icerock.gradle.generator.NOPObjectBodyExtendable
import dev.icerock.gradle.generator.ObjectBodyExtendable
import org.gradle.api.file.FileTree
import java.io.File
import java.util.Locale

class AndroidFontsGenerator(
    ownInputFileTree: FileTree,
    lowerInputFileTree: FileTree,
    private val androidRClassPackage: String,
) : FontsGenerator(ownInputFileTree), ObjectBodyExtendable by NOPObjectBodyExtendable() {
    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(fontFile: File): CodeBlock {
        return CodeBlock.of("FontResource(fontResourceId = R.font.%L)", keyToResourceId(fontFile.nameWithoutExtension))
    }

    override fun getImports(): List<ClassName> = listOf(
        ClassName(androidRClassPackage, "R")
    )

    override fun generateResources(
        resourcesGenerationDir: File,
        files: List<FontFile>
    ) {
        val fontResDir = File(resourcesGenerationDir, "font")
        fontResDir.mkdirs()

        files.forEach { (key, file) ->
            val fileName = keyToResourceId(key) + "." + file.extension
            file.copyTo(File(fontResDir, fileName))
        }
    }

    private fun keyToResourceId(key: String): String {
        return key.replace("-", "_").lowercase(Locale.ROOT)
    }
}
