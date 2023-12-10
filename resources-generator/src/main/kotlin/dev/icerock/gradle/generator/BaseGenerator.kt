/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.PropertySpec.Builder
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.metadata.GeneratedObject
import dev.icerock.gradle.metadata.GeneratedObjectModifier
import dev.icerock.gradle.metadata.GeneratedProperties
import dev.icerock.gradle.metadata.addActual
import dev.icerock.gradle.metadata.getActualInterfaces
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.gradle.api.Project
import java.io.File

abstract class BaseGenerator<T> : MRGenerator.Generator {
    override fun generate(
        project: Project,
        inputMetadata: MutableList<GeneratedObject>,
        generatedObjects: MutableList<GeneratedObject>,
        targetObject: GeneratedObject,
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        objectBuilder: TypeSpec.Builder,
    ): TypeSpec {
        // Read previous languages map from metadata
        // if target object is expect object or interface return emptyMap()
        val previousLanguagesMap: Map<LanguageType, Map<KeyType, T>> = getPreviousLanguagesMap(
            inputMetadata = inputMetadata,
            targetObject = targetObject
        )

        // Read actual resources of target
        // If target object is actual object: skip read files again
        //
        // Structure: language - key - value
        val languageMap: Map<LanguageType, Map<KeyType, T>> = if (targetObject.isActualObject) {
            emptyMap()
        } else {
            loadLanguageMap()
        }

        // Sum of previous and target language fields
        val languagesAllMaps: Map<LanguageType, Map<KeyType, T>> = getLanguagesAllMaps(
            previousLanguageMaps = previousLanguagesMap,
            languageMap = languageMap
        )
        val languageKeyValues: Map<KeyType, T> = languagesAllMaps[LanguageType.Base].orEmpty()

        beforeGenerateResources(objectBuilder, languagesAllMaps)

        val stringsClass: TypeSpec = createTypeSpec(
            inputMetadata = inputMetadata,
            generatedObjects = generatedObjects,
            targetObject = targetObject,
            keys = languageKeyValues.keys.toList(),
            languageMap = languagesAllMaps,
            objectBuilder = objectBuilder
        )

        languagesAllMaps.forEach { (language: LanguageType, strings: Map<KeyType, T>) ->
            generateResources(resourcesGenerationDir, language, strings)
        }

        return stringsClass
    }

    private fun createTypeSpec(
        inputMetadata: MutableList<GeneratedObject>,
        generatedObjects: MutableList<GeneratedObject>,
        targetObject: GeneratedObject,
        keys: List<KeyType>,
        languageMap: Map<LanguageType, Map<KeyType, T>>,
        objectBuilder: TypeSpec.Builder,
    ): TypeSpec {
        objectBuilder.addModifiers(*getClassModifiers())

        if (targetObject.isActualObject) {
            extendObjectBodyAtStart(objectBuilder)
        }

        val generatedProperties = mutableListOf<GeneratedProperties>()

        keys.forEach { key ->
            val name = key.replace(".", "_")

            //Create metadata property
            var generatedProperty = GeneratedProperties(
                modifier = GeneratedObjectModifier.None,
                name = name,
                data = JsonObject(
                    content = getPropertyMetadata(
                        key = key,
                        languageMap = languageMap
                    )
                )
            )

            val property: Builder = PropertySpec.builder(name, resourceClassName)

            if (targetObject.isObject) {
                // Add modifier for property and setup metadata
                generatedProperty = generatedProperty.copy(
                    modifier = addActualOverrideModifier(
                        propertyName = name,
                        property = property,
                        inputMetadata = inputMetadata,
                        targetObject = targetObject
                    )
                )

                getPropertyInitializer(key)?.let {
                    property.initializer(it)
                }
            }

            objectBuilder.addProperty(property.build())
            generatedProperties.add(generatedProperty)
        }

        extendObjectBodyAtEnd(objectBuilder)

        // Add object in metadata with remove expect realisation
        generatedObjects.addActual(
            targetObject.copy(properties = generatedProperties)
        )

        return objectBuilder.build()
    }

    abstract fun getPropertyMetadata(
        key: KeyType,
        languageMap: Map<LanguageType, Map<KeyType, T>>,
    ): Map<String, JsonElement>

    private fun addActualOverrideModifier(
        propertyName: String,
        property: PropertySpec.Builder,
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
//                    GeneratedObjectModifier.Expect -> {
//                        property.addModifiers(KModifier.EXPECT)
//                        GeneratedObjectModifier.Expect
//                    }

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

    abstract fun getLanguagesAllMaps(
        previousLanguageMaps: Map<LanguageType, Map<KeyType, T>>,
        languageMap: Map<LanguageType, Map<KeyType, T>>
    ): Map<LanguageType, Map<KeyType, T>>

    abstract fun getPreviousLanguagesMap(
        inputMetadata: List<GeneratedObject>,
        targetObject: GeneratedObject,
    ): Map<LanguageType, Map<KeyType, T>>

    protected abstract fun loadLanguageMap(): Map<LanguageType, Map<KeyType, T>>
    protected abstract fun getPropertyInitializer(key: String): CodeBlock?

    protected open fun getClassModifiers(): Array<KModifier> = emptyArray()
    protected open fun getPropertyModifiers(): Array<KModifier> = emptyArray()

    protected open fun beforeGenerateResources(
        objectBuilder: TypeSpec.Builder,
        languageMap: Map<LanguageType, Map<KeyType, T>>,
    ) = Unit

    protected open fun generateResources(
        resourcesGenerationDir: File,
        language: LanguageType,
        strings: Map<KeyType, T>,
    ) = Unit

    protected companion object {
        const val BASE_LANGUAGE = "base"
    }
}
