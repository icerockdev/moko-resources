/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.color

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addEmptyPlatformResourceProperty
import dev.icerock.gradle.generator.addValuesFunction
import dev.icerock.gradle.metadata.resource.ColorMetadata
import java.io.File

internal class AndroidColorResourceGenerator(
    private val androidRClassPackage: String,
    private val resourcesGenerationDir: File,
) : PlatformResourceGenerator<ColorMetadata> {
    override fun imports(): List<ClassName> = listOf(
        ClassName(androidRClassPackage, "R")
    )

    override fun generateInitializer(metadata: ColorMetadata): CodeBlock {
        return CodeBlock.of("ColorResource(R.color.%L)", metadata.key)
    }

    override fun generateBeforeProperties(
        parentObjectName: String,
        builder: Builder,
        metadata: List<ColorMetadata>,
        modifier: KModifier?,
    ) {
        builder.addEmptyPlatformResourceProperty(modifier)
    }

    override fun generateAfterProperties(
        builder: Builder,
        metadata: List<ColorMetadata>,
        modifier: KModifier?,
    ) {
        builder.addValuesFunction(
            modifier = modifier,
            metadata = metadata,
            classType = Constants.colorResourceName
        )
    }

    override fun generateResourceFiles(data: List<ColorMetadata>) {
        val valuesDir = File(resourcesGenerationDir, "values")
        val defaultStringsFile = File(valuesDir, COLORS_XML_FILE_NAME)
        valuesDir.mkdirs()

        val valuesNightDir = File(resourcesGenerationDir, "values-night")
        val darkStringsFile = File(valuesNightDir, COLORS_XML_FILE_NAME)
        valuesNightDir.mkdirs()

        val header =
            """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
            """.trimIndent()

        val footer =
            """
            </resources>
            """.trimIndent()

        val defaultContent: String = data.joinToString("\n") { metadata ->
            when (metadata.value) {
                is ColorMetadata.ColorItem.Single ->
                    buildColorString(metadata.key, metadata.value.color)

                is ColorMetadata.ColorItem.Themed ->
                    buildColorString(metadata.key, metadata.value.light)
            }
        }

        val darkContent: String = data
            .filter { it.value is ColorMetadata.ColorItem.Themed }
            .joinToString("\n") { metadata ->
                val value: ColorMetadata.ColorItem.Themed =
                    (metadata.value as ColorMetadata.ColorItem.Themed)
                buildColorString(metadata.key, value.dark)
            }

        defaultStringsFile.writeText(header + "\n")
        defaultStringsFile.appendText(defaultContent)
        defaultStringsFile.appendText("\n" + footer)

        darkStringsFile.writeText(header + "\n")
        darkStringsFile.appendText(darkContent)
        darkStringsFile.appendText("\n" + footer)
    }

    private fun buildColorString(name: String, color: ColorMetadata.Color): String {
        val argbString: String = color.toArgbHex()
        return "\t<color name=\"$name\">#$argbString</color>"
    }

    private companion object {
        const val COLORS_XML_FILE_NAME = "colors.xml"
    }
}
