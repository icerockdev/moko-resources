/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.js

import com.squareup.kotlinpoet.*
import dev.icerock.gradle.generator.KeyType
import dev.icerock.gradle.generator.LanguageType
import dev.icerock.gradle.generator.StringsGenerator
import dev.icerock.gradle.generator.js.JsMRGenerator.Companion.STRINGS_FALLBACK_FILE_URI_PROPERTY_NAME
import dev.icerock.gradle.generator.js.JsMRGenerator.Companion.SUPPORTED_LOCALES_PROPERTY_NAME
import dev.icerock.gradle.generator.js_jvm_common.generateFallbackAndSupportedLanguageProperties
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.gradle.api.file.FileTree
import java.io.File

class JsStringsGenerator(
    stringsFileTree: FileTree,
    private val mrClassPackage: String
) : StringsGenerator(stringsFileTree) {

    private val flattenClassPackage = mrClassPackage.replace(".", "")

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(key: String) =
        CodeBlock.of(
            "StringResource(key = %S, supportedLocales = %N, fallbackFileUri = %N)",
            key,
            SUPPORTED_LOCALES_PROPERTY_NAME,
            STRINGS_FALLBACK_FILE_URI_PROPERTY_NAME
        )

    override fun extendObjectBodyAtStart(classBuilder: TypeSpec.Builder) = Unit

    override fun extendObjectBodyAtEnd(classBuilder: TypeSpec.Builder) = Unit

    override fun beforeGenerateResources(
        objectBuilder: TypeSpec.Builder,
        languageMap: Map<LanguageType, Map<KeyType, String>>
    ) {
        objectBuilder
            .generateFallbackAndSupportedLanguageProperties(
                languages = languageMap.keys.toList(),
                folder = JsMRGenerator.LOCALIZATION_DIR,
                fallbackFilePropertyName = STRINGS_FALLBACK_FILE_URI_PROPERTY_NAME,
                fallbackFile = "${flattenClassPackage}_${JsMRGenerator.STRINGS_JSON_NAME}.json",
                supportedLocalesPropertyName = SUPPORTED_LOCALES_PROPERTY_NAME,
                getFileNameForLanguage = { language -> "${flattenClassPackage}_${JsMRGenerator.STRINGS_JSON_NAME}_$language.json" }
            )
    }

    override fun generateResources(resourcesGenerationDir: File, language: String?, strings: Map<KeyType, String>) {
        val fileDirName = when (language) {
            null -> "${flattenClassPackage}_${JsMRGenerator.STRINGS_JSON_NAME}"
            else -> "${flattenClassPackage}_${JsMRGenerator.STRINGS_JSON_NAME}_$language"
        }

        val localizationDir = File(resourcesGenerationDir, JsMRGenerator.LOCALIZATION_DIR).apply {
            mkdirs()
        }

        val stringsFile = File(localizationDir, "$fileDirName.json")

        val content = buildJsonObject {
            strings.forEach { (key, value) ->
                put(key, value.replaceAndroidParams())
            }
        }.toString()

        stringsFile.writeText(content)
    }
}