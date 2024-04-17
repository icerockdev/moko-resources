/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.font

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addEmptyPlatformResourceProperty
import dev.icerock.gradle.metadata.resource.FontMetadata
import dev.icerock.gradle.utils.flatName
import java.io.File

internal class JsFontResourceGenerator(
    resourcesPackageName: String,
    private val resourcesGenerationDir: File,
) : PlatformResourceGenerator<FontMetadata> {
    private val flattenClassPackage: String = resourcesPackageName.flatName
    private val cssDeclarationsFileName: String = "$flattenClassPackage-generated-declarations.css"

    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: FontMetadata): CodeBlock {
        val requireDeclaration = """require("$FONTS_DIR/${metadata.filePath.name}")"""
        return CodeBlock.of(
            "FontResource(fileUrl = js(%S) as String, fontFamily = %S)",
            requireDeclaration,
            metadata.key
        )
    }

    override fun generateResourceFiles(data: List<FontMetadata>) {
        val fontsDir = File(resourcesGenerationDir, FONTS_DIR)
        fontsDir.mkdirs()

        data.map { it.filePath }.forEach { file ->
            file.copyTo(File(fontsDir, file.name))
        }

        val cssDeclarationsFile = File(fontsDir, cssDeclarationsFileName)

        val declarations: String = data
            .joinToString(separator = "\n") { (_, family, file) ->
                // language=css
                """
                    @font-face {
                        font-family: "$family";
                        src: url("${file.name}");
                    }
                """.trimIndent()
            }

        cssDeclarationsFile.writeText(declarations)
    }

    override fun generateBeforeProperties(
        builder: Builder,
        metadata: List<FontMetadata>,
        modifier: KModifier?,
    ) {
        builder.addEmptyPlatformResourceProperty(modifier)
    }

    override fun generateAfterProperties(
        builder: Builder,
        metadata: List<FontMetadata>,
        modifier: KModifier?
    ) {
        val languageKeysList: String = metadata.joinToString { it.key }

        val valuesFun: FunSpec = FunSpec.builder("values")
            .also {
                if (modifier != null) {
                    it.addModifiers(modifier)
                }
            }
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return listOf($languageKeysList)")
            .returns(
                ClassName("kotlin.collections", "List")
                    .parameterizedBy(Constants.fontResourceName)
            )
            .build()

        builder.addFunction(valuesFun)

        val addFontsFun: FunSpec = FunSpec.builder("addFontsToPage")
            .addCode(
                "js(%S)",
                """require("$FONTS_DIR/$cssDeclarationsFileName")"""
            ).build()
        builder.addFunction(addFontsFun)
    }

    private companion object {
        const val FONTS_DIR = "fonts"
    }
}
