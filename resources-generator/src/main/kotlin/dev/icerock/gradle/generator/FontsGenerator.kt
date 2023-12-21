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
import dev.icerock.gradle.metadata.model.GeneratedObject
import dev.icerock.gradle.metadata.model.GeneratedObjectModifier
import dev.icerock.gradle.metadata.model.GeneratedProperty
import dev.icerock.gradle.metadata.model.GeneratorType
import dev.icerock.gradle.metadata.addActual
import dev.icerock.gradle.metadata.getActualInterfaces
import dev.icerock.gradle.metadata.objectsWithProperties
import dev.icerock.gradle.utils.decapitalize
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import java.io.File

abstract class FontsGenerator(
    private val resourcesFileTree: FileTree,
) : MRGenerator.Generator {

    override val inputFiles: Iterable<File>
        get() = resourcesFileTree.matching {
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
    ): TypeSpec? {
        val previousFontFiles: List<FontFile> = getPreviousFontFiles(
            inputMetadata = inputMetadata,
            targetObject = targetObject
        )

        val fontFiles: List<FontFile> = if (
            targetObject.isActualObject || targetObject.isTargetObject
        ) {
            emptyList()
        } else {
            inputFiles.getFontFiles()
        }

        // Sum of all fonts files and filter his on unical fonts
        val allFontFiles: List<FontFile> = (previousFontFiles + fontFiles).distinctBy { it.key }

        beforeGenerateResources(objectBuilder, allFontFiles)

        val typeSpec = createTypeSpec(
            inputMetadata = inputMetadata,
            generatedObjects = generatedObjects,
            targetObject = targetObject,
            filesSpec = allFontFiles.sortedBy { it.key },
            objectBuilder = objectBuilder
        )

        generateResources(resourcesGenerationDir, allFontFiles)

        return typeSpec
    }

    private fun getPreviousFontFiles(
        inputMetadata: List<GeneratedObject>,
        targetObject: GeneratedObject,
    ): List<FontFile> {
        if (!targetObject.isObject || !targetObject.isActual) return emptyList()

        val json = Json
        val objectsWithProperties: List<GeneratedObject> = inputMetadata.objectsWithProperties(
            targetObject = targetObject
        )

        val fontFiles: MutableList<File> = mutableListOf()

        objectsWithProperties.forEach { generatedObject ->
            generatedObject.properties.forEach { property ->
                val data = json.decodeFromJsonElement<Map<String, JsonPrimitive>>(property.data)

                data.forEach {
                    fontFiles.add(File(it.value.content))
                }
            }
        }

        return fontFiles.getFontFiles()
    }

    private fun Iterable<File>.getFontFiles(): List<FontFile> {
        return this.map {
            FontFile(
                key = it.nameWithoutExtension,
                file = it
            )
        }
    }

    /*
    @param keys: names of files like anastasia-regular.ttf
     */
    private fun createTypeSpec(
        inputMetadata: MutableList<GeneratedObject>,
        generatedObjects: MutableList<GeneratedObject>,
        targetObject: GeneratedObject,
        filesSpec: List<FontFile>,
        objectBuilder: TypeSpec.Builder,
    ): TypeSpec? {
        if (targetObject.isActual) {
            objectBuilder.addModifiers(KModifier.ACTUAL)
        }

        if (targetObject.isActualObject || targetObject.isTargetObject) {
            extendObjectBodyAtStart(objectBuilder)
        }

        /*
        * 1. Group fileNames by family name (split('-').first())
        * 2. Generate subtype for each family `classBuilder.addType(...)`
        * 3. Generate properties in family subtype for each font style
        * */

        val familyGroups: Map<String, List<FontFile>> = filesSpec.groupBy { fileSpec ->
            fileSpec.file.nameWithoutExtension.substringBefore("-")
        }

        val generatedProperties = mutableListOf<GeneratedProperty>()

        familyGroups.forEach { group ->
            // Make pairs: "style name" - "font file"
            val stylePairs: List<Pair<String, FontFile>> = group
                .value
                .map { it.file.nameWithoutExtension.substringAfter("-") to it }

            val propertyName: String = group.key
            val property: TypeSpec.Builder = TypeSpec.objectBuilder(propertyName)

            var generatedProperty = GeneratedProperty(
                modifier = addObjectActualOverrideModifier(
                    propertyName = propertyName,
                    property = property,
                    inputMetadata = inputMetadata,
                    targetObject = targetObject
                ),
                name = propertyName,
                data = JsonPrimitive("")
            )

            val fontsProperty: MutableMap<String, JsonPrimitive> = mutableMapOf()

            stylePairs.map {
                Pair(it.first, it.second.file)
            }.forEach { (styleName, file) ->
                val styleProperty: PropertySpec.Builder = PropertySpec
                    .builder(styleName.decapitalize(), resourceClassName)

                if (generatedProperty.isActualProperty){
                    styleProperty.addModifiers(KModifier.ACTUAL)
                }

                if (targetObject.isActualObject || targetObject.isTargetObject) {
                    getPropertyInitializer(file)?.let { codeBlock ->
                        styleProperty.initializer(codeBlock)
                    }
                }

                fontsProperty[styleName] = JsonPrimitive(file.path)

                property.addProperty(styleProperty.build())
                generatedProperty = generatedProperty.copy(
                    data = JsonObject(fontsProperty)
                )
            }

            objectBuilder.addType(property.build())
            generatedProperties.add(generatedProperty)
        }

        extendObjectBodyAtEnd(objectBuilder)

        return if (generatedProperties.isNotEmpty()) {
            // Add object in metadata with remove expect realisation
            generatedObjects.addActual(
                targetObject.copy(properties = generatedProperties)
            )

            objectBuilder.build()
        } else {
            null
        }
    }

    private fun addObjectActualOverrideModifier(
        propertyName: String,
        property: TypeSpec.Builder,
        inputMetadata: List<GeneratedObject>,
        targetObject: GeneratedObject,
    ): GeneratedObjectModifier {
        // Read actual interfaces of target object generator type
        val actualInterfaces: List<GeneratedObject> = inputMetadata.getActualInterfaces(
            generatorType = targetObject.generatorType
        )

        var containsInActualInterfaces = false

        // Search property in actual interfaces
        actualInterfaces.forEach { genInterface ->
            val hasInInterface = genInterface.properties.any {
                it.name == propertyName
            }

            if (hasInInterface) {
                containsInActualInterfaces = true
            }
        }

        return if (targetObject.isObject) {
            if (containsInActualInterfaces) {
                property.addModifiers(KModifier.OVERRIDE)
                GeneratedObjectModifier.Override
            } else {
                when (targetObject.modifier) {
                    GeneratedObjectModifier.Actual -> {
                        property.addModifiers(KModifier.ACTUAL)
                        GeneratedObjectModifier.Actual
                    }
                    else -> {
                        GeneratedObjectModifier.None
                    }
                }
            }
        } else {
            GeneratedObjectModifier.None
        }

    }

    override fun getImports(): List<ClassName> = emptyList()

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
        )

        override fun createAppleGenerator(): FontsGenerator = AppleFontsGenerator(
            ownInputFileTree = settings.ownResourcesFileTree,
        )

        override fun createAndroidGenerator(): FontsGenerator = AndroidFontsGenerator(
            ownInputFileTree = settings.ownResourcesFileTree,
            androidRClassPackage = settings.androidRClassPackage
        )

        override fun createJsGenerator(): FontsGenerator = JsFontsGenerator(
            ownInputFileTree = settings.ownResourcesFileTree,
            mrClassPackage = settings.packageName
        )

        override fun createJvmGenerator(): FontsGenerator = JvmFontsGenerator(
            ownInputFileTree = settings.ownResourcesFileTree,
            settings = settings
        )
    }
}
