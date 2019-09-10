/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.plurals

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.strings.KeyType
import org.gradle.api.file.FileTree
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

class AndroidPluralsGenerator(
    sourceSet: KotlinSourceSet,
    pluralsFileTree: FileTree,
    private val androidRClassPackage: String
) : PluralsGenerator(
    sourceSet = sourceSet,
    pluralsFileTree = pluralsFileTree
) {
    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(key: String): CodeBlock? {
        val processedKey = key.replace(".", "_")
        return CodeBlock.of("PluralsResource(R.plurals.%L)", processedKey)
    }

    override fun getImports(): List<ClassName> = listOf(
        ClassName(androidRClassPackage, "R")
    )

    override fun generateResources(
        resourcesGenerationDir: File,
        language: String?,
        strings: Map<KeyType, PluralMap>
    ) {
        val valuesDirName = when (language) {
            null -> "values"
            else -> "values-$language"
        }

        val valuesDir = File(resourcesGenerationDir, valuesDirName)
        val stringsFile = File(valuesDir, "multiplatform_plurals.xml")
        valuesDir.mkdirs()

        val header = """
<?xml version="1.0" encoding="utf-8"?>
<resources>
            """.trimIndent()

        val content = strings.map { (key, pluralMap) ->
            val start = "\t<plurals name=\"$key\">\n"
            val items = pluralMap.map { (quantity, value) ->
                "\t\t<item quantity=\"$quantity\">$value</item>"
            }.joinToString("\n")
            val end = "\n\t</plurals>"

            start + items + end
        }.joinToString("\n")

        val footer = """
</resources>
            """.trimIndent()

        stringsFile.writeText(header + "\n")
        stringsFile.appendText(content)
        stringsFile.appendText("\n" + footer)
    }
}
