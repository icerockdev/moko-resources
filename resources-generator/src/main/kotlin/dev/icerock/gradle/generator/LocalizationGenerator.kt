///*
// * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
// */
//
//package dev.icerock.gradle.generator
//
//import com.squareup.kotlinpoet.CodeBlock
//import com.squareup.kotlinpoet.KModifier
//import com.squareup.kotlinpoet.PropertySpec
//import com.squareup.kotlinpoet.PropertySpec.Builder
//import com.squareup.kotlinpoet.TypeSpec
//import dev.icerock.gradle.metadata.getInterfaceName
//import dev.icerock.gradle.metadata.model.GeneratedObject
//import dev.icerock.gradle.metadata.model.GeneratedObjectModifier
//import dev.icerock.gradle.metadata.model.GeneratedObjectType
//import dev.icerock.gradle.metadata.model.GeneratedProperty
//import dev.icerock.gradle.metadata.model.GeneratorType
//import kotlinx.serialization.json.JsonElement
//import kotlinx.serialization.json.JsonObject
//import org.gradle.api.Project
//import java.io.File
//
//abstract class LocalizationGenerator<T> : MRGenerator.Generator {
//
//    override fun generateObject(
//        project: Project,
//        inputMetadata: List<GeneratedObject>,
//        outputMetadata: GeneratedObject,
//        assetsGenerationDir: File,
//        resourcesGenerationDir: File,
//        objectBuilder: TypeSpec.Builder,
//    ): MRGenerator.GenerationResult? {
//        // Read previous languages map from metadata
//        // if target object is expect object or interface return emptyMap()
//        val previousLanguagesMap: Map<LanguageType, Map<KeyType, T>> = getPreviousLanguagesMap(
//            inputMetadata = inputMetadata,
//            targetObject = outputMetadata
//        )
//
//        // Read actual resources of target
//        // If target object is actual object: skip read files again
//        //
//        // Structure: language - key - value
//        val languageMap: Map<LanguageType, Map<KeyType, T>> =
//            if (outputMetadata.isActualObject || outputMetadata.isTargetObject) {
//                emptyMap()
//            } else {
//                loadLanguageMap()
//            }
//
//        // Sum of previous and target language fields
//        val languagesAllMaps: Map<LanguageType, Map<KeyType, T>> = getLanguagesAllMaps(
//            previousLanguageMaps = previousLanguagesMap,
//            languageMap = languageMap
//        )
//        val languageKeyValues: Map<KeyType, T> = languagesAllMaps[LanguageType.Base].orEmpty()
//
//        beforeGenerateResources(objectBuilder, languagesAllMaps)
//
//        val result: MRGenerator.GenerationResult = createTypeSpec(
//            inputMetadata = inputMetadata,
//            targetObject = outputMetadata,
//            keys = languageKeyValues.keys.toList(),
//            languageMap = languagesAllMaps,
//            objectBuilder = objectBuilder
//        ) ?: return null
//
//        languagesAllMaps.forEach { (language: LanguageType, strings: Map<KeyType, T>) ->
//            generateResources(
//                resourcesGenerationDir = resourcesGenerationDir,
//                language = language,
//                strings = strings
//            )
//        }
//
//        return result
//    }
//
//    private fun createTypeSpec(
//        inputMetadata: List<GeneratedObject>,
//        targetObject: GeneratedObject,
//        keys: List<KeyType>,
//        languageMap: Map<LanguageType, Map<KeyType, T>>,
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
//        keys.forEach { key ->
//            val name = key.replace(".", "_")
//
//            //Create metadata property
//            var generatedProperty = GeneratedProperty(
//                modifier = GeneratedObjectModifier.None,
//                name = name,
//                data = JsonObject(
//                    content = getPropertyMetadata(
//                        key = key,
//                        languageMap = languageMap
//                    )
//                )
//            )
//
//            val property: Builder = PropertySpec.builder(name, resourceClassName)
//
//            if (targetObject.isActualObject || targetObject.isTargetObject) {
//                // Add modifier for property and setup metadata
//                generatedProperty = generatedProperty.copy(
//                    modifier = addActualOverrideModifier(
//                        propertyName = name,
//                        property = property,
//                        inputMetadata = inputMetadata,
//                        targetObject = targetObject
//                    )
//                )
//
//                getPropertyInitializer(key)?.let {
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
//        // Add object in metadata with remove expect realisation
//        return MRGenerator.GenerationResult(
//            typeSpec = objectBuilder.build(),
//            metadata = targetObject.copy(properties = generatedProperties)
//        )
//    }
//
//    abstract fun getPropertyMetadata(
//        key: KeyType,
//        languageMap: Map<LanguageType, Map<KeyType, T>>,
//    ): Map<String, JsonElement>
//
//    abstract fun getLanguagesAllMaps(
//        previousLanguageMaps: Map<LanguageType, Map<KeyType, T>>,
//        languageMap: Map<LanguageType, Map<KeyType, T>>
//    ): Map<LanguageType, Map<KeyType, T>>
//
//    abstract fun getPreviousLanguagesMap(
//        inputMetadata: List<GeneratedObject>,
//        targetObject: GeneratedObject,
//    ): Map<LanguageType, Map<KeyType, T>>
//
//    protected abstract fun loadLanguageMap(): Map<LanguageType, Map<KeyType, T>>
//    protected abstract fun getPropertyInitializer(key: String): CodeBlock?
//
//    protected open fun getClassModifiers(): Array<KModifier> = emptyArray()
//    protected open fun getPropertyModifiers(): Array<KModifier> = emptyArray()
//
//    protected open fun beforeGenerateResources(
//        objectBuilder: TypeSpec.Builder,
//        languageMap: Map<LanguageType, Map<KeyType, T>>,
//    ) = Unit
//
//    protected open fun generateResources(
//        resourcesGenerationDir: File,
//        language: LanguageType,
//        strings: Map<KeyType, T>,
//    ) = Unit
//
//    protected companion object {
//        const val BASE_LANGUAGE = "base"
//    }
//}
//
//internal class CommonLocalizationInterfaceGenerator(
//    private val sourceSetName: String,
//    private val filesRegex: Regex,
//    private val generatorType: GeneratorType,
//    private val visibilityModifier: KModifier
//) : MRGenerator.InterfaceGenerator {
//    override fun generateInterface(
//        project: Project,
//        metadata: List<GeneratedObject>,
//        resourcesFiles: MRGenerator.ResourcesFiles
//    ): MRGenerator.GenerationResult? {
//        if (resourcesFiles.lowerResourcesFileTree.isEmpty.not()) {
//            // we should generate actual interfaces for own resources
//            return generateActualInterface(project, metadata, resourcesFiles)
//        }
//
//        return generateExpectInterface(project, metadata, resourcesFiles)
//    }
//
//    private fun generateActualInterface(
//        project: Project,
//        metadata: List<GeneratedObject>,
//        resourcesFiles: MRGenerator.ResourcesFiles
//    ): MRGenerator.GenerationResult? {
//        val interfaceName: String = getInterfaceName(
//            sourceSetName = sourceSetName,
//            generatorType = generatorType
//        )
//
//        val resourcesInterfaceBuilder: TypeSpec.Builder =
//            TypeSpec.interfaceBuilder(interfaceName)
//                .addModifiers(visibilityModifier)
//                .addModifiers(KModifier.ACTUAL)
//
//        generator.generateObject(
//            project = project,
//            metadata = inputMetadata,
//            outputMetadata = GeneratedObject(
//                generatorType = generator.type,
//                name = interfaceName,
//                type = GeneratedObjectType.Interface,
//                modifier = GeneratedObjectModifier.Actual,
//            ),
//            assetsGenerationDir = assetsGenerationDir,
//            resourcesGenerationDir = resourcesGenerationDir,
//            objectBuilder = resourcesInterfaceBuilder,
//        )
//    }
//
//    private fun generateExpectInterface(
//        project: Project,
//        metadata: List<GeneratedObject>,
//        resourcesFiles: MRGenerator.ResourcesFiles
//    ): MRGenerator.GenerationResult? {
//        if (resourcesFiles.upperResourcesFileTree.isEmpty) return null
//
//        val name: String = getInterfaceName(
//            sourceSetName = sourceSetName,
//            generatorType = generatorType
//        )
//
//        return MRGenerator.GenerationResult(
//            typeSpec = TypeSpec.interfaceBuilder(name)
//                .addModifiers(visibilityModifier)
//                .addModifiers(KModifier.EXPECT)
//                .build(),
//            metadata = GeneratedObject(
//                generatorType = generatorType,
//                type = GeneratedObjectType.Interface,
//                modifier = GeneratedObjectModifier.Expect,
//                name = name
//            )
//        )
//    }
//}
//
//internal class PropertiesGenerator {
//
//}
//
//// разница в ресурсах - в итоговом генерируемом actual классе
//// (точнее в значениях, которые должны быть сгенерированы)
//// и в самих файлах, которые генерируются для платформы
//
//// генерацию файлов и значений, то есть actual данные, можно делать из метаданных, не читая
//// вообще файлы
//
//// тогда пайплайн такой: читаем файлы -> генерируем метаданные для них -> пишем файлы expect, interface
//// а в таргет генераторах пишем уже значения и сами файлы платформы
