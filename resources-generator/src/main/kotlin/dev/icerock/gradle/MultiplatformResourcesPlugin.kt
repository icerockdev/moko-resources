/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import dev.icerock.gradle.generator.AssetsGenerator
import dev.icerock.gradle.generator.ColorsGenerator
import dev.icerock.gradle.generator.FilesGenerator
import dev.icerock.gradle.generator.FontsGenerator
import dev.icerock.gradle.generator.ImagesGenerator
import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.PluralsGenerator
import dev.icerock.gradle.generator.ResourceGeneratorFeature
import dev.icerock.gradle.generator.SourceInfo
import dev.icerock.gradle.generator.StringsGenerator
import dev.icerock.gradle.generator.apple.AppleMRGenerator
import dev.icerock.gradle.generator.common.CommonMRGenerator
import dev.icerock.gradle.generator.js.JsMRGenerator
import dev.icerock.gradle.generator.jvm.JvmMRGenerator
import dev.icerock.gradle.tasks.CopyXCFrameworkResourcesToApp
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import dev.icerock.gradle.utils.ifDependsOn
import org.gradle.api.DomainObjectCollection
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFrameworkTask
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrCompilation
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.konan.target.HostManager

@Suppress("TooManyFunctions")
class MultiplatformResourcesPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val mrExtension: MultiplatformResourcesPluginExtension = target.extensions.create(
            "multiplatformResources",
            MultiplatformResourcesPluginExtension::class
        )
        target.plugins.withType(KotlinMultiplatformPluginWrapper::class) {
            target.afterEvaluate {
                configureGenerators(
                    target = target,
                    resourcesSourceSetName = mrExtension.sourceSetName,
                    mrExtension = mrExtension,
                    multiplatformExtension = target.extensions.getByType(KotlinMultiplatformExtension::class)
                )
            }
        }
    }

    private fun configureGenerators(
        target: Project,
        resourcesSourceSetName: String,
        mrExtension: MultiplatformResourcesPluginExtension,
        multiplatformExtension: KotlinMultiplatformExtension,
    ) {
        multiplatformExtension.sourceSets
            .matching { it.name == resourcesSourceSetName }
            .all { commonSourceSet ->
                configureGenerators(
                    target = target,
                    mrExtension = mrExtension,
                    multiplatformExtension = multiplatformExtension,
                    commonSourceSet = commonSourceSet,
                )
            }
    }

    @Suppress("LongMethod")
    private fun configureGenerators(
        target: Project,
        mrExtension: MultiplatformResourcesPluginExtension,
        multiplatformExtension: KotlinMultiplatformExtension,
        commonSourceSet: KotlinSourceSet,
    ) {
        val projectLayout: ProjectLayout = target.layout
        val providerFactory: ProviderFactory = target.providers
        val commonResources = commonSourceSet.resources

        val generatedDir = projectLayout.buildDirectory.dir("generated/moko")
        val mrClassPackage = providerFactory.provider<String> { mrExtension.multiplatformResourcesPackage }
            .orElse(providerFactory.provider { "${target.group}.${target.name}" })

        val mrSettings = object : MRGenerator.MRSettings {
            override val packageName = mrClassPackage
            override val className = providerFactory.provider(mrExtension::multiplatformResourcesClassName)
            override val visibility = providerFactory.provider(mrExtension::multiplatformResourcesVisibility)
        }

        val sourceInfo = object : SourceInfo {
            override val commonResources: SourceDirectorySet
                get() = commonResources
            override val mrClassPackage = mrClassPackage
            override var androidRClassPackageProvider: Provider<String>? = null
        }

        val strictLineBreaks: Boolean = target
            .findProperty("moko.resources.strictLineBreaks")
            .let { it as? String }
            ?.toBoolean()
            ?: false

        val iosLocalizationRegion = mrExtension.iosBaseLocalizationRegion
        val features = listOf(
            StringsGenerator.Feature(
                info = sourceInfo,
                iosBaseLocalizationRegion = iosLocalizationRegion,
                strictLineBreaks = strictLineBreaks,
                mrSettings = mrSettings
            ),
            PluralsGenerator.Feature(
                info = sourceInfo,
                iosBaseLocalizationRegion = iosLocalizationRegion,
                strictLineBreaks = strictLineBreaks,
                mrSettings = mrSettings
            ),
            ImagesGenerator.Feature(sourceInfo, mrSettings, target.logger),
            FontsGenerator.Feature(sourceInfo, mrSettings),
            FilesGenerator.Feature(sourceInfo, mrSettings),
            ColorsGenerator.Feature(sourceInfo, mrSettings),
            AssetsGenerator.Feature(sourceInfo, mrSettings)
        )
        val targets: NamedDomainObjectCollection<KotlinTarget> = multiplatformExtension.targets

        val commonGenerationTask = setupCommonGenerator(
            commonSourceSet = commonSourceSet,
            generatedDir = generatedDir,
            mrSettings = mrSettings,
            features = features,
            target = target
        )

        listOf("com.android.library", "com.android.application").forEach { id ->
            target.plugins.withId(id) {
                AndroidPluginLogic(
                    commonSourceSet = commonSourceSet,
                    targets = targets.withType<KotlinAndroidTarget>(),
                    generatedDir = generatedDir,
                    mrSettings = mrSettings,
                    features = features,
                    sourceInfo = sourceInfo,
                    project = target
                ).setup()
            }
        }

        setupJvmGenerator(
            commonSourceSet = commonSourceSet,
            targets = targets,
            generatedDir = generatedDir,
            mrSettings = mrSettings,
            features = features,
            target = target
        )

        setupJsGenerator(
            commonSourceSet = commonSourceSet,
            targets = targets,
            generatedDir = generatedDir,
            mrSettings = mrSettings,
            features = features,
            target = target
        )

        if (HostManager.hostIsMac) {
            setupAppleGenerator(
                commonSourceSet,
                targets,
                generatedDir,
                mrSettings,
                features,
                target,
                iosLocalizationRegion
            )
        } else {
            target.logger.warn("MR file generation for iOS is not supported on your system!")
        }

        target.tasks.withType<GenerateMultiplatformResourcesTask>()
            .matching { it != commonGenerationTask }
            .configureEach { it.dependsOn(commonGenerationTask) }
    }

    private fun setupCommonGenerator(
        commonSourceSet: KotlinSourceSet,
        generatedDir: Provider<Directory>,
        mrSettings: MRGenerator.MRSettings,
        features: List<ResourceGeneratorFeature<out MRGenerator.Generator>>,
        target: Project
    ): GenerateMultiplatformResourcesTask {
        val commonGeneratorSourceSet: MRGenerator.SourceSet = createSourceSet(commonSourceSet)
        return CommonMRGenerator(
            generatedDir,
            commonGeneratorSourceSet,
            mrSettings,
            generators = features.map { it.createCommonGenerator() }
        ).apply(target)
    }

    @Suppress("LongParameterList")
    private fun setupJvmGenerator(
        commonSourceSet: KotlinSourceSet,
        targets: DomainObjectCollection<KotlinTarget>,
        generatedDir: Provider<Directory>,
        mrSettings: MRGenerator.MRSettings,
        features: List<ResourceGeneratorFeature<out MRGenerator.Generator>>,
        target: Project
    ) {
        targets
            .withType<KotlinJvmTarget>()
            .configureEach { kotlinJvmTarget ->
                kotlinJvmTarget.compilations
                    .configureEach { compilation ->
                        compilation.defaultSourceSet.ifDependsOn(commonSourceSet) {
                            JvmMRGenerator(
                                generatedDir = generatedDir,
                                sourceSet = createSourceSet(compilation.defaultSourceSet),
                                mrSettings = mrSettings,
                                generators = features.map { it.createJvmGenerator() }
                            ).apply(project = target)
                        }
                    }
            }
    }

    @Suppress("LongParameterList")
    private fun setupJsGenerator(
        commonSourceSet: KotlinSourceSet,
        targets: DomainObjectCollection<KotlinTarget>,
        generatedDir: Provider<Directory>,
        mrSettings: MRGenerator.MRSettings,
        features: List<ResourceGeneratorFeature<out MRGenerator.Generator>>,
        target: Project
    ) {
        targets
            .withType<KotlinJsIrTarget>()
            .configureEach { kotlinJsIrTarget ->
                kotlinJsIrTarget.compilations
                    .withType<KotlinJsIrCompilation>()
                    .all { compilation ->
                        compilation.defaultSourceSet.ifDependsOn(commonSourceSet) {
                            JsMRGenerator(
                                generatedDir,
                                createSourceSet(compilation.defaultSourceSet),
                                mrSettings = mrSettings,
                                generators = features.map { it.createJsGenerator() },
                                compilation = compilation,
                            ).apply(project = target)
                        }
                    }
            }
    }

    @Suppress("LongParameterList")
    private fun setupAppleGenerator(
        commonSourceSet: KotlinSourceSet,
        targets: DomainObjectCollection<KotlinTarget>,
        generatedDir: Provider<Directory>,
        mrSettings: MRGenerator.MRSettings,
        features: List<ResourceGeneratorFeature<out MRGenerator.Generator>>,
        target: Project,
        iosLocalizationRegion: String
    ) {
        targets
            .withType<KotlinNativeTarget>()
            .matching { it.konanTarget.family.isAppleFamily }
            .configureEach { appleFamilyTarget ->
                appleFamilyTarget.compilations
                    .matching { it.name == KotlinCompilation.MAIN_COMPILATION_NAME }
                    .all { compilation ->
                        compilation.defaultSourceSet.ifDependsOn(commonSourceSet) {
                            AppleMRGenerator(
                                generatedDir = generatedDir,
                                sourceSet = createSourceSet(compilation.defaultSourceSet),
                                mrSettings = mrSettings,
                                generators = features.map { it.createIosGenerator() },
                                compilation = compilation,
                                baseLocalizationRegion = iosLocalizationRegion,
                            ).apply(project = target)
                        }
                    }
            }
        setupCopyXCFrameworkResourcesTask(target)
    }

    private fun setupCopyXCFrameworkResourcesTask(project: Project) {
        // can't use here configureEach because we will add new task when found xcframeworktask
        project.afterEvaluate {
            project.tasks.filterIsInstance<XCFrameworkTask>()
                .forEach { task ->
                    val copyTaskName: String =
                        task.name.replace("assemble", "copyResources").plus("ToApp")

                    val copyTask = project.tasks.create(
                        copyTaskName,
                        CopyXCFrameworkResourcesToApp::class.java
                    ) {
                        it.xcFrameworkDir = task.outputDir
                    }
                    copyTask.dependsOn(task)
                }
        }
    }

    private fun createSourceSet(kotlinSourceSet: KotlinSourceSet): MRGenerator.SourceSet {
        return object : MRGenerator.SourceSet {
            override val name: String
                get() = kotlinSourceSet.name

            override fun addSourceDir(directory: Provider<Directory>) {
                kotlinSourceSet.kotlin.srcDir(directory)
            }

            override fun addResourcesDir(directory: Provider<Directory>) {
                kotlinSourceSet.resources.srcDir(directory)
            }

            override fun addAssetsDir(directory: Provider<Directory>) {
                // nothing
            }
        }
    }
}
