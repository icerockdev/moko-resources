/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.plural

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.localization.LanguageType
import dev.icerock.gradle.metadata.resource.PluralMetadata
import org.apache.commons.text.StringEscapeUtils
import java.io.File

internal class AndroidPluralResourceGenerator(
    private val androidRClassPackage: String,
    private val resourcesGenerationDir: File
) : PlatformResourceGenerator<PluralMetadata> {
    override fun imports(): List<ClassName> = listOf(
        ClassName(androidRClassPackage, "R")
    )

    override fun generateInitializer(metadata: PluralMetadata): CodeBlock {
        return CodeBlock.of("PluralsResource(R.plurals.%L)", processKey(metadata.key))
    }

    override fun generateResourceFiles(data: List<PluralMetadata>) {
        data.processLanguages().forEach { (lang, pluralMap) ->
            generateLanguageFile(
                language = LanguageType.fromLanguage(lang),
                strings = pluralMap
            )
        }
    }

    private fun generateLanguageFile(
        language: LanguageType,
        strings: Map<String, Map<String, String>>
    ) {
        val valuesDir = File(resourcesGenerationDir, language.androidResourcesDir)
        val stringsFile = File(valuesDir, "multiplatform_plurals.xml")
        valuesDir.mkdirs()

        val header =
            """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
            """.trimIndent()

        val content = strings.map { (key, pluralMap) ->
            val processedKey = processKey(key)
            val start = "\t<plurals name=\"$processedKey\">\n"
            val items: String = pluralMap.map { (quantity, value) ->
                val processedValue = StringEscapeUtils.escapeXml10(value)
                "\t\t<item quantity=\"$quantity\">$processedValue</item>"
            }.joinToString("\n")
            val end = "\n\t</plurals>"

            start + items + end
        }.joinToString("\n")

        val footer =
            """
            </resources>
            """.trimIndent()

        stringsFile.writeText(header + "\n")
        stringsFile.appendText(content)
        stringsFile.appendText("\n" + footer)
    }

    // TODO should we do that?
    private fun processKey(key: String): String {
        return key.replace(".", "_")
    }
}
