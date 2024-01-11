/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.metadata.container.ActualInterfaceMetadata
import dev.icerock.gradle.metadata.container.ExpectInterfaceMetadata
import dev.icerock.gradle.metadata.container.ObjectMetadata
import dev.icerock.gradle.metadata.container.ResourceType
import dev.icerock.gradle.metadata.resource.ResourceMetadata
import dev.icerock.gradle.utils.capitalize
import dev.icerock.gradle.utils.filterClass
import org.gradle.api.tasks.util.PatternFilterable
import kotlin.reflect.KClass

@Suppress("LongParameterList", "TooManyFunctions")
internal class ResourceTypeGenerator<T : ResourceMetadata>(
    private val generationPackage: String,
    private val resourceClass: ClassName,
    private val resourceType: ResourceType,
    private val metadataClass: KClass<T>,
    private val visibilityModifier: KModifier,
    private val generator: ResourceGenerator<T>,
    private val platformResourceGenerator: PlatformResourceGenerator<T>,
    private val filter: PatternFilterable.() -> Unit
) {
    fun generateMetadata(files: ResourcesFiles): List<T> {
        return generator.generateMetadata(files.matching(filter).ownSourceSet.fileTree.files)
    }

    fun getImports(): List<ClassName> = platformResourceGenerator.imports()

    fun generateExpectInterfaces(files: ResourcesFiles): List<GenerationResult> {
        // we should generate expect interface only if we have resources of our type upper
        return files.matching(filter).upperSourceSets.mapNotNull { sourceSetResources ->
            if (sourceSetResources.fileTree.isEmpty) return@mapNotNull null

            val interfaceName: String = buildString {
                append(sourceSetResources.sourceSetName.capitalize())
                append(resourceType.name.lowercase().capitalize())
            }

            GenerationResult(
                typeSpec = TypeSpec.interfaceBuilder(interfaceName)
                    .addModifiers(visibilityModifier)
                    .addModifiers(KModifier.EXPECT)
                    .build(),
                metadata = ExpectInterfaceMetadata(
                    name = interfaceName,
                    sourceSet = sourceSetResources.sourceSetName,
                    resourceType = resourceType
                )
            )
        }
    }

    fun generateExpectObject(
        metadata: List<ResourceMetadata>,
        interfaces: List<ExpectInterfaceMetadata>
    ): GenerationResult? {
        val typeMetadata: List<T> = metadata.filterClass(typeClass = metadataClass)
        val typeInterfaces: List<ExpectInterfaceMetadata> = interfaces
            .filter { it.resourceType == resourceType }

        // if we not have any resources of our type at all - not generate object
        if (typeMetadata.isEmpty() && typeInterfaces.isEmpty()) return null

        val objectName: String = resourceType.name.lowercase()

        val objectBuilder: TypeSpec.Builder = TypeSpec
            .objectBuilder(objectName)
            .addModifiers(visibilityModifier)
            // implement ResourceType<**Resource> for extensions
            .addSuperinterface(Constants.resourceContainerName.parameterizedBy(resourceClass))
            // implement interfaces for generated expect object
            .addSuperinterfaces(
                typeInterfaces.map { ClassName(packageName = generationPackage, it.name) }
            )
            // add all properties of available resources
            .addProperties(typeMetadata.map { generator.generateProperty(it).build() })

        return GenerationResult(
            typeSpec = objectBuilder.build(),
            metadata = ObjectMetadata(
                name = objectName,
                resourceType = resourceType,
                interfaces = typeInterfaces.map { it.name },
                resources = typeMetadata
            )
        )
    }

    fun generateActualInterface(
        interfaces: List<ExpectInterfaceMetadata>,
        metadata: List<ResourceMetadata>,
        sourceSet: String
    ): GenerationResult? {
        val typeMetadata: List<T> = metadata.filterClass(typeClass = metadataClass)
        val typeInterface: ExpectInterfaceMetadata = interfaces
            .filter { it.resourceType == resourceType }
            .singleOrNull { it.sourceSet == sourceSet }
            ?: return null

        return GenerationResult(
            typeSpec = TypeSpec.interfaceBuilder(typeInterface.name)
                .addModifiers(visibilityModifier)
                .addModifiers(KModifier.ACTUAL)
                // add all properties of available resources
                .addProperties(typeMetadata.map { generator.generateProperty(it).build() })
                .build(),
            metadata = ActualInterfaceMetadata(
                name = typeInterface.name,
                resources = typeMetadata
            )
        )
    }

    fun generateActualObject(
        objects: List<ObjectMetadata>,
        interfaces: List<ActualInterfaceMetadata>
    ): GenerationResult? {
        val typeObject: ObjectMetadata = objects
            .singleOrNull { it.resourceType == resourceType } ?: return null
        val typeInterfaces: List<ActualInterfaceMetadata> = interfaces
            .filter { typeObject.interfaces.contains(it.name) }

        val interfaceResources: List<T> = typeInterfaces
            .flatMap { it.resources }
            .filterClass(metadataClass)

        val typeResources: List<T> = typeObject.resources.filterClass(metadataClass)
        val resources: List<T> = (interfaceResources + typeResources)

        val objectBuilder: TypeSpec.Builder = TypeSpec
            .objectBuilder(typeObject.name)
            .addModifiers(visibilityModifier)
            .addModifiers(KModifier.ACTUAL)
            // implement ResourceType<**Resource> for extensions
            .addSuperinterface(Constants.resourceContainerName.parameterizedBy(resourceClass))
            // implement interfaces for generated expect object
            .addSuperinterfaces(
                typeInterfaces.map { ClassName(packageName = generationPackage, it.name) }
            ).also { builder ->
                platformResourceGenerator.generateBeforeProperties(builder, resources)
            }
            // add all properties of interfaces
            .addProperties(interfaceResources.map(::createOverrideProperty))
            // add all properties of object
            .addProperties(typeResources.map(::createActualProperty))
            .also { builder ->
                platformResourceGenerator.generateAfterProperties(builder, resources)
            }

        return GenerationResult(
            typeSpec = objectBuilder.build(),
            metadata = ObjectMetadata(
                name = typeObject.name,
                resourceType = resourceType,
                interfaces = typeInterfaces.map { it.name },
                resources = typeObject.resources + interfaceResources
            )
        )
    }

    fun generateObject(
        metadata: List<ResourceMetadata>
    ): GenerationResult? {
        val typeResources: List<T> = metadata.filterClass(metadataClass)
        if (typeResources.isEmpty()) return null

        val objectName: String = resourceType.name.lowercase()

        val objectBuilder: TypeSpec.Builder = TypeSpec
            .objectBuilder(objectName)
            .addModifiers(visibilityModifier)
            // implement ResourceType<**Resource> for extensions
            .addSuperinterface(Constants.resourceContainerName.parameterizedBy(resourceClass))
            // implement interfaces for generated expect object
            .also { builder ->
                platformResourceGenerator.generateBeforeProperties(builder, typeResources)
            }
            // add all properties of object
            .addProperties(typeResources.map(::createSimpleProperty))
            .also { builder ->
                platformResourceGenerator.generateAfterProperties(builder, typeResources)
            }

        return GenerationResult(
            typeSpec = objectBuilder.build(),
            metadata = ObjectMetadata(
                name = objectName,
                resourceType = resourceType,
                interfaces = emptyList(),
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

    private fun createOverrideProperty(resource: T): PropertySpec {
        return createProperty(resource, KModifier.OVERRIDE)
    }

    private fun createProperty(
        resource: T,
        modifier: KModifier? = null
    ): PropertySpec {
        return generator.generateProperty(resource)
            .apply {
                if (modifier != null) addModifiers(modifier)
            }
            .initializer(platformResourceGenerator.generateInitializer(resource))
            .build()
    }
}