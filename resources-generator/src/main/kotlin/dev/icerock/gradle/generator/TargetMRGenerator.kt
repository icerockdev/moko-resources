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

        //Read list of generated resources on previous level
        val inputMetadata: List<GeneratedObject> = readInputMetadata(
            buildDir = project.buildDir,
            sourceSetName = settings.lowerResourcesFileTree.files.first().targetName
        )
// TODO: Check need of this for empty resources targets
//        if (hasNoResourcesForGenerator(inputMetadata)) return null

        val fileSpec: FileSpec.Builder = FileSpec.builder(
            packageName = settings.packageName,
            fileName = settings.className
        )

        val generatedObjects = mutableListOf<GeneratedObject>()

        inputMetadata.forEach {
            logger.warn("i prev: $it")
        }

        logger.warn("i mrClassSpec")

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

        //Add actual implementation of expect interfaces from previous level
        generateActualInterface(
            visibilityModifier = settings.visibility.toModifier(),
            generatedObjectsList = generatedObjects,
            fileSpec = fileSpec
        )

        generatedObjects.forEach {
            logger.warn("i generated objects item: ${it.name}")
        }

        //Implementation not realised expect interfaces
        expectInterfacesList.forEach { expectInterface ->
            val hasInGeneratedActualObjects = generatedObjects.firstOrNull {
                it.name == expectInterface.name
            } != null

            logger.warn("i has in generatedObjects: $hasInGeneratedActualObjects")

            if (hasInGeneratedActualObjects) return@forEach

            val resourcesInterface: TypeSpec =
                TypeSpec.interfaceBuilder(expectInterface.name)
                    .addModifiers(visibilityModifier)
                    .addModifiers(KModifier.ACTUAL)
                    .build()

            fileSpec.addType(resourcesInterface)
        }

        expectInterfacesList.forEach {
            logger.warn("i expectInterfacesList item: $it")
        }

        generators.forEach { generator ->
            val builder: TypeSpec.Builder = TypeSpec
                .objectBuilder(generator.mrObjectName)
                .addModifiers(visibilityModifier)

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
                    assetsGenerationDir,
                    resourcesGenerationDir,
                    builder
                )
            )
        }

        processMRClass(mrClassSpec)

        val mrClass = mrClassSpec.build()
        fileSpec.addType(mrClass)

        createOutputMetadata(
            buildDir = project.buildDir,
            sourceSetName = settings.ownResourcesFileTree.first().targetName,
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
        logger.warn("i res")
        settings.lowerResourcesFileTree.forEach {
            logger.warn("i lowerResourcesFileTree file: $it")
        }
        settings.upperResourcesFileTree.forEach {
            logger.warn("i upperResourcesFileTree file: $it")
        }
        settings.ownResourcesFileTree.forEach {
            logger.warn("i ownResourcesFileTree file: $it")
        }
        val targetName =
            settings.ownResourcesFileTree.files.firstOrNull()?.targetName ?: "androidMain"

        generators.forEach { generator ->
            val interfaceName = getInterfaceName(
                targetName = targetName,
                generator = generator
            )

            val resourcesInterfaceBuilder: TypeSpec.Builder =
                TypeSpec.interfaceBuilder(interfaceName)
                    .addModifiers(visibilityModifier)

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