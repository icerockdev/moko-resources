/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.jvm

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.KeyType
import dev.icerock.gradle.generator.ObjectBodyExtendable
import dev.icerock.gradle.generator.PluralMap
import dev.icerock.gradle.generator.PluralsGenerator
import dev.icerock.gradle.generator.jvm.JvmStringsGenerator.Companion.replaceAndroidFormatParameters
import org.gradle.api.file.FileTree
import java.io.File

class JvmPluralsGenerator(
    pluralsFileTree: FileTree
) : PluralsGenerator(pluralsFileTree), ObjectBodyExtendable by ClassLoaderExtender() {

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
        language: String?,
        strings: Map<KeyType, PluralMap>
    ) {
        val fileDirName = when (language) {
            null -> JvmMRGenerator.PLURALS_BUNDLE_NAME
            else -> "${JvmMRGenerator.PLURALS_BUNDLE_NAME}_$language"
        }

        val localizationDir =
            File(resourcesGenerationDir, JvmMRGenerator.LOCALIZATION_DIR).apply { mkdirs() }
        val stringsFile = File(localizationDir, "$fileDirName.properties")

        val content = strings.map { (key, pluralMap) ->
            val keysWithPlurals = pluralMap.map { (quantity, value) ->
                "$key.$quantity" to value
            }

            keysWithPlurals.joinToString("\n") { (key, value) ->
                "$key = ${value.replaceAndroidFormatParameters()}"
            }
        }.joinToString("\n")

        stringsFile.writeText(content)
    }
}
