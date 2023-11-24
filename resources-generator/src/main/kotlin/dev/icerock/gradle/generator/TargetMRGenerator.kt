package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.metadata.GeneratedObject
import dev.icerock.gradle.metadata.GeneratedObjectModifier
import dev.icerock.gradle.metadata.GeneratedObjectType
import dev.icerock.gradle.metadata.Metadata.readInputMetadata
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

        val inputMetadata: List<GeneratedObject> = readInputMetadata(
            buildDir = project.buildDir,
            sourceSetName = settings.lowerResourcesFileTree.files.first().targetName
        )
        val generatedObjects = mutableListOf<GeneratedObject>()

        inputMetadata.forEach {
            logger.warn("i prev: $it")

        }

        @Suppress("SpreadOperator")
        val mrClassSpec = TypeSpec.objectBuilder(settings.className)
            .addModifiers(*getMRClassModifiers())
            .addModifiers(visibilityModifier)

        val targetName = settings.ownResourcesFileTree.first().targetName

        generators.forEach { generator ->
            val builder: Builder = TypeSpec
                .objectBuilder(generator.mrObjectName)
                .addModifiers(visibilityModifier)

            inputMetadata.distinctBy {
                it.type == GeneratedObjectType.INTERFACE && it.modifier == GeneratedObjectModifier.EXPECT
            }.forEach { generatedObject: GeneratedObject ->
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

        val fileSpec = FileSpec.builder(
            packageName = settings.packageName,
            fileName = settings.className
        ).addType(mrClass)

        logger.warn("i fileSpec")

//        createOutputMetadata(
//            buildDir = project.buildDir,
//            sourceSetName = settings.ownResourcesFileTree.files.first().targetName,
//            generatedObjects = generatedObjects
//        )

        logger.warn("i fileSpec.addImport")

        generators
            .flatMap { it.getImports() }
            .plus(getImports())
            .forEach { fileSpec.addImport(it.packageName, it.simpleName) }

        return fileSpec.build()
    }

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