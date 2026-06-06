/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.string

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addValuesFunction
import dev.icerock.gradle.generator.localization.LanguageType
import dev.icerock.gradle.metadata.resource.StringMetadata
import dev.icerock.gradle.utils.convertXmlStringToLocalization
import java.io.File

internal class AppleStringResourceGenerator(
    private val baseLocalizationRegion: String,
    private val resourcesGenerationDir: File,
    private val bundleIdentifier: String,
) : PlatformResourceGenerator<StringMetadata> {
    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: StringMetadata): CodeBlock {
        return CodeBlock.of(
            "StringResource(resourceId = %S, bundle = %L)",
            metadata.key,
            "$PLATFORM_DETAILS_PROVIDER_NAME.$BUNDLE_PROPERTY_NAME"
        )
    }

    override fun supportsBatchedAccessors(): Boolean = true

    override fun generateBatchedInitializer(metadata: StringMetadata): CodeBlock {
        return generateInitializer(metadata)
    }

    override fun generateAdditionalBatchedFiles(packageName: String): List<FileSpec> {
        val providerObject = TypeSpec.objectBuilder(PLATFORM_DETAILS_PROVIDER_NAME)
            .addModifiers(KModifier.INTERNAL)
            .addProperty(
                PropertySpec.builder(BUNDLE_PROPERTY_NAME, Constants.Apple.nsBundleName)
                    .delegate(
                        CodeBlock.of(
                            "lazy { NSBundle.loadableBundle(%S) }",
                            bundleIdentifier
                        )
                    )
                    .build()
            )
            .addProperty(
                PropertySpec.builder(DETAILS_PROPERTY_NAME, Constants.resourcePlatformDetailsName)
                    .getter(
                        FunSpec.getterBuilder()
                            .addStatement(
                                "return %T(%N)",
                                Constants.resourcePlatformDetailsName,
                                BUNDLE_PROPERTY_NAME
                            )
                            .build()
                    )
                    .build()
            )
            .build()

        return listOf(
            FileSpec.builder(packageName, PLATFORM_DETAILS_PROVIDER_NAME)
                .addImport(
                    Constants.Apple.nsBundleName.packageName,
                    Constants.Apple.nsBundleName.simpleNames
                )
                .addImport(
                    Constants.Apple.loadableBundleName.packageName,
                    Constants.Apple.loadableBundleName.simpleNames
                )
                .addType(providerObject)
                .build()
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
        val resourcePlatformDetailsPropertySpec = PropertySpec
            .builder(
                Constants.PlatformDetails.platformDetailsPropertyName,
                Constants.resourcePlatformDetailsName
            )
            .also {
                if (modifier != null) {
                    it.addModifiers(modifier)
                }
            }
            .addModifiers(KModifier.OVERRIDE)
            .initializer(CodeBlock.of("$PLATFORM_DETAILS_PROVIDER_NAME.$DETAILS_PROPERTY_NAME"))
            .build()

        builder.addProperty(resourcePlatformDetailsPropertySpec)
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
        val resDir = File(resourcesGenerationDir, language.appleResourcesDir)
        val localizableFile = File(resDir, "Localizable.strings")
        resDir.mkdirs()

        val content = strings.mapValues { (_, value) ->
            value.convertXmlStringToLocalization()
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

    private companion object {
        const val PLATFORM_DETAILS_PROVIDER_NAME = "PlatformDetailsProvider"
        const val BUNDLE_PROPERTY_NAME = "bundle"
        const val DETAILS_PROPERTY_NAME = "details"
    }
}
