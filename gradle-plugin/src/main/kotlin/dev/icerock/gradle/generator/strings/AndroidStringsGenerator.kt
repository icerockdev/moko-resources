/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.strings

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import org.apache.commons.lang3.StringEscapeUtils
import org.gradle.api.file.FileTree
import java.io.File

class AndroidStringsGenerator(
    stringsFileTree: FileTree,
    private val androidRClassPackage: String
) : StringsGenerator(
    stringsFileTree = stringsFileTree
) {
    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(key: String): CodeBlock? {
        val processedKey = processKey(key)
        return CodeBlock.of("StringResource(R.string.%L)", processedKey)
    }

    override fun getImports(): List<ClassName> = listOf(
        ClassName(androidRClassPackage, "R")
    )

    override fun generateResources(
        resourcesGenerationDir: File,
        language: String?,
        strings: Map<KeyType, String>
    ) {
        val valuesDirName = when (language) {
            null -> "values"
            else -> "values-$language"
        }

        val valuesDir = File(resourcesGenerationDir, valuesDirName)
        val stringsFile = File(valuesDir, "multiplatform_strings.xml")
        valuesDir.mkdirs()

        val header = """
<?xml version="1.0" encoding="utf-8"?>
<resources>
            """.trimIndent()

        val content = strings.map { (key, value) ->
            val processedKey = processKey(key)
            val processedValue = StringEscapeUtils.escapeXml(value)
            "\t<string name=\"$processedKey\">$processedValue</string>"
        }.joinToString("\n")

        val footer = """
</resources>
            """.trimIndent()

        stringsFile.writeText(header + "\n")
        stringsFile.appendText(content)
        stringsFile.appendText("\n" + footer)
    }

    private fun processKey(key: String): String {
        return key.replace(".", "_").toLowerCase()
    }
}
