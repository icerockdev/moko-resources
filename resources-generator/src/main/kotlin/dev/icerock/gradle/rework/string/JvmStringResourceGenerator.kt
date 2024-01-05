/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.rework.string

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import dev.icerock.gradle.generator.LanguageType
import dev.icerock.gradle.rework.PlatformGenerator
import dev.icerock.gradle.rework.metadata.resource.StringMetadata
import dev.icerock.gradle.utils.flatName
import org.apache.commons.text.StringEscapeUtils
import java.io.File

class JvmStringResourceGenerator(
    resourcesPackageName: String,
    private val resourcesGenerationDir: File
) : PlatformGenerator<StringMetadata> {
    private val flattenClassPackage: String = resourcesPackageName.flatName

    override fun imports(): List<ClassName> = emptyList()

    // TODO we should add resourcesClassLoader to MR object
    // TODO we should add stringsBundle to MR object
    override fun generateInitializer(metadata: StringMetadata): CodeBlock {
        return CodeBlock.of(
            "StringResource(resourcesClassLoader = %L, bundleName = %L, key = %S)",
            "resourcesClassLoader",
            STRINGS_BUNDLE_PROPERTY_NAME,
            metadata.key
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
        val fileDirName =
            "${flattenClassPackage}_${STRINGS_BUNDLE_NAME}${language.jvmResourcesSuffix}"

        val localizationDir = File(resourcesGenerationDir, LOCALIZATION_DIR)
        localizationDir.mkdirs()

        val stringsFile = File(localizationDir, "$fileDirName.properties")

        val content: String = strings.map { (key, value) ->
            "$key = ${convertXmlStringToJvmLocalization(value)}"
        }.joinToString("\n")

        stringsFile.writeText(content)
    }

    // TODO should we do that?
    private fun convertXmlStringToJvmLocalization(input: String): String {
        return StringEscapeUtils.unescapeXml(input)
            .replace("\n", "\\n")
            .replace("\"", "\\\"")
    }

    // TODO share const
    private companion object {
        const val STRINGS_BUNDLE_PROPERTY_NAME = "stringsBundle"
        const val STRINGS_BUNDLE_NAME = "mokoBundle"
        const val LOCALIZATION_DIR = "localization"
    }
}
