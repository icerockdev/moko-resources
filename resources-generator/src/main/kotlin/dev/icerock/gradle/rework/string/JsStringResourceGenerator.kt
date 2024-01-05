/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.rework.string

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import dev.icerock.gradle.generator.LanguageType
import dev.icerock.gradle.generator.js.convertToMessageFormat
import dev.icerock.gradle.rework.PlatformGenerator
import dev.icerock.gradle.rework.metadata.resource.StringMetadata
import dev.icerock.gradle.utils.flatName
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File

// TODO migrate dev.icerock.gradle.generator.js.JsStringsGenerator.beforeGenerateResources
//  add fun values to MR.strings object
//  add property stringsFallbackFileUrl to MR.strings object
//  add property supportedLocales to MR.strings object
class JsStringResourceGenerator(
    resourcesPackageName: String,
    private val resourcesGenerationDir: File
) : PlatformGenerator<StringMetadata> {
    private val flattenClassPackage: String = resourcesPackageName.flatName

    override fun imports(): List<ClassName> = emptyList()

    // TODO we should add stringsLoader to MR
    override fun generateInitializer(metadata: StringMetadata): CodeBlock {
        return CodeBlock.of(
            "StringResource(key = %S, loader = %L)",
            metadata.key,
            "stringsLoader"
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
            "${flattenClassPackage}_${STRINGS_JSON_NAME}${language.jsResourcesSuffix}"

        val localizationDir = File(resourcesGenerationDir, LOCALIZATION_DIR)

        localizationDir.mkdirs()

        val stringsFile = File(localizationDir, "$fileDirName.json")

        val content: String = buildJsonObject {
            strings.forEach { (key, value) ->
                put(key, value.convertToMessageFormat())
            }
        }.toString()

        stringsFile.writeText(content)
    }

    // TODO share const
    private companion object {
        const val STRINGS_JSON_NAME = "stringsJson"
        const val LOCALIZATION_DIR = "localization"
    }
}
