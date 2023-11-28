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
import java.io.File

abstract class BaseGenerator<T> : MRGenerator.Generator {

    override fun generate(
        metadata: List<GeneratedObject>,
        typeSpecIsInterface: Boolean,
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        objectBuilder: TypeSpec.Builder,
    ): TypeSpec {
        // language - key - value
        val languageMap: Map<LanguageType, Map<KeyType, T>> = loadLanguageMap()
        val languageKeyValues = languageMap[LanguageType.Base].orEmpty()

        beforeGenerateResources(objectBuilder, languageMap)

        val stringsClass = createTypeSpec(
            metadata = metadata,
            typeSpecIsInterface = typeSpecIsInterface,
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
        metadata: List<GeneratedObject> = emptyList(),
        typeSpecIsInterface: Boolean,
        keys: List<KeyType>,
        objectBuilder: TypeSpec.Builder,
    ): TypeSpec {
        objectBuilder.addModifiers(*getClassModifiers())

        extendObjectBodyAtStart(objectBuilder)

        keys.forEach { key ->
            val name = key.replace(".", "_")
            val property: Builder = PropertySpec.builder(name, resourceClassName)

            if (!typeSpecIsInterface) {
                property
                    .addModifiers(*getPropertyModifiers())

                addOverrideModifier(property, metadata)

                getPropertyInitializer(
                    key
                )?.let { property.initializer(it) }
            }

            objectBuilder.addProperty(property.build())
        }

        extendObjectBodyAtEnd(objectBuilder)
        return objectBuilder.build()
    }

    private fun addActualOverrideModifier(
        property: PropertySpec.Builder,
        metadata: List<GeneratedObject>,
    ) {


        property
            .addModifiers(KModifier.OVERRIDE)

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
