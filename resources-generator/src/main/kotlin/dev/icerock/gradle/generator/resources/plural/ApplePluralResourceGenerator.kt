/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.plural

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addAppleContainerBundleInitializerProperty
import dev.icerock.gradle.generator.addValuesFunction
import dev.icerock.gradle.generator.localization.LanguageType
import dev.icerock.gradle.metadata.resource.PluralMetadata
import java.io.File

internal class ApplePluralResourceGenerator(
    private val baseLocalizationRegion: String,
    private val resourcesGenerationDir: File,
) : PlatformResourceGenerator<PluralMetadata> {
    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: PluralMetadata): CodeBlock {
        return CodeBlock.of(
            "PluralsResource(resourceId = %S, bundle = %L)",
            metadata.key,
            Constants.Apple.platformContainerBundlePropertyName
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
        modifier: KModifier?,
    ) {
        builder.addAppleContainerBundleInitializerProperty(modifier)
    }

    override fun generateAfterProperties(
        builder: Builder,
        metadata: List<PluralMetadata>,
        modifier: KModifier?,
    ) {
        builder.addValuesFunction(
            modifier = modifier,
            metadata = metadata,
            classType = Constants.pluralsResourceName
        )
    }

    private fun generateLanguageFile(
        language: LanguageType,
        strings: Map<String, Map<String, String>>,
    ) {
        val resDir = File(resourcesGenerationDir, language.appleResourcesDir)
        val localizableFile = File(resDir, "Localizable.stringsdict")
        resDir.mkdirs()
        writeStringsFile(localizableFile, strings)

        if (language == LanguageType.Base) {
            val regionDir = File(resourcesGenerationDir, "$baseLocalizationRegion.lproj")
            regionDir.mkdirs()
            val regionFile = File(regionDir, "Localizable.stringsdict")
            writeStringsFile(regionFile, strings)
        }
    }

    private fun writeStringsFile(file: File, strings: Map<String, Map<String, String>>) {
        val head = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
	<dict>
        """

        val content = strings.map { (key, pluralMap) ->
            val start = """<key>$key</key>
		<dict>
			<key>NSStringLocalizedFormatKey</key>
			<string>%#@pluraled@</string>
			<key>pluraled</key>
			<dict>
				<key>NSStringFormatSpecTypeKey</key>
				<string>NSStringPluralRuleType</string>
				<key>NSStringFormatValueTypeKey</key>
				<string>d</string>
"""

            val items = pluralMap.map { (quantity, value) ->
                val processedValue = value.escapeFormatArguments()
                """				<key>$quantity</key>
				<string>$processedValue</string>"""
            }.joinToString(separator = "\n")

            val end = """
			</dict>
		</dict>"""

            start + items + end
        }.joinToString("\n")

        val footer = """
	</dict>
</plist>"""

        file.writeText(head)
        file.appendText(content)
        file.appendText(footer)
    }

    private fun String.escapeFormatArguments(): String {
        return this.replace(Regex("%(((?:\\.|\\d|\\$)*)[abcdefs])"), "%%$1")
    }
}
