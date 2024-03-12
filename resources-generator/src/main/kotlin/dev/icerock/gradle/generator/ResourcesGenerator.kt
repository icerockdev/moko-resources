/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FileSpec.Builder
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.metadata.container.ContainerMetadata
import dev.icerock.gradle.metadata.resource.ResourceMetadata
import dev.icerock.gradle.utils.calculateHash
import org.gradle.api.logging.Logger
import java.io.File

@Suppress("LongParameterList")
internal class ResourcesGenerator(
    val logger: Logger,
    private val containerGenerator: PlatformContainerGenerator,
    private val typesGenerators: List<ResourceTypeGenerator<*>>,
    private val resourcesPackageName: String,
    private val resourcesClassName: String,
    private val sourceSetName: String,
    private val visibilityModifier: KModifier,
    private val sourcesGenerationDir: File,
) {
    @Suppress("LongMethod")
    fun generateTargetKotlin(
        files: ResourcesFiles,
        inputMetadata: List<ContainerMetadata>,
    ): List<ContainerMetadata> {
        val ownMetadata: List<ResourceMetadata> = typesGenerators
            .flatMap { it.generateMetadata(files) }

        // no resources - no file
        if (inputMetadata.isEmpty() && ownMetadata.isEmpty()) return emptyList()

        val fileSpec: FileSpec.Builder = FileSpec.builder(
            packageName = resourcesPackageName,
            fileName = resourcesClassName
        )

        val outputMetadata: MutableList<ContainerMetadata> = mutableListOf()

        if (inputMetadata.isEmpty()) {
            // we not have expect - we should generate simple object
            generateSimpleResourceObject(
                ownMetadata = ownMetadata,
                outputMetadata = outputMetadata,
                parentObjectName = resourcesClassName,
                fileSpec = fileSpec,
                inputMetadata = inputMetadata
            )
        } else {
            val inputMetadataMap: Map<String, List<ContainerMetadata>> = inputMetadata.groupBy {
                it.parentObjectName
            }

            inputMetadataMap.keys.contains(resourcesClassName)

            inputMetadataMap.keys.forEach { expectObjectName ->
                val inputMetadataList: List<ContainerMetadata> = inputMetadataMap
                    .getOrElse(expectObjectName) {
                        throw Exception("Current sourceSet not found.")
                    }

                // for each input metadata we should generate actual object
                val objects: List<GenerationResult> = typesGenerators.mapNotNull { typeGenerator ->
                    typeGenerator.generateActualObject(
                        parentObjectName = expectObjectName,
                        objects = inputMetadataList
                    )
                }
                objects.forEach { outputMetadata.add(it.metadata) }

                val objectSpec: TypeSpec.Builder =
                    TypeSpec.objectBuilder(expectObjectName) // default: object MR
                        .addModifiers(KModifier.ACTUAL)
                        .addModifiers(visibilityModifier)

                finalizeObjectSpec(
                    fileSpec = fileSpec,
                    objectSpec = objectSpec,
                    generatedObjects = objects,
                    inputMetadata = inputMetadataList,
                    outputMetadata = outputMetadata
                )
            }

            // if current level has resources need generate simple resource object,
            // but we have "MR" object from previous level and need change name of object
            if (ownMetadata.isNotEmpty()) {
                val targetObjectResourceName: String = "$resourcesClassName$sourceSetName"

                generateSimpleResourceObject(
                    ownMetadata = ownMetadata,
                    outputMetadata = outputMetadata,
                    parentObjectName = targetObjectResourceName,
                    fileSpec = fileSpec,
                    inputMetadata = inputMetadata
                )
            }
        }

        // write file
        fileSpec.build().writeTo(sourcesGenerationDir)

        return outputMetadata
    }

    private fun generateSimpleResourceObject(
        ownMetadata: List<ResourceMetadata>,
        parentObjectName: String,
        outputMetadata: MutableList<ContainerMetadata>,
        fileSpec: Builder,
        inputMetadata: List<ContainerMetadata>,
    ) {
        val objects: List<GenerationResult> = typesGenerators.mapNotNull { typeGenerator ->
            typeGenerator.generateObject(
                parentObjectName = parentObjectName,
                metadata = ownMetadata
            )
        }
        objects.forEach { outputMetadata.add(it.metadata) }

        val objectSpec: TypeSpec.Builder =
            TypeSpec.objectBuilder(parentObjectName) // default: object MR
                .addModifiers(visibilityModifier)

        finalizeObjectSpec(
            fileSpec = fileSpec,
            objectSpec = objectSpec,
            generatedObjects = objects,
            inputMetadata = inputMetadata,
            outputMetadata = outputMetadata
        )
    }

    fun generateCommonKotlin(
        files: ResourcesFiles,
        inputMetadata: List<ContainerMetadata>,
    ): List<ContainerMetadata> {
        val ownMetadata: List<ResourceMetadata> = typesGenerators
            .flatMap { it.generateMetadata(files) }

        // we not have any resources on this level.
        // so we should not generate expect and actual too
        if (ownMetadata.isEmpty()) return emptyList()

        // here we see metadata on our level
        val fileSpec: FileSpec.Builder = FileSpec.builder(
            packageName = resourcesPackageName,
            fileName = resourcesClassName
        )

        val outputMetadata: MutableList<ContainerMetadata> = mutableListOf()

        // if previous levels doesn't have resources should use "MR"
        // but if resources is found, need generate "MRsourceSet" object
        val expectObjectName: String = if (inputMetadata.isNotEmpty()) {
            "$resourcesClassName$sourceSetName"
        } else {
            resourcesClassName
        }

        // at lower level we not see any resources. so we should generate expects
        // read own metadata for object
        val objects: List<GenerationResult> = typesGenerators.mapNotNull { typeGenerator ->
            typeGenerator.generateExpectObject(
                parentObjectName = expectObjectName,
                metadata = ownMetadata,
            )
        }

        val objectSpec: TypeSpec.Builder =
            TypeSpec.objectBuilder(expectObjectName) // default: object MR
                .addModifiers(KModifier.EXPECT)
                .addModifiers(visibilityModifier)

        objects.forEach { result ->
            objectSpec.addType(result.typeSpec)
            outputMetadata.add(result.metadata)
        }

        fileSpec.addType(objectSpec.build())

        // write file
        fileSpec.build().writeTo(sourcesGenerationDir)

        return outputMetadata
    }

    fun generateResources(metadata: List<ContainerMetadata>) {
        val resources: List<ResourceMetadata> = metadata.flatMap { it.resources }
        typesGenerators.forEach { it.generateFiles(resources) }
    }

    private fun finalizeObjectSpec(
        fileSpec: Builder,
        objectSpec: TypeSpec.Builder,
        generatedObjects: List<GenerationResult>,
        inputMetadata: List<ContainerMetadata>,
        outputMetadata: MutableList<ContainerMetadata>
    ) {
        containerGenerator.getImports()
            .plus(typesGenerators.flatMap { it.getImports() })
            .forEach { fileSpec.addImport(it.packageName, it.simpleNames) }

        val contentHash: String = (inputMetadata + outputMetadata)
            .map { it.contentHash() }
            .calculateHash()

        objectSpec.addContentHashProperty(contentHash)

        objectSpec.also(containerGenerator::generateBeforeTypes)

        generatedObjects.forEach { objectSpec.addType(it.typeSpec) }

        objectSpec.also(containerGenerator::generateAfterTypes)

        fileSpec.addType(objectSpec.build())
    }
}
