///*
// * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
// */
//
//package dev.icerock.gradle.generator
//
//import com.squareup.kotlinpoet.ClassName
//import com.squareup.kotlinpoet.CodeBlock
//import com.squareup.kotlinpoet.KModifier
//import com.squareup.kotlinpoet.PropertySpec
//import com.squareup.kotlinpoet.TypeSpec
//import dev.icerock.gradle.generator.android.AndroidFilesGenerator
//import dev.icerock.gradle.generator.apple.AppleFilesGenerator
//import dev.icerock.gradle.generator.common.CommonFilesGenerator
//import dev.icerock.gradle.generator.js.JsFilesGenerator
//import dev.icerock.gradle.generator.jvm.JvmFilesGenerator
//import dev.icerock.gradle.metadata.model.GeneratedObject
//import dev.icerock.gradle.metadata.model.GeneratedObjectModifier
//import dev.icerock.gradle.metadata.model.GeneratedProperty
//import dev.icerock.gradle.metadata.model.GeneratorType
//import dev.icerock.gradle.metadata.objectsWithProperties
//import kotlinx.serialization.json.Json
//import kotlinx.serialization.json.JsonPrimitive
//import kotlinx.serialization.json.decodeFromJsonElement
//import org.gradle.api.Project
//import org.gradle.api.file.FileTree
//import java.io.File
//
//abstract class FilesGenerator(
//    private val resourceFiles: FileTree,
//) : MRGenerator.Generator {
//
//    override val inputFiles: Iterable<File>
//        get() = resourceFiles.matching { it.include("files/**") }
//    override val resourceClassName = ClassName("dev.icerock.moko.resources", "FileResource")
//    override val mrObjectName: String = "files"
//
//    override val type: GeneratorType = GeneratorType.Files
//
//    override fun generateObject(
//        project: Project,
//        inputMetadata: List<GeneratedObject>,
//        outputMetadata: GeneratedObject,
//        assetsGenerationDir: File,
//        resourcesGenerationDir: File,
//        objectBuilder: TypeSpec.Builder
//    ): MRGenerator.GenerationResult? {
//        val previousFilesSpec: List<FileSpec> = getPreviousFiles(
//            inputMetadata = inputMetadata,
//            targetObject = outputMetadata
//        )
//
//        val targetFilesSpecs: List<FileSpec> = if (
//            outputMetadata.isActualObject || outputMetadata.isTargetObject
//        ) {
//            emptyList()
//        } else {
//            inputFiles.getFileSpecList()
//        }
//
//        val allFilesSpecs = (previousFilesSpec + targetFilesSpecs).distinctBy { it.key }
//
//        beforeGenerate(objectBuilder, allFilesSpecs)
//
//        val result: MRGenerator.GenerationResult = createTypeSpec(
//            inputMetadata = inputMetadata,
//            targetObject = outputMetadata,
//            keys = allFilesSpecs,
//            objectBuilder = objectBuilder
//        ) ?: return null
//
//        generateResources(
//            resourcesGenerationDir = resourcesGenerationDir,
//            files = allFilesSpecs
//        )
//
//        return result
//    }
//
//    private fun Iterable<File>.getFileSpecList(): List<FileSpec> {
//        return this.map { file ->
//            FileSpec(
//                key = processKey(file.nameWithoutExtension),
//                file = file
//            )
//        }.sortedBy { it.key }
//    }
//
//    private fun getPreviousFiles(
//        inputMetadata: List<GeneratedObject>,
//        targetObject: GeneratedObject,
//    ): List<FileSpec> {
//        if (!targetObject.isObject || !targetObject.isActual) return emptyList()
//
//        val json = Json
//        val objectsWithProperties: List<GeneratedObject> = inputMetadata.objectsWithProperties(
//            targetObject = targetObject
//        )
//
//        val files = mutableListOf<File>()
//
//        objectsWithProperties.forEach { generatedObject ->
//            generatedObject.properties.forEach { property ->
//                val data = json.decodeFromJsonElement<JsonPrimitive>(property.data)
//                files.add(
//                    File(data.content)
//                )
//            }
//        }
//
//        return files.getFileSpecList()
//    }
//
//    private fun createTypeSpec(
//        inputMetadata: List<GeneratedObject>,
//        targetObject: GeneratedObject,
//        keys: List<FileSpec>,
//        objectBuilder: TypeSpec.Builder,
//    ): MRGenerator.GenerationResult? {
//        if (targetObject.isActual) {
//            objectBuilder.addModifiers(KModifier.ACTUAL)
//        }
//
//        if (targetObject.isActualObject || targetObject.isTargetObject) {
//            extendObjectBodyAtStart(objectBuilder)
//        }
//
//        val generatedProperties = mutableListOf<GeneratedProperty>()
//
//        keys.forEach { fileSpec ->
//            val property = PropertySpec.builder(fileSpec.key, resourceClassName)
//
//            var generatedProperty = GeneratedProperty(
//                modifier = GeneratedObjectModifier.None,
//                name = fileSpec.key,
//                data = JsonPrimitive(fileSpec.file.path)
//            )
//
//            if (targetObject.isActualObject || targetObject.isTargetObject) {
//                // Add modifier for property and setup metadata
//                generatedProperty = generatedProperty.copy(
//                    modifier = addActualOverrideModifier(
//                        propertyName = fileSpec.key,
//                        property = property,
//                        inputMetadata = inputMetadata,
//                        targetObject = targetObject
//                    )
//                )
//
//                getPropertyInitializer(fileSpec)?.let {
//                    property.initializer(it)
//                }
//            }
//
//            objectBuilder.addProperty(property.build())
//            generatedProperties.add(generatedProperty)
//        }
//
//        extendObjectBodyAtEnd(objectBuilder)
//
//        if (generatedProperties.isEmpty()) return null
//
//        return MRGenerator.GenerationResult(
//            typeSpec = objectBuilder.build(),
//            metadata = targetObject.copy(properties = generatedProperties)
//        )
//    }
//
//    override fun getImports(): List<ClassName> = emptyList()
//
//    protected open fun beforeGenerate(
//        objectBuilder: TypeSpec.Builder,
//        files: List<FileSpec>,
//    ) = Unit
//
//    protected open fun generateResources(
//        resourcesGenerationDir: File,
//        files: List<FileSpec>,
//    ) = Unit
//
//    private fun processKey(key: String): String {
//        return key.replace("-", "_")
//    }
//
//    abstract fun getClassModifiers(): Array<KModifier>
//
//    abstract fun getPropertyModifiers(): Array<KModifier>
//
//    abstract fun getPropertyInitializer(fileSpec: FileSpec): CodeBlock?
//
//    data class FileSpec(
//        val key: String,
//        val file: File,
//    )
//
//    class Feature(
//        private val settings: MRGenerator.Settings,
//    ) : ResourceGeneratorFeature<FilesGenerator> {
//
//        override fun createCommonGenerator(): FilesGenerator = CommonFilesGenerator(
//            ownInputFileTree = settings.ownResourcesFileTree,
//        )
//
//        override fun createAppleGenerator(): FilesGenerator = AppleFilesGenerator(
//            ownInputFileTree = settings.ownResourcesFileTree,
//        )
//
//        override fun createAndroidGenerator(): FilesGenerator = AndroidFilesGenerator(
//            ownInputFileTree = settings.ownResourcesFileTree,
//            androidRClassPackage = settings.androidRClassPackage,
//        )
//
//        override fun createJsGenerator(): FilesGenerator = JsFilesGenerator(
//            ownInputFileTree = settings.ownResourcesFileTree,
//        )
//
//        override fun createJvmGenerator(): FilesGenerator = JvmFilesGenerator(
//            ownInputFileTree = settings.ownResourcesFileTree,
//            settings = settings
//        )
//    }
//}
