/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.MRVisibility
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import dev.icerock.gradle.toModifier
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File

abstract class MRGenerator(
    generatedDir: File,
    protected val sourceSet: SourceSet,
    protected val mrSettings: MRSettings,
    internal val generators: List<Generator>
) {

    internal val outputDir = File(generatedDir, sourceSet.name)
    protected open val sourcesGenerationDir
        get() = File(outputDir, "src")
    protected open val resourcesGenerationDir
        get() = File(outputDir, "res")

    init {
        setupGenerationDirs()
    }

    private fun setupGenerationDirs() {
        sourcesGenerationDir.mkdirs()
        sourceSet.addSourceDir(sourcesGenerationDir)

        resourcesGenerationDir.mkdirs()
        sourceSet.addResourcesDir(resourcesGenerationDir)
    }

    internal fun generate() {
        sourcesGenerationDir.deleteRecursively()
        resourcesGenerationDir.deleteRecursively()

        beforeMRGeneration()

        @Suppress("SpreadOperator")
        val mrClassSpec = TypeSpec.objectBuilder(mrClassName)
            .addModifiers(*getMRClassModifiers())
            .addModifiers(mrSettings.visibility.toModifier())

        generators.forEach { generator ->
            val builder = TypeSpec.objectBuilder(generator.mrObjectName)
                .addModifiers(mrSettings.visibility.toModifier())

            val fileResourceInterfaceClassName =
                ClassName("dev.icerock.moko.resources", "ResourceContainer")
            builder.addSuperinterface(fileResourceInterfaceClassName.parameterizedBy(generator.resourceClassName))

            mrClassSpec.addType(generator.generate(resourcesGenerationDir, builder))
        }

        processMRClass(mrClassSpec)

        val mrClass = mrClassSpec.build()

        val fileSpec = FileSpec.builder(mrSettings.packageName, mrClassName)
            .addType(mrClass)

        generators
            .flatMap { it.getImports() }
            .plus(getImports())
            .forEach { fileSpec.addImport(it.packageName, it.simpleName) }

        val file = fileSpec.build()
        file.writeTo(sourcesGenerationDir)

        afterMRGeneration()
    }

    fun apply(project: Project): GenerateMultiplatformResourcesTask {
        val name = sourceSet.name
        val genTaskName = "generateMR$name"
        val genTask = runCatching {
            project.tasks.getByName(genTaskName) as GenerateMultiplatformResourcesTask
        }.getOrNull() ?: project.tasks.create(
            genTaskName,
            GenerateMultiplatformResourcesTask::class.java
        ) {
            it.generator = this
        }

        apply(generationTask = genTask, project = project)

        return genTask
    }

    protected open fun beforeMRGeneration() = Unit
    protected open fun afterMRGeneration() = Unit

    protected abstract fun getMRClassModifiers(): Array<KModifier>
    protected abstract fun apply(generationTask: Task, project: Project)

    protected open fun processMRClass(mrClass: TypeSpec.Builder) {}
    protected open fun getImports(): List<ClassName> = emptyList()

    private companion object {
        const val mrClassName = "MR"
    }

    interface Generator : ObjectBodyExtendable {
        val mrObjectName: String
        val resourceClassName: ClassName
        val inputFiles: Iterable<File>

        fun generate(resourcesGenerationDir: File, objectBuilder: TypeSpec.Builder): TypeSpec
        fun getImports(): List<ClassName>
    }

    interface SourceSet {
        val name: String

        fun addSourceDir(directory: File)
        fun addResourcesDir(directory: File)
    }

    data class MRSettings(
        val packageName: String,
        val visibility: MRVisibility
    )
}
