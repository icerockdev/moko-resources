/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.MRVisibility
import dev.icerock.gradle.metadata.GeneratedObject
import dev.icerock.gradle.metadata.GeneratorType
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.FileTree
import java.io.File

abstract class MRGenerator(
    protected val settings: Settings,
    internal val generators: List<Generator>,
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

        val file = generateFileSpec()
        file?.writeTo(sourcesGenerationDir)

        afterMRGeneration()
    }

    abstract fun generateFileSpec(): FileSpec?

    fun apply(project: Project): GenerateMultiplatformResourcesTask {
        //TODO add sourceSetName
        val name: String = project.displayName

        val genTask = project.tasks.create(
            "generateMR$name",
            GenerateMultiplatformResourcesTask::class.java
        ) {
            it.inputs.property("mokoSettingsPackageName", settings.packageName)
            it.inputs.property("mokoSettingsClassName", settings.className)
            it.inputs.property("mokoSettingsVisibility", settings.visibility)
            it.inputs.property(
                "mokoSettingsIosLocalizationRegion",
                settings.iosLocalizationRegion
            )
        }

        apply(generationTask = genTask, project = project)

        return genTask
    }

    protected open fun beforeMRGeneration() = Unit
    protected open fun afterMRGeneration() = Unit

    protected abstract fun getMRClassModifiers(): Array<KModifier>
    protected abstract fun apply(
        generationTask: GenerateMultiplatformResourcesTask,
        project: Project,
    )

    protected open fun processMRClass(mrClass: TypeSpec.Builder) {}
    protected open fun getImports(): List<ClassName> = emptyList()

    interface Generator : ObjectBodyExtendable {
        val mrObjectName: String
        val resourceClassName: ClassName
        val inputFiles: Iterable<File>

        val type: GeneratorType

        fun generate(
            project: Project,
            inputMetadata: MutableList<GeneratedObject>,
            generatedObjects: MutableList<GeneratedObject>, //TODO: Remove emptyList() after complete realisation
            targetObject: GeneratedObject,
            assetsGenerationDir: File,
            resourcesGenerationDir: File,
            objectBuilder: TypeSpec.Builder,
        ): TypeSpec

        fun getImports(): List<ClassName>
    }

    interface SourceSet {
        val name: String

        fun addSourceDir(directory: File)
        fun addResourcesDir(directory: File)
        fun addAssetsDir(directory: File)
    }

    data class Settings(
        val inputMetadataFiles: FileTree,
        val outputMetadataFile: File,
        val packageName: String,
        val className: String,
        val visibility: MRVisibility,
        val generatedDir: Directory,
        val isStrictLineBreaks: Boolean,
        val iosLocalizationRegion: String,
        val ownResourcesFileTree: FileTree,
        val lowerResourcesFileTree: FileTree,
        val upperResourcesFileTree: FileTree,
        val androidRClassPackage: String,
    )
}
