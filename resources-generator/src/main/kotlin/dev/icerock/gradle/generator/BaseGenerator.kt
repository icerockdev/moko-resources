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
import dev.icerock.gradle.metadata.GeneratedObjectModifier.Actual
import dev.icerock.gradle.metadata.GeneratedObjectModifier.Expect
import dev.icerock.gradle.metadata.GeneratedObjectModifier.None
import dev.icerock.gradle.metadata.GeneratedObjectType
import dev.icerock.gradle.metadata.GeneratedProperties
import java.io.File

abstract class BaseGenerator<T> : MRGenerator.Generator {

    override fun generate(
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
            objectBuilder = objectBuilder
        )

        languageMap.forEach { (language, strings) ->
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
        objectBuilder: TypeSpec.Builder,
    ): TypeSpec {
        objectBuilder.addModifiers(*getClassModifiers())

        extendObjectBodyAtStart(objectBuilder)

        val generatedProperties = mutableListOf<GeneratedProperties>()

        keys.forEach { key ->
            val name = key.replace(".", "_")

            var generatedProperty = GeneratedProperties(
                modifier = None,
                name = name,
                data = ""
            )

            val property: Builder = PropertySpec.builder(name, resourceClassName)

            if (targetObject.type == GeneratedObjectType.Object) {
                generatedProperty = generatedProperty.copy(
                    modifier = if (getPropertyModifiers().contains(KModifier.ACTUAL)) {
                        Actual
                    } else {
                        Expect
                    }
                )

                addActualOverrideModifier(
                    propertyName = name,
                    property = property,
                    inputMetadata = inputMetadata,
                    targetObject = targetObject
                )

                getPropertyInitializer(key)?.let {
                    property.initializer(it)
                }
            }

            objectBuilder.addProperty(property.build())

            generatedProperties.add(generatedProperty)
        }

        extendObjectBodyAtEnd(objectBuilder)

        generatedObjects.add(
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
        targetObject: GeneratedObject
    ) {
        val actualInterfaces = (inputMetadata).filter {
            it.type == GeneratedObjectType.Interface
                    && it.modifier == Actual
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

        if (targetObject.type == GeneratedObjectType.Object) {
            if (containsInActualInterfaces) {
                property.addModifiers(KModifier.OVERRIDE)
            } else {
                when (targetObject.modifier) {
                    Expect -> property.addModifiers(KModifier.EXPECT)
                    Actual -> property.addModifiers(KModifier.ACTUAL)
                    None -> Unit
                }
            }
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
