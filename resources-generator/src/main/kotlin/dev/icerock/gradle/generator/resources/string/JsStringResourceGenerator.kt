/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.string

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addEmptyPlatformResourceProperty
import dev.icerock.gradle.generator.addJsContainerStringsLoaderProperty
import dev.icerock.gradle.generator.addJsFallbackProperty
import dev.icerock.gradle.generator.addJsSupportedLocalesProperty
import dev.icerock.gradle.generator.localization.LanguageType
import dev.icerock.gradle.generator.platform.js.convertToMessageFormat
import dev.icerock.gradle.metadata.resource.StringMetadata
import dev.icerock.gradle.utils.flatName
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File

internal class JsStringResourceGenerator(
    resourcesPackageName: String,
    private val resourcesGenerationDir: File
) : PlatformResourceGenerator<StringMetadata> {
    private val flattenClassPackage: String = resourcesPackageName.flatName

    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: StringMetadata): CodeBlock {
        return CodeBlock.of(
            "StringResource(key = %S, loader = %L)",
            metadata.key,
            Constants.Js.stringsLoaderPropertyName
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

    override fun generateBeforeProperties(
        parentObjectName: String,
        builder: Builder,
        metadata: List<StringMetadata>,
        modifier: KModifier?
    ) {
        builder.addEmptyPlatformResourceProperty(modifier)

        builder.addSuperinterface(Constants.Js.loaderHolderName)

        builder.addJsFallbackProperty(
            fallbackFilePath = LOCALIZATION_DIR + "/" + getFileNameForLanguage(LanguageType.Base)
        )
        builder.addJsSupportedLocalesProperty(
            bcpLangToPath = metadata.asSequence()
                .flatMap { resource ->
                    resource.values.map { it.locale }
                }.distinct().map { locale ->
                    LanguageType.fromLanguage(locale)
                }.filterIsInstance<LanguageType.Locale>().map { language ->
                    val fileName: String = getFileNameForLanguage(language)
                    language.toBcpString() to "$LOCALIZATION_DIR/$fileName"
                }.toList()
        )
        builder.addJsContainerStringsLoaderProperty()
    }

    override fun generateAfterProperties(
        builder: Builder,
        metadata: List<StringMetadata>,
        modifier: KModifier?
    ) {
        val languageKeysList: String = metadata.joinToString { it.key }

        val valuesFun: FunSpec = FunSpec.builder("values")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return listOf($languageKeysList)")
            .returns(
                ClassName("kotlin.collections", "List")
                    .parameterizedBy(Constants.stringResourceName)
            )
            .build()

        builder.addFunction(valuesFun)
    }

    private fun generateLanguageFile(language: LanguageType, strings: Map<String, String>) {
        val localizationDir = File(resourcesGenerationDir, LOCALIZATION_DIR)
        localizationDir.mkdirs()

        val stringsFile = File(localizationDir, getFileNameForLanguage(language))

        val content: String = buildJsonObject {
            strings.forEach { (key: String, value: String) ->
                put(key, value.convertToMessageFormat())
            }
        }.toString()

        stringsFile.writeText(content)
    }

    private fun getFileNameForLanguage(language: LanguageType): String {
        return "${flattenClassPackage}_${STRINGS_JSON_NAME}${language.jsResourcesSuffix}.json"
    }

    private companion object {
        const val STRINGS_JSON_NAME = "stringsJson"
        const val LOCALIZATION_DIR = "localization"
    }
}
