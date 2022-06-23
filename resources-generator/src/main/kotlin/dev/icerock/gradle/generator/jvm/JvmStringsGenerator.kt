/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.jvm

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.*
import org.gradle.api.file.FileTree
import java.io.File

class JvmStringsGenerator(
    stringsFileTree: FileTree,
    strictLineBreaks: Boolean,
    mrSettings: MRGenerator.MRSettings
) : StringsGenerator(stringsFileTree, strictLineBreaks),
    ObjectBodyExtendable by ClassLoaderExtender(mrSettings.className) {

    private val flattenClassPackage = mrSettings.packageName.replace(".", "")

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(key: String) =
        CodeBlock.of(
            "StringResource(resourcesClassLoader = resourcesClassLoader, bundleName = %L, key = %S)",
            JvmMRGenerator.STRINGS_BUNDLE_PROPERTY_NAME,
            key
        )

    override fun generateResources(
        resourcesGenerationDir: File,
        language: LanguageType,
        strings: Map<KeyType, String>
    ) {
        val fileDirName = when (language) {
            LanguageType.Base -> "${flattenClassPackage}_${JvmMRGenerator.STRINGS_BUNDLE_NAME}"
            is LanguageType.Locale -> {
                // JVM ResourceBundle uses locale format, eg `en_US`, instead of BCP format
                // like `en-US`.
                "${flattenClassPackage}_${JvmMRGenerator.STRINGS_BUNDLE_NAME}_${language.toLocaleString()}"
            }
        }

        val localizationDir = File(resourcesGenerationDir, JvmMRGenerator.LOCALIZATION_DIR).apply {
            mkdirs()
        }
        val stringsFile = File(localizationDir, "$fileDirName.properties")

        val content = strings.map { (key, value) ->
            "$key = ${convertXmlStringToJvmLocalization(value)}"
        }.joinToString("\n")

        stringsFile.writeText(content)
    }
}
