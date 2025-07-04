/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.plural

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
import dev.icerock.gradle.generator.platform.js.JsFilePathMode
import dev.icerock.gradle.generator.platform.js.convertToMessageFormat
import dev.icerock.gradle.metadata.resource.PluralMetadata
import dev.icerock.gradle.utils.flatName
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File

internal class JsPluralResourceGenerator(
    resourcesPackageName: String,
    private val resourcesGenerationDir: File,
    private val filePathMode: JsFilePathMode
) : PlatformResourceGenerator<PluralMetadata> {
    private val flattenClassPackage: String = resourcesPackageName.flatName

    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: PluralMetadata): CodeBlock {
        return CodeBlock.of(
            "PluralsResource(key = %S, loader = %L)",
            metadata.key,
            Constants.Js.stringsLoaderPropertyName
        )
    }

    override fun generateResourceFiles(data: List<PluralMetadata>) {
        data.processLanguages().forEach { (lang, strings) ->
            generateLanguageFile(
                language = LanguageType.fromLanguage(lang),
                strings = strings
            )
        }
    }

    override fun generateBeforeProperties(
        builder: Builder,
        metadata: List<PluralMetadata>,
        modifier: KModifier?
    ) {
        builder.addEmptyPlatformResourceProperty(modifier)

        builder.addSuperinterface(Constants.Js.loaderHolderName)

        builder.addJsFallbackProperty(
            fallbackFilePath = "./" + LOCALIZATION_DIR + "/" + getFileNameForLanguage(LanguageType.Base),
            filePathMode = filePathMode
        )
        builder.addJsSupportedLocalesProperty(
            bcpLangToPath = metadata.asSequence()
                .flatMap { resource ->
                    resource.values.map { it.locale }
                }.distinct().map { locale ->
                    LanguageType.fromLanguage(locale)
                }.filterIsInstance<LanguageType.Locale>().map { language ->
                    val fileName: String = getFileNameForLanguage(language)
                    language.toBcpString() to "./$LOCALIZATION_DIR/$fileName"
                }.toList(),
            filePathMode = filePathMode
        )
        builder.addJsContainerStringsLoaderProperty()
    }

    override fun generateAfterProperties(
        builder: Builder,
        metadata: List<PluralMetadata>,
        modifier: KModifier?
    ) {
        val languageKeysList: String = metadata.joinToString { it.key }

        val valuesFun: FunSpec = FunSpec.builder("values")
            .also {
                if (modifier != null) {
                    it.addModifiers(modifier)
                }
            }
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return listOf($languageKeysList)")
            .returns(
                ClassName("kotlin.collections", "List")
                    .parameterizedBy(Constants.pluralsResourceName)
            )
            .build()

        builder.addFunction(valuesFun)
    }

    private fun generateLanguageFile(
        language: LanguageType,
        strings: Map<String, Map<String, String>>
    ) {
        val localizationDir = File(resourcesGenerationDir, LOCALIZATION_DIR)
        localizationDir.mkdirs()

        val pluralsFile = File(localizationDir, getFileNameForLanguage(language))

        val content: String = buildJsonObject {
            strings.forEach { (key: String, pluralMap: Map<String, String>) ->
                val messageFormatString = StringBuilder().apply {
                    append("{ PLURAL, plural, ")
                    pluralMap.forEach { (pluralKey, pluralString) ->
                        // Zero isn't allowed in english (which is default for base), but we support it through =0
                        val actPluralKey: String = when (pluralKey) {
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

    private fun getFileNameForLanguage(language: LanguageType): String {
        return "${flattenClassPackage}_${PLURALS_JSON_NAME}${language.jsResourcesSuffix}.json"
    }

    private companion object {
        const val PLURALS_JSON_NAME = "pluralsJson"
        const val LOCALIZATION_DIR = "localization"
    }
}
