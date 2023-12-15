package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.metadata.GeneratedObject
import dev.icerock.gradle.metadata.GeneratedObjectModifier
import dev.icerock.gradle.metadata.GeneratedObjectType
import dev.icerock.gradle.metadata.GeneratorType
import dev.icerock.gradle.metadata.Metadata.createOutputMetadata
import dev.icerock.gradle.metadata.Metadata.readInputMetadata
import dev.icerock.gradle.metadata.addActual
import dev.icerock.gradle.metadata.getActualInterfaces
import dev.icerock.gradle.metadata.getExpectInterfaces
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

    override fun generateFileSpec(): FileSpec? {
        val inputMetadata: MutableList<GeneratedObject> = mutableListOf()

        //Read list of generated resources on previous level
        inputMetadata.addAll(
            readInputMetadata(
                inputMetadataFiles = settings.inputMetadataFiles
            )
        )

        if (
            inputMetadata.isEmpty()
            && settings.ownResourcesFileTree.files.none {
                it.isFile
            }
        ) return null

        val visibilityModifier: KModifier = settings.visibility.toModifier()

        inputMetadata.forEach {
            logger.warn("i prev: $it")
        }

        val fileSpec: FileSpec.Builder = FileSpec.builder(
            packageName = settings.packageName,
            fileName = settings.className
        )

        @Suppress("SpreadOperator")
        val mrClassSpec = TypeSpec.objectBuilder(settings.className) // default: object MR
            .addModifiers(KModifier.ACTUAL)
            .addModifiers(visibilityModifier) // public/internal

        // Add actual implementation of expect interfaces from previous levels
        if (inputMetadata.isNotEmpty()) {
            generateActualInterface(
                inputMetadata = inputMetadata,
                visibilityModifier = settings.visibility.toModifier(),
                fileSpec = fileSpec
            )

            // Generation of actual interfaces not realised on current level
            val expectInterfaces = inputMetadata.getExpectInterfaces()

            expectInterfaces.forEach { expectInterface ->
                val resourcesInterface: TypeSpec =
                    TypeSpec.interfaceBuilder(expectInterface.name)
                        .addModifiers(visibilityModifier)
                        .addModifiers(KModifier.ACTUAL)
                        .build()

                inputMetadata.addActual(
                    actualObject = expectInterface.copy(modifier = GeneratedObjectModifier.Actual)
                )

                fileSpec.addType(resourcesInterface)
            }
        }

        val generatedActualObjects = mutableListOf<GeneratedObject>()

        generators.forEach { generator: Generator ->
            val builder: TypeSpec.Builder = TypeSpec
                .objectBuilder(generator.mrObjectName)
                .addModifiers(visibilityModifier)
                .addSuperinterface(generator.resourceContainerClass.parameterizedBy(generator.resourceClassName))

            // Implement to object expect interfaces from previous
            // levels of resources
            inputMetadata.getActualInterfaces(generator.type)
                .forEach { generatedObject: GeneratedObject ->
                    builder.addSuperinterface(
                        ClassName(
                            packageName = settings.packageName,
                            generatedObject.name
                        )
                    )
                }

            mrClassSpec.addType(
                generator.generate(
                    project = project,
                    inputMetadata = inputMetadata,
                    generatedObjects = generatedActualObjects,
                    targetObject = GeneratedObject(
                        generatorType = generator.type,
                        modifier = GeneratedObjectModifier.Actual,
                        type = GeneratedObjectType.Object,
                        name = generator.mrObjectName,
                        interfaces = getObjectInterfaces(
                            generatorType = generator.type,
                            objectName = generator.mrObjectName,
                            inputMetadata = inputMetadata
                        )
                    ),
                    assetsGenerationDir = assetsGenerationDir,
                    resourcesGenerationDir = resourcesGenerationDir,
                    objectBuilder = builder,
                )
            )
        }

        inputMetadata.addActual(
            actualObject = GeneratedObject(
                generatorType = GeneratorType.None,
                type = GeneratedObjectType.Object,
                name = settings.className,
                modifier = GeneratedObjectModifier.Actual,
                objects = generatedActualObjects
            )
        )

        processMRClass(mrClassSpec)

        val mrClass = mrClassSpec.build()
        fileSpec.addType(mrClass)

        createOutputMetadata(
            outputMetadataFile = settings.outputMetadataFile,
            generatedObjects = inputMetadata
        )

        generators
            .flatMap { it.getImports() }
            .plus(getImports())
            .forEach { fileSpec.addImport(it.packageName, it.simpleName) }

        return fileSpec.build()
    }

    private fun generateActualInterface(
        inputMetadata: MutableList<GeneratedObject>,
        visibilityModifier: KModifier,
        fileSpec: FileSpec.Builder,
    ) {
        if (settings.ownResourcesFileTree.files.isEmpty()) return

        val targetName: String = settings.ownResourcesFileTree.files
            .firstOrNull()?.targetName ?: return

        generators.forEach { generator ->
            val interfaceName = getInterfaceName(
                targetName = targetName,
                generator = generator
            )

            val resourcesInterfaceBuilder: TypeSpec.Builder =
                TypeSpec.interfaceBuilder(interfaceName)
                    .addModifiers(visibilityModifier)

            val generatedResources: TypeSpec = generator.generate(
                project = project,
                inputMetadata = inputMetadata,
                generatedObjects = inputMetadata,
                targetObject = GeneratedObject(
                    generatorType = generator.type,
                    modifier = GeneratedObjectModifier.Actual,
                    type = GeneratedObjectType.Interface,
                    name = interfaceName
                ),
                assetsGenerationDir = assetsGenerationDir,
                resourcesGenerationDir = resourcesGenerationDir,
                objectBuilder = resourcesInterfaceBuilder
            )

            if (generatedResources.propertySpecs.isNotEmpty()) {
                fileSpec.addType(generatedResources)
            }
        }
    }

    private fun getObjectInterfaces(
        generatorType: GeneratorType,
        objectName: String,
        inputMetadata: List<GeneratedObject>,
    ): List<String> {
        val interfaces = mutableListOf<String>()

        val mrObjects: List<GeneratedObject> = inputMetadata.filter {
            it.isExpectObject && it.generatorType == GeneratorType.None
        }

        mrObjects.forEach { mrObject ->
            mrObject.objects.forEach {
                if (it.generatorType == generatorType && it.name == objectName) {
                    interfaces.addAll(it.interfaces)
                }
            }
        }

        return interfaces.distinct()
    }
}