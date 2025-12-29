/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("Filename")

package dev.icerock.gradle.extra

import dev.icerock.gradle.MultiplatformResourcesPluginExtension
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import dev.icerock.gradle.utils.dependsOnObservable
import dev.icerock.gradle.utils.getAndroidRClassPackage
import dev.icerock.gradle.utils.isStrictLineBreaks
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.tooling.core.extrasKeyOf
import java.io.File

private fun mokoResourcesGenTaskKey() =
    extrasKeyOf<TaskProvider<GenerateMultiplatformResourcesTask>>("moko-resources-generate-task")

internal fun KotlinSourceSet.getOrRegisterGenerateResourcesTask(
    mrExtension: MultiplatformResourcesPluginExtension,
): TaskProvider<GenerateMultiplatformResourcesTask> {
    val currentProvider: TaskProvider<GenerateMultiplatformResourcesTask>? =
        this.extras[mokoResourcesGenTaskKey()]
    if (currentProvider != null) return currentProvider

    val resourcesSourceDirectory: SourceDirectorySet =
        getOrCreateResourcesSourceDirectory(mrExtension)

    val genTask: TaskProvider<GenerateMultiplatformResourcesTask> = registerGenerateTask(
        kotlinSourceSet = this,
        project = project,
        resourcesSourceDirectory = resourcesSourceDirectory,
        mrExtension = mrExtension
    )

    configureLowerDependencies(
        mrExtension = mrExtension,
        kotlinSourceSet = this,
        genTask = genTask
    )

    configureUpperDependencies(
        kotlinSourceSet = this,
        resourcesSourceSetName = this.name,
        resourcesSourceDirectory = resourcesSourceDirectory,
        mrExtension = mrExtension
    )

    configureTaskDependencies(
        kotlinSourceSet = this,
        genTask = genTask,
        mrExtension = mrExtension
    )

    this.extras[mokoResourcesGenTaskKey()] = genTask

    return genTask
}

private fun registerGenerateTask(
    kotlinSourceSet: KotlinSourceSet,
    project: Project,
    resourcesSourceDirectory: SourceDirectorySet,
    mrExtension: MultiplatformResourcesPluginExtension,
): TaskProvider<GenerateMultiplatformResourcesTask> {
    val generateTaskName: String = "generateMR" + kotlinSourceSet.name
    val generatedMokoResourcesDir = File(
        project.layout.buildDirectory.get().asFile,
        "generated/moko-resources"
    )

    val taskProvider: TaskProvider<GenerateMultiplatformResourcesTask> = project.tasks.register(
        generateTaskName,
        GenerateMultiplatformResourcesTask::class.java
    ) { generateTask ->
        generateTask.sourceSetName.set(kotlinSourceSet.name)

        val files: Set<File> = resourcesSourceDirectory.srcDirs
        generateTask.ownResources.setFrom(files)
        // make the generate task depend on tasks that the resourcesSourceDirectory depends on, e.g.
        // resource generating tasks.
        generateTask.dependsOn(resourcesSourceDirectory)

        generateTask.iosBaseLocalizationRegion.set(mrExtension.iosBaseLocalizationRegion)
        generateTask.resourcesClassName.set(mrExtension.resourcesClassName)
        generateTask.resourcesPackageName.set(mrExtension.resourcesPackage)
        generateTask.resourcesVisibility.set(mrExtension.resourcesVisibility)
        generateTask.androidRClassPackage.set(project.getAndroidRClassPackage())
        generateTask.strictLineBreaks.set(project.provider { project.isStrictLineBreaks })
        generateTask.outputMetadataFile.set(
            File(
                File(generatedMokoResourcesDir, "metadata"),
                "${kotlinSourceSet.name}-metadata.json"
            )
        )
        val sourceSetResourceDir = File(generatedMokoResourcesDir, kotlinSourceSet.name)
        generateTask.outputAssetsDir.set(File(sourceSetResourceDir, "assets"))
        generateTask.outputResourcesDir.set(File(sourceSetResourceDir, "res"))
        generateTask.outputSourcesDir.set(File(sourceSetResourceDir, "src"))

        // by default source set will be common
        generateTask.platformType.set(KotlinPlatformType.common.name)

        generateTask.onlyIf("generation on Android supported only for main flavor") { task ->
            task as GenerateMultiplatformResourcesTask

            val platform: String = task.platformType.get()

            if (platform != KotlinPlatformType.androidJvm.name) return@onlyIf true

            val flavor: String = task.androidSourceSetName.get()

            flavor in listOf(
                "androidMain",
                "androidTest",
                "androidInstrumentedTest",
                "main",
                "test"
            )
        }
    }

    return taskProvider
}

private fun configureLowerDependencies(
    mrExtension: MultiplatformResourcesPluginExtension,
    kotlinSourceSet: KotlinSourceSet,
    genTask: TaskProvider<GenerateMultiplatformResourcesTask>,
) {
    kotlinSourceSet.dependsOnObservable.forAll { dependsSourceSet ->
        val resourcesDir: SourceDirectorySet = dependsSourceSet
            .getOrCreateResourcesSourceDirectory(mrExtension)

        genTask.configure {
            val files: Set<File> = resourcesDir.srcDirs
            it.lowerResources.from(files)
        }

        configureLowerDependencies(
            mrExtension = mrExtension,
            kotlinSourceSet = dependsSourceSet,
            genTask = genTask
        )
    }
}

private fun configureUpperDependencies(
    kotlinSourceSet: KotlinSourceSet,
    resourcesSourceSetName: String,
    resourcesSourceDirectory: SourceDirectorySet,
    mrExtension: MultiplatformResourcesPluginExtension,
) {
    kotlinSourceSet.dependsOnObservable.forAll { dependsSourceSet ->
        val dependsGenTask: TaskProvider<GenerateMultiplatformResourcesTask> = dependsSourceSet
            .getOrRegisterGenerateResourcesTask(mrExtension)

        dependsGenTask.configure {
            val files: Set<File> = resourcesSourceDirectory.srcDirs
            it.upperSourceSets.put(resourcesSourceSetName, kotlinSourceSet.project.files(files))
        }

        configureUpperDependencies(
            kotlinSourceSet = dependsSourceSet,
            resourcesSourceSetName = resourcesSourceSetName,
            resourcesSourceDirectory = resourcesSourceDirectory,
            mrExtension = mrExtension
        )
    }
}

private fun configureTaskDependencies(
    kotlinSourceSet: KotlinSourceSet,
    genTask: TaskProvider<GenerateMultiplatformResourcesTask>,
    mrExtension: MultiplatformResourcesPluginExtension,
) {
    kotlinSourceSet.dependsOnObservable.forAll { dependsSourceSet ->
        val dependsGenTask: TaskProvider<GenerateMultiplatformResourcesTask> = dependsSourceSet
            .getOrRegisterGenerateResourcesTask(mrExtension)

        genTask.configure { resourceTask ->
            resourceTask.inputMetadataFiles.from(dependsGenTask.flatMap { it.outputMetadataFile })
        }

        configureTaskDependencies(
            kotlinSourceSet = dependsSourceSet,
            genTask = genTask,
            mrExtension = mrExtension
        )
    }
}
