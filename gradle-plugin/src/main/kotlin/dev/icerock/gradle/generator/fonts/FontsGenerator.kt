/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.fonts

import com.squareup.kotlinpoet.*
import dev.icerock.gradle.generator.MRGenerator
import org.gradle.api.file.FileTree
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

abstract class FontsGenerator(
    protected val sourceSet: KotlinSourceSet,
    private val inputFileTree: FileTree
) : MRGenerator.Generator {

    private val resourceClass = ClassName("dev.icerock.moko.resources", "FontResource")

    override fun generate(resourcesGenerationDir: File): TypeSpec {
        val typeSpec = createTypeSpec(inputFileTree.map { it.nameWithoutExtension }.sorted())
        generateResources(resourcesGenerationDir, inputFileTree.map { it })
        return typeSpec
    }

    private fun createTypeSpec(keys: List<String>): TypeSpec {
        val classBuilder = TypeSpec.objectBuilder("fonts")
        classBuilder.addModifiers(*getClassModifiers())

        /*
        * 1. Group keys by family name (split('-').first())
        * 2. Generate subtype for each family `classBuilder.addType(...)`
        * 3. Generate properties in family subtype for each font style
        * */

        val familyGroups = keys.groupBy { key ->
            key.substringBefore("-")
        }

        familyGroups.forEach { group ->
            //Make pairs: "style name" - "font file"
            val stylePairs = group
                .value
                .map { it.substringAfter("-") to it }
                .toList()

            classBuilder.addType(
                generateFontFamilySpec(
                    familyName = group.key,
                    fontStyleFiles = stylePairs
                )
            )
        }


        return classBuilder.build()
    }

    override fun getImports(): List<ClassName> = emptyList()

    private fun generateFontFamilySpec(
        familyName: String,
        fontStyleFiles: List<Pair<String, String>>
    ): TypeSpec {
        val spec = TypeSpec
            .objectBuilder(familyName)
            .addModifiers(*getClassModifiers())
        fontStyleFiles
            .forEach{ (styleName, fileName) ->
                val styleProperty = PropertySpec
                    .builder(styleName.decapitalize(), resourceClass)
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
        files: List<File>
    ) {
    }

    abstract fun getClassModifiers(): Array<KModifier>

    abstract fun getPropertyModifiers(): Array<KModifier>

    abstract fun getPropertyInitializer(fontFileName: String): CodeBlock?
}
