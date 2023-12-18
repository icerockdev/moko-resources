/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.gradle.api.BaseVariant
import dev.icerock.gradle.generator.apple.setupAppleKLibResources
import dev.icerock.gradle.generator.apple.setupFatFrameworkTasks
import dev.icerock.gradle.generator.apple.setupFrameworkResources
import dev.icerock.gradle.generator.apple.setupTestsResources
import dev.icerock.gradle.generator.js.setupJsKLibResources
import dev.icerock.gradle.generator.js.setupJsResources
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import dev.icerock.gradle.utils.dependsOnObservable
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
            project.logger.warn("i kmpExtension.sourceSets: ${kotlinSourceSet.name}")

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

            configureLowerDependencies(
                kotlinSourceSet = kotlinSourceSet,
                genTask = genTask
            )

            configureUpperDependencies(
                kotlinSourceSet = kotlinSourceSet,
                resourcesSourceDirectory = resourcesSourceDirectory
            )

            configureTaskDependencies(
                kotlinSourceSet = kotlinSourceSet,
                genTask = genTask
            )
        }

        // If use configureEach, we get exception of task context in project, on configuration step
        kmpExtension.targets.matching {
            it.platformType == KotlinPlatformType.native
        }.configureEach { target ->
            target.compilations.configureEach { compilation ->

                compilation as KotlinNativeCompilation

                setupFrameworkResources(compilation = compilation)
                setupTestsResources(compilation = compilation)
                setupFatFrameworkTasks(compilation = compilation)
            }
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

                    compilation.compileTaskProvider.configure { compileTask: KotlinCompilationTask<*> ->
                        compileTask.dependsOn(genTaskProvider)

                        if (compileTask is Kotlin2JsCompile) {
                            setupJsResources(
                                compileTask = compileTask,
                                resourcesGenerationDir = genTaskProvider.flatMap {
                                    it.outputResourcesDir.asFile
                                }
                            )
                            setupJsKLibResources(
                                compileTask = compileTask,
                                resourcesGenerationDir = genTaskProvider.flatMap {
                                    it.outputResourcesDir.asFile
                                }
                            )
                        }

                        if (target is KotlinNativeTarget) {
                            compilation as KotlinNativeCompilation
                            compileTask as KotlinNativeCompile

                            setupAppleKLibResources(
                                compileTask = compileTask,
                                assetsDirectory = genTaskProvider.flatMap {
                                    it.outputAssetsDir.asFile
                                },
                                resourcesGenerationDir = genTaskProvider.flatMap {
                                    it.outputResourcesDir.asFile
                                },
                                iosLocalizationRegion = genTaskProvider.flatMap {
                                    it.iosBaseLocalizationRegion
                                },
                                resourcePackageName = genTaskProvider.flatMap {
                                    it.resourcesPackageName
                                }
                            )
                        }
                    }
                }
            }
        }


