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
import dev.icerock.gradle.metadata.getExpectInterfaces
import dev.icerock.gradle.metadata.getGeneratorInterfaces
import dev.icerock.gradle.metadata.getInterfaceName
import dev.icerock.gradle.metadata.isNotEmptyMetadata
import dev.icerock.gradle.metadata.resourcesIsEmpty
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

    // TODO not used. remove after complete migration of task configuration to Plugin configuration time
//    override fun apply(generationTask: GenerateMultiplatformResourcesTask, project: Project) {
//        val name = settings.ownResourcesFileTree.first().targetName
//        val genTaskName = "generateMR$name"
//
//        val genTask = runCatching {
//            project.tasks.getByName(genTaskName) as GenerateMultiplatformResourcesTask
//        }.getOrNull() ?: project.tasks.create(
//            genTaskName,
//            GenerateMultiplatformResourcesTask::class.java
//        ) {
//            it.generate()
//        }
//
//        apply(generationTask = genTask, project = project)
//    }

    override fun getMRClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun generateFileSpec(): FileSpec? {
        val inputMetadata: MutableList<GeneratedObject> = mutableListOf()

        //Read list of generated resources on previous level's
        inputMetadata.addAll(
            readInputMetadata(
                inputMetadataFiles = settings.inputMetadataFiles
            )
        )

        // Check resources for generation: if lower resources is empty
        // and own resources has no files - skip step
        if (resourcesIsEmpty(inputMetadata, settings)) return null

        //TODO: Добавить обработку кейса, когда данные есть только в таргете, т.е. генерация MR объекта
        // Если есть свои ресурсы, но нет нижестоящих - просто MR
        // Если есть нижестоящие - actual MR
        // [checked] Нет своих и нет нижестоящих - ничего не генерировать

        // If previous levels has resources, need generate actual objects
        val needGenerateActual: Boolean = inputMetadata.isNotEmptyMetadata()

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
            .addModifiers(visibilityModifier) // public/internal

        if (needGenerateActual) {
            mrClassSpec.addModifiers(KModifier.ACTUAL)
        }

        // Add actual implementation of expect interfaces from previous levels
        if (inputMetadata.isNotEmpty()) {
            generateTargetInterfaces(
                inputMetadata = inputMetadata,
                visibilityModifier = settings.visibility.toModifier(),
                generateActualObject = needGenerateActual,
                fileSpec = fileSpec
            )

            // Generation of actual interfaces not realised on current level
            val expectInterfaces = inputMetadata.getExpectInterfaces()

            expectInterfaces.forEach { expectInterface ->
                val actualInterfaceTypeSpec: TypeSpec =
                    TypeSpec.interfaceBuilder(expectInterface.name)
                        .addModifiers(visibilityModifier)
                        .addModifiers(KModifier.ACTUAL)
                        .build()

                fileSpec.addType(actualInterfaceTypeSpec)
                inputMetadata.addActual(
                    actualObject = expectInterface.copy(modifier = GeneratedObjectModifier.Actual)
                )
            }
        }

        val generatedActualObjects = mutableListOf<GeneratedObject>()

        generators.forEach { generator: Generator ->
            val objectBuilder: TypeSpec.Builder = TypeSpec
                .objectBuilder(generator.mrObjectName)
                .addModifiers(visibilityModifier)
                .addSuperinterface(
                    superinterface = generator.resourceContainerClass.parameterizedBy(
                        generator.resourceClassName
                    )
                )

            // Implement to object expect interfaces from previous
            // levels of resources
            inputMetadata.getGeneratorInterfaces(generator.type)
                .forEach { generatedObject: GeneratedObject ->
                    objectBuilder.addSuperinterface(
                        ClassName(
                            packageName = settings.packageName,
                            generatedObject.name
                        )
                    )
                }

            val generatedResourceObjectTypeSpec: TypeSpec? = generator.generate(
                project = project,
                inputMetadata = inputMetadata,
                generatedObjects = generatedActualObjects,
                targetObject = GeneratedObject(
                    generatorType = generator.type,
                    modifier = if (needGenerateActual) {
                        GeneratedObjectModifier.Actual
                    } else {
                        GeneratedObjectModifier.None
                    },
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
                objectBuilder = objectBuilder,
            )

            if (generatedResourceObjectTypeSpec != null) {
                mrClassSpec.addType(generatedResourceObjectTypeSpec)
            }
        }

        processMRClass(mrClassSpec)

        if (generatedActualObjects.isNotEmpty()){
            val mrClass = mrClassSpec.build()
            fileSpec.addType(mrClass)

            inputMetadata.addActual(
                actualObject = GeneratedObject(
                    generatorType = GeneratorType.None,
                    type = GeneratedObjectType.Object,
                    name = settings.className,
                    modifier = GeneratedObjectModifier.Actual,
                    objects = generatedActualObjects
                )
            )
        }

        generators
            .flatMap { it.getImports() }
            .plus(getImports())
            .forEach { fileSpec.addImport(it.packageName, it.simpleName) }

        createOutputMetadata(
            outputMetadataFile = settings.outputMetadataFile,
            generatedObjects = inputMetadata
        )

        return fileSpec.build()
    }

    private fun generateTargetInterfaces(
        inputMetadata: MutableList<GeneratedObject>,
        visibilityModifier: KModifier,
        generateActualObject: Boolean,
        fileSpec: FileSpec.Builder,
    ) {
        if (settings.ownResourcesFileTree.files.isEmpty()) return

        val targetName: String = settings.ownResourcesFileTree.files
            .firstOrNull()?.targetName ?: return

        generators.forEach { generator ->
            val interfaceName = getInterfaceName(
                targetName = targetName,
                generatorType = generator.type
            )

            val resourcesInterfaceBuilder: TypeSpec.Builder =
                TypeSpec.interfaceBuilder(interfaceName)
                    .addModifiers(visibilityModifier)

            val generatedResourcesTypeSpec: TypeSpec? = generator.generate(
                project = project,
                inputMetadata = inputMetadata,
                generatedObjects = inputMetadata,
                targetObject = GeneratedObject(
                    generatorType = generator.type,
                    modifier = if (generateActualObject) {
                        GeneratedObjectModifier.Actual
                    } else {
                        GeneratedObjectModifier.None
                    },
                    type = GeneratedObjectType.Interface,
                    name = interfaceName
                ),
                assetsGenerationDir = assetsGenerationDir,
                resourcesGenerationDir = resourcesGenerationDir,
                objectBuilder = resourcesInterfaceBuilder
            )

            if (generatedResourcesTypeSpec != null) {
                fileSpec.addType(generatedResourcesTypeSpec)
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