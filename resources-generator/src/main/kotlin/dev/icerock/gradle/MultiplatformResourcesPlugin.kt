/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import dev.icerock.gradle.extra.getOrRegisterGenerateResourcesTask
import dev.icerock.gradle.generator.platform.apple.setupAppleKLibResources
import dev.icerock.gradle.generator.platform.apple.setupCopyXCFrameworkResourcesTask
import dev.icerock.gradle.generator.platform.apple.setupExecutableResources
import dev.icerock.gradle.generator.platform.apple.setupFatFrameworkTasks
import dev.icerock.gradle.generator.platform.apple.setupFrameworkResources
import dev.icerock.gradle.generator.platform.apple.setupTestsResources
import dev.icerock.gradle.generator.platform.js.setupJsKLibResources
import dev.icerock.gradle.generator.platform.js.setupJsResources
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import dev.icerock.gradle.utils.kotlinSourceSetsObservable
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
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
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.sources.android.findAndroidSourceSet
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

            setupCopyXCFrameworkResourcesTask(project = project)
            setupFatFrameworkTasks(project = project)
            registerGenerateAllResources(project = project)
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
                setupTestsResources(target = target)
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
                                    it.outputAssetsDir.asFile
                                },
                                resourcesGenerationDir = genTaskProvider.flatMap {
                                    it.outputResourcesDir.asFile
                                },
                                iosLocalizationRegion = mrExtension.iosBaseLocalizationRegion,
                                acToolMinimalDeploymentTarget = mrExtension.acToolMinimalDeploymentTarget,
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
}
