/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.apple

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.*
import org.gradle.api.file.FileTree
import java.io.File

class ApplePluralsGenerator(
    pluralsFileTree: FileTree,
    strictLineBreaks: Boolean,
    private val baseLocalizationRegion: String
) : PluralsGenerator(pluralsFileTree, strictLineBreaks),
    ObjectBodyExtendable by AppleGeneratorHelper() {

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(key: String) = CodeBlock.of(
        "PluralsResource(resourceId = %S, bundle = ${AppleMRGenerator.BUNDLE_PROPERTY_NAME})",
        key
    )

    private fun writeStringsFile(file: File, strings: Map<KeyType, PluralMap>) {
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

    override fun generateResources(
        resourcesGenerationDir: File,
        language: LanguageType,
        strings: Map<KeyType, PluralMap>
    ) {
        val resDirName = when (language) {
            LanguageType.Base -> "Base.lproj"
            else -> "$language.lproj"
        }

        val resDir = File(resourcesGenerationDir, resDirName)
        val localizableFile = File(resDir, "Localizable.stringsdict")
        resDir.mkdirs()
        writeStringsFile(localizableFile, strings)

        if (language == null) {
            val regionDir = File(resourcesGenerationDir, "$baseLocalizationRegion.lproj")
            regionDir.mkdirs()
            val regionFile = File(regionDir, "Localizable.stringsdict")
            writeStringsFile(regionFile, strings)
        }
    }

    private fun String.escapeFormatArguments(): String {
        return this.replace(Regex("%(((?:\\.|\\d|\\$)*)[abcdefs])"), "%%$1")
    }
}
