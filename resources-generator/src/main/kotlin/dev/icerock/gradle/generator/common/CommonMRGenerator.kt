/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.common

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FileSpec.Builder
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.ColorsGenerator
import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.PluralsGenerator
import dev.icerock.gradle.generator.StringsGenerator
import dev.icerock.gradle.metadata.GeneratedObject
import dev.icerock.gradle.metadata.GeneratedObjectModifier
import dev.icerock.gradle.metadata.GeneratedObjectType
import dev.icerock.gradle.metadata.GeneratorType
import dev.icerock.gradle.metadata.Metadata.createOutputMetadata
import dev.icerock.gradle.metadata.Metadata.readInputMetadata
import dev.icerock.gradle.metadata.addActual
import dev.icerock.gradle.metadata.getInterfaceName
import dev.icerock.gradle.metadata.resourcesIsEmpty
import dev.icerock.gradle.toModifier
import dev.icerock.gradle.utils.targetName
import org.gradle.api.Project
import org.gradle.api.file.FileTree

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

    // TODO not used. remove after complete migration of task configuration to Plugin configuration time
//    override fun apply(generationTask: GenerateMultiplatformResourcesTask, project: Project) {
//        project.tasks
//            .withType<KotlinCompile<*>>()
////            .matching { it.name.contains(sourceSet.name, ignoreCase = true) }
//            .configureEach { it.dependsOn(generationTask) }
//
//        project.rootProject.tasks.matching {
//            it.name.contains("prepareKotlinBuildScriptModel")
//        }.configureEach {
//            it.dependsOn(generationTask)
//        }
//
//        project.tasks
//            .matching { it.name.startsWith("metadata") && it.name.endsWith("ProcessResources") }
//            .configureEach {
//                it.dependsOn(generationTask)
//            }
//    }

    override fun generateFileSpec(): FileSpec? {
        val inputMetadata: MutableList<GeneratedObject> = mutableListOf()

        //Read list of generated resources on previous level
        if (settings.lowerResourcesFileTree.files.any { it.isFile }) {
            inputMetadata.addAll(
                readInputMetadata(
                    inputMetadataFiles = settings.inputMetadataFiles
                )
            )
        }

        // If previous level's doesn't has resources for generation
        // and target resource has no files - skip step
        if (resourcesIsEmpty(inputMetadata, settings)) return null

        val fileSpec: Builder = FileSpec.builder(
            packageName = settings.packageName,
            fileName = settings.className
        )

        val visibilityModifier: KModifier = settings.visibility.toModifier()
        val generatedObjects = mutableListOf<GeneratedObject>()

        if (settings.lowerResourcesFileTree.files.none { it.isFile }) {
            // When lower resources is empty, should generate expect MR object
            generateExpectMRObjects(
                inputMetadata = inputMetadata,
                generatedObjects = generatedObjects,
                resourcePackage = settings.packageName,
                visibilityModifier = visibilityModifier,
                fileSpec = fileSpec
            )
        } else {
            generatedObjects.addAll(inputMetadata)

            // If lower resources has files, when on lower level has expect object and
            // need to generate actual interface with fields
            generateActualInterfacesFileSpec(
                visibilityModifier = visibilityModifier,
                generatedObjects = generatedObjects,
                inputMetadata = inputMetadata,
                fileSpec = fileSpec
            )

            createOutputMetadata(
                outputMetadataFile = settings.outputMetadataFile,
                generatedObjects = generatedObjects
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
        project.logger.warn("OUTPUT METADATA: ${settings.outputMetadataFile}")

        return fileSpec.build()
    }

    private fun generateExpectMRObjects(
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
                .addSuperinterface(generator.resourceContainerClass.parameterizedBy(generator.resourceClassName))

            val expectInterfaces: List<GeneratedObject> = generatedObjects.filter {
                it.generatorType == generator.type && it.isExpectInterface
            }

            //Implement interfaces for generated expect object
            expectInterfaces.forEach { expectInterface ->
                builder.addSuperinterface(
                    ClassName(packageName = resourcePackage, expectInterface.name)
                )
            }

            val generatedResourcesTypeSpec: TypeSpec? = generator.generate(
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

            if (generatedResourcesTypeSpec != null) {
                mrClassSpec.addType(generatedResourcesTypeSpec)
            }
        }

        processMRClass(mrClassSpec)

        //Create file only if generated expect objects has expect MR object
        if (generatedExpectObjects.isNotEmpty()) {
            val mrClass: TypeSpec = mrClassSpec.build()
            fileSpec.addType(mrClass)

            //  Metadata: Add generated objects in MR
            generatedObjects.add(
                GeneratedObject(
                    generatorType = GeneratorType.None,
                    type = GeneratedObjectType.Object,
                    name = settings.className,
                    modifier = GeneratedObjectModifier.Expect,
                    objects = generatedExpectObjects
                )
            )
        }
    }

    private fun generateExpectInterfaces(
        visibilityModifier: KModifier,
        upperResourcesFileTree: FileTree,
        generatedObjects: MutableList<GeneratedObject>,
        fileSpec: Builder,
    ) {
        val expectInterfaces = mutableListOf<GeneratedObject>()

        upperResourcesFileTree.forEach {
            val generatorType: GeneratorType =
                if (it.path.matches(StringsGenerator.STRINGS_REGEX)) {
                    GeneratorType.Strings
                } else if (it.path.matches(PluralsGenerator.PLURALS_REGEX)) {
                    GeneratorType.Plurals
                } else if (it.path.matches(ColorsGenerator.COLORS_REGEX)) {
                    GeneratorType.Colors
                } else if (it.parentFile.name == "images") {
                    GeneratorType.Images
                } else if (it.parentFile.name == "files") {
                    GeneratorType.Files
                } else if (it.parentFile.name == "fonts") {
                    GeneratorType.Fonts
                } else return@forEach

            //TODO: Implement with generator

//            else if (it.parentFile.name == "assets") {
//                GeneratorType.Assets
//            }  else {
//                GeneratorType.None
//            }

            expectInterfaces.add(
                GeneratedObject(
                    generatorType = generatorType,
                    type = GeneratedObjectType.Interface,
                    modifier = GeneratedObjectModifier.Expect,
                    name = getInterfaceName(
                        targetName = it.targetName,
                        generatorType = generatorType
                    )
                )
            )
        }

        expectInterfaces.distinctBy { it.name }.forEach { expectInterface ->
            val resourcesInterface: TypeSpec =
                TypeSpec.interfaceBuilder(expectInterface.name)
                    .addModifiers(visibilityModifier)
                    .addModifiers(KModifier.EXPECT)
                    .build()

            fileSpec.addType(resourcesInterface)

            generatedObjects.add(expectInterface)
        }
    }

    private fun generateActualInterfacesFileSpec(
        inputMetadata: MutableList<GeneratedObject>,
        generatedObjects: MutableList<GeneratedObject>,
        visibilityModifier: KModifier,
        fileSpec: Builder,
    ) {
        val targetName: String =
            settings.ownResourcesFileTree.files.firstOrNull()?.targetName ?: return

        generators.forEach { generator ->
            val interfaceName = getInterfaceName(
                targetName = targetName,
                generatorType = generator.type
            )

            val resourcesInterfaceBuilder: TypeSpec.Builder =
                TypeSpec.interfaceBuilder(interfaceName)
                    .addModifiers(visibilityModifier)
                    .addModifiers(KModifier.ACTUAL)

            val generatedResourcesTypeSpec: TypeSpec? = generator.generate(
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

            if (generatedResourcesTypeSpec != null) {
                fileSpec.addType(generatedResourcesTypeSpec)
            }
        }

        // Collect generated actual interfaces and replaced expect in metadata
        val generatedActualInterfaces: MutableList<GeneratedObject> = mutableListOf()

        inputMetadata.forEach { metadata ->
            val hasInGeneratedActual = generatedObjects.firstOrNull { actualMetadata ->
                metadata.name == actualMetadata.name
            } != null

            if (!hasInGeneratedActual) {
                generatedActualInterfaces.add(metadata)
            }
        }

        generatedActualInterfaces.forEach { actualInterface ->
            inputMetadata.addActual(actualInterface)
        }
    }
}
