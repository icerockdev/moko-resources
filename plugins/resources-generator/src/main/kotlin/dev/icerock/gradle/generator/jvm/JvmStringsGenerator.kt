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
    stringsFileTree: FileTree
) : StringsGenerator(stringsFileTree), ObjectBodyExtendable by ClassLoaderExtender() {

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
            null -> JvmMRGenerator.STRINGS_BUNDLE_NAME
            else -> "${JvmMRGenerator.STRINGS_BUNDLE_NAME}_$language"
        }

        val localizationDir = File(resourcesGenerationDir, JvmMRGenerator.LOCALIZATION_DIR).apply {
            mkdirs()
        }
        val stringsFile = File(localizationDir, "$fileDirName.properties")

        val content = strings.map { (key, value) ->
            "$key = ${value.replaceAndroidFormatParameters()}"
        }.joinToString("\n")

        stringsFile.writeText(content)
    }

    companion object {
        private val androidFormatRegex = "%.(\\$.)?".toRegex()

        fun String.replaceAndroidFormatParameters(): String {

            var formattedValue = this
            var paramNr = 0

            while (androidFormatRegex.containsMatchIn(formattedValue)) {
                formattedValue = formattedValue.replaceFirst(androidFormatRegex, "{${paramNr++}}")
            }
            return formattedValue
        }
    }
}
