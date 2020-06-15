/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.ios

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.ios.IosMRGenerator.Companion.BUNDLE_PROPERTY_NAME
import dev.icerock.gradle.generator.KeyType
import dev.icerock.gradle.generator.StringsGenerator
import org.gradle.api.file.FileTree
import java.io.File

class IosStringsGenerator(
    stringsFileTree: FileTree,
    private val baseLocalizationRegion: String
) : StringsGenerator(
    stringsFileTree = stringsFileTree
) {
    private val iosGeneratorHelper = IosGeneratorHelper()

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(key: String): CodeBlock? {
        return CodeBlock.of("StringResource(resourceId = %S, bundle = $BUNDLE_PROPERTY_NAME)", key)
    }

    override fun generateResources(
        resourcesGenerationDir: File,
        language: String?,
        strings: Map<KeyType, String>
    ) {
        val resDirName = when (language) {
            null -> "Base.lproj"
            else -> "$language.lproj"
        }

        val resDir = File(resourcesGenerationDir, resDirName)
        val localizableFile = File(resDir, "Localizable.strings")
        resDir.mkdirs()

        val content = strings.map { (key, value) ->
            "\"$key\" = \"$value\";"
        }.joinToString("\n")
        localizableFile.writeText(content)

        if (language == null) {
            val regionDir = File(resourcesGenerationDir, "$baseLocalizationRegion.lproj")
            regionDir.mkdirs()
            val regionFile = File(regionDir, "Localizable.strings")
            regionFile.writeText(content)
        }
    }

    override fun extendObjectBody(classBuilder: TypeSpec.Builder) {
        iosGeneratorHelper.addBundlePropertyTo(classBuilder)
    }
}
