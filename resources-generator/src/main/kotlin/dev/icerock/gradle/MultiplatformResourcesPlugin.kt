/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.android.build.api.dsl.AndroidSourceSet
import dev.icerock.gradle.extra.getOrRegisterGenerateResourcesTask
import dev.icerock.gradle.generator.platform.android.getAndroidSourceSetOrNull
import dev.icerock.gradle.generator.platform.android.setupAndroidTasks
import dev.icerock.gradle.generator.platform.android.setupAndroidVariantsSync
import dev.icerock.gradle.generator.platform.apple.registerCopyFrameworkResourcesToAppTask
import dev.icerock.gradle.generator.platform.apple.setupAppleKLibResources
import dev.icerock.gradle.generator.platform.apple.setupExecutableResources
import dev.icerock.gradle.generator.platform.apple.setupFatFrameworkTasks
import dev.icerock.gradle.generator.platform.apple.setupFrameworkResources
import dev.icerock.gradle.generator.platform.js.setupJsKLibResources
import dev.icerock.gradle.generator.platform.js.setupJsResourcesWithLinkTask
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import dev.icerock.gradle.utils.kotlinSourceSetsObservable
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

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

            setupFatFrameworkTasks(project = project)
            registerGenerateAllResources(project = project)
            registerCopyFrameworkResourcesToAppTask(project = project)
            setupAndroidVariantsSync(project = project)
            setupGradleSync(project = project)
        }
    }

    private fun setupGradleSync(project: Project) {
        val tasks: TaskCollection<GenerateMultiplatformResourcesTask> = project.tasks.withType()
        project.tasks.matching { it.name == "prepareKotlinIdeaImport" }.configureEach {
            it.dependsOn(tasks)
        }
    }

    @Suppress("LongMethod")
    private fun configureKotlinTargetGenerator(
        project: Project,
        mrExtension: MultiplatformResourcesPluginExtension,
        kmpExtension: KotlinMultiplatformExtension,
    ) {
        kmpExtension.sourceSets.configureEach { kotlinSourceSet: KotlinSourceSet ->
            kotlinSourceSet.getOrRegisterGenerateResourcesTask(mrExtension)
        }

        kmpExtension.targets.configureEach { target ->
            if (target is KotlinNativeTarget) {
                setupExecutableResources(target = target)
                setupFrameworkResources(target = target)
            }

            if (target is KotlinJsIrTarget) {
                setupJsResourcesWithLinkTask(target = target, project = project)
            }

            target.compilations.configureEach { compilation ->
                compilation.kotlinSourceSetsObservable.forAll { sourceSet: KotlinSourceSet ->
                    val genTaskProvider: TaskProvider<GenerateMultiplatformResourcesTask> =
                        sourceSet.getOrRegisterGenerateResourcesTask(mrExtension)

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
                        genTaskProvider = genTaskProvider
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
                    }

                    if (target is KotlinJsIrTarget) {
                        compilation.compileTaskProvider.configure { compileTask ->
                            compileTask as Kotlin2JsCompile

                            setupJsKLibResources(
                                compileTask = compileTask,
                                resourcesGenerationDir = genTaskProvider.flatMap {
                                    it.outputResourcesDir.asFile
                                }
                            )
                        }
                    }

                    if (target is KotlinNativeTarget && target.konanTarget.family.isAppleFamily) {
                        val appleIdentifier: Provider<String> = mrExtension.resourcesPackage
                            .map { it + "." + compilation.name }

                        genTaskProvider.configure {
                            it.appleBundleIdentifier.set(appleIdentifier)
                        }

                        compilation.compileTaskProvider.configure { compileTask: KotlinCompilationTask<*> ->
                            compileTask as KotlinNativeCompile

                            setupAppleKLibResources(
                                compileTask = compileTask,
                                assetsDirectory = genTaskProvider.flatMap {
                                    it.outputAssetsDir
                                },
                                resourcesGenerationDir = genTaskProvider.flatMap {
                                    it.outputResourcesDir
                                },
                                iosLocalizationRegion = mrExtension.iosBaseLocalizationRegion,
                                iosMinimalDeploymentTarget = mrExtension.iosMinimalDeploymentTarget,
                                appleBundleIdentifier = appleIdentifier
                            )
                        }
                    }
                }
            }
        }
    }

    private fun registerGenerateAllResources(project: Project) {
        project.tasks.register("generateMR") {
            it.group = "moko-resources"
            it.dependsOn(project.tasks.withType<GenerateMultiplatformResourcesTask>())
        }
    }

    private fun setupSourceSets(
        target: KotlinTarget,
        sourceSet: KotlinSourceSet,
        genTaskProvider: TaskProvider<GenerateMultiplatformResourcesTask>,
    ) {
        sourceSet.kotlin.srcDir(genTaskProvider.map { it.outputSourcesDir })

        when (target.platformType) {
            KotlinPlatformType.jvm, KotlinPlatformType.js -> {
                sourceSet.resources.srcDir(genTaskProvider.map { it.outputResourcesDir })
                sourceSet.resources.srcDir(genTaskProvider.map { it.outputAssetsDir })
            }

            KotlinPlatformType.androidJvm -> {
                // Fix: android sourceSets indexation in IDE
                // Usage of api of v2.model in AGP broken for IDE resources indexing
                // For correct indexing of resources set resource directory
                // https://issuetracker.google.com/issues/329702045
                @OptIn(ExperimentalKotlinGradlePluginApi::class)
                val androidSourceSet: AndroidSourceSet =
                    target.project.getAndroidSourceSetOrNull(sourceSet) ?: return
                androidSourceSet.res.srcDir(genTaskProvider.map { it.outputResourcesDir })

                // Assets added in variants for correct generation
                // see: dev.icerock.gradle.generator.platform.android.SetupAndroidUtilsKt.addGenerationTaskDependency
                // androidSourceSet.assets.srcDir(genTaskProvider.map { it.outputAssetsDir })
            }

            KotlinPlatformType.common, KotlinPlatformType.native,
            KotlinPlatformType.wasm -> Unit
        }
    }
}
