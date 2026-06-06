/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.metadata.container.ContainerMetadata
import dev.icerock.gradle.metadata.container.ResourceType
import dev.icerock.gradle.metadata.resource.ResourceMetadata
import dev.icerock.gradle.utils.capitalize
import dev.icerock.gradle.utils.filterClass
import org.gradle.api.tasks.util.PatternFilterable
import kotlin.reflect.KClass

@Suppress("LongParameterList", "TooManyFunctions", "UnusedPrivateMember")
internal class ResourceTypeGenerator<T : ResourceMetadata>(
    private val propertiesGenerationStrategy: PropertiesGenerationStrategy<T>,
    private val resourceClass: ClassName,
    private val resourceType: ResourceType,
    private val metadataClass: KClass<T>,
    private val visibilityModifier: KModifier,
    private val generator: ResourceGenerator<T>,
    private val platformResourceGenerator: PlatformResourceGenerator<T>,
    private val filter: PatternFilterable.() -> Unit,
    private val resourcesPackageName: String? = null,
    private val generatePropertiesAsExtensions: Boolean = false,
    private val batchSize: Int = DEFAULT_BATCH_SIZE,
) {
    fun generateMetadata(files: ResourcesFiles): List<T> {
        return generator.generateMetadata(files.matching(filter).ownSourceSet.fileTree.files)
    }

    fun getImports(): List<ClassName> = platformResourceGenerator.imports()

    fun generateExpectObject(
        parentObjectName: String,
        resources: List<ResourceMetadata>,
        sourceSetName: String,
    ): GenerationResult? {
        val typeMetadata: List<T> = resources
            .filterClass(typeClass = metadataClass)
            .sortedBy { it.key }

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
            .also { builder ->
                if (!generatePropertiesAsExtensions) {
                    // add all properties of available resources
                    propertiesGenerationStrategy.generateProperties(
                        builder = builder,
                        resources = typeMetadata,
                        modifier = null,
                        generateProperty = {
                            generator.generateProperty(it).build()
                        }
                    )
                }
            }
            // implement ResourceContainer values function
            .addOverrideAbstractValuesFunction(resourceClass)

        val fileSpecs: List<FileSpec> = if (generatePropertiesAsExtensions) {
            createExpectExtensionFileSpecs(
                parentObjectName = parentObjectName,
                objectName = objectName,
                sourceSetName = sourceSetName,
                resources = typeMetadata
            )
        } else {
            emptyList()
        }

        return GenerationResult(
            typeSpec = objectBuilder.build(),
            metadata = ContainerMetadata(
                parentObjectName = parentObjectName,
                name = objectName,
                resourceType = resourceType,
                resources = typeMetadata,
                sourceSetName = sourceSetName
            ),
            fileSpecs = fileSpecs
        )
    }

    fun generateActualObject(
        parentObjectName: String,
        objects: List<ContainerMetadata>,
        sourceSetName: String,
    ): GenerationResult? {
        val typeObject: ContainerMetadata = objects
            .singleOrNull { it.resourceType == resourceType } ?: return null

        val typeResources: List<T> = typeObject.resources
            .filterClass(metadataClass)
            .sortedBy { it.key }
        val useBatchedAccessors: Boolean = generatePropertiesAsExtensions &&
            platformResourceGenerator.supportsBatchedAccessors()

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
            .also { builder ->
                if (useBatchedAccessors) {
                    builder.addBatchValuesFunction(
                        groupNames = getBatchGroupNames(
                            sourceSetName = typeObject.sourceSetName ?: sourceSetName,
                            objectName = typeObject.name,
                            resources = typeResources
                        ),
                        classType = resourceClass,
                        modifier = KModifier.ACTUAL
                    )
                } else if (!generatePropertiesAsExtensions) {
                    propertiesGenerationStrategy.generateProperties(
                        builder = builder,
                        resources = typeResources,
                        modifier = KModifier.ACTUAL,
                        generateProperty = ::createActualProperty
                    )
                }
            }
            .also { builder ->
                if (!useBatchedAccessors) {
                    platformResourceGenerator.generateAfterProperties(
                        builder = builder,
                        metadata = typeResources,
                        modifier = KModifier.ACTUAL,
                    )
                }
            }

        val fileSpecs: List<FileSpec> = if (generatePropertiesAsExtensions) {
            if (useBatchedAccessors) {
                createBatchFileSpecs(
                    parentObjectName = parentObjectName,
                    objectName = typeObject.name,
                    resourceSourceSetName = typeObject.sourceSetName ?: sourceSetName,
                    targetSourceSetName = sourceSetName,
                    resources = typeResources,
                    actualModifier = KModifier.ACTUAL
                ) + platformResourceGenerator.generateAdditionalBatchedFiles(requireResourcesPackageName())
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }

        return GenerationResult(
            typeSpec = objectBuilder.build(),
            metadata = ContainerMetadata(
                parentObjectName = parentObjectName,
                name = typeObject.name,
                resourceType = resourceType,
                resources = typeObject.resources,
                sourceSetName = typeObject.sourceSetName
            ),
            fileSpecs = fileSpecs
        )
    }

    fun generateObject(
        parentObjectName: String,
        resources: List<ResourceMetadata>,
        sourceSetName: String,
    ): GenerationResult? {
        val typeResources: List<T> = resources
            .filterClass(typeClass = metadataClass)
            .sortedBy { it.key }

        // if we not have any resources of our type at all - not generate object
        if (typeResources.isEmpty()) return null

        val objectName: String = resourceType.name.lowercase()
        val useBatchedAccessors: Boolean = generatePropertiesAsExtensions &&
            platformResourceGenerator.supportsBatchedAccessors()
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
            .also { builder ->
                if (useBatchedAccessors) {
                    builder.addBatchValuesFunction(
                        groupNames = getBatchGroupNames(
                            sourceSetName = sourceSetName,
                            objectName = objectName,
                            resources = typeResources
                        ),
                        classType = resourceClass
                    )
                } else if (!generatePropertiesAsExtensions) {
                    propertiesGenerationStrategy.generateProperties(
                        builder = builder,
                        resources = typeResources,
                        modifier = null,
                        generateProperty = ::createSimpleProperty
                    )
                }
            }
            .also { builder ->
                if (!useBatchedAccessors) {
                    platformResourceGenerator.generateAfterProperties(
                        builder = builder,
                        metadata = typeResources,
                    )
                }
            }

        val fileSpecs: List<FileSpec> = if (generatePropertiesAsExtensions) {
            val batchFiles = if (useBatchedAccessors) {
                createBatchFileSpecs(
                    parentObjectName = parentObjectName,
                    objectName = objectName,
                    resourceSourceSetName = sourceSetName,
                    targetSourceSetName = sourceSetName,
                    resources = typeResources,
                    actualModifier = null
                ) + platformResourceGenerator.generateAdditionalBatchedFiles(requireResourcesPackageName())
            } else {
                emptyList()
            }

            batchFiles
        } else {
            emptyList()
        }

        return GenerationResult(
            typeSpec = objectBuilder.build(),
            metadata = ContainerMetadata(
                parentObjectName = parentObjectName,
                name = objectName,
                resourceType = resourceType,
                resources = typeResources,
                sourceSetName = sourceSetName
            ),
            fileSpecs = fileSpecs
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

    private fun createExpectExtensionFileSpecs(
        parentObjectName: String,
        objectName: String,
        sourceSetName: String,
        resources: List<T>,
    ): List<FileSpec> {
        return resources.chunked(batchSize).mapIndexed { index, batch ->
            val groupName: String = getBatchGroupName(
                sourceSetName = sourceSetName,
                objectName = objectName,
                index = index
            )

            val fileSpec = FileSpec.builder(
                packageName = requireResourcesPackageName(),
                fileName = getBatchFileName(
                    objectName = objectName,
                    index = index,
                    targetSourceSetName = sourceSetName
                )
            )

            val groupObject = TypeSpec.objectBuilder(groupName)
                .addModifiers(KModifier.INTERNAL, KModifier.EXPECT)
                .addProperties(
                    batch.map { resource ->
                        generator.generateProperty(resource)
                            .build()
                    }
                )
                .also { builder ->
                    builder.addPlainValuesFunction(
                        metadata = batch,
                        classType = resourceClass,
                        isExpect = true
                    )
                }
                .build()

            fileSpec.addType(groupObject)

            batch.forEach { resource ->
                fileSpec.addProperty(
                    PropertySpec.builder(resource.key, resourceClass)
                        .receiver(getReceiverClassName(parentObjectName, objectName))
                        .addModifiers(visibilityModifier)
                        .getter(
                            FunSpec.getterBuilder()
                                .addStatement("return %N.%N", groupName, resource.key)
                                .build()
                        )
                        .build()
                )
            }

            fileSpec.build()
        }
    }

    private fun createBatchFileSpecs(
        parentObjectName: String,
        objectName: String,
        resourceSourceSetName: String,
        targetSourceSetName: String,
        resources: List<T>,
        actualModifier: KModifier?,
    ): List<FileSpec> {
        return resources.chunked(batchSize).mapIndexed { index, batch ->
            val groupName: String = getBatchGroupName(
                sourceSetName = resourceSourceSetName,
                objectName = objectName,
                index = index
            )
            val fileSpec = createExtensionFileSpecBuilder(
                fileName = "$groupName.$targetSourceSetName"
            )

            val groupObject = TypeSpec.objectBuilder(groupName)
                .addModifiers(KModifier.INTERNAL)
                .also { builder ->
                    if (actualModifier != null) builder.addModifiers(actualModifier)
                }
                .addProperties(
                    batch.map { resource ->
                        generator.generateProperty(resource)
                            .also { builder ->
                                if (actualModifier != null) builder.addModifiers(actualModifier)
                            }
                            .delegate(CodeBlock.of("lazy { init_%L() }", resource.key))
                            .build()
                    }
                )
                .also { builder ->
                    builder.addPlainValuesFunction(
                        metadata = batch,
                        classType = resourceClass,
                        modifier = actualModifier
                    )
                }
                .build()

            fileSpec.addType(groupObject)

            if (actualModifier == null) {
                batch.forEach { resource ->
                    fileSpec.addProperty(
                        PropertySpec.builder(resource.key, resourceClass)
                            .receiver(getReceiverClassName(parentObjectName, objectName))
                            .addModifiers(visibilityModifier)
                            .getter(
                                FunSpec.getterBuilder()
                                    .addStatement("return %N.%N", groupName, resource.key)
                                    .build()
                            )
                            .build()
                    )
                }
            }

            batch.forEach { resource ->
                fileSpec.addFunction(
                    FunSpec.builder("init_${resource.key}")
                        .addModifiers(KModifier.PRIVATE)
                        .returns(resourceClass)
                        .addStatement(
                            "return %L",
                            platformResourceGenerator.generateBatchedInitializer(resource)
                        )
                        .build()
                )
            }

            fileSpec.build()
        }
    }

    private fun createExtensionFileSpecBuilder(fileName: String): FileSpec.Builder {
        return FileSpec.builder(
            packageName = requireResourcesPackageName(),
            fileName = fileName
        ).also { fileSpec ->
            platformResourceGenerator.imports().forEach { import ->
                fileSpec.addImport(import.packageName, import.simpleNames)
            }
        }
    }

    private fun getReceiverClassName(parentObjectName: String, objectName: String): ClassName {
        return ClassName(
            packageName = requireResourcesPackageName(),
            parentObjectName,
            objectName
        )
    }

    private fun getBatchGroupNames(
        sourceSetName: String,
        objectName: String,
        resources: List<T>,
    ): List<String> {
        return resources.chunked(batchSize).indices.map { index ->
            getBatchGroupName(
                sourceSetName = sourceSetName,
                objectName = objectName,
                index = index
            )
        }
    }

    private fun getBatchGroupName(
        sourceSetName: String,
        objectName: String,
        index: Int,
    ): String {
        return "${sourceSetName.capitalize()}${objectName.capitalize()}$index"
    }

    private fun getBatchFileName(
        objectName: String,
        index: Int,
        targetSourceSetName: String,
    ): String {
        return "${objectName.capitalize()}$index.$targetSourceSetName"
    }

    private fun requireResourcesPackageName(): String {
        return requireNotNull(resourcesPackageName) {
            "resourcesPackageName should be provided to generate extension properties"
        }
    }

    private companion object {
        const val DEFAULT_BATCH_SIZE = 100
    }
}
