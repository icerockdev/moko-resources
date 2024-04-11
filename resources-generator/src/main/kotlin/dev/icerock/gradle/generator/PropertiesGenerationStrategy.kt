/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.metadata.resource.HierarchyMetadata
import dev.icerock.gradle.metadata.resource.ResourceMetadata

internal interface PropertiesGenerationStrategy<T : ResourceMetadata> {
    fun generateProperties(
        builder: TypeSpec.Builder,
        resources: List<T>,
        modifier: KModifier?,
        generateProperty: (T) -> PropertySpec,
    )
}

internal class FlatPropertiesGenerationStrategy<T : ResourceMetadata> :
    PropertiesGenerationStrategy<T> {
    override fun generateProperties(
        builder: TypeSpec.Builder,
        resources: List<T>,
        modifier: KModifier?,
        generateProperty: (T) -> PropertySpec,
    ) {
        builder.addProperties(
            resources.map {
                generateProperty(it)
            }
        )
    }
}

internal class HierarchyPropertiesGenerationStrategy<T : HierarchyMetadata> :
    PropertiesGenerationStrategy<T> {
    override fun generateProperties(
        builder: TypeSpec.Builder,
        resources: List<T>,
        modifier: KModifier?,
        generateProperty: (T) -> PropertySpec,
    ) {
        builder.addObjectsProperties(
            typeResources = resources,
            generateProperty = generateProperty,
            modifier = modifier
        )
    }

    private fun TypeSpec.Builder.addObjectsProperties(
        typeResources: List<T>,
        generateProperty: (T) -> PropertySpec,
        modifier: KModifier? = null,
    ) {
        val filesMap = typeResources.groupBy {
            it.path
        }.mapValues {
            it.value.map { resValue ->
                generateProperty(resValue)
            }
        }

        println("groupBy")

        typeResources.groupBy {
            it.path
        }.forEach {
            println(it)
        }

        println("filesMap")

        filesMap.forEach {
            println(it)

        }

        this.generateObjects(
            map = filesMap,
            objectKey = emptyList(),
            modifier = modifier
        )
    }

    private fun TypeSpec.Builder.generateObjects(
        map: Map<List<String>, List<PropertySpec>>,
        objectKey: List<String>,
        modifier: KModifier?,
    ) {
        println("generateObjects map[objectKey]")
        println(map[objectKey])

        addProperties(map[objectKey].orEmpty())

        addTypes(
            map.filter {
                it.key.size == objectKey.size + 1
            }.filter {
                it.key.dropLast(1) == objectKey
            }.map {
                TypeSpec.objectBuilder(it.key.last()).also { builder ->
                    builder.generateObjects(
                        map = map,
                        objectKey = it.key,
                        modifier = modifier
                    )
                }.also { builder ->
                    if (modifier != null) {
                        builder.addModifiers(modifier)
                    }
                }.build()
            }
        )
    }
}