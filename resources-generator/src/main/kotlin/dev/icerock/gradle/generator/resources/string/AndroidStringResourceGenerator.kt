/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.string

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addEmptyPlatformResourceProperty
import dev.icerock.gradle.generator.addValuesFunction
import dev.icerock.gradle.generator.localization.LanguageType
import dev.icerock.gradle.metadata.resource.StringMetadata
import dev.icerock.gradle.utils.convertXmlStringToLocalizationValue
import java.io.File

internal class AndroidStringResourceGenerator(
    private val androidRClassPackage: String,
    private val resourcesGenerationDir: File,
) : PlatformResourceGenerator<StringMetadata> {
    override fun imports(): List<ClassName> = listOf(
        ClassName(androidRClassPackage, "R")
    )

    override fun generateInitializer(metadata: StringMetadata): CodeBlock {
        return CodeBlock.of("StringResource(R.string.%L)", metadata.key)
    }

    override fun generateBeforeProperties(
        builder: Builder,
        metadata: List<StringMetadata>,
        modifier: KModifier?,
    ) {
        builder.addEmptyPlatformResourceProperty(modifier)
    }

    override fun generateAfterProperties(
        builder: Builder,
        metadata: List<StringMetadata>,
        modifier: KModifier?,
    ) {
        builder.addValuesFunction(
            modifier = modifier,
            metadata = metadata,
            classType = Constants.stringResourceName
        )
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
            val processedValue = value.convertXmlStringToLocalizationValue()
            "\t<string name=\"$key\">$processedValue</string>"
        }.joinToString("\n")

        val footer =
            """
            </resources>
            """.trimIndent()

        stringsFile.writeText(header + "\n")
        stringsFile.appendText(content)
        stringsFile.appendText("\n" + footer)
    }
}
