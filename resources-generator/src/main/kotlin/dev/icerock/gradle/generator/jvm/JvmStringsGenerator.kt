/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.jvm

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.KeyType
import dev.icerock.gradle.generator.ObjectBodyExtendable
import dev.icerock.gradle.generator.StringsGenerator
import org.gradle.api.file.FileTree
import java.io.File

class JvmStringsGenerator(
    stringsFileTree: FileTree,
    private val mrClassPackage: String
) : StringsGenerator(stringsFileTree), ObjectBodyExtendable by ClassLoaderExtender() {

    private val flattenClassPackage = mrClassPackage.replace(".", "")

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
        language: String?,
        strings: Map<KeyType, String>
    ) {
        val fileDirName = when (language) {
            null -> "${flattenClassPackage}_${JvmMRGenerator.STRINGS_BUNDLE_NAME}"
            else -> "${flattenClassPackage}_${JvmMRGenerator.STRINGS_BUNDLE_NAME}_$language"
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
