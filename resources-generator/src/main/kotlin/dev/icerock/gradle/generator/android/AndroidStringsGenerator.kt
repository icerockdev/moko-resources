/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.android

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.KeyType
import dev.icerock.gradle.generator.LanguageType
import dev.icerock.gradle.generator.NOPObjectBodyExtendable
import dev.icerock.gradle.generator.ObjectBodyExtendable
import dev.icerock.gradle.generator.StringsGenerator
import org.apache.commons.text.StringEscapeUtils
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Provider
import java.io.File

class AndroidStringsGenerator(
    stringsFileTree: FileTree,
    strictLineBreaks: Boolean,
    private val androidRClassPackageProvider: Provider<String>,
) : StringsGenerator(stringsFileTree, strictLineBreaks),
    ObjectBodyExtendable by NOPObjectBodyExtendable() {

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(key: String) =
        CodeBlock.of("StringResource(R.string.%L)", processKey(key))

    override fun getImports(): List<ClassName> = listOf(
        ClassName(androidRClassPackageProvider.get(), "R")
    )

    override fun generateResources(
        resourcesGenerationDir: File,
        language: LanguageType,
        strings: Map<KeyType, String>
    ) {
        val valuesDir = File(resourcesGenerationDir, language.androidResourcesDir)
        val stringsFile = File(valuesDir, "multiplatform_strings.xml")
        valuesDir.mkdirs()

        val header =
            """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
            """.trimIndent()

        val content = strings.map { (key, value) ->
            val processedKey = processKey(key)
            val processedValue = convertXmlStringToAndroidLocalization(value)
            "\t<string name=\"$processedKey\">$processedValue</string>"
        }.joinToString("\n")

        val footer =
            """
            </resources>
            """.trimIndent()

        stringsFile.writeText(header + "\n")
        stringsFile.appendText(content)
        stringsFile.appendText("\n" + footer)
    }

    private fun processKey(key: String): String {
        return key.replace(".", "_")
    }

    private fun convertXmlStringToAndroidLocalization(input: String): String {
        val xmlDecoded = StringEscapeUtils.unescapeXml(input)
        return xmlDecoded.replace("\n", "\\n")
            .replace("\"", "\\\"").let { StringEscapeUtils.escapeXml11(it) }
    }
}
