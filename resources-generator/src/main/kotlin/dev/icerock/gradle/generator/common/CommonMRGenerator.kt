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
import dev.icerock.gradle.metadata.GeneratedObject
import dev.icerock.gradle.metadata.GeneratedObjectModifier
import dev.icerock.gradle.metadata.GeneratedObjectType
import dev.icerock.gradle.metadata.GeneratorType
import dev.icerock.gradle.metadata.GeneratorType.None
import dev.icerock.gradle.metadata.Metadata.createOutputMetadata
import dev.icerock.gradle.metadata.Metadata.readInputMetadata
import dev.icerock.gradle.metadata.getInterfaceName
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import dev.icerock.gradle.toModifier
import dev.icerock.gradle.utils.targetName
import org.gradle.api.Project
import org.gradle.api.file.FileTree
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

        val inputMetadata: MutableList<GeneratedObject> = mutableListOf()

        //Read list of generated resources on previous level
        if (settings.lowerResourcesFileTree.files.isNotEmpty()) {
            inputMetadata.addAll(
                readInputMetadata(
                    inputMetadataFiles = settings.inputMetadataFiles
                )
            )
        }

        inputMetadata.forEach {
            logger.warn("i prev: $it")
        }

        val fileSpec: Builder = FileSpec.builder(
            packageName = settings.packageName,
            fileName = settings.className
        )

        val visibilityModifier: KModifier = settings.visibility.toModifier()
        val generatedObjects = mutableListOf<GeneratedObject>()

        if (settings.lowerResourcesFileTree.files.isEmpty()) {
            // When lower resources is empty, should generate expect MR object
            generateExpectMRObjectFileSpec(
                inputMetadata = inputMetadata,
                generatedObjects = generatedObjects,
                resourcePackage = settings.packageName,
                visibilityModifier = visibilityModifier,
                fileSpec = fileSpec
            )
        } else {
            // If lower resources has files, when on lower level has expect object and
            // need to generate actual interface with fields
            generateActualInterfacesFileSpec(
                visibilityModifier = visibilityModifier,
                generatedObjects = generatedObjects,
                inputMetadata = inputMetadata,
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
            outputMetadataFile = settings.outputMetadataFile,
            generatedObjects = generatedObjects
        )

        return fileSpec.build()
    }

    private fun generateExpectMRObjectFileSpec(
        inputMetadata: MutableList<GeneratedObject>,
        generatedObjects: MutableList<GeneratedObject>,
        resourcePackage: String,
        visibilityModifier: KModifier,
        fileSpec: Builder,
    ) {
        // generated MR class structure:
        val mrClassSpec: TypeSpec.Builder =
            TypeSpec.objectBuilder(settings.className) // default: object MR
                .addModifiers(KModifier.EXPECT) // expect/actual
                .addModifiers(visibilityModifier) // public/internal

        val generatedExpectObjects = mutableListOf<GeneratedObject>()

        generateExpectInterfaces(
            visibilityModifier = visibilityModifier,
            upperResourcesFileTree = settings.upperResourcesFileTree,
            generatedObjects = generatedObjects,
            fileSpec = fileSpec
        )

        generators.forEach { generator: Generator ->
            val builder: TypeSpec.Builder = TypeSpec
                .objectBuilder(generator.mrObjectName) // resource name: example strings
                .addModifiers(visibilityModifier) // public/internal

            val expectInterfaces: List<GeneratedObject> = generatedObjects.filter {
                it.generatorType == generator.type && it.isExpectInterface
            }

            //Implement interfaces for generated expect object
            expectInterfaces.forEach { expectInterface ->
                builder.addSuperinterface(
                    ClassName(
                        packageName = resourcePackage,
                        expectInterface.name
                    )
                )
            }

            val generatedResourcesTypeSpec = generator.generate(
                project = project,
                inputMetadata = inputMetadata,
                generatedObjects = generatedExpectObjects,
                targetObject = GeneratedObject(
                    generatorType = generator.type,
                    modifier = GeneratedObjectModifier.Expect,
                    type = GeneratedObjectType.Object,
                    name = generator.mrObjectName,
                    interfaces = expectInterfaces.map { it.name }
                ),
                assetsGenerationDir = assetsGenerationDir,
                resourcesGenerationDir = resourcesGenerationDir,
                objectBuilder = builder
            )

            mrClassSpec.addType(generatedResourcesTypeSpec)
        }

        //  Add generated objects in MR
        generatedObjects.add(
            GeneratedObject(
                generatorType = None,
                type = GeneratedObjectType.Object,
                name = settings.className,
                modifier = GeneratedObjectModifier.Expect,
                objects = generatedExpectObjects
            )
        )

        processMRClass(mrClassSpec)

        val mrClass: TypeSpec = mrClassSpec.build()
        fileSpec.addType(mrClass)
    }

    private fun generateExpectInterfaces(
        visibilityModifier: KModifier,
        upperResourcesFileTree: FileTree,
        generatedObjects: MutableList<GeneratedObject>,
        fileSpec: Builder,
    ) {
        val expectInterfaces = mutableListOf<GeneratedObject>()

        upperResourcesFileTree.forEach {
            logger.warn("parentFile 1 = ${it.parentFile.name}") // need for images,colors, files, fonts, assets

            if (it.path.matches("^.*/strings.*.xml".toRegex())) {
                val generatorType: GeneratorType = GeneratorType.Strings
                val interfaceName: String = getInterfaceName(
                    targetName = it.targetName,
                    generatorType = generatorType
                )

                expectInterfaces.add(
                    GeneratedObject(
                        generatorType = generatorType,
                        type = GeneratedObjectType.Interface,
                        modifier = GeneratedObjectModifier.Expect,
                        name = interfaceName
                    )
                )
            } else if (it.path.matches("^.*/plurals.*.xml".toRegex())) {
                val generatorType: GeneratorType = GeneratorType.Plurals
                val interfaceName: String = getInterfaceName(
                    targetName = it.targetName,
                    generatorType = generatorType
                )

                expectInterfaces.add(
                    GeneratedObject(
                        generatorType = generatorType,
                        type = GeneratedObjectType.Interface,
                        modifier = GeneratedObjectModifier.Expect,
                        name = interfaceName
                    )
                )
            }
        }

        expectInterfaces.distinctBy {
            it.name
        }.forEach { expectInterface ->
            val resourcesInterface: TypeSpec =
                TypeSpec.interfaceBuilder(expectInterface.name)
                    .addModifiers(visibilityModifier)
                    .addModifiers(KModifier.EXPECT)
                    .build()

            fileSpec.addType(resourcesInterface)

            generatedObjects.add(expectInterface)
        }


        logger.warn("i")
        logger.warn("i")
        logger.warn("i")

        generatedObjects.forEach {
            logger.warn("metadata : $it")
        }

        logger.warn("i")
        logger.warn("i")
        logger.warn("i")
    }

    private fun generateActualInterfacesFileSpec(
        inputMetadata: MutableList<GeneratedObject>,
        generatedObjects: MutableList<GeneratedObject>,
        visibilityModifier: KModifier,
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
                project = project,
                targetObject = GeneratedObject(
                    generatorType = generator.type,
                    name = interfaceName,
                    type = GeneratedObjectType.Interface,
                    modifier = GeneratedObjectModifier.Actual,
                ),
                inputMetadata = inputMetadata,
                generatedObjects = generatedObjects,
                assetsGenerationDir = assetsGenerationDir,
                resourcesGenerationDir = resourcesGenerationDir,
                objectBuilder = resourcesInterfaceBuilder,
            )

            fileSpec.addType(generatedResources)
        }

        inputMetadata.forEach { metadata ->
            val hasInGeneratedActual = generatedObjects.firstOrNull { actualMetadata ->
                metadata.name == actualMetadata.name
            } != null

            if (!hasInGeneratedActual) {
                generatedObjects.add(metadata)
            }
        }
    }
}
