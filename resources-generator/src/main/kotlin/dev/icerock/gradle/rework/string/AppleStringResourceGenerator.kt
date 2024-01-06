/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.rework.string

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.LanguageType
import dev.icerock.gradle.rework.CodeConst
import dev.icerock.gradle.rework.PlatformResourceGenerator
import dev.icerock.gradle.rework.addAppleContainerBundleProperty
import dev.icerock.gradle.rework.metadata.resource.StringMetadata
import org.apache.commons.text.StringEscapeUtils
import java.io.File

class AppleStringResourceGenerator(
    private val baseLocalizationRegion: String,
    private val resourcesGenerationDir: File
) : PlatformResourceGenerator<StringMetadata> {
    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: StringMetadata): CodeBlock {
        return CodeBlock.of(
            "StringResource(resourceId = %S, bundle = %L)",
            metadata.key,
            CodeConst.Apple.containerBundlePropertyName
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
        val resDir = File(resourcesGenerationDir, language.appleResourcesDir)
        val localizableFile = File(resDir, "Localizable.strings")
        resDir.mkdirs()

        val content = strings.mapValues { (_, value) ->
            convertXmlStringToAppleLocalization(value)
        }.map { (key, value) ->
            "\"$key\" = \"$value\";"
        }.joinToString("\n")
        localizableFile.writeText(content)

        if (language == LanguageType.Base) {
            val regionDir = File(resourcesGenerationDir, "$baseLocalizationRegion.lproj")
            regionDir.mkdirs()
            val regionFile = File(regionDir, "Localizable.strings")
            regionFile.writeText(content)
        }
    }

    override fun generateBeforeProperties(builder: TypeSpec.Builder) {
        builder.addAppleContainerBundleProperty()
    }

    // TODO should we do that?
    private fun convertXmlStringToAppleLocalization(input: String): String {
        return StringEscapeUtils.unescapeXml(input)
            .replace("\n", "\\n")
            .replace("\"", "\\\"")
    }
}