//        val resourcesSourceDirectory: SourceDirectorySet = project.objects.sourceDirectorySet(
//            "moko-resources",
//            "moko-resources"
//        )
//
//        val ss = mrExtension.resourcesSourceSet
//        project.logger.warn("source set $ss")
//
//        kmpExtension.sourceSets
//            .matching { it.name == mrExtension.resourcesSourceSet }
//            .configureEach { kotlinSourceSet ->
//                val sources = File(project.projectDir, "src")
//                val resourceSourceSetDir = File(sources, kotlinSourceSet.name)
//                val mokoResourcesDir = File(resourceSourceSetDir, "moko-resources")
//
//                resourcesSourceDirectory.srcDirs(mokoResourcesDir)
//            }
//
//        val generatedDir = File(project.buildDir, "generated/moko-resources")
//
//        val settings = MRGenerator.Settings(
//            packageName = mrExtension.resourcesPackage,
//            className = mrExtension.resourcesClassName,
//            visibility = mrExtension.resourcesVisibility,
//            generatedDir = generatedDir,
//            isStrictLineBreaks = project.isStrictLineBreaks,
//            iosLocalizationRegion = mrExtension.iosBaseLocalizationRegion,
//            resourcesSourceDirectory = resourcesSourceDirectory,
//            androidRClassPackage = project.getAndroidRClassPackage()
//        )
//
//        kmpExtension.targets.configureEach { kotlinTarget ->
//            var found = false
//
//            kotlinTarget.compilations.configureEach { compilation ->
//                compilation.kotlinSourceSetsObservable.forAll { kotlinSourceSet ->
//                    kotlinSourceSet.whenDependsOn(mrExtension.resourcesSourceSet) {
//                        if(found) return@whenDependsOn
//
//                        found = true
//                        configureKotlinTargetGenerator(
//                            target = kotlinTarget,
//                            settings = settings
//                        )
//                    }
//                }
//            }
//        }
//
//        setupProjectForApple(project)
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
        compilation: KotlinCompilation<*>
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
        val generatedMokoResourcesDir = File(project.buildDir, "generated/moko-resources")

        val taskProvider: TaskProvider<GenerateMultiplatformResourcesTask> = project.tasks.register(
            generateTaskName,
            GenerateMultiplatformResourcesTask::class.java
        ) { generateTask ->
            val files: Set<File> = resourcesSourceDirectory.srcDirs
            generateTask.ownResources.setFrom(files)

            generateTask.iosBaseLocalizationRegion.set(mrExtension.iosBaseLocalizationRegion)
            generateTask.resourcesClassName.set(mrExtension.resourcesClassName)
            generateTask.resourcesPackageName.set(mrExtension.resourcesPackage)
            generateTask.resourcesVisibility.set(mrExtension.resourcesVisibility)
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
        }

        kotlinSourceSet.extras[mokoResourcesGenTaskKey()] = taskProvider

        return taskProvider
    }

    private fun configureLowerDependencies(
        kotlinSourceSet: KotlinSourceSet,
        genTask: TaskProvider<GenerateMultiplatformResourcesTask>,
    ) {
        kotlinSourceSet.dependsOnObservable.forAll { dependsSourceSet ->
            val resourcesDir: SourceDirectorySet = requireNotNull(
                dependsSourceSet.extras[mokoResourcesSourceDirectoryKey()]
            )

            genTask.configure {
                val files: Set<File> = resourcesDir.srcDirs
                it.lowerResources.from(files)
            }

            configureLowerDependencies(
                kotlinSourceSet = dependsSourceSet,
                genTask = genTask
            )
        }
    }

    private fun configureUpperDependencies(
        kotlinSourceSet: KotlinSourceSet,
        resourcesSourceDirectory: SourceDirectorySet,
    ) {
        kotlinSourceSet.dependsOnObservable.forAll { dependsSourceSet ->
            val dependsGenTask: TaskProvider<GenerateMultiplatformResourcesTask> = requireNotNull(
                dependsSourceSet.extras[mokoResourcesGenTaskKey()]
            )

            dependsGenTask.configure {
                val files: Set<File> = resourcesSourceDirectory.srcDirs
                it.upperResources.from(files)
            }

            configureUpperDependencies(
                kotlinSourceSet = dependsSourceSet,
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
                resourceTask.inputMetadataFiles.setFrom(
                    dependsGenTask.flatMap {
                        it.outputMetadataFile
                    }
                )
            }
        }
    }
}

private fun KotlinSourceSet.whenDependsOn(sourceSetName: String, action: () -> Unit) {
    if (this.name == sourceSetName) {
        action()
    }

    dependsOnObservable.forAll { dependencySourceSet ->
        if (dependencySourceSet.name == sourceSetName) action()
        else dependencySourceSet.whenDependsOn(sourceSetName, action)
    }
}

internal fun mokoResourcesSourceDirectoryKey() =
    extrasKeyOf<SourceDirectorySet>("moko-resources-source-directory")

internal fun mokoResourcesGenTaskKey() =
    extrasKeyOf<TaskProvider<GenerateMultiplatformResourcesTask>>("moko-resources-generate-task")
