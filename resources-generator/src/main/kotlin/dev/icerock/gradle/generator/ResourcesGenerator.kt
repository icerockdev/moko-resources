/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.metadata.container.ContainerMetadata
import dev.icerock.gradle.metadata.container.ObjectMetadata
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
    private val sourcesGenerationDir: File
) {
    @Suppress("LongMethod")
    fun generateTargetKotlin(
        files: ResourcesFiles,
//        metadataSourceSetName: String,
        inputMetadata: List<ContainerMetadata>
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
            val objects: List<GenerationResult> = typesGenerators.mapNotNull { typeGenerator ->
                typeGenerator.generateObject(ownMetadata)
            }
            objects.forEach { outputMetadata.add(it.metadata) }

            val objectSpec: TypeSpec.Builder =
                TypeSpec.objectBuilder(resourcesClassName) // default: object MR
                    .addModifiers(visibilityModifier)

            containerGenerator.getImports()
                .plus(typesGenerators.flatMap { it.getImports() })
                .forEach { fileSpec.addImport(it.packageName, it.simpleNames) }

            val contentHash: String = (inputMetadata + outputMetadata)
                .mapNotNull { it.contentHash() }
                .calculateHash()
            objectSpec.addContentHashProperty(contentHash)

            objectSpec.also(containerGenerator::generateBeforeTypes)

            objects.forEach { objectSpec.addType(it.typeSpec) }

            objectSpec.also(containerGenerator::generateAfterTypes)

            fileSpec.addType(objectSpec.build())
        } else {
            // for each input metadata we should generate actual object
            val objects: List<GenerationResult> = typesGenerators.mapNotNull { typeGenerator ->
                typeGenerator.generateActualObject(
                    objects = inputMetadata.mapNotNull { it as? ObjectMetadata },
                )
            }
            objects.forEach { outputMetadata.add(it.metadata) }

            //TODO Нужно переработать в работу с отдельными источниками, а не всеми
//            val actualObjectName: String = if (metadataSourceSetName != sourceSetName) {
//                "$resourcesClassName$sourceSetName"
//            } else {
//                resourcesClassName
//            }

            val objectSpec: TypeSpec.Builder =
                TypeSpec.objectBuilder(resourcesClassName) // default: object MR
                    .addModifiers(KModifier.ACTUAL)
                    .addModifiers(visibilityModifier)

            containerGenerator.getImports()
                .plus(typesGenerators.flatMap { it.getImports() })
                .forEach { fileSpec.addImport(it.packageName, it.simpleNames) }

            val contentHash: String = (inputMetadata + outputMetadata)
                    .mapNotNull { it.contentHash() }
                    .calculateHash()
            objectSpec.addContentHashProperty(contentHash)

            objectSpec.also(containerGenerator::generateBeforeTypes)

            objects.forEach { objectSpec.addType(it.typeSpec) }

            objectSpec.also(containerGenerator::generateAfterTypes)

            fileSpec.addType(objectSpec.build())
        }

        // write file
        fileSpec.build().writeTo(sourcesGenerationDir)

        return outputMetadata
    }

    fun generateCommonKotlin(
        files: ResourcesFiles,
        inputMetadata: List<ContainerMetadata>
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

        // at lower level we not see any resources. so we should generate expects
        // read own metadata for object
        val objects: List<GenerationResult> = typesGenerators.mapNotNull { typeGenerator ->
            typeGenerator.generateExpectObject(
                metadata = ownMetadata,
            )
        }

        // if previous levels doesn't have resources should use "MR"
        // but if resources found should generate SourceSet "MR" object
        val expectObjectName: String = if (inputMetadata.isNotEmpty()) {
            "$resourcesClassName$sourceSetName"
        } else {
            resourcesClassName
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

    fun generateResources(metadata: List<ObjectMetadata>) {
        val resources: List<ResourceMetadata> = metadata.flatMap { it.resources }
        typesGenerators.forEach { it.generateFiles(resources) }
    }
}
