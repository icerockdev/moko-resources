/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File

abstract class BaseGenerator<T> : MRGenerator.Generator {

    override fun generate(
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        objectBuilder: TypeSpec.Builder
    ): TypeSpec {
        // language - key - value
        val languageMap: Map<LanguageType, Map<KeyType, T>> = loadLanguageMap()
        val languageKeyValues = languageMap[LanguageType.Base].orEmpty()

        beforeGenerateResources(objectBuilder, languageMap)

        val stringsClass = createTypeSpec(languageKeyValues.keys.toList(), objectBuilder)

        languageMap.forEach { (language, strings) ->
            generateResources(resourcesGenerationDir, language, strings)
        }

        return stringsClass
    }

    @Suppress("SpreadOperator")
    private fun createTypeSpec(keys: List<KeyType>, objectBuilder: TypeSpec.Builder): TypeSpec {
        objectBuilder.addModifiers(*getClassModifiers())

        extendObjectBodyAtStart(objectBuilder)

        keys.forEach { key ->
            val name = key.replace(".", "_")
            val property =
                PropertySpec.builder(name, resourceClassName)
            property.addModifiers(*getPropertyModifiers())
            getPropertyInitializer(
                key
            )?.let { property.initializer(it) }
            objectBuilder.addProperty(property.build())
        }

        extendObjectBodyAtEnd(objectBuilder)
        return objectBuilder.build()
    }

    protected abstract fun loadLanguageMap(): Map<LanguageType, Map<KeyType, T>>
    protected abstract fun getPropertyInitializer(key: String): CodeBlock?

    protected open fun getClassModifiers(): Array<KModifier> = emptyArray()
    protected open fun getPropertyModifiers(): Array<KModifier> = emptyArray()

    protected open fun beforeGenerateResources(
        objectBuilder: TypeSpec.Builder,
        languageMap: Map<LanguageType, Map<KeyType, T>>
    ) = Unit

    protected open fun generateResources(
        resourcesGenerationDir: File,
        language: LanguageType,
        strings: Map<KeyType, T>
    ) {
    }

    protected companion object {
        const val BASE_LANGUAGE = "base"
    }
}
