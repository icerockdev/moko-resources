/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.common

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FileSpec.Builder
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.KModifier.ACTUAL
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.MRGenerator.Generator
import dev.icerock.gradle.generator.common.GeneratedObject.GeneratedObjectModifier
import dev.icerock.gradle.generator.common.GeneratedObject.GeneratedObjectModifier.Companion.asGeneratedModifier
import dev.icerock.gradle.generator.common.GeneratedObject.GeneratedObjectType
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import dev.icerock.gradle.toModifier
import dev.icerock.gradle.utils.capitalize
import dev.icerock.gradle.utils.targetName
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import java.io.File

class CommonMRGenerator(
    project: Project,
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

        val lowerResourcesIsEmpty = settings.lowerResourcesFileTree.files.isEmpty()

        if (lowerResourcesIsEmpty) {
            generateExpectMRObjectFileSpec(
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
            .flatMap { it.getImports() }
            .plus(getImports())
            .forEach {
                logger.warn("Imports: ${it.packageName}, ${it.simpleName}")

                fileSpec.addImport(it.packageName, it.simpleName)
            }

        return fileSpec.build()
    }

    private fun generateExpectMRObjectFileSpec(
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
                modifier = KModifier.EXPECT.asGeneratedModifier()
            )
        )

        // When lower resources is empty, should generate expect MR object
        generators.forEach { generator ->

            val builder: TypeSpec.Builder = TypeSpec
                .objectBuilder(generator.mrObjectName) // resource name: example strings
                .addModifiers(visibilityModifier) // public/internal

            // Check upper directories on files with resources
            val upperDirectoryWithResources: FileCollection = settings.upperResourcesFileTree
            //                .filter { it.isDirectory }
            //                .filter { file: File ->
            //                    file.listFiles()?.isNotEmpty() ?: false
            //                }

            val generatedInterfaces: List<String> = if (!upperDirectoryWithResources.isEmpty) {
                val expectInterfacesList = mutableListOf<String>()

                // If upper directory has files of resources, need to generate
                // expect interface for sourceSet
                upperDirectoryWithResources.forEach { file: File ->
                    val interfaceName = getInterfaceName(
                        targetName = file.targetName,
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
                        packageName = "dev.icerock.moko.resources",
                        interfaceName
                    )
                )

                generatedObjectsList.add(
                    GeneratedObject(
                        name = interfaceName,
                        type = GeneratedObjectType.INTERFACE,
                        modifier = GeneratedObjectModifier.EXPECT
                    )
                )
            }

            mrClassSpec.addType(
                generator.generate(
                    assetsGenerationDir,
                    resourcesGenerationDir,
                    builder
                )
            )
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

        settings.ownResourcesFileTree.forEach {
            logger.warn("ownDirectoryWithResources: ${it.absoluteFile}")
        }

        generators.forEach { generator ->
            val interfaceName = getInterfaceName(
                targetName = targetName,
                generator = generator
            )

            val resourcesInterfaceBuilder: TypeSpec.Builder =
                TypeSpec.interfaceBuilder(interfaceName)
                    .addModifiers(visibilityModifier)
                    .addModifiers(ACTUAL)

            generatedObjectsList.add(
                GeneratedObject(
                    name = interfaceName,
                    type = GeneratedObjectType.INTERFACE,
                    modifier = GeneratedObjectModifier.ACTUAL
                )
            )

            fileSpec.addType(
                generator.generate(
                    assetsGenerationDir,
                    resourcesGenerationDir,
                    resourcesInterfaceBuilder
                )
            )
        }
    }
}

private fun getInterfaceName(targetName: String, generator: Generator): String{
    return targetName.capitalize() + generator.mrObjectName.capitalize()
}

data class GeneratedObject(
    val type: GeneratedObjectType,
    val name: String,
    val modifier: GeneratedObjectModifier,
) {
    val objectSpec: String
        get() = "${modifier.name.lowercase()} ${type.name.lowercase()} $name"

    enum class GeneratedObjectType {
        OBJECT,
        INTERFACE
    }

    enum class GeneratedObjectModifier {
        EXPECT,
        ACTUAL;

        companion object {
            fun KModifier.asGeneratedModifier(): GeneratedObjectModifier {
                return when (this) {
                    KModifier.EXPECT -> EXPECT
                    KModifier.ACTUAL -> ACTUAL
                    else -> throw GradleException("Invalid object modifier")
                }
            }
        }
    }
}
