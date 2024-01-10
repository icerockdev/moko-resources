/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import dev.icerock.gradle.generator.platform.apple.setupAppleKLibResources
import dev.icerock.gradle.generator.platform.apple.setupFatFrameworkTasks
import dev.icerock.gradle.generator.platform.apple.setupFrameworkResources
import dev.icerock.gradle.generator.platform.apple.setupTestsResources
import dev.icerock.gradle.generator.platform.js.setupJsKLibResources
import dev.icerock.gradle.generator.platform.js.setupJsResources
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import dev.icerock.gradle.utils.dependsOnObservable
import dev.icerock.gradle.utils.getAndroidRClassPackage
import dev.icerock.gradle.utils.isStrictLineBreaks
import dev.icerock.gradle.utils.kotlinSourceSetsObservable
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.sources.android.findAndroidSourceSet
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.tooling.core.extrasKeyOf
import java.io.File

open class MultiplatformResourcesPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val mrExtension: MultiplatformResourcesPluginExtension = project.extensions.create(
            name = "multiplatformResources",
            type = MultiplatformResourcesPluginExtension::class
        ).apply { setupConvention(project) }

        project.plugins.withType(KotlinMultiplatformPluginWrapper::class) {
            val kmpExtension: KotlinMultiplatformExtension = project.extensions.getByType()

            configureKotlinTargetGenerator(
                project = project,
                mrExtension = mrExtension,
                kmpExtension = kmpExtension
            )
        }
    }

    private fun configureKotlinTargetGenerator(
        project: Project,
        mrExtension: MultiplatformResourcesPluginExtension,
        kmpExtension: KotlinMultiplatformExtension,
    ) {
        kmpExtension.sourceSets.configureEach { kotlinSourceSet: KotlinSourceSet ->
            val resourcesSourceDirectory: SourceDirectorySet = createMokoResourcesSourceSet(
                project = project,
                kotlinSourceSet = kotlinSourceSet
            )

            val genTask: TaskProvider<GenerateMultiplatformResourcesTask> = registerGenerateTask(
                kotlinSourceSet = kotlinSourceSet,
                project = project,
                resourcesSourceDirectory = resourcesSourceDirectory,
                mrExtension = mrExtension
            )

            configureUpperDependencies(
                kotlinSourceSet = kotlinSourceSet,
                resourcesSourceSetName = kotlinSourceSet.name,
                resourcesSourceDirectory = resourcesSourceDirectory
            )

            configureTaskDependencies(
                kotlinSourceSet = kotlinSourceSet,
                genTask = genTask
            )
        }

        kmpExtension.targets.configureEach { target ->
            target.compilations.configureEach { compilation ->
                compilation.kotlinSourceSetsObservable.forAll { sourceSet: KotlinSourceSet ->
                    val genTaskProvider: TaskProvider<GenerateMultiplatformResourcesTask> =
                        requireNotNull(sourceSet.extras[mokoResourcesGenTaskKey()])

                    genTaskProvider.configure {
                        it.platformType.set(target.platformType.name)

                        if (target is KotlinNativeTarget) {
                            it.konanTarget.set(target.konanTarget.name)
                        }
                    }

                    // Setup generated sourceSets, assets, resources as sourceSet of target
                    setupSourceSets(
                        target = target,
                        sourceSet = sourceSet,
                        genTaskProvider = genTaskProvider,
                        compilation = compilation
                    )

                    // Setup android specific tasks
                    setupAndroidTasks(
                        target = target,
                        sourceSet = sourceSet,
                        genTaskProvider = genTaskProvider,
                        compilation = compilation
                    )

                    // Setup apple specific tasks
                    setupAppleTasks(
                        target = target,
                        compilation = compilation
                    )

                    compilation.compileTaskProvider.configure { compileTask: KotlinCompilationTask<*> ->
                        compileTask.dependsOn(genTaskProvider)

                        if (compileTask is Kotlin2JsCompile) {
                            setupJsResources(
                                compileTask = compileTask,
                                resourcesGenerationDir = genTaskProvider.flatMap {
                                    it.outputResourcesDir.asFile
                                },
                                projectDir = project.provider { project.projectDir }
                            )
                            setupJsKLibResources(
                                compileTask = compileTask,
                                resourcesGenerationDir = genTaskProvider.flatMap {
                                    it.outputResourcesDir.asFile
                                }
                            )
                        }

                        if (target is KotlinNativeTarget) {
                            compileTask as KotlinNativeCompile

                            setupAppleKLibResources(
                                compileTask = compileTask,
                                assetsDirectory = genTaskProvider.flatMap {
                                    it.outputAssetsDir.asFile
                                },
                                resourcesGenerationDir = genTaskProvider.flatMap {
                                    it.outputResourcesDir.asFile
                                },
                                iosLocalizationRegion = mrExtension.iosBaseLocalizationRegion,
                                resourcePackageName = mrExtension.resourcesPackage,
                                acToolMinimalDeploymentTarget = mrExtension.acToolMinimalDeploymentTarget
                            )
                        }
                    }
                }
            }
        }

        project.tasks.register("generateMR") {
            it.group = "moko-resources"
            it.dependsOn(project.tasks.withType<GenerateMultiplatformResourcesTask>())
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    private fun setupSourceSets(
        target: KotlinTarget,
        sourceSet: KotlinSourceSet,
        genTaskProvider: TaskProvider<GenerateMultiplatformResourcesTask>,
        compilation: KotlinCompilation<*>,
    ) {
        val project: Project = target.project

        sourceSet.kotlin.srcDir(genTaskProvider.map { it.outputSourcesDir })

        when (target.platformType) {
            KotlinPlatformType.jvm, KotlinPlatformType.js -> {
                sourceSet.resources.srcDir(genTaskProvider.map { it.outputResourcesDir })
                sourceSet.resources.srcDir(genTaskProvider.map { it.outputAssetsDir })
            }

            KotlinPlatformType.androidJvm -> {
                target as KotlinAndroidTarget
                compilation as KotlinJvmAndroidCompilation

                val androidSourceSet: AndroidSourceSet = project.findAndroidSourceSet(sourceSet)
                    ?: throw GradleException("can't find android source set for $sourceSet")

                @Suppress("UnstableApiUsage")
                androidSourceSet.kotlin.srcDir(genTaskProvider.map { it.outputSourcesDir })
                @Suppress("UnstableApiUsage")
                androidSourceSet.res.srcDir(genTaskProvider.map { it.outputResourcesDir })
                @Suppress("UnstableApiUsage")
                androidSourceSet.assets.srcDir(genTaskProvider.map { it.outputAssetsDir })
            }

            KotlinPlatformType.common, KotlinPlatformType.native, KotlinPlatformType.wasm -> Unit
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    private fun setupAndroidTasks(
        target: KotlinTarget,
        sourceSet: KotlinSourceSet,
        genTaskProvider: TaskProvider<GenerateMultiplatformResourcesTask>,
        compilation: KotlinCompilation<*>,
    ) {
        if (target !is KotlinAndroidTarget) return

        compilation as KotlinJvmAndroidCompilation

        val project: Project = target.project

        val androidSourceSet: AndroidSourceSet = project.findAndroidSourceSet(sourceSet)
            ?: throw GradleException("can't find android source set for $sourceSet")

        // save android sourceSet name to skip build type specific tasks
        @Suppress("UnstableApiUsage")
        genTaskProvider.configure { it.androidSourceSetName.set(androidSourceSet.name) }

        // connect generateMR task with android preBuild
        @Suppress("DEPRECATION")
        val androidVariant: BaseVariant = compilation.androidVariant
        androidVariant.preBuildProvider.configure { it.dependsOn(genTaskProvider) }

        // TODO this way do more than required - we trigger generate all android related resources at all
        project.tasks.withType<AndroidLintAnalysisTask>().configureEach {
            it.dependsOn(genTaskProvider)
        }
    }

    private fun setupAppleTasks(
        target: KotlinTarget,
        compilation: KotlinCompilation<*>,
    ) {
        if (target !is KotlinNativeTarget) return

        compilation as KotlinNativeCompilation

        setupFrameworkResources(compilation = compilation)
        setupTestsResources(compilation = compilation)
        setupFatFrameworkTasks(compilation = compilation)
    }

    private fun createMokoResourcesSourceSet(
        project: Project,
        kotlinSourceSet: KotlinSourceSet,
    ): SourceDirectorySet {
        val sources = File(project.projectDir, "src")
        val resourceSourceSetDir = File(sources, kotlinSourceSet.name)
        val mokoResourcesDir = File(resourceSourceSetDir, "moko-resources")

        val resourcesSourceDirectory: SourceDirectorySet = project.objects.sourceDirectorySet(
            kotlinSourceSet.name + "MokoResources",
            "moko-resources for ${kotlinSourceSet.name} sourceSet"
        )
        resourcesSourceDirectory.srcDirs(mokoResourcesDir)

        kotlinSourceSet.extras[mokoResourcesSourceDirectoryKey()] = resourcesSourceDirectory

        return resourcesSourceDirectory
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
                flavor in listOf("main", "test", "androidTest")
            }
        }

        kotlinSourceSet.extras[mokoResourcesGenTaskKey()] = taskProvider

        return taskProvider
    }

    private fun configureUpperDependencies(
        kotlinSourceSet: KotlinSourceSet,
        resourcesSourceSetName: String,
        resourcesSourceDirectory: SourceDirectorySet,
    ) {
        kotlinSourceSet.dependsOnObservable.forAll { dependsSourceSet ->
            val dependsGenTask: TaskProvider<GenerateMultiplatformResourcesTask> = requireNotNull(
                dependsSourceSet.extras[mokoResourcesGenTaskKey()]
            )

            dependsGenTask.configure {
                val files: Set<File> = resourcesSourceDirectory.srcDirs
                it.upperSourceSets.put(resourcesSourceSetName, kotlinSourceSet.project.files(files))
            }

            configureUpperDependencies(
                kotlinSourceSet = dependsSourceSet,
                resourcesSourceSetName = resourcesSourceSetName,
                resourcesSourceDirectory = resourcesSourceDirectory
            )
        }
    }

    private fun configureTaskDependencies(
        kotlinSourceSet: KotlinSourceSet,
        genTask: TaskProvider<GenerateMultiplatformResourcesTask>,
    ) {
        kotlinSourceSet.dependsOnObservable.forAll { dependsSourceSet ->
            val dependsGenTask: TaskProvider<GenerateMultiplatformResourcesTask> = requireNotNull(
                dependsSourceSet.extras[mokoResourcesGenTaskKey()]
            )

            genTask.configure { resourceTask ->
                resourceTask.inputMetadataFiles.from(dependsGenTask.flatMap { it.outputMetadataFile })
            }
        }
    }
}

internal fun mokoResourcesSourceDirectoryKey() =
    extrasKeyOf<SourceDirectorySet>("moko-resources-source-directory")

internal fun mokoResourcesGenTaskKey() =
    extrasKeyOf<TaskProvider<GenerateMultiplatformResourcesTask>>("moko-resources-generate-task")
