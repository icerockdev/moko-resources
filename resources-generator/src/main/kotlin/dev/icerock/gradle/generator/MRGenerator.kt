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
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import java.io.File

abstract class MRGenerator(
    generatedDir: Provider<Directory>,
    protected val sourceSet: SourceSet,
    protected val mrSettings: MRSettings,
    internal val generators: List<Generator>
) {
    internal val outputDir: Provider<Directory> = generatedDir.map { it.dir(sourceSet.name) }
    protected open val sourcesGenerationDir: Provider<Directory> = outputDir.map { it.dir("src") }
    protected open val resourcesGenerationDir: Provider<Directory> = outputDir.map { it.dir("res") }
    protected open val assetsGenerationDir: Provider<Directory> = outputDir.map {
        it.dir(AssetsGenerator.ASSETS_DIR_NAME)
    }

    internal fun generate() {
        val sourcesGenerationDir = sourcesGenerationDir.get().asFile
        val assetsGenerationDir = assetsGenerationDir.get().asFile
        val resourcesGenerationDir = resourcesGenerationDir.get().asFile

        sourcesGenerationDir.deleteRecursively()
        resourcesGenerationDir.deleteRecursively()
        assetsGenerationDir.deleteRecursively()

        beforeMRGeneration()

        @Suppress("SpreadOperator")
        val mrClassSpec = TypeSpec.objectBuilder(mrSettings.className.get())
            .addModifiers(*getMRClassModifiers())
            .addModifiers(mrSettings.visibility.get().toModifier())

        generators.forEach { generator ->
            val builder = TypeSpec.objectBuilder(generator.mrObjectName)
                .addModifiers(mrSettings.visibility.get().toModifier())

            val fileResourceInterfaceClassName =
                ClassName("dev.icerock.moko.resources", "ResourceContainer")
            builder.addSuperinterface(fileResourceInterfaceClassName.parameterizedBy(generator.resourceClassName))

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

        val fileSpec = FileSpec.builder(mrSettings.packageName.get(), mrSettings.className.get())
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
        setupGenerationDirs()
        val genTask = runCatching {
            project.tasks.getByName(genTaskName) as GenerateMultiplatformResourcesTask
        }.getOrNull() ?: project.tasks.create(
            genTaskName,
            GenerateMultiplatformResourcesTask::class.java
        ) {
            it.generator = this
            it.inputs.property("mokoSettingsPackageName", mrSettings.packageName)
            it.inputs.property("mokoSettingsClassName", mrSettings.className)
            it.inputs.property("mokoSettingsVisibility", mrSettings.visibility)
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

    private fun setupGenerationDirs() {
        sourceSet.addSourceDir(sourcesGenerationDir)
        sourceSet.addResourcesDir(resourcesGenerationDir)
        sourceSet.addAssetsDir(assetsGenerationDir)
    }

    interface Generator : ObjectBodyExtendable {
        val mrObjectName: String
        val resourceClassName: ClassName
        val inputFiles: Iterable<File>

        fun generate(
            assetsGenerationDir: File,
            resourcesGenerationDir: File,
            objectBuilder: TypeSpec.Builder
        ): TypeSpec

        fun getImports(): List<ClassName>
    }

    interface SourceSet {
        val name: String

        fun addSourceDir(directory: Provider<Directory>)
        fun addResourcesDir(directory: Provider<Directory>)
        fun addAssetsDir(directory: Provider<Directory>)
    }

    interface MRSettings {
        val packageName: Provider<String>
        val className: Provider<String>
        val visibility: Provider<MRVisibility>
    }
}
