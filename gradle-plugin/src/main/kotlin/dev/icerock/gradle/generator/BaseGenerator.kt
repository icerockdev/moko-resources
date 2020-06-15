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

    override fun generate(resourcesGenerationDir: File, objectBuilder: TypeSpec.Builder): TypeSpec {
        // language - key - value
        val languageMap: Map<LanguageType, Map<KeyType, T>> = loadLanguageMap()
        val languageKeyValues = languageMap[BASE_LANGUAGE].orEmpty()

        val stringsClass = createTypeSpec(languageKeyValues.keys.toList(), objectBuilder)

        languageMap.forEach { (language, strings) ->
            if (language == BASE_LANGUAGE) {
                generateResources(resourcesGenerationDir, null, strings)
            } else {
                generateResources(resourcesGenerationDir, language, strings)
            }
        }

        return stringsClass
    }

    @Suppress("SpreadOperator")
    private fun createTypeSpec(keys: List<KeyType>, objectBuilder: TypeSpec.Builder): TypeSpec {
        objectBuilder.addModifiers(*getClassModifiers())

        keys.forEach { key ->
            val name = key.replace(".", "_")
            val property =
                PropertySpec.builder(name, resourceClassName)
            property.addModifiers(*getPropertyModifiers())
            getPropertyInitializer(key)?.let { property.initializer(it) }
            objectBuilder.addProperty(property.build())
        }

        extendObjectBody(objectBuilder)
        return objectBuilder.build()
    }

    protected abstract fun loadLanguageMap(): Map<LanguageType, Map<KeyType, T>>
    protected abstract fun getPropertyInitializer(key: String): CodeBlock?

    protected open fun extendObjectBody(classBuilder: TypeSpec.Builder) {}
    protected open fun getClassModifiers(): Array<KModifier> = emptyArray()
    protected open fun getPropertyModifiers(): Array<KModifier> = emptyArray()
    protected open fun generateResources(
        resourcesGenerationDir: File,
        language: String?,
        strings: Map<KeyType, T>
    ) {
    }

    protected companion object {
        const val BASE_LANGUAGE = "base"
    }
}
