/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.common

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FileSpec.Builder
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import dev.icerock.gradle.toModifier
import dev.icerock.gradle.utils.targetName
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

    @OptIn(DelicateKotlinPoetApi::class)
    override fun generateFileSpec(): FileSpec? {
        val visibilityModifier: KModifier = settings.visibility.toModifier()

        @Suppress("SpreadOperator")
        // generated MR class structure:
        val mrClassSpec: TypeSpec.Builder =
            TypeSpec.objectBuilder(settings.className) // default: object MR
                .addModifiers(*getMRClassModifiers()) // expect/actual
                .addModifiers(visibilityModifier) // public/internal

        val ownIsEmpty: Boolean = settings.ownResourcesFileTree.files.isEmpty()

        if (ownIsEmpty) return null

        val fileSpec: Builder = FileSpec.builder(
            packageName = settings.packageName,
            fileName = settings.className
        )

        val lowerIsEmpty = settings.lowerResourcesFileTree.files.isEmpty()

        if (lowerIsEmpty) {
            // Check upper directories on files with resources
            val upperDirectoryWithResources: FileCollection = settings.upperResourcesFileTree
//                .filter { it.isDirectory }
//                .filter { file: File ->
//                    file.listFiles()?.isNotEmpty() ?: false
//                }

            val lowerResourcesInterfaceList = mutableListOf<Pair<TypeName, CodeBlock>>()
            if (!upperDirectoryWithResources.isEmpty) {

                // If upper directory has files of resources, need to generate
                // expect interface for sourceSet
                upperDirectoryWithResources.forEach { file: File ->
                    logger.warn("i file - $file")

                    val sourceSetName: String = file.targetName

                    logger.warn("i sourceSetName - $sourceSetName")

                    val resourcesInterface: TypeSpec = TypeSpec.interfaceBuilder(sourceSetName)
                        .addModifiers(visibilityModifier)
                        .addModifiers(KModifier.EXPECT)
                        .build()

                    lowerResourcesInterfaceList.add(Pair(resourcesInterface.superclass, resourcesInterface.initializerBlock))
                    fileSpec.addType(resourcesInterface)
                }
            }

            // When lower resources is empty, should generate expect MR object
            generators.forEach { generator ->

                val builder: TypeSpec.Builder = TypeSpec
                    .objectBuilder(generator.mrObjectName) // resource name: example strings
                    .addModifiers(visibilityModifier) // public/internal

                lowerResourcesInterfaceList.forEach {
                    builder.addSuperinterface(it.first, it.second)
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

        } else {
            // If lower resources has files, when on lower level has expect object and
            // need to generate actual interface with fields

            //TODO actual
            // Добавить генерацию actual интерфейсов в которых будут поля
        }

        generators
            .flatMap { it.getImports() }
            .plus(getImports())
            .forEach { fileSpec.addImport(it.packageName, it.simpleName) }

        return fileSpec.build()
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
