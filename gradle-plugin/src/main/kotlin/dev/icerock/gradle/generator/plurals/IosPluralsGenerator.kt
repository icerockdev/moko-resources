/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.plurals

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.IosMRGenerator
import dev.icerock.gradle.generator.strings.KeyType
import org.gradle.api.file.FileTree
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

class IosPluralsGenerator(
    sourceSet: KotlinSourceSet,
    pluralsFileTree: FileTree,
    private val baseLocalizationRegion: String
) : PluralsGenerator(
    sourceSet = sourceSet,
    pluralsFileTree = pluralsFileTree
) {
    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(key: String): CodeBlock? {
        return CodeBlock.of(
            "PluralsResource(resourceId = %S, bundle = ${IosMRGenerator.BUNDLE_PROPERTY_NAME})",
            key
        )
    }

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
                """				<key>$quantity</key>
				<string>$value</string>"""
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
        language: String?,
        strings: Map<KeyType, PluralMap>
    ) {
        val resDirName = when (language) {
            null -> "Base.lproj"
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
}
