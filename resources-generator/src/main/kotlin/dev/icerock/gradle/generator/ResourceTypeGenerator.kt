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
import dev.icerock.gradle.metadata.resource.HierarchyMetadata
import dev.icerock.gradle.metadata.resource.ResourceMetadata
import dev.icerock.gradle.utils.capitalize
import dev.icerock.gradle.utils.filterClass
import org.gradle.api.tasks.util.PatternFilterable
import java.io.File
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
    private val batchSize: Int = DEFAULT_BATCH_SIZE,
) {
    fun generateMetadata(files: ResourcesFiles): List<T> {
        // Sort for deterministic output; FileTree.files iteration is filesystem-dependent.
        val sortedFiles: Set<File> = files.matching(filter).ownSourceSet.fileTree.files
            .sortedBy { it.absolutePath }
            .toCollection(LinkedHashSet())
        return generator.generateMetadata(sortedFiles)
    }

    fun getImports(): List<ClassName> = platformResourceGenerator.imports()

    fun generateExpectObject(
        parentObjectName: String,
        resources: List<ResourceMetadata>,
        sourceSetName: String,
    ): GenerationResult? {
        val typeMetadata: List<T> = resources
            .filterClass(typeClass = metadataClass)
            .sortedResources()

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
                if (isHierarchyPropertiesStrategy()) {
                    propertiesGenerationStrategy.generateSkeleton(
                        builder = builder,
                        resources = typeMetadata,
                        modifier = KModifier.EXPECT,
                        generateProperty = {
                            generator.generateProperty(it).build()
                        }
                    )
                }
            }
            // implement ResourceContainer values function
            .addOverrideAbstractValuesFunction(resourceClass)

        val fileSpecs: List<FileSpec> = createExpectExtensionFileSpecs(
            parentObjectName = parentObjectName,
            objectName = objectName,
            sourceSetName = sourceSetName,
            resources = typeMetadata
        )

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

    @Suppress("LongMethod")
    fun generateActualObject(
        parentObjectName: String,
        objects: List<ContainerMetadata>,
        sourceSetName: String,
    ): GenerationResult? {
        val typeObject: ContainerMetadata = objects
            .singleOrNull { it.resourceType == resourceType } ?: return null

        val typeResources: List<T> = typeObject.resources
            .filterClass(metadataClass)
            .sortedResources()

        val objectBuilder: TypeSpec.Builder = TypeSpec
            .objectBuilder(typeObject.name)
            .addModifiers(visibilityModifier)
            .addModifiers(KModifier.ACTUAL)
            // implement ResourceType<**Resource> for extensions
            .addSuperinterface(Constants.resourceContainerName.parameterizedBy(resourceClass))
            .also { builder ->
                platformResourceGenerator.generateContainerProperties(
                    builder = builder,
                    metadata = typeResources,
                    modifier = KModifier.ACTUAL,
                )
            }
            .also { builder ->
                if (isHierarchyPropertiesStrategy()) {
                    propertiesGenerationStrategy.generateSkeleton(
                        builder = builder,
                        resources = typeResources,
                        modifier = KModifier.ACTUAL,
                        generateProperty = {
                            generator.generateProperty(it).build()
                        }
                    )
                }
                builder.addValuesFunctionFromAccessors(
                    accessorObjectNames = getAccessorObjectNames(
                        sourceSetName = typeObject.sourceSetName ?: sourceSetName,
                        objectName = typeObject.name,
                        resources = typeResources
                    ),
                    classType = resourceClass,
                    modifier = KModifier.ACTUAL
                )
                platformResourceGenerator.generateContainerMembers(
                    builder = builder,
                    metadata = typeResources,
                    modifier = KModifier.ACTUAL,
                )
            }

        val fileSpecs: List<FileSpec> = createAccessorFileSpecs(
            parentObjectName = parentObjectName,
            objectName = typeObject.name,
            resourceSourceSetName = typeObject.sourceSetName ?: sourceSetName,
            targetSourceSetName = sourceSetName,
            resources = typeResources,
            actualModifier = KModifier.ACTUAL
        ) + platformResourceGenerator.generateAdditionalAccessorFiles(requireResourcesPackageName())

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

    @Suppress("LongMethod")
    fun generateObject(
        parentObjectName: String,
        resources: List<ResourceMetadata>,
        sourceSetName: String,
    ): GenerationResult? {
        val typeResources: List<T> = resources
            .filterClass(typeClass = metadataClass)
            .sortedResources()

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
                platformResourceGenerator.generateContainerProperties(
                    builder = builder,
                    metadata = typeResources,
                )
            }
            .also { builder ->
                if (isHierarchyPropertiesStrategy()) {
                    propertiesGenerationStrategy.generateSkeleton(
                        builder = builder,
                        resources = typeResources,
                        modifier = null,
                        generateProperty = {
                            generator.generateProperty(it).build()
                        }
                    )
                }
                builder.addValuesFunctionFromAccessors(
                    accessorObjectNames = getAccessorObjectNames(
                        sourceSetName = sourceSetName,
                        objectName = objectName,
                        resources = typeResources
                    ),
                    classType = resourceClass
                )
                platformResourceGenerator.generateContainerMembers(
                    builder = builder,
                    metadata = typeResources,
                )
            }

        val fileSpecs: List<FileSpec> = createAccessorFileSpecs(
            parentObjectName = parentObjectName,
            objectName = objectName,
            resourceSourceSetName = sourceSetName,
            targetSourceSetName = sourceSetName,
            resources = typeResources,
            actualModifier = null
        ) + platformResourceGenerator.generateAdditionalAccessorFiles(requireResourcesPackageName())

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

    private fun createExpectExtensionFileSpecs(
        parentObjectName: String,
        objectName: String,
        sourceSetName: String,
        resources: List<T>,
    ): List<FileSpec> {
        return resources.chunked(batchSize).mapIndexed { index, batch ->
            val accessorObjectName: String = getAccessorObjectName(
                sourceSetName = sourceSetName,
                objectName = objectName,
                index = index
            )

            createExtensionFileSpecBuilder("$accessorObjectName.$sourceSetName")
                .also { fileSpec ->
                    addExpectExtensionAccessors(
                        fileSpec = fileSpec,
                        parentObjectName = parentObjectName,
                        objectName = objectName,
                        resources = batch
                    )
                }
                .build()
        }
    }

    @Suppress("LongMethod")
    private fun createAccessorFileSpecs(
        parentObjectName: String,
        objectName: String,
        resourceSourceSetName: String,
        targetSourceSetName: String,
        resources: List<T>,
        actualModifier: KModifier?,
    ): List<FileSpec> {
        return resources.chunked(batchSize).mapIndexed { index, batch ->
            val accessorObjectName: String = getAccessorObjectName(
                sourceSetName = resourceSourceSetName,
                objectName = objectName,
                index = index
            )
            val accessorResources: List<AccessorResource<T>> = buildAccessorResources(batch)
            val fileSpec = createExtensionFileSpecBuilder(
                fileName = "$accessorObjectName.$targetSourceSetName"
            )

            platformResourceGenerator.generateAccessorFilePreamble(
                builder = fileSpec,
                metadata = batch,
                objectName = accessorObjectName
            )

            val groupObject = TypeSpec.objectBuilder(accessorObjectName)
                .addModifiers(KModifier.INTERNAL)
                .also { builder ->
                    accessorResources.forEach { resource ->
                        builder.addProperty(
                            PropertySpec.builder(resource.internalName, resourceClass)
                                .addModifiers(KModifier.INTERNAL)
                                .delegate(
                                    CodeBlock.of(
                                        "lazy { %L() }",
                                        initFunctionName(resource.internalName)
                                    )
                                )
                                .build()
                        )
                    }
                    builder.addAccessorValuesFunction(
                        propertyReferences = accessorResources.map { it.internalName },
                        classType = resourceClass,
                        memberModifier = KModifier.INTERNAL
                    )
                }
                .build()

            fileSpec.addType(groupObject)

            accessorResources.forEach { resource ->
                fileSpec.addFunction(
                    FunSpec.builder(initFunctionName(resource.internalName))
                        .addModifiers(KModifier.PRIVATE)
                        .returns(resourceClass)
                        .addStatement(
                            "return %L",
                            platformResourceGenerator.generateAccessorInitializer(
                                metadata = resource.metadata,
                                accessorObjectName = accessorObjectName
                            )
                        )
                        .build()
                )
            }

            addActualExtensionAccessors(
                fileSpec = fileSpec,
                parentObjectName = parentObjectName,
                objectName = objectName,
                groupName = accessorObjectName,
                resources = accessorResources,
                actualModifier = actualModifier
            )

            fileSpec.build()
        }
    }

    private fun addExpectExtensionAccessors(
        fileSpec: FileSpec.Builder,
        parentObjectName: String,
        objectName: String,
        resources: List<T>,
    ) {
        resources.forEach { resource ->
            fileSpec.addProperty(
                PropertySpec
                    .builder(
                        name = resource.key,
                        type = resourceClass
                    )
                    .receiver(
                        receiverType = getReceiverClassName(
                            parentObjectName = parentObjectName,
                            objectName = objectName,
                            path = resource.pathSegments()
                        )
                    )
                    .addModifiers(visibilityModifier, KModifier.EXPECT)
                    .build()
            )
        }
    }

    private fun addActualExtensionAccessors(
        fileSpec: FileSpec.Builder,
        parentObjectName: String,
        objectName: String,
        groupName: String,
        resources: List<AccessorResource<T>>,
        actualModifier: KModifier?,
    ) {
        resources.forEach { resource ->
            val resourcePath: List<String> = resource.metadata.pathSegments()
            fileSpec.addProperty(
                PropertySpec
                    .builder(
                        name = resource.metadata.key,
                        type = resourceClass
                    )
                    .receiver(
                        receiverType = getReceiverClassName(
                            parentObjectName = parentObjectName,
                            objectName = objectName,
                            path = resourcePath
                        )
                    )
                    .addModifiers(visibilityModifier)
                    .also { property ->
                        if (actualModifier != null) {
                            property.addModifiers(actualModifier)
                        }
                    }
                    .getter(
                        FunSpec.getterBuilder()
                            .addStatement(
                                "return %L",
                                "$groupName.${resource.internalName}"
                            )
                            .build()
                    )
                    .build()
            )
        }
    }

    private fun createExtensionFileSpecBuilder(fileName: String): FileSpec.Builder {
        return FileSpec.builder(
            packageName = requireResourcesPackageName(),
            fileName = fileName
        ).also { fileSpec ->
            platformResourceGenerator.imports().forEach { import ->
                fileSpec.addImport(
                    packageName = import.packageName,
                    names = import.simpleNames
                )
            }
        }
    }

    private fun getReceiverClassName(
        parentObjectName: String,
        objectName: String,
        path: List<String> = emptyList(),
    ): ClassName {
        return ClassName(
            packageName = requireResourcesPackageName(),
            simpleNames = listOf(
                parentObjectName,
                objectName
            ) + path
        )
    }

    private fun buildAccessorResources(resources: List<T>): List<AccessorResource<T>> {
        if (!isHierarchyPropertiesStrategy()) {
            return resources.map { resource ->
                AccessorResource(
                    metadata = resource,
                    internalName = resource.key
                )
            }
        }

        val duplicates: Map<String, List<T>> = resources
            .groupBy { it.key }
            .mapValues { (_, groupedResources) ->
                groupedResources.sortedBy { resourceLogicalPath(it = it) }
            }

        return resources.map { resource ->
            val duplicateGroup: List<T> = duplicates.getValue(resource.key)
            val internalName = if (duplicateGroup.size == 1) {
                resource.key
            } else {
                "${resource.key}__${duplicateGroup.indexOf(resource)}"
            }

            AccessorResource(metadata = resource, internalName = internalName)
        }
    }

    private fun initFunctionName(internalName: String): String {
        return "init_$internalName"
    }

    private fun resourceLogicalPath(it: T): String {
        return it.pathSegments().joinToString(separator = "/") + "/${it.key}"
    }

    private fun getAccessorObjectNames(
        sourceSetName: String,
        objectName: String,
        resources: List<T>,
    ): List<String> {
        return resources.chunked(batchSize).indices.map { index ->
            getAccessorObjectName(
                sourceSetName = sourceSetName,
                objectName = objectName,
                index = index
            )
        }
    }

    private fun getAccessorObjectName(
        sourceSetName: String,
        objectName: String,
        index: Int,
    ): String {
        return "${sourceSetName.capitalize()}${objectName.capitalize()}$index"
    }

    private fun requireResourcesPackageName(): String {
        return requireNotNull(resourcesPackageName) {
            "resourcesPackageName should be provided to generate extension properties"
        }
    }

    private fun isHierarchyPropertiesStrategy(): Boolean {
        return propertiesGenerationStrategy is HierarchyPropertiesGenerationStrategy<*>
    }

    private fun T.pathSegments(): List<String> {
        return (this as? HierarchyMetadata)?.path.orEmpty()
    }

    private fun List<T>.sortedResources(): List<T> {
        return if (firstOrNull() is HierarchyMetadata) {
            sortedBy {
                val hierarchy = it as HierarchyMetadata
                hierarchy.path.joinToString(separator = "/") + "/" + it.key
            }
        } else {
            sortedBy { it.key }
        }
    }

    private companion object {
        const val DEFAULT_BATCH_SIZE = 50
    }

    private data class AccessorResource<T : ResourceMetadata>(
        val metadata: T,
        val internalName: String,
    )
}
