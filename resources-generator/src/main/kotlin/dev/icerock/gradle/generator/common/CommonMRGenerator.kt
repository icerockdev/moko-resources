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
import dev.icerock.gradle.generator.AssetsGenerator
import dev.icerock.gradle.generator.ColorsGenerator
import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.PluralsGenerator
import dev.icerock.gradle.generator.StringsGenerator
import dev.icerock.gradle.metadata.Metadata.createOutputMetadata
import dev.icerock.gradle.metadata.Metadata.readInputMetadata
import dev.icerock.gradle.metadata.getInterfaceName
import dev.icerock.gradle.metadata.model.GeneratedObject
import dev.icerock.gradle.metadata.model.GeneratedObjectModifier
import dev.icerock.gradle.metadata.model.GeneratedObjectType
import dev.icerock.gradle.metadata.model.GeneratorType
import dev.icerock.gradle.metadata.resourcesIsEmpty
import dev.icerock.gradle.toModifier
import dev.icerock.gradle.utils.targetName
import org.gradle.api.Project
import org.gradle.api.file.FileTree

class CommonMRGenerator(
    private val project: Project,
    private val sourceSetName: String,
    settings: Settings,
    generators: List<Generator>,
) : MRGenerator(
    settings = settings,
    generators = generators
) {
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
        val inputMetadata: List<GeneratedObject> = readInputMetadata(
            inputMetadataFiles = settings.inputMetadataFiles
        )

        // If previous level's doesn't has resources for generation
        // and target resource has no files - skip step
        if (resourcesIsEmpty(inputMetadata, settings)) return null

        val fileSpec: Builder = FileSpec.builder(
            packageName = settings.packageName,
            fileName = settings.className
        )

        val visibilityModifier: KModifier = settings.visibility.toModifier()

        val isShouldGenerateExpectObject: Boolean = settings.lowerResourcesFileTree.files
            .none { it.isFile }

        val results: List<GenerationResult> = if (isShouldGenerateExpectObject) {
            // When lower resources is empty, should generate expect MR object
            generateExpectMRObjects(
                inputMetadata = inputMetadata,
                resourcePackage = settings.packageName,
                visibilityModifier = visibilityModifier,
            )
        } else {
            // If lower resources has files, when on lower level has expect object and
            // need to generate actual interface with fields
            generateActualInterfacesFileSpec(
                visibilityModifier = visibilityModifier,
                inputMetadata = inputMetadata,
            )
        }

        if (results.isEmpty()) return null

        generators
            .flatMap { generator -> generator.getImports() }
            .plus(getImports())
            .forEach { className ->
                fileSpec.addImport(className.packageName, className.simpleName)
            }

        results.forEach { fileSpec.addType(it.typeSpec) }

        createOutputMetadata(
            outputMetadataFile = settings.outputMetadataFile,
            generatedObjects = results.map { it.metadata }
        )

        return fileSpec.build()
    }

    private fun generateExpectMRObjects(
        inputMetadata: List<GeneratedObject>,
        resourcePackage: String,
        visibilityModifier: KModifier,
    ): List<GenerationResult> {
        // generated MR class structure:
        val mrClassSpec: TypeSpec.Builder =
            TypeSpec.objectBuilder(settings.className) // default: object MR
                .addModifiers(KModifier.EXPECT)
                .addModifiers(visibilityModifier) // public/internal

        val expectInterfaceResults: List<GenerationResult> = generateExpectInterfaces(
            visibilityModifier = visibilityModifier,
            upperResourcesFileTree = settings.upperResourcesFileTree
        )
        val generatedObjects: MutableList<GeneratedObject> = mutableListOf()

        generators.forEach { generator: Generator ->
            val builder: TypeSpec.Builder = TypeSpec
                .objectBuilder(generator.mrObjectName) // resource name: example strings
                .addModifiers(visibilityModifier) // public/internal
                .addSuperinterface(generator.resourceContainerClass.parameterizedBy(generator.resourceClassName))

            val expectInterfaces: List<GeneratedObject> = expectInterfaceResults
                .filter { it.metadata.generatorType == generator.type }
                .map { it.metadata }

            //Implement interfaces for generated expect object
            expectInterfaces
                .forEach {
                    builder.addSuperinterface(
                        ClassName(packageName = resourcePackage, it.name)
                    )
                }

            val result: GenerationResult? = generator.generate(
                project = project,
                inputMetadata = inputMetadata,
                outputMetadata = GeneratedObject(
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

            if (result != null) {
                mrClassSpec.addType(result.typeSpec)
                generatedObjects.add(result.metadata)
            }
        }

        processMRClass(mrClassSpec)

        if (generatedObjects.isEmpty()) return expectInterfaceResults

        return expectInterfaceResults + GenerationResult(
            typeSpec = mrClassSpec.build(),
            //  Metadata: Add generated objects in MR
            metadata = GeneratedObject(
                generatorType = GeneratorType.None,
                type = GeneratedObjectType.Object,
                name = settings.className,
                modifier = GeneratedObjectModifier.Expect,
                objects = generatedObjects
            )
        )
    }

    private fun generateExpectInterfaces(
        visibilityModifier: KModifier,
        upperResourcesFileTree: FileTree,
    ): List<GenerationResult> {
        return upperResourcesFileTree.mapNotNull { file ->
            val generatorType: GeneratorType =
                if (file.path.matches(StringsGenerator.STRINGS_REGEX)) {
                    GeneratorType.Strings
                } else if (file.path.matches(PluralsGenerator.PLURALS_REGEX)) {
                    GeneratorType.Plurals
                } else if (file.path.matches(ColorsGenerator.COLORS_REGEX)) {
                    GeneratorType.Colors
                } else if (file.parentFile.name == "images") {
                    GeneratorType.Images
                } else if (file.parentFile.name == "files") {
                    GeneratorType.Files
                } else if (file.parentFile.name == "fonts") {
                    GeneratorType.Fonts
                } else if (file.path.matches(AssetsGenerator.ASSETS_REGEX)) {
                    GeneratorType.Assets
                } else return@mapNotNull null

            GeneratedObject(
                generatorType = generatorType,
                type = GeneratedObjectType.Interface,
                modifier = GeneratedObjectModifier.Expect,
                name = getInterfaceName(
                    sourceSetName = file.targetName,
                    generatorType = generatorType
                )
            )
        }.distinctBy { it.name }.map { expectInterface ->
            val resourcesInterface: TypeSpec = TypeSpec.interfaceBuilder(expectInterface.name)
                .addModifiers(visibilityModifier)
                .addModifiers(KModifier.EXPECT)
                .build()

            GenerationResult(
                typeSpec = resourcesInterface,
                metadata = expectInterface
            )
        }
    }

    private fun generateActualInterfacesFileSpec(
        inputMetadata: List<GeneratedObject>,
        visibilityModifier: KModifier,
    ): List<GenerationResult> {
        return generators.mapNotNull { generator ->
            val interfaceName: String = getInterfaceName(
                sourceSetName = sourceSetName,
                generatorType = generator.type
            )

            val resourcesInterfaceBuilder: TypeSpec.Builder =
                TypeSpec.interfaceBuilder(interfaceName)
                    .addModifiers(visibilityModifier)
                    .addModifiers(KModifier.ACTUAL)

            generator.generate(
                project = project,
                inputMetadata = inputMetadata,
                outputMetadata = GeneratedObject(
                    generatorType = generator.type,
                    name = interfaceName,
                    type = GeneratedObjectType.Interface,
                    modifier = GeneratedObjectModifier.Actual,
                ),
                assetsGenerationDir = assetsGenerationDir,
                resourcesGenerationDir = resourcesGenerationDir,
                objectBuilder = resourcesInterfaceBuilder,
            )
        }
    }
}
