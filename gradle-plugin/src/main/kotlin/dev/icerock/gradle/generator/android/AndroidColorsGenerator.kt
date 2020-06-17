/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.android

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.ColorNode
import dev.icerock.gradle.generator.ColorsGenerator
import org.gradle.api.file.FileTree
import java.io.File

class AndroidColorsGenerator(
    colorsFileTree: FileTree
) : ColorsGenerator(colorsFileTree) {
    override fun getImports(): List<ClassName> {
        return listOf(
            ClassName("dev.icerock.moko.graphics", "Color")
        )
    }

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun generateResources(resourcesGenerationDir: File, colors: List<ColorNode>) {
        val valuesDirName = "values"
        val valuesDir = File(resourcesGenerationDir, valuesDirName)
        val stringsFile = File(valuesDir, "colors.xml")
        valuesDir.mkdirs()

        val header = """
<?xml version="1.0" encoding="utf-8"?>
<resources>
            """.trimIndent()

        val content = colors.map { colorNode ->
            if (colorNode.isThemed()) {
                "\t<color name=\"${colorNode.name}_light\">#${colorNode.lightColor}</color>\n\t<color name=\"${colorNode.name}_dark\">#${colorNode.darkColor}</color>"
            } else {
                "\t<color name=\"${colorNode.name}\">#${colorNode.singleColor}</color>"
            }
        }.joinToString("\n")

        val footer = """
</resources>
            """.trimIndent()

        stringsFile.writeText(header + "\n")
        stringsFile.appendText(content)
        stringsFile.appendText("\n" + footer)
    }
}
