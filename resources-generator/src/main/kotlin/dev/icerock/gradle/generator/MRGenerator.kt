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
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

abstract class MRGenerator(
    generatedDir: File,
    protected val sourceSet: Provider<SourceSet>,
    protected val settings: Settings,
    internal val generators: List<Generator>
) {
    internal val outputDir: Provider<File> = sourceSet.map { File(generatedDir, it.name) }
    protected open val sourcesGenerationDir: Provider<File> = outputDir.map { File(it, "src") }
    protected open val resourcesGenerationDir: Provider<File> = outputDir.map { File(it, "res") }
    protected open val assetsGenerationDir: Provider<File> = outputDir.map {
        File(it, AssetsGenerator.ASSETS_DIR_NAME)
    }

    internal fun generate() {
        val sourcesGenerationDir: File = sourcesGenerationDir.get()
        val assetsGenerationDir: File = assetsGenerationDir.get()
        val resourcesGenerationDir: File = resourcesGenerationDir.get()

        sourcesGenerationDir.deleteRecursively()
        resourcesGenerationDir.deleteRecursively()
        assetsGenerationDir.deleteRecursively()

        beforeMRGeneration()

        val visibilityModifier: KModifier = settings.visibility.get().toModifier()

        @Suppress("SpreadOperator")
        val mrClassSpec = TypeSpec.objectBuilder(settings.className.get())
            .addModifiers(*getMRClassModifiers())
            .addModifiers(visibilityModifier)

        generators.forEach { generator ->
            val builder = TypeSpec.objectBuilder(generator.mrObjectName)
                .addModifiers(visibilityModifier)

            val fileResourceInterfaceClassName = ClassName(
                packageName = "dev.icerock.moko.resources",
                "ResourceContainer"
            )
            builder.addSuperinterface(
                fileResourceInterfaceClassName.parameterizedBy(generator.resourceClassName)
            )

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
            packageName = settings.packageName.get(),
            fileName = settings.className.get()
        ).addType(mrClass)

        generators
            .flatMap { it.getImports() }
            .plus(getImports())
            .forEach { fileSpec.addImport(it.packageName, it.simpleName) }

        val file = fileSpec.build()
        file.writeTo(sourcesGenerationDir)

        afterMRGeneration()
    }

    fun apply(project: Project): GenerateMultiplatformResourcesTask {
        setupGenerationDirs()

        val name: String = sourceSet.get().name
        val genTask = project.tasks.create(
            "generateMR$name",
            GenerateMultiplatformResourcesTask::class.java
        ) {
            it.generator = this
            it.inputs.property("mokoSettingsPackageName", settings.packageName)
            it.inputs.property("mokoSettingsClassName", settings.className)
            it.inputs.property("mokoSettingsVisibility", settings.visibility)
        }

        apply(generationTask = genTask, project = project)

        return genTask
    }

    protected open fun beforeMRGeneration() = Unit
    protected open fun afterMRGeneration() = Unit

    protected abstract fun getMRClassModifiers(): Array<KModifier>
    protected abstract fun apply(
        generationTask: GenerateMultiplatformResourcesTask,
        project: Project
    )

    protected open fun processMRClass(mrClass: TypeSpec.Builder) {}
    protected open fun getImports(): List<ClassName> = emptyList()

    private fun setupGenerationDirs() {
        with(sourceSet.get()) {
            addSourceDir(sourcesGenerationDir)
            addResourcesDir(resourcesGenerationDir)
            addAssetsDir(assetsGenerationDir)
        }
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

        fun addSourceDir(directory: Provider<File>)
        fun addResourcesDir(directory: Provider<File>)
        fun addAssetsDir(directory: Provider<File>)
    }

    data class Settings(
        val packageName: Provider<String>,
        val className: Provider<String>,
        val visibility: Provider<MRVisibility>,
        val generatedDir: File,
        val isStrictLineBreaks: Boolean,
        val iosLocalizationRegion: Provider<String>,
        val resourcesSourceDirectory: SourceDirectorySet,
        val resourcesSourceSet: Provider<KotlinSourceSet>,
        val androidRClassPackage: Provider<String>
    )
}
