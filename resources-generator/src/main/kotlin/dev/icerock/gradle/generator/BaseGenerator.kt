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
import dev.icerock.gradle.metadata.GeneratedObjectType
import dev.icerock.gradle.metadata.GeneratedProperties
import dev.icerock.gradle.metadata.addActual
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
        // language - key - value
        val languageMap: Map<LanguageType, Map<KeyType, T>> = loadLanguageMap()
        val languageKeyValues = languageMap[LanguageType.Base].orEmpty()

        // Добавить входящие ресурсы из метаданных
        // + чтение своих ресурсов отдельно, как было

        beforeGenerateResources(objectBuilder, languageMap)

        val stringsClass = createTypeSpec(
            inputMetadata = inputMetadata,
            generatedObjects = generatedObjects,
            targetObject = targetObject,
            keys = languageKeyValues.keys.toList(),
            languageMap = languageMap,
            objectBuilder = objectBuilder
        )

        languageMap.forEach { (language: LanguageType, strings: Map<KeyType, T>) ->
            project.logger.warn("i: language ${language.language()}")
            strings.forEach { (k, v) ->
                project.logger.warn("i: strings $k $v")

            }
            generateResources(resourcesGenerationDir, language, strings)
        }

        return stringsClass
    }

    @Suppress("SpreadOperator")
    private fun createTypeSpec(
        inputMetadata: MutableList<GeneratedObject>,
        generatedObjects: MutableList<GeneratedObject>,
        targetObject: GeneratedObject,
        keys: List<KeyType>,
        languageMap: Map<LanguageType, Map<KeyType, T>>,
        objectBuilder: TypeSpec.Builder,
    ): TypeSpec {
        objectBuilder.addModifiers(*getClassModifiers())

        extendObjectBodyAtStart(objectBuilder)

        val generatedProperties = mutableListOf<GeneratedProperties>()

        keys.forEach { key ->
            val name = key.replace(".", "_")

            val values = mutableMapOf<String, String>()

            languageMap.forEach { (language, strings) ->
                strings.forEach { (stringKey, value) ->
                    if (stringKey == key) {
                        values[language.language()] = value as String
                    }
                }
            }

            var generatedProperty = GeneratedProperties(
                modifier = GeneratedObjectModifier.None,
                name = name,
                data = Json.encodeToString(values.toList())
            )

            val property: Builder = PropertySpec.builder(name, resourceClassName)

            if (targetObject.type == GeneratedObjectType.Object) {
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

        generatedObjects.addActual(
            targetObject.copy(
                properties = generatedProperties
            )
        )

        return objectBuilder.build()
    }

    private fun addActualOverrideModifier(
        propertyName: String,
        property: PropertySpec.Builder,
        inputMetadata: List<GeneratedObject>,
        targetObject: GeneratedObject,
    ): GeneratedObjectModifier {
        val actualInterfaces = (inputMetadata).filter {
            it.type == GeneratedObjectType.Interface
                    && it.modifier == GeneratedObjectModifier.Actual
                    && it.generatorType == targetObject.generatorType
        }

        var containsInActualInterfaces = false

        actualInterfaces.forEach { genInterface ->
            val hasInInterface = genInterface.properties.any {
                it.name == propertyName
            }

            if (hasInInterface) {
                containsInActualInterfaces = true
            }
        }

        return if (targetObject.type == GeneratedObjectType.Object) {
            if (containsInActualInterfaces) {
                property.addModifiers(KModifier.OVERRIDE)
                GeneratedObjectModifier.Override
            } else {
                when (targetObject.modifier) {
                    GeneratedObjectModifier.Expect -> {
                        property.addModifiers(KModifier.EXPECT)
                        GeneratedObjectModifier.Expect
                    }
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
