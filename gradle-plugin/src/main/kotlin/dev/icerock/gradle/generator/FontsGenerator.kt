/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.android.AndroidFontsGenerator
import dev.icerock.gradle.generator.common.CommonFontsGenerator
import dev.icerock.gradle.generator.ios.IosFontsGenerator
import org.gradle.api.file.FileTree
import java.io.File

abstract class FontsGenerator(
    private val inputFileTree: FileTree
) : MRGenerator.Generator, ObjectBodyExtendable {

    override val resourceClassName = ClassName("dev.icerock.moko.resources", "FontResource")
    override val mrObjectName: String = "fonts"

    override fun generate(resourcesGenerationDir: File, objectBuilder: TypeSpec.Builder): TypeSpec {
        val typeSpec = createTypeSpec(inputFileTree.map { it.nameWithoutExtension }.sorted(), objectBuilder)
        generateResources(resourcesGenerationDir, inputFileTree.map {
            FontFile(
                key = it.nameWithoutExtension,
                file = it
            )
        })
        return typeSpec
    }

    private fun createTypeSpec(keys: List<String>, objectBuilder: TypeSpec.Builder): TypeSpec {
        @Suppress("SpreadOperator")
        objectBuilder.addModifiers(*getClassModifiers())

        /*
        * 1. Group keys by family name (split('-').first())
        * 2. Generate subtype for each family `classBuilder.addType(...)`
        * 3. Generate properties in family subtype for each font style
        * */

        val familyGroups = keys.groupBy { key ->
            key.substringBefore("-")
        }

        familyGroups.forEach { group ->
            // TODO Make pairs: "style name" - "font file"
            val stylePairs = group
                .value
                .map { it.substringAfter("-") to it }
                .toList()

            objectBuilder.addType(
                generateFontFamilySpec(
                    familyName = group.key,
                    fontStyleFiles = stylePairs
                )
            )
        }
        extendObjectBody(objectBuilder)
        return objectBuilder.build()
    }

    override fun getImports(): List<ClassName> = emptyList()

    @Suppress("SpreadOperator")
    private fun generateFontFamilySpec(
        familyName: String,
        fontStyleFiles: List<Pair<String, String>>
    ): TypeSpec {
        val spec = TypeSpec
            .objectBuilder(familyName)
            .addModifiers(*getClassModifiers())
        fontStyleFiles
            .forEach { (styleName, fileName) ->
                val styleProperty = PropertySpec
                    .builder(styleName.decapitalize(), resourceClassName)
                    .addModifiers(*getPropertyModifiers())
                getPropertyInitializer(fileName)?.let { codeBlock ->
                    styleProperty.initializer(codeBlock)
                }
                spec.addProperty(styleProperty.build())
            }
        return spec.build()
    }

    protected open fun generateResources(
        resourcesGenerationDir: File,
        files: List<FontFile>
    ) {
    }

    abstract fun getClassModifiers(): Array<KModifier>

    abstract fun getPropertyModifiers(): Array<KModifier>

    abstract fun getPropertyInitializer(fontFileName: String): CodeBlock?

    data class FontFile(
        val key: String,
        val file: File
    )

    class Feature(private val info: SourceInfo) : ResourceGeneratorFeature<FontsGenerator> {
        private val stringsFileTree = info.commonResources.matching {
            include("MR/fonts/**.ttf")
        }

        override fun createCommonGenerator(): FontsGenerator {
            return CommonFontsGenerator(stringsFileTree)
        }

        override fun createIosGenerator(): FontsGenerator {
            return IosFontsGenerator(stringsFileTree)
        }

        override fun createAndroidGenerator(): FontsGenerator {
            return AndroidFontsGenerator(
                stringsFileTree,
                info.androidRClassPackage
            )
        }
    }
}
