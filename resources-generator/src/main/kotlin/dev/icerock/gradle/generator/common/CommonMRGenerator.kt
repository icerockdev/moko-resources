/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.common

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FileSpec.Builder
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.MRGenerator
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

    override fun generateFileSpec(): FileSpec? {
        if (settings.ownResourcesFileTree.files.isEmpty()) return null

        val visibilityModifier: KModifier = settings.visibility.toModifier()
        val generatedObjectsList = mutableListOf<GeneratedObject>()

        val fileSpec: Builder = FileSpec.builder(
            packageName = settings.packageName,
            fileName = settings.className
        )

        val lowerResourcesIsEmpty = settings.lowerResourcesFileTree.files.isEmpty()

        if (!lowerResourcesIsEmpty) {
            generateExpectMRObjectFileSpec(
                visibilityModifier = visibilityModifier,
                generatedObjectsList = generatedObjectsList,
                fileSpec = fileSpec
            )
        } else {
            // If lower resources has files, when on lower level has expect object and
            // need to generate actual interface with fields

            //TODO actual
            // Добавить генерацию actual интерфейсов в которых будут поля

            // generated MR class structure:
//            val mrClassSpec: TypeSpec.Builder = TypeSpec.classBuilder(settings.className) // default: object MR

            val ownDirectoryWithResources: FileCollection = settings.ownResourcesFileTree

            ownDirectoryWithResources.forEach {
                logger.warn("ownDirectoryWithResources: ${it.absoluteFile}")
            }

            generators.forEach { generator ->
                ownDirectoryWithResources.forEach { file: File ->
                    val sourceSetName: String = file.targetName

                    val interfaceName =
                        sourceSetName.capitalize() + generator.mrObjectName.capitalize()
                    val resourcesInterfaceBuilder: TypeSpec.Builder =
                        TypeSpec.interfaceBuilder("${interfaceName}${generator.resourceClassName.simpleName}")
                            .addModifiers(visibilityModifier)
                            .addModifiers(KModifier.ACTUAL)

                    generatedObjectsList.add(
                        GeneratedObject(
                            type = GeneratedObjectType.INTERFACE,
                            name = interfaceName,
                            modifier = GeneratedObjectModifier.ACTUAL
                        )
                    )

                    resourcesInterfaceBuilder.addType(
                        generator.generate(
                            assetsGenerationDir,
                            resourcesGenerationDir,
                            resourcesInterfaceBuilder
                        )
                    )
                    processMRClass(resourcesInterfaceBuilder)

                    val mrClass: TypeSpec = resourcesInterfaceBuilder.build()
                    fileSpec.addType(mrClass)
                }
            }

//            processMRClass(mrClassSpec)
//
//            val mrClass: TypeSpec = mrClassSpec.build()
//            fileSpec.addType(mrClass)
        }

        generators
            .flatMap { it.getImports() }
            .plus(getImports())
            .forEach { fileSpec.addImport(it.packageName, it.simpleName) }

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
            //TODO: Спискок ресурсов содержит только файлы, без "пустых" директорий
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
                    val sourceSetName: String = file.targetName

                    val interfaceName =
                        sourceSetName.capitalize() + generator.mrObjectName.capitalize()
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

            generatedInterfaces.forEach { interfaceName ->
                builder.addSuperinterface(
                    ClassName(
                        packageName = "dev.icerock.moko.resources",
                        interfaceName
                    )
                )

                generatedObjectsList.add(
                    GeneratedObject(
                        type = GeneratedObjectType.INTERFACE,
                        name = interfaceName,
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
