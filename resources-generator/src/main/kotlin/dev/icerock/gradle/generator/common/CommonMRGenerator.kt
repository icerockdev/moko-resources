/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.common

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FileSpec.Builder
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.metadata.GeneratedObject
import dev.icerock.gradle.metadata.GeneratedObjectModifier
import dev.icerock.gradle.metadata.GeneratedObjectType
import dev.icerock.gradle.metadata.GeneratedVariables
import dev.icerock.gradle.metadata.Metadata.createOutputMetadata
import dev.icerock.gradle.metadata.getInterfaceName
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import dev.icerock.gradle.toModifier
import dev.icerock.gradle.utils.targetName
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

class CommonMRGenerator(
    private val project: Project,
    settings: Settings,
    generators: List<Generator>,
) : MRGenerator(
    settings = settings,
    generators = generators
) {
    val logger = project.logger

    override fun getMRClassModifiers(): Array<KModifier> = arrayOf(KModifier.EXPECT)

    override fun apply(generationTask: GenerateMultiplatformResourcesTask, project: Project) {
        project.tasks
            .withType<KotlinCompile<*>>()
//            .matching { it.name.contains(sourceSet.name, ignoreCase = true) }
            .configureEach { it.dependsOn(generationTask) }

        project.rootProject.tasks.matching {
            it.name.contains("prepareKotlinBuildScriptModel")
        }.configureEach {
            it.dependsOn(generationTask)
        }

        project.tasks
            .matching { it.name.startsWith("metadata") && it.name.endsWith("ProcessResources") }
            .configureEach {
                it.dependsOn(generationTask)
            }
    }

    override fun generateFileSpec(): FileSpec? {
        if (settings.ownResourcesFileTree.files.isEmpty()) return null

        val visibilityModifier: KModifier = settings.visibility.toModifier()
        val generatedObjectsList = mutableListOf<GeneratedObject>()

        val fileSpec: Builder = FileSpec.builder(
            packageName = settings.packageName,
            fileName = settings.className
        )

        if (settings.lowerResourcesFileTree.files.isEmpty()) {
            // When lower resources is empty, should generate expect MR object
            generateExpectMRObjectFileSpec(
                resourcePackage = settings.packageName,
                visibilityModifier = visibilityModifier,
                generatedObjectsList = generatedObjectsList,
                fileSpec = fileSpec
            )
        } else {
            // If lower resources has files, when on lower level has expect object and
            // need to generate actual interface with fields
            generateActualInterfacesFileSpec(
                visibilityModifier = visibilityModifier,
                generatedObjectsList = generatedObjectsList,
                fileSpec = fileSpec
            )
        }

        generators
            .flatMap { generator -> generator.getImports() }
            .plus(getImports())
            .forEach { className ->
                fileSpec.addImport(className.packageName, className.simpleName)
            }

        createOutputMetadata(
            buildDir = project.buildDir,
            sourceSetName = settings.ownResourcesFileTree.first().targetName,
            generatedObjects = generatedObjectsList
        )

        return fileSpec.build()
    }

    private fun generateExpectMRObjectFileSpec(
        resourcePackage: String,
        visibilityModifier: KModifier,
        generatedObjectsList: MutableList<GeneratedObject>,
        fileSpec: Builder,
    ) {
        // generated MR class structure:
        val mrClassSpec: TypeSpec.Builder =
            TypeSpec.objectBuilder(settings.className) // default: object MR
                .addModifiers(KModifier.EXPECT) // expect/actual
                .addModifiers(visibilityModifier) // public/internal

        generatedObjectsList.add(
            GeneratedObject(
                type = GeneratedObjectType.OBJECT,
                name = settings.className,
                modifier = GeneratedObjectModifier.EXPECT,
                variables = emptyList()
            )
        )

        generators.forEach { generator ->
            val builder: TypeSpec.Builder = TypeSpec
                .objectBuilder(generator.mrObjectName) // resource name: example strings
                .addModifiers(visibilityModifier) // public/internal

            // Check upper directories on files with resources
            val targetsWithResources: List<String> = settings.upperResourcesFileTree.map {
                it.targetName
            }.distinct()
            //                .filter { it.isDirectory }
            //                .filter { file: File ->
            //                    file.listFiles()?.isNotEmpty() ?: false
            //                }

            val generatedInterfaces: List<String> = if (targetsWithResources.isNotEmpty()) {
                val expectInterfacesList = mutableListOf<String>()

                // If upper directory has files of resources, need to generate
                // expect interface for sourceSet
                targetsWithResources.forEach { targetName ->
                    val interfaceName = getInterfaceName(
                        targetName = targetName,
                        generator = generator
                    )

                    val resourcesInterface: TypeSpec =
                        TypeSpec.interfaceBuilder(interfaceName)
                            .addModifiers(visibilityModifier)
                            .addModifiers(KModifier.EXPECT)
                            .build()

                    expectInterfacesList.add("${resourcesInterface.name}")
                    fileSpec.addType(resourcesInterface)
                }

                expectInterfacesList
            } else emptyList()

            //Implement interfaces for generated expect object
            generatedInterfaces.forEach { interfaceName ->
                builder.addSuperinterface(
                    ClassName(
                        packageName = resourcePackage,
                        interfaceName
                    )
                )

                generatedObjectsList.add(
                    GeneratedObject(
                        name = interfaceName,
                        type = GeneratedObjectType.INTERFACE,
                        modifier = GeneratedObjectModifier.EXPECT,
                        variables = emptyList()
                    )
                )
            }

            val generatedResources = generator.generate(
                assetsGenerationDir = assetsGenerationDir,
                resourcesGenerationDir = resourcesGenerationDir,
                objectBuilder = builder
            )

            generatedObjectsList.add(
                GeneratedObject(
                    name = generator.mrObjectName,
                    type = GeneratedObjectType.OBJECT,
                    modifier = GeneratedObjectModifier.EXPECT,
                    variables = generatedResources.propertySpecs.toGeneratedVariables(),
                )
            )

            mrClassSpec.addType(generatedResources)
        }

        processMRClass(mrClassSpec)

        val mrClass: TypeSpec = mrClassSpec.build()
        fileSpec.addType(mrClass)
    }

    private fun generateActualInterfacesFileSpec(
        visibilityModifier: KModifier,
        generatedObjectsList: MutableList<GeneratedObject>,
        fileSpec: Builder,
    ) {
        val targetName = settings.ownResourcesFileTree.files.first().targetName

        generators.forEach { generator ->
            val interfaceName = getInterfaceName(
                targetName = targetName,
                generator = generator
            )

            val resourcesInterfaceBuilder: TypeSpec.Builder =
                TypeSpec.interfaceBuilder(interfaceName)
                    .addModifiers(visibilityModifier)
                    .addModifiers(KModifier.ACTUAL)

            val generatedResources: TypeSpec = generator.generate(
                assetsGenerationDir,
                resourcesGenerationDir,
                resourcesInterfaceBuilder
            )

            generatedObjectsList.add(
                GeneratedObject(
                    name = interfaceName,
                    type = GeneratedObjectType.INTERFACE,
                    modifier = GeneratedObjectModifier.ACTUAL,
                    variables = generatedResources.propertySpecs.toGeneratedVariables()
                )
            )

            fileSpec.addType(
                generatedResources
            )
        }
    }
}

private fun List<PropertySpec>.toGeneratedVariables() : List<GeneratedVariables> {
    return map {
        GeneratedVariables(
            name = it.name,
            modifier = if (it.modifiers.contains(KModifier.ACTUAL)) {
                GeneratedObjectModifier.ACTUAL
            } else {
                GeneratedObjectModifier.EXPECT
            }
        )
    }
}
