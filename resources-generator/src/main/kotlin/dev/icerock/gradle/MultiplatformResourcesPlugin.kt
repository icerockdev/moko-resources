/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.gradle.BaseExtension
import dev.icerock.gradle.configuration.configureAndroidTargetGenerator
import dev.icerock.gradle.configuration.configureAppleTargetGenerator
import dev.icerock.gradle.configuration.configureCommonTargetGenerator
import dev.icerock.gradle.configuration.configureJsTargetGenerator
import dev.icerock.gradle.configuration.configureJvmTargetGenerator
import dev.icerock.gradle.generator.AssetsGenerator
import dev.icerock.gradle.generator.ColorsGenerator
import dev.icerock.gradle.generator.FilesGenerator
import dev.icerock.gradle.generator.FontsGenerator
import dev.icerock.gradle.generator.ImagesGenerator
import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.PluralsGenerator
import dev.icerock.gradle.generator.ResourceGeneratorFeature
import dev.icerock.gradle.generator.StringsGenerator
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import dev.icerock.gradle.utils.dependsOnObservable
import dev.icerock.gradle.utils.kotlinSourceSetsObservable
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget
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

            //TODO add configuration for generated resources
            setupResourcesSourceSet(project, kmpExtension)
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

        kmpExtension.targets.configureEach { target ->
            project.logger.warn("i target ${target.targetName}")

            target.compilations.configureEach { compilation ->

                compilation.kotlinSourceSetsObservable.forAll { sourceSet ->
                    project.logger.warn("i compilation kotlinSourceSets: $sourceSet")

                    project.logger.warn(("i compilationSourceSet: $sourceSet"))

                    val genTask: TaskProvider<GenerateMultiplatformResourcesTask> = requireNotNull(
                        sourceSet.extras[mokoResourcesGenTaskKey()]
                    )

                    genTask.configure {
                        project.logger.warn("i configure platformName ${target.platformType.name}")

                        it.platformType.set(target.platformType.name)
                    }

                    compilation.compileTaskProvider.configure {
                        it.dependsOn(genTask)
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

    private fun setupResourcesSourceSet(
        project: Project,
        kmpExtension: KotlinMultiplatformExtension,
    ) {
        val kotlinExtension: KotlinProjectExtension = project.extensions.getByType(
            KotlinProjectExtension::class.java
        )
        val commonSourceSet: KotlinSourceSet? = kotlinExtension.sourceSets.findByName(
            KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME
        )
        val androidExtension: BaseExtension? =
            project.extensions.findByName("android") as BaseExtension?
        val androidSourceSet: com.android.build.gradle.api.AndroidSourceSet? =
            androidExtension?.sourceSets?.getByName(SourceSet.MAIN_SOURCE_SET_NAME)

        when {
            commonSourceSet != null -> {
                setupKotlinSourceSet(
                    project = project,
                    kotlinSourceSet = commonSourceSet,
                )
            }
            androidSourceSet != null -> {
                setupAndroidSourceSet(
                    project = project,
                    androidSourceSet = androidSourceSet
                )
            }
            else -> kmpExtension.sourceSets.configureEach { kotlinSourceSet ->
                setupKotlinSourceSet(
                    project = project,
                    kotlinSourceSet = kotlinSourceSet,
                )
            }
        }
    }

    private fun setupKotlinSourceSet(
        project: Project,
        kotlinSourceSet: KotlinSourceSet,
    ) {
        val mokoResourcesDir = getGeneratedResourcesDir(
            project = project,
            sourceSetName = kotlinSourceSet.name
        )

        val sourcesGenerationDir = File(mokoResourcesDir, "src")
        val resourcesGenerationDir = File(mokoResourcesDir, "res")

        kotlinSourceSet.kotlin.srcDir(sourcesGenerationDir)
        kotlinSourceSet.resources.srcDir(resourcesGenerationDir)
    }

    private fun setupAndroidSourceSet(
        project: Project,
        androidSourceSet: AndroidSourceSet,
    ) {
        project.logger.warn("i android sourceSets: ${androidSourceSet.name}")

        val mokoResourcesDir = getGeneratedResourcesDir(
            project = project,
            sourceSetName = androidSourceSet.name
        )

        val sourcesGenerationDir = File(mokoResourcesDir, "src")
        val resourcesGenerationDir = File(mokoResourcesDir, "res")
        val assetsGenerationDir = File(mokoResourcesDir, AssetsGenerator.ASSETS_DIR_NAME)

        androidSourceSet.assets.srcDir(assetsGenerationDir)
        androidSourceSet.java.srcDir(sourcesGenerationDir)
        androidSourceSet.resources.srcDir(resourcesGenerationDir)
    }

    private fun getGeneratedResourcesDir(
        project: Project,
        sourceSetName: String,
    ): File {
        val generatedDir = File(project.buildDir, "generated/moko-resources")
        return File(generatedDir, sourceSetName)
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
                    File(project.buildDir, "generated/moko-resources/metadata"),
                    "${kotlinSourceSet.name}-metadata.json"
                )
            )
            generateTask.outputDirectory.set(
                File(File(project.buildDir, "generated/moko-resources"), kotlinSourceSet.name)
            )
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
                resourceTask.dependsOn(dependsGenTask) //TODO: Убрать после реализации связи через метадату

                // Заменить на список файлов
                resourceTask.inputMetadataFiles.setFrom(
                    dependsGenTask.flatMap {
                        it.outputMetadataFile
                    }
                )
            }
        }
    }

    private fun configureKotlinTargetGenerator(
        target: KotlinTarget,
        settings: MRGenerator.Settings,
    ) {
        val features = listOf(
            StringsGenerator.Feature(settings),
            PluralsGenerator.Feature(settings),
            ImagesGenerator.Feature(settings, target.project.logger),
            FontsGenerator.Feature(settings),
            FilesGenerator.Feature(target.project, settings),
            ColorsGenerator.Feature(target.project, settings),
            AssetsGenerator.Feature(settings)
        )

        when (target.platformType) {
            KotlinPlatformType.common -> configureCommonTargetGenerator(target, settings, features)
            KotlinPlatformType.jvm -> configureJvmTargetGenerator(target, settings, features)
            KotlinPlatformType.js -> configureJsTargetGenerator(target, settings, features)
            KotlinPlatformType.androidJvm -> configureAndroidTargetGenerator(
                target,
                settings,
                features
            )

            KotlinPlatformType.native -> configureNativeTargetGenerator(
                target as KotlinNativeTarget,
                settings,
                features
            )

            KotlinPlatformType.wasm -> {
                target.project.logger.warn("wasm target not supported by MOKO Resources now")
            }
        }
    }

    private fun configureNativeTargetGenerator(
        target: KotlinNativeTarget,
        settings: MRGenerator.Settings,
        features: List<ResourceGeneratorFeature<out MRGenerator.Generator>>,
    ) {
        when (target.konanTarget) {
            KonanTarget.IOS_ARM32,
            KonanTarget.IOS_ARM64,
            KonanTarget.IOS_SIMULATOR_ARM64,
            KonanTarget.IOS_X64,

            KonanTarget.MACOS_ARM64,
            KonanTarget.MACOS_X64,

            KonanTarget.TVOS_ARM64,
            KonanTarget.TVOS_SIMULATOR_ARM64,
            KonanTarget.TVOS_X64,

            KonanTarget.WATCHOS_ARM32,
            KonanTarget.WATCHOS_ARM64,
            KonanTarget.WATCHOS_DEVICE_ARM64,
            KonanTarget.WATCHOS_SIMULATOR_ARM64,
            KonanTarget.WATCHOS_X64,
            KonanTarget.WATCHOS_X86,
            -> configureAppleTargetGenerator(
                target = target,
                settings = settings,
                features = features
            )

            else -> {
                target.project.logger.warn("$target is not supported by MOKO Resources at now")
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
