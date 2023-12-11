/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.js

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.KeyType
import dev.icerock.gradle.generator.LanguageType
import dev.icerock.gradle.generator.NOPObjectBodyExtendable
import dev.icerock.gradle.generator.ObjectBodyExtendable
import dev.icerock.gradle.generator.PluralMap
import dev.icerock.gradle.generator.PluralsGenerator
import dev.icerock.gradle.utils.flatName
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.gradle.api.file.FileTree
import java.io.File

class JsPluralsGenerator(
    ownResourcesFileTree: FileTree,
    mrClassPackage: String,
    strictLineBreaks: Boolean
) : PluralsGenerator(ownResourcesFileTree, strictLineBreaks),
    ObjectBodyExtendable by NOPObjectBodyExtendable() {

    private val flattenClassPackage = mrClassPackage.flatName

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(key: String): CodeBlock {
        return CodeBlock.of("PluralsResource(key = %S, loader = stringsLoader)", key)
    }

    override fun beforeGenerateResources(
        objectBuilder: TypeSpec.Builder,
        languageMap: Map<LanguageType, Map<KeyType, PluralMap>>
    ) {
        objectBuilder.generateFallbackAndSupportedLanguageProperties(
            languages = languageMap.keys.toList(),
            folder = JsMRGenerator.LOCALIZATION_DIR,
            fallbackFilePropertyName = PLURALS_FALLBACK_FILE_URL_PROPERTY_NAME,
            fallbackFile = "${flattenClassPackage}_${PLURALS_JSON_NAME}.json",
            supportedLocalesPropertyName = JsMRGenerator.SUPPORTED_LOCALES_PROPERTY_NAME,
            getFileNameForLanguage = { language ->
                "${flattenClassPackage}_${PLURALS_JSON_NAME}${language.jsResourcesSuffix}.json"
            }
        )
        val languageKeys = languageMap[LanguageType.Base].orEmpty().keys
        val languageKeysList = languageKeys.joinToString { it.replace(".", "_") }

        objectBuilder.addFunction(
            FunSpec.builder("values")
                .addModifiers(KModifier.OVERRIDE)
                .addStatement("return listOf($languageKeysList)")
                .returns(
                    ClassName("kotlin.collections", "List")
                        .parameterizedBy(resourceClassName)
                )
                .build()
        )
    }

    override fun generateResources(
        resourcesGenerationDir: File,
        language: LanguageType,
        strings: Map<KeyType, PluralMap>
    ) {
        val fileDirName =
            "${flattenClassPackage}_${PLURALS_JSON_NAME}${language.jsResourcesSuffix}"

        val localizationDir = File(resourcesGenerationDir, JsMRGenerator.LOCALIZATION_DIR).apply {
            mkdirs()
        }

        val pluralsFile = File(localizationDir, "$fileDirName.json")

        val content = buildJsonObject {
            strings.forEach { (key, pluralMap) ->
                val messageFormatString = StringBuilder().apply {
                    append("{ PLURAL, plural, ")
                    pluralMap.forEach { (pluralKey, pluralString) ->
                        // Zero isn't allowed in english (which is default for base), but we support it through =0
                        val actPluralKey = when (pluralKey) {
                            "zero" -> "=0"
                            "two" -> "=2"
                            else -> pluralKey
                        }

                        append(actPluralKey)
                        append(" ")
                        append("{")
                        append(pluralString.convertToMessageFormat())
                        append("} ")
                    }

                    append("}")
                }.toString()

                put(key, messageFormatString)
            }
        }.toString()

        pluralsFile.writeText(content)
    }

    companion object {
        const val PLURALS_JSON_NAME = "pluralsJson"
        const val PLURALS_FALLBACK_FILE_URL_PROPERTY_NAME = "stringsFallbackFileUrl"
    }
}
