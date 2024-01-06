/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.rework

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.rework.metadata.container.ActualInterfaceMetadata
import dev.icerock.gradle.rework.metadata.container.ContainerMetadata
import dev.icerock.gradle.rework.metadata.container.ExpectInterfaceMetadata
import dev.icerock.gradle.rework.metadata.container.ObjectMetadata
import dev.icerock.gradle.rework.metadata.resource.ResourceMetadata
import dev.icerock.gradle.utils.calculateHash
import java.io.File

class ResourcesGenerator(
    private val containerGenerator: PlatformContainerGenerator,
    private val typesGenerators: List<ResourceTypeGenerator<*>>,
    private val resourcesPackageName: String,
    private val resourcesClassName: String,
    private val sourceSetName: String,
    private val visibilityModifier: KModifier,
    private val sourcesGenerationDir: File
) {
    fun generateTargetKotlin(
        files: ResourcesFiles,
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

        val inputObject: ObjectMetadata? = inputMetadata.mapNotNull { it as? ObjectMetadata }
            .singleOrNull()
        if (inputObject == null) {
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

            val contentHash: String = (inputMetadata + outputMetadata).mapNotNull { it.contentHash() }
                .calculateHash()
            objectSpec.addContentHashProperty(contentHash)

            objectSpec.also(containerGenerator::generateBeforeTypes)

            objects.forEach { objectSpec.addType(it.typeSpec) }

            objectSpec.also(containerGenerator::generateAfterTypes)

            fileSpec.addType(objectSpec.build())
        } else {
            // for each input metadata we should generate actual

            // at first we should filter already generated expect-actual interfaces
            val expectInterfaces: List<ExpectInterfaceMetadata> = inputMetadata
                .mapNotNull { it as? ExpectInterfaceMetadata }
                .filter { expectInterface ->
                    inputMetadata.mapNotNull { it as? ActualInterfaceMetadata }
                        .none { expectInterface.name == it.name }
                }
            val alreadyActualInterfaces: List<String> = inputMetadata
                .mapNotNull { it as? ActualInterfaceMetadata }
                .map { it.name }

            // then we should generate actual interfaces with resources at our level
            val actualInterfaces: List<GenerationResult> = typesGenerators.mapNotNull { typeGenerator ->
                typeGenerator.generateActualInterface(
                    interfaces = expectInterfaces.filterNot { alreadyActualInterfaces.contains(it.name) },
                    metadata = ownMetadata,
                    sourceSet = sourceSetName
                )
            }

            actualInterfaces.forEach { result ->
                fileSpec.addType(result.typeSpec)
                outputMetadata.add(result.metadata)
            }

            // then we should generate dummy actual interfaces
            val dummyInterfaces: List<GenerationResult> = expectInterfaces.map { it.name }
                .minus(actualInterfaces.map { (it.metadata as ActualInterfaceMetadata).name }.toSet())
                .map { dummyInterfaceName ->
                    GenerationResult(
                        typeSpec = TypeSpec.interfaceBuilder(dummyInterfaceName)
                            .addModifiers(visibilityModifier)
                            .addModifiers(KModifier.ACTUAL)
                            .build(),
                        metadata = ActualInterfaceMetadata(
                            name = dummyInterfaceName,
                            resources = emptyList()
                        )
                    )
                }

            dummyInterfaces
                .forEach { result ->
                    fileSpec.addType(result.typeSpec)
                    outputMetadata.add(result.metadata)
                }

            // then we should generate actual object
            val objects: List<GenerationResult> = typesGenerators.mapNotNull { typeGenerator ->
                typeGenerator.generateActualObject(
                    objects = inputMetadata.mapNotNull { it as? ObjectMetadata },
                    interfaces = inputMetadata.mapNotNull { it as? ActualInterfaceMetadata } +
                            (actualInterfaces + dummyInterfaces).map { it.metadata as ActualInterfaceMetadata }
                )
            }
            objects.forEach { outputMetadata.add(it.metadata) }

            val objectSpec: TypeSpec.Builder =
                TypeSpec.objectBuilder(resourcesClassName) // default: object MR
                    .addModifiers(KModifier.ACTUAL)
                    .addModifiers(visibilityModifier)

            containerGenerator.getImports()
                .plus(typesGenerators.flatMap { it.getImports() })
                .forEach { fileSpec.addImport(it.packageName, it.simpleNames) }

            val contentHash: String = (inputMetadata + outputMetadata).mapNotNull { it.contentHash() }
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
        // so we should not generate expect and actuals too
        if (ownMetadata.isEmpty()) return emptyList()

        // here we see metadata on our level
        val fileSpec: FileSpec.Builder = FileSpec.builder(
            packageName = resourcesPackageName,
            fileName = resourcesClassName
        )

        val outputMetadata: MutableList<ContainerMetadata> = mutableListOf()

        if (inputMetadata.isEmpty()) {
            // at lower level we not see any resources. so we should generate expects
            // read upper files for interface
            val interfaces: List<GenerationResult> = typesGenerators
                .flatMap { it.generateExpectInterfaces(files) }
            // read own metadata for object
            val objects: List<GenerationResult> = typesGenerators.mapNotNull { typeGenerator ->
                typeGenerator.generateExpectObject(
                    metadata = ownMetadata,
                    interfaces = interfaces.mapNotNull { it.metadata as? ExpectInterfaceMetadata }
                )
            }

            interfaces.forEach { result ->
                fileSpec.addType(result.typeSpec)
                outputMetadata.add(result.metadata)
            }

            val objectSpec: TypeSpec.Builder =
                TypeSpec.objectBuilder(resourcesClassName) // default: object MR
                    .addModifiers(KModifier.EXPECT)
                    .addModifiers(visibilityModifier)

            objects.forEach { result ->
                objectSpec.addType(result.typeSpec)
                outputMetadata.add(result.metadata)
            }

            fileSpec.addType(objectSpec.build())
        } else {
            // at lower level already exist expect. we should generate our actual interfaces
            val interfaces: List<GenerationResult> = typesGenerators.mapNotNull { typeGenerator ->
                typeGenerator.generateActualInterface(
                    interfaces = inputMetadata.mapNotNull { it as? ExpectInterfaceMetadata },
                    metadata = ownMetadata,
                    sourceSet = sourceSetName
                )
            }

            interfaces.forEach { result ->
                fileSpec.addType(result.typeSpec)
                outputMetadata.add(result.metadata)
            }
        }

        // write file
        fileSpec.build().writeTo(sourcesGenerationDir)

        return outputMetadata
    }

    fun generateResources(metadata: List<ObjectMetadata>) {
        val resources: List<ResourceMetadata> = metadata.flatMap { it.resources }
        typesGenerators.forEach { it.generateFiles(resources) }
    }
}
