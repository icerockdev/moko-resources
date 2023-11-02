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
import dev.icerock.gradle.toModifier
import org.gradle.api.file.Directory
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Provider
import java.io.File

abstract class MRGenerator(
    protected val sourceSet: Provider<SourceSet>,
    protected val settings: Settings,
    internal val generators: List<Generator>
) {
    internal val outputDir: File = settings.generatedDir.asFile
    protected open val sourcesGenerationDir: File = File(outputDir, "src")
    protected open val resourcesGenerationDir: File = File(outputDir, "res")
    protected open val assetsGenerationDir: File = File(outputDir, AssetsGenerator.ASSETS_DIR_NAME)

    internal fun generate() {
        sourcesGenerationDir.deleteRecursively()
        resourcesGenerationDir.deleteRecursively()
        assetsGenerationDir.deleteRecursively()

        beforeMRGeneration()

        val visibilityModifier: KModifier = settings.visibility.toModifier()

        @Suppress("SpreadOperator")
        val mrClassSpec = TypeSpec.objectBuilder(settings.className)
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
            packageName = settings.packageName,
            fileName = settings.className
        ).addType(mrClass)

        generators
            .flatMap { it.getImports() }
            .plus(getImports())
            .forEach { fileSpec.addImport(it.packageName, it.simpleName) }

        val file = fileSpec.build()
        file.writeTo(sourcesGenerationDir)

        afterMRGeneration()
    }

//    fun apply(project: Project): GenerateMultiplatformResourcesTask {
//        setupGenerationDirs()
//
//        val name: String = sourceSet.get().name
//        val genTask = project.tasks.create(
//            "generateMR$name",
//            GenerateMultiplatformResourcesTask::class.java
//        ) {
////            it.generator = this
//            it.inputs.property("mokoSettingsPackageName", settings.packageName)
//            it.inputs.property("mokoSettingsClassName", settings.className)
//            it.inputs.property("mokoSettingsVisibility", settings.visibility)
//        }
//
//        apply(generationTask = genTask, project = project)
//
//        return genTask
//    }

    protected open fun beforeMRGeneration() = Unit
    protected open fun afterMRGeneration() = Unit

    protected abstract fun getMRClassModifiers(): Array<KModifier>
//    protected abstract fun apply(
//        generationTask: GenerateMultiplatformResourcesTask,
//        project: Project
//    )

    protected open fun processMRClass(mrClass: TypeSpec.Builder) {}
    protected open fun getImports(): List<ClassName> = emptyList()

//    private fun setupGenerationDirs() {
//        with(sourceSet.get()) {
//            addSourceDir(sourcesGenerationDir)
//            addResourcesDir(resourcesGenerationDir)
//            addAssetsDir(assetsGenerationDir)
//        }
//    }

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
        val packageName: String,
        val className: String,
        val visibility: MRVisibility,
        val generatedDir: Directory,
        val isStrictLineBreaks: Boolean,
        val iosLocalizationRegion: String,
        val ownResourcesFileTree: FileTree,
        val lowerResourcesFileTree: FileTree,
        val upperResourcesFileTree: FileTree,
        val androidRClassPackage: String
    )
}
