/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.metadata.container.ContainerMetadata
import dev.icerock.gradle.metadata.container.ResourceType
import dev.icerock.gradle.metadata.resource.ResourceMetadata
import dev.icerock.gradle.utils.filterClass
import org.gradle.api.tasks.util.PatternFilterable
import kotlin.reflect.KClass

@Suppress("LongParameterList", "TooManyFunctions", "UnusedPrivateMember")
internal class ResourceTypeGenerator<T : ResourceMetadata>(
    private val generationPackage: String,
    private val resourceClass: ClassName,
    private val resourceType: ResourceType,
    private val metadataClass: KClass<T>,
    private val visibilityModifier: KModifier,
    private val generator: ResourceGenerator<T>,
    private val platformResourceGenerator: PlatformResourceGenerator<T>,
    private val filter: PatternFilterable.() -> Unit,
) {
    fun generateMetadata(files: ResourcesFiles): List<T> {
        return generator.generateMetadata(files.matching(filter).ownSourceSet.fileTree.files)
    }

    fun getImports(): List<ClassName> = platformResourceGenerator.imports()

    fun generateExpectObject(
        parentObjectName: String,
        metadata: List<ResourceMetadata>
    ): GenerationResult? {
        val typeMetadata: List<T> = metadata.filterClass(typeClass = metadataClass)

        // if we not have any resources of our type at all - not generate object
        if (typeMetadata.isEmpty()) return null

        val objectName: String = resourceType.name.lowercase()
        val objectBuilder: TypeSpec.Builder = TypeSpec
            .objectBuilder(objectName)
            .addModifiers(visibilityModifier)
            // implement ResourceType<**Resource> for extensions
            .addSuperinterface(Constants.resourceContainerName.parameterizedBy(resourceClass))
            // implement ResourceContainer platform property
            .addOverridePlatformProperty()
            // add all properties of available resources
            .addProperties(typeMetadata.map { generator.generateProperty(it).build() })
            // implement ResourceContainer values function
            .addOverrideAbstractValuesFunction(resourceClass)

        return GenerationResult(
            typeSpec = objectBuilder.build(),
            metadata = ContainerMetadata(
                parentObjectName = parentObjectName,
                name = objectName,
                resourceType = resourceType,
                resources = typeMetadata
            )
        )
    }

    fun generateActualObject(
        parentObjectName: String,
        objects: List<ContainerMetadata>,
    ): GenerationResult? {
        val typeObject: ContainerMetadata = objects
            .singleOrNull { it.resourceType == resourceType } ?: return null

        val typeResources: List<T> = typeObject.resources.filterClass(metadataClass)
        val objectBuilder: TypeSpec.Builder = TypeSpec
            .objectBuilder(typeObject.name)
            .addModifiers(visibilityModifier)
            .addModifiers(KModifier.ACTUAL)
            // implement ResourceType<**Resource> for extensions
            .addSuperinterface(Constants.resourceContainerName.parameterizedBy(resourceClass))
            .also { builder ->
                platformResourceGenerator.generateBeforeProperties(
                    builder = builder,
                    metadata = typeResources,
                    modifier = KModifier.ACTUAL,
                )
            }
            // add all properties of object
            .addProperties(typeResources.map(::createActualProperty))
            .also { builder ->
                platformResourceGenerator.generateAfterProperties(
                    builder = builder,
                    metadata = typeResources,
                    modifier = KModifier.ACTUAL,
                )
            }

        return GenerationResult(
            typeSpec = objectBuilder.build(),
            metadata = ContainerMetadata(
                parentObjectName = parentObjectName,
                name = typeObject.name,
                resourceType = resourceType,
                resources = typeObject.resources
            )
        )
    }

    fun generateObject(
        parentObjectName: String,
        metadata: List<ResourceMetadata>,
    ): GenerationResult? {
        val typeResources: List<T> = metadata.filterClass(metadataClass)

        // if we not have any resources of our type at all - not generate object
        if (typeResources.isEmpty()) return null

        val objectName: String = resourceType.name.lowercase()
        val objectBuilder: TypeSpec.Builder = TypeSpec
            .objectBuilder(objectName)
            .addModifiers(visibilityModifier)
            // implement ResourceType<**Resource> for extensions
            .addSuperinterface(Constants.resourceContainerName.parameterizedBy(resourceClass))
            // implement interfaces for generated expect object
            .also { builder ->
                platformResourceGenerator.generateBeforeProperties(
                    builder = builder,
                    metadata = typeResources,
                )
            }
            // add all properties of object
            .addProperties(typeResources.map(::createSimpleProperty))
            .also { builder ->
                platformResourceGenerator.generateAfterProperties(
                    builder = builder,
                    metadata = typeResources,
                )
            }

        return GenerationResult(
            typeSpec = objectBuilder.build(),
            metadata = ContainerMetadata(
                parentObjectName = parentObjectName,
                name = objectName,
                resourceType = resourceType,
                resources = typeResources
            )
        )
    }

    fun generateFiles(resources: List<ResourceMetadata>) {
        val typeMetadata: List<T> = resources.filterClass(metadataClass)

        platformResourceGenerator.generateResourceFiles(typeMetadata)
    }

    private fun createSimpleProperty(resource: T): PropertySpec {
        return createProperty(resource)
    }

    private fun createActualProperty(resource: T): PropertySpec {
        return createProperty(resource, KModifier.ACTUAL)
    }

    private fun createProperty(
        resource: T,
        modifier: KModifier? = null,
    ): PropertySpec {
        return generator.generateProperty(resource)
            .apply {
                if (modifier != null) addModifiers(modifier)
            }
            .initializer(platformResourceGenerator.generateInitializer(resource))
            .build()
    }
}
