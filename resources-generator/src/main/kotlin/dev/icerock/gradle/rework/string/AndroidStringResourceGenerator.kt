/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.rework.string

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import dev.icerock.gradle.generator.LanguageType
import dev.icerock.gradle.rework.PlatformResourceGenerator
import dev.icerock.gradle.rework.metadata.resource.StringMetadata
import org.apache.commons.text.StringEscapeUtils
import java.io.File

class AndroidStringResourceGenerator(
    private val androidRClassPackage: String,
    private val resourcesGenerationDir: File
) : PlatformResourceGenerator<StringMetadata> {
    override fun imports(): List<ClassName> = listOf(
        ClassName(androidRClassPackage, "R")
    )

    override fun generateInitializer(metadata: StringMetadata): CodeBlock {
        return CodeBlock.of("StringResource(R.string.%L)", processKey(metadata.key))
    }

    override fun generateResourceFiles(data: List<StringMetadata>) {
        data.processLanguages().forEach { (lang, strings) ->
            generateLanguageFile(
                language = LanguageType.fromLanguage(lang),
                strings = strings
            )
        }
    }

    private fun generateLanguageFile(language: LanguageType, strings: Map<String, String>) {
        val valuesDir = File(resourcesGenerationDir, language.androidResourcesDir)
        val stringsFile = File(valuesDir, "multiplatform_strings.xml")
        valuesDir.mkdirs()

        val header =
            """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
            """.trimIndent()

        val content = strings.map { (key, value) ->
            val processedKey = processKey(key)
            val processedValue = convertXmlStringToAndroidLocalization(value)
            "\t<string name=\"$processedKey\">$processedValue</string>"
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

    // TODO should we do that?
    private fun convertXmlStringToAndroidLocalization(input: String): String {
        val xmlDecoded = StringEscapeUtils.unescapeXml(input)
        return xmlDecoded.replace("\n", "\\n")
            .replace("\"", "\\\"").let { StringEscapeUtils.escapeXml11(it) }
    }
}
