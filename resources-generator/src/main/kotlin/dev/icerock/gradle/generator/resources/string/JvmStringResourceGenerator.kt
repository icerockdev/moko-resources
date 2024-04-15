/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.string

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.Constants.Jvm
import dev.icerock.gradle.generator.Constants.PlatformDetails
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addJvmPlatformResourceClassLoaderProperty
import dev.icerock.gradle.generator.addValuesFunction
import dev.icerock.gradle.generator.localization.LanguageType
import dev.icerock.gradle.metadata.resource.StringMetadata
import dev.icerock.gradle.utils.convertXmlStringToLocalizationValue
import org.apache.commons.text.StringEscapeUtils
import java.io.File

internal class JvmStringResourceGenerator(
    private val flattenClassPackage: String,
    private val resourcesGenerationDir: File,
) : PlatformResourceGenerator<StringMetadata> {
    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: StringMetadata): CodeBlock {
        return CodeBlock.of(
            "StringResource(resourcesClassLoader = %L, bundleName = %L, key = %S)",
            "${PlatformDetails.platformDetailsPropertyName}.${Jvm.resourcesClassLoaderPropertyName}",
            stringsBundlePropertyName,
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

    override fun generateBeforeProperties(
        builder: Builder,
        metadata: List<StringMetadata>,
        modifier: KModifier?,
    ) {
        builder.addJvmPlatformResourceClassLoaderProperty(modifier = modifier)

        // FIXME duplication
        val property: PropertySpec = PropertySpec.builder(
            stringsBundlePropertyName,
            STRING,
            KModifier.PRIVATE
        ).initializer(CodeBlock.of("\"%L/%L\"", Jvm.localizationDir, getBundlePath()))
            .build()

        builder.addProperty(property)
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

    private fun generateLanguageFile(language: LanguageType, strings: Map<String, String>) {
        val fileDirName = "${getBundlePath()}${language.jvmResourcesSuffix}"

        val localizationDir = File(resourcesGenerationDir, Jvm.localizationDir)
        localizationDir.mkdirs()

        val stringsFile = File(localizationDir, "$fileDirName.properties")

        val content: String = strings.map { (key, value) ->
            "$key = ${value.convertXmlStringToLocalizationValue()}"
        }.joinToString("\n")

        stringsFile.writeText(content)
    }

    private fun getBundlePath(): String = "${flattenClassPackage}_$stringsBundleName"

    private companion object {
        const val stringsBundlePropertyName = "stringsBundle"
        const val stringsBundleName = "mokoBundle"
    }
}
