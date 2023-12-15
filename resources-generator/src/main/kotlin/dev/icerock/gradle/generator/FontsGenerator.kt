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
import dev.icerock.gradle.generator.apple.AppleFontsGenerator
import dev.icerock.gradle.generator.common.CommonFontsGenerator
import dev.icerock.gradle.generator.js.JsFontsGenerator
import dev.icerock.gradle.generator.jvm.JvmFontsGenerator
import dev.icerock.gradle.metadata.GeneratedObject
import dev.icerock.gradle.metadata.GeneratorType
import dev.icerock.gradle.utils.decapitalize
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import java.io.File

abstract class FontsGenerator(
    private val inputFileTree: FileTree,
) : MRGenerator.Generator {

    override val inputFiles: Iterable<File>
        get() = inputFileTree.matching {
            it.include("fonts/**.ttf", "fonts/**.otf")
        }
    override val resourceClassName = ClassName("dev.icerock.moko.resources", "FontResource")
    override val mrObjectName: String = "fonts"

    override val type: GeneratorType = GeneratorType.Fonts

    override fun generate(
        project: Project,
        inputMetadata: MutableList<GeneratedObject>,
        generatedObjects: MutableList<GeneratedObject>,
        targetObject: GeneratedObject,
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        objectBuilder: TypeSpec.Builder,
    ): TypeSpec {
        val fontFiles = inputFileTree.map {
            FontFile(
                key = it.nameWithoutExtension,
                file = it
            )
        }

        beforeGenerateResources(objectBuilder, fontFiles)
        val typeSpec = createTypeSpec(inputFileTree.sortedBy { it.name }, objectBuilder)
        generateResources(resourcesGenerationDir, fontFiles)
        return typeSpec
    }

    /*
    @param keys: names of files like anastasia-regular.ttf
     */
    private fun createTypeSpec(files: List<File>, objectBuilder: TypeSpec.Builder): TypeSpec {
        @Suppress("SpreadOperator")
        objectBuilder.addModifiers(*getClassModifiers())

        extendObjectBodyAtStart(objectBuilder)

        /*
        * 1. Group fileNames by family name (split('-').first())
        * 2. Generate subtype for each family `classBuilder.addType(...)`
        * 3. Generate properties in family subtype for each font style
        * */

        val familyGroups = files.groupBy { file ->
            file.nameWithoutExtension.substringBefore("-")
        }

        familyGroups.forEach { group ->
            // TODO Make pairs: "style name" - "font file"
            val stylePairs: List<Pair<String, File>> = group
                .value
                .map { it.nameWithoutExtension.substringAfter("-") to it }

            objectBuilder.addType(
                generateFontFamilySpec(
                    familyName = group.key,
                    fontStyleFiles = stylePairs
                )
            )
        }
        extendObjectBodyAtEnd(objectBuilder)
        return objectBuilder.build()
    }

    override fun getImports(): List<ClassName> = emptyList()

    @Suppress("SpreadOperator")
    private fun generateFontFamilySpec(
        familyName: String,
        fontStyleFiles: List<Pair<String, File>>,
    ): TypeSpec {
        val spec = TypeSpec
            .objectBuilder(familyName)
            .addModifiers(*getClassModifiers())
        fontStyleFiles
            .forEach { (styleName, file) ->
                val styleProperty = PropertySpec
                    .builder(styleName.decapitalize(), resourceClassName)
                    .addModifiers(*getPropertyModifiers())
                getPropertyInitializer(file)?.let { codeBlock ->
                    styleProperty.initializer(codeBlock)
                }
                spec.addProperty(styleProperty.build())
            }
        return spec.build()
    }

    protected open fun generateResources(
        resourcesGenerationDir: File,
        files: List<FontFile>,
    ) {
    }

    abstract fun getClassModifiers(): Array<KModifier>

    abstract fun getPropertyModifiers(): Array<KModifier>

    abstract fun getPropertyInitializer(fontFile: File): CodeBlock?

    open fun beforeGenerateResources(objectBuilder: TypeSpec.Builder, files: List<FontFile>) {}

    data class FontFile(
        val key: String,
        val file: File,
    )

    class Feature(
        private val settings: MRGenerator.Settings,
    ) : ResourceGeneratorFeature<FontsGenerator> {
        override fun createCommonGenerator(): FontsGenerator = CommonFontsGenerator(
            ownInputFileTree = settings.ownResourcesFileTree,
            upperInputFileTree = settings.upperResourcesFileTree
        )

        override fun createAppleGenerator(): FontsGenerator = AppleFontsGenerator(
            ownInputFileTree = settings.ownResourcesFileTree,
            lowerInputFileTree = settings.lowerResourcesFileTree
        )

        override fun createAndroidGenerator(): FontsGenerator = AndroidFontsGenerator(
            ownInputFileTree = settings.ownResourcesFileTree,
            lowerInputFileTree = settings.lowerResourcesFileTree,
            androidRClassPackage = settings.androidRClassPackage
        )

        override fun createJsGenerator(): FontsGenerator = JsFontsGenerator(
            ownInputFileTree = settings.ownResourcesFileTree,
            mrClassPackage = settings.packageName
        )

        override fun createJvmGenerator(): FontsGenerator = JvmFontsGenerator(
            ownInputFileTree = settings.ownResourcesFileTree,
            lowerInputFileTree = settings.lowerResourcesFileTree,
            settings = settings
        )
    }
}
