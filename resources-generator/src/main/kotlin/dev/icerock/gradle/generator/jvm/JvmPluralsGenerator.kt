/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.jvm

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.KeyType
import dev.icerock.gradle.generator.LanguageType
import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.ObjectBodyExtendable
import dev.icerock.gradle.generator.PluralMap
import dev.icerock.gradle.generator.PluralsGenerator
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Provider
import java.io.File

class JvmPluralsGenerator(
    pluralsFileTree: FileTree,
    strictLineBreaks: Boolean,
    mrSettings: MRGenerator.MRSettings
) : PluralsGenerator(pluralsFileTree, strictLineBreaks),
    ObjectBodyExtendable by ClassLoaderExtender(mrSettings.className) {

    private val flattenClassPackage: Provider<String> = mrSettings.packageName
        .map { it.replace(".", "") }

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(key: String) =
        CodeBlock.of(
            "PluralsResource(resourcesClassLoader = resourcesClassLoader, bundleName = %L, key = %S)",
            JvmMRGenerator.PLURALS_BUNDLE_PROPERTY_NAME,
            key,
        )

    override fun generateResources(
        resourcesGenerationDir: File,
        language: LanguageType,
        strings: Map<KeyType, PluralMap>
    ) {
        val fileDirName =
            "${flattenClassPackage.get()}_${JvmMRGenerator.PLURALS_BUNDLE_NAME}${language.jvmResourcesSuffix}"

        val localizationDir =
            File(resourcesGenerationDir, JvmMRGenerator.LOCALIZATION_DIR).apply { mkdirs() }
        val stringsFile = File(localizationDir, "$fileDirName.properties")

        val content = strings.map { (key, pluralMap) ->
            val keysWithPlurals = pluralMap.map { (quantity, value) ->
                "$key.$quantity" to value
            }

            keysWithPlurals.joinToString("\n") { (key, value) ->
                "$key = ${convertXmlStringToJvmLocalization(value)}"
            }
        }.joinToString("\n")

        stringsFile.writeText(content)
    }
}
