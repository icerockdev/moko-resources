package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.common.toGeneratedVariables
import dev.icerock.gradle.metadata.GeneratedObject
import dev.icerock.gradle.metadata.GeneratedObjectModifier
import dev.icerock.gradle.metadata.GeneratedObjectType
import dev.icerock.gradle.metadata.Metadata.createOutputMetadata
import dev.icerock.gradle.metadata.Metadata.readInputMetadata
import dev.icerock.gradle.metadata.getInterfaceName
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import dev.icerock.gradle.toModifier
import dev.icerock.gradle.utils.targetName
import org.gradle.api.Project

abstract class TargetMRGenerator(
    private val project: Project,
    settings: Settings,
    generators: List<Generator>,
) : MRGenerator(
    settings = settings,
    generators = generators
) {
    val logger = project.logger

    override fun generateFileSpec(): FileSpec? {
        val visibilityModifier: KModifier = settings.visibility.toModifier()
        val inputMetadata: MutableList<GeneratedObject> = mutableListOf()

        //Read list of generated resources on previous level
        if (settings.lowerResourcesFileTree.files.isNotEmpty()) {
            inputMetadata.addAll(
                readInputMetadata(
                    inputMetadataFile = project.buildDir,
                    sourceSetName = settings.lowerResourcesFileTree.files.firstOrNull()?.targetName
                        ?: throw Exception("Lower resources is empty")
                )
            )
        }

        inputMetadata.forEach {
            logger.warn("i prev: $it")
        }

        // TODO: Check need of this for empty resources targets
        // if (hasNoResourcesForGenerator(inputMetadata)) return null

        val fileSpec: FileSpec.Builder = FileSpec.builder(
            packageName = settings.packageName,
            fileName = settings.className
        )

        val generatedObjects = mutableListOf<GeneratedObject>()

        @Suppress("SpreadOperator")
        val mrClassSpec = TypeSpec.objectBuilder(settings.className) // default: object MR
            .addModifiers(KModifier.ACTUAL)
            .addModifiers(visibilityModifier) // public/internal

        generatedObjects.add(
            GeneratedObject(
                type = GeneratedObjectType.OBJECT,
                name = settings.className,
                modifier = GeneratedObjectModifier.ACTUAL,
                properties = emptyList()
            )
        )

        val expectInterfacesList: List<GeneratedObject> = inputMetadata.filter {
            it.type == GeneratedObjectType.INTERFACE &&
                    it.modifier == GeneratedObjectModifier.EXPECT
        }

        // Add actual implementation of expect interfaces from previous levels
        if (inputMetadata.isNotEmpty()) {
            generateActualInterface(
                visibilityModifier = settings.visibility.toModifier(),
                generatedObjectsList = generatedObjects,
                fileSpec = fileSpec
            )

            // Generation of actual interfaces not realised on current level
            expectInterfacesList.forEach { expectInterface ->
                val hasInGeneratedActualObjects = generatedObjects.firstOrNull {
                    it.name == expectInterface.name
                } != null

                if (hasInGeneratedActualObjects) return@forEach

                val resourcesInterface: TypeSpec =
                    TypeSpec.interfaceBuilder(expectInterface.name)
                        .addModifiers(visibilityModifier)
                        .addModifiers(KModifier.ACTUAL)
                        .build()

                fileSpec.addType(resourcesInterface)
            }
        }

        generators.forEach { generator ->
            val builder: TypeSpec.Builder = TypeSpec
                .objectBuilder(generator.mrObjectName)
                .addModifiers(visibilityModifier)

            // Implement to object expect interfaces from previous
            // levels of resources
            expectInterfacesList.forEach { generatedObject: GeneratedObject ->
                builder.addSuperinterface(
                    ClassName(
                        packageName = settings.packageName,
                        generatedObject.name
                    )
                )
            }

            mrClassSpec.addType(
                generator.generate(
                    assetsGenerationDir = assetsGenerationDir,
                    resourcesGenerationDir = resourcesGenerationDir,
                    objectBuilder = builder,
                )
            )
        }

        processMRClass(mrClassSpec)

        val mrClass = mrClassSpec.build()
        fileSpec.addType(mrClass)

        createOutputMetadata(
            outputMetadataFile = project.buildDir,
            sourceSetName = settings.ownResourcesFileTree.firstOrNull()?.targetName
                ?: "unknownTarget",
            generatedObjects = generatedObjects
        )

        generators
            .flatMap { it.getImports() }
            .plus(getImports())
            .forEach { fileSpec.addImport(it.packageName, it.simpleName) }

        return fileSpec.build()
    }

    private fun generateActualInterface(
        visibilityModifier: KModifier,
        generatedObjectsList: MutableList<GeneratedObject>,
        fileSpec: FileSpec.Builder,
    ) {
        settings.lowerResourcesFileTree.forEach {
            logger.warn("i generateActualInterface lowerResourcesFileTree file: $it")
        }
        settings.upperResourcesFileTree.forEach {
            logger.warn("i generateActualInterface upperResourcesFileTree file: $it")
        }
        settings.ownResourcesFileTree.forEach {
            logger.warn("i generateActualInterface ownResourcesFileTree file: $it")
        }

        if (settings.ownResourcesFileTree.files.isEmpty()) return

        val targetName: String =
            settings.ownResourcesFileTree.files.firstOrNull()?.targetName ?: return

        generators.forEach { generator ->
            val interfaceName = getInterfaceName(
                targetName = targetName,
                generator = generator
            )

            val resourcesInterfaceBuilder: TypeSpec.Builder =
                TypeSpec.interfaceBuilder(interfaceName)
                    .addModifiers(visibilityModifier)

            val generatedResources: TypeSpec = generator.generate(
                metadata = generatedObjectsList,
                typeSpecIsInterface = true,
                assetsGenerationDir = assetsGenerationDir,
                resourcesGenerationDir = resourcesGenerationDir,
                objectBuilder = resourcesInterfaceBuilder
            )

            generatedObjectsList.add(
                GeneratedObject(
                    name = interfaceName,
                    type = GeneratedObjectType.INTERFACE,
                    modifier = GeneratedObjectModifier.ACTUAL,
                    properties = generatedResources.propertySpecs.toGeneratedVariables()
                )
            )

            fileSpec.addType(generatedResources)
        }
    }

    private fun hasNoResourcesForGenerator(inputMetadata: List<GeneratedObject>) =
        inputMetadata.isEmpty() && settings.ownResourcesFileTree.isEmpty

    override fun apply(generationTask: GenerateMultiplatformResourcesTask, project: Project) {
        val name = settings.ownResourcesFileTree.first().targetName
        val genTaskName = "generateMR$name"

        val genTask = runCatching {
            project.tasks.getByName(genTaskName) as GenerateMultiplatformResourcesTask
        }.getOrNull() ?: project.tasks.create(
            genTaskName,
            GenerateMultiplatformResourcesTask::class.java
        ) {
            it.generate()
        }

        apply(generationTask = genTask, project = project)
    }

    override fun getMRClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)
}