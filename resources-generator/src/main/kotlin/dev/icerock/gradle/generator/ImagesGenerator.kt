/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.android.AndroidImagesGenerator
import dev.icerock.gradle.generator.apple.AppleImagesGenerator
import dev.icerock.gradle.generator.common.CommonImagesGenerator
import dev.icerock.gradle.generator.js.JsImagesGenerator
import dev.icerock.gradle.generator.jvm.JvmImagesGenerator
import dev.icerock.gradle.metadata.addActual
import dev.icerock.gradle.metadata.model.GeneratedObject
import dev.icerock.gradle.metadata.model.GeneratedObjectModifier
import dev.icerock.gradle.metadata.model.GeneratedProperty
import dev.icerock.gradle.metadata.model.GeneratorType
import dev.icerock.gradle.metadata.objectsWithProperties
import dev.icerock.gradle.utils.withoutScale
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.logging.Logger
import java.io.File

abstract class ImagesGenerator(
    private val resourcesFileTree: FileTree,
) : MRGenerator.Generator {

    override val inputFiles: Iterable<File>
        get() = resourcesFileTree.matching {
            it.include("images/**/*.png", "images/**/*.jpg", "images/**/*.svg")
        }

    override val resourceClassName = ClassName("dev.icerock.moko.resources", "ImageResource")
    override val mrObjectName: String = "images"

    override val type: GeneratorType = GeneratorType.Images

    override fun generate(
        project: Project,
        inputMetadata: MutableList<GeneratedObject>,
        generatedObjects: MutableList<GeneratedObject>,
        targetObject: GeneratedObject,
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        objectBuilder: TypeSpec.Builder,
    ): TypeSpec? {
        val previousFilesMap: Map<String, List<File>> = getPreviousImageFilesMap(
            inputMetadata = inputMetadata,
            targetObject = targetObject
        )

        val imageFileMap: Map<String, List<File>> = if (
            targetObject.isActualObject || targetObject.isTargetObject
        ) {
            emptyMap()
        } else {
            inputFiles.getImageMap()
        }


        val allImagesMap: Map<String, List<File>> = getAllImagesMap(previousFilesMap, imageFileMap)

        beforeGenerateResources(objectBuilder, allImagesMap.keys.sorted())

        val typeSpec: TypeSpec? = createTypeSpec(
            inputMetadata = inputMetadata,
            generatedObjects = generatedObjects,
            targetObject = targetObject,
            fileNames = allImagesMap.keys.sorted(),
            allImagesMap = allImagesMap,
            objectBuilder = objectBuilder
        )

        generateResources(
            assetsGenerationDir = assetsGenerationDir,
            resourcesGenerationDir = resourcesGenerationDir,
            keyFileMap = allImagesMap.mapKeys { (key, _) ->
                key.substringBeforeLast(".") // Remove file extension from keys
            }
        )

        return typeSpec
    }

    private fun createTypeSpec(
        inputMetadata: MutableList<GeneratedObject>,
        generatedObjects: MutableList<GeneratedObject>,
        targetObject: GeneratedObject,
        fileNames: List<String>,
        allImagesMap: Map<String, List<File>>,
        objectBuilder: TypeSpec.Builder,
    ): TypeSpec? {
        if (targetObject.isActual) {
            objectBuilder.addModifiers(KModifier.ACTUAL)
        }

        if (targetObject.isActualObject || targetObject.isTargetObject) {
            extendObjectBodyAtStart(objectBuilder)
        }

        val generatedProperties = mutableListOf<GeneratedProperty>()

        fileNames.forEach { fileName: String ->
            val updatedFileName = fileName.substringBeforeLast(".")
                .replace(".", "_") + ".${fileName.substringAfterLast(".")}"
            val propertyName = updatedFileName.substringBeforeLast(".")
            val property = PropertySpec.builder(propertyName, resourceClassName)

            var generatedProperty = GeneratedProperty(
                modifier = GeneratedObjectModifier.None,
                name = propertyName,
                data = JsonObject(
                    content = getPropertyMetadata(
                        fileName = fileName,
                        allImagesMap = allImagesMap
                    )
                )
            )

            if (targetObject.isActualObject || targetObject.isTargetObject) {
                // Add modifier for property and setup metadata
                generatedProperty = generatedProperty.copy(
                    modifier = addActualOverrideModifier(
                        propertyName = propertyName,
                        property = property,
                        inputMetadata = inputMetadata,
                        targetObject = targetObject
                    )
                )

                getPropertyInitializer(updatedFileName)?.let {
                    property.initializer(it)
                }
            }

            objectBuilder.addProperty(property.build())
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

    private fun getPropertyMetadata(
        fileName: String,
        allImagesMap: Map<String, List<File>>,
    ): Map<String, JsonElement> {
        //Structure: FileName, Path
        val resultMap = mutableMapOf<String, JsonPrimitive>()

        allImagesMap[fileName]?.forEach {
            resultMap[it.name] = JsonPrimitive(it.path)
        }

        return resultMap
    }

    private fun getPreviousImageFilesMap(
        inputMetadata: List<GeneratedObject>,
        targetObject: GeneratedObject,
    ): Map<String, List<File>> {
        if (!targetObject.isObject || !targetObject.isActual) return emptyMap()

        val json = Json
        val objectsWithProperties: List<GeneratedObject> = inputMetadata.objectsWithProperties(
            targetObject = targetObject
        )

        val fileImage = mutableListOf<File>()

        objectsWithProperties.forEach { generatedObject ->
            generatedObject.properties.forEach { property ->
                val data = json.decodeFromJsonElement<Map<String, JsonPrimitive>>(property.data)

                data.forEach {
                    fileImage.add(File(it.value.content))
                }
            }
        }

        return fileImage.getImageMap()
    }

    private fun Iterable<File>.getImageMap(): Map<String, List<File>> {
        return this.groupBy { file ->
            // SVGs do not have scale suffixes, so we need to remove the extension first
            val key = file
                .nameWithoutExtension
                .withoutScale

            "$key.${file.extension}"
        }
    }

    private fun getAllImagesMap(
        previousFilesMap: Map<String, List<File>>,
        imageFileMap: Map<String, List<File>>,
    ): Map<String, List<File>> {
        val resultMap = mutableMapOf<String, List<File>>()

        previousFilesMap.forEach { map ->
            resultMap[map.key] = map.value
        }

        imageFileMap.forEach { map ->
            resultMap[map.key] = map.value
        }

        return resultMap
    }

    override fun getImports(): List<ClassName> = emptyList()

    protected open fun beforeGenerateResources(
        objectBuilder: TypeSpec.Builder,
        keys: List<String>,
    ) = Unit

    protected open fun generateResources(
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        keyFileMap: Map<String, List<File>>,
    ) = Unit

    abstract fun getClassModifiers(): Array<KModifier>

    abstract fun getPropertyModifiers(): Array<KModifier>

    abstract fun getPropertyInitializer(fileName: String): CodeBlock?

    class Feature(
        private val settings: MRGenerator.Settings,
        private val logger: Logger,
    ) : ResourceGeneratorFeature<ImagesGenerator> {
        override fun createCommonGenerator(): ImagesGenerator = CommonImagesGenerator(
            ownInputFileTree = settings.ownResourcesFileTree,
        )

        override fun createAppleGenerator(): ImagesGenerator = AppleImagesGenerator(
            ownInputFileTree = settings.ownResourcesFileTree,
        )

        override fun createAndroidGenerator(): ImagesGenerator = AndroidImagesGenerator(
            ownInputFileTree = settings.ownResourcesFileTree,
            androidRClassPackage = settings.androidRClassPackage,
            logger = logger
        )

        override fun createJsGenerator(): ImagesGenerator = JsImagesGenerator(
            ownInputFileTree = settings.ownResourcesFileTree,
        )

        override fun createJvmGenerator(): ImagesGenerator = JvmImagesGenerator(
            ownInputFileTree = settings.ownResourcesFileTree,
            settings = settings
        )
    }
}
