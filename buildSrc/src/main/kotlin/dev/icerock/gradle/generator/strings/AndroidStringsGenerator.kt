/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.strings

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import org.gradle.api.file.FileTree
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

class AndroidStringsGenerator(
    sourceSet: KotlinSourceSet,
    stringsFileTree: FileTree,
    private val androidRClassPackage: String
) : StringsGenerator(
    sourceSet = sourceSet,
    stringsFileTree = stringsFileTree
) {
    override fun getStringsClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getStringsPropertyModifiers(): Array<KModifier> = arrayOf(
        KModifier.ACTUAL
    )

    override fun getStringsPropertyInitializer(key: String): CodeBlock? {
        val processedKey = key.replace(".", "_")
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
            "\t<string name=\"$key\">$value</string>"
        }.joinToString("\n")

        val footer = """
</resources>
            """.trimIndent()

        stringsFile.writeText(header + "\n")
        stringsFile.appendText(content)
        stringsFile.appendText("\n" + footer)
    }
}
