/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.js

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.FontsGenerator
import org.gradle.api.file.FileTree
import java.io.File

class JsFontsGenerator(
    inputFileTree: FileTree,
    mrClassPackage: String,
) : FontsGenerator(inputFileTree) {

    private val flattenPackage: String = mrClassPackage.replace(".", "")
    private val cssDeclarationsFileName: String = "$flattenPackage-generated-declarations.css"

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    @Suppress("StringTemplate")
    override fun getPropertyInitializer(fontFile: File): CodeBlock {
        return CodeBlock.of("FontResource(" +
                "fileUrl = js(\"require(\\\"$FONTS_DIR/${fontFile.name}\\\")\") as String, " +
                "fontFamily = %S)", fontFile.nameWithoutExtension)
    }

    override fun beforeGenerateResources(objectBuilder: TypeSpec.Builder, files: List<FontFile>) {
        if (files.isEmpty()) return

        objectBuilder.addSuperinterface(ClassName("dev.icerock.moko.resources", "CssDeclarationsUriHolder"))

        objectBuilder.addProperty(
            PropertySpec.builder("cssDeclarationsUri", STRING, KModifier.OVERRIDE)
                .initializer("js(%S) as String", """require("$FONTS_DIR/$cssDeclarationsFileName")""")
                .build()
        )
    }

    override fun generateResources(resourcesGenerationDir: File, files: List<FontFile>) {
        val fontsDir = File(resourcesGenerationDir, FONTS_DIR).apply { mkdirs() }

        files.forEach { (_, file) ->
            file.copyTo(File(fontsDir, file.name))
        }

        val cssDeclarationsFile = File(fontsDir, cssDeclarationsFileName)

        val declarations = files
            .takeIf(List<*>::isNotEmpty)
            ?.joinToString(separator = "\n") { (family, file) ->
                // language=css
                """
                    @font-face {
                        font-family: "$family";
                        src: url("$FONTS_DIR/${file.name}");
                    }
                """.trimIndent()
            }

        if (declarations != null) cssDeclarationsFile.writeText(declarations)
    }

    override fun extendObjectBodyAtStart(classBuilder: TypeSpec.Builder) = Unit

    override fun extendObjectBodyAtEnd(classBuilder: TypeSpec.Builder) = Unit

    companion object {
        const val FONTS_DIR = "fonts"
    }
}
