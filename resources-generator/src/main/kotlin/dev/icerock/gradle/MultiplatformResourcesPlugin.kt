/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import dev.icerock.gradle.configuration.configureAndroidTargetGenerator
import dev.icerock.gradle.configuration.configureAppleTargetGenerator
import dev.icerock.gradle.configuration.configureCommonTargetGenerator
import dev.icerock.gradle.configuration.configureJsTargetGenerator
import dev.icerock.gradle.configuration.configureJvmTargetGenerator
import dev.icerock.gradle.configuration.getAndroidRClassPackage
import dev.icerock.gradle.configuration.setupProjectForApple
import dev.icerock.gradle.generator.AssetsGenerator
import dev.icerock.gradle.generator.ColorsGenerator
import dev.icerock.gradle.generator.FilesGenerator
import dev.icerock.gradle.generator.FontsGenerator
import dev.icerock.gradle.generator.ImagesGenerator
import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.PluralsGenerator
import dev.icerock.gradle.generator.ResourceGeneratorFeature
import dev.icerock.gradle.generator.StringsGenerator
import dev.icerock.gradle.utils.isStrictLineBreaks
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

open class MultiplatformResourcesPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val mrExtension: MultiplatformResourcesPluginExtension = project.extensions.create(
            name = "multiplatformResources",
            type = MultiplatformResourcesPluginExtension::class
        )

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
        kmpExtension: KotlinMultiplatformExtension
    ) {
        val resourcesSourceDirectory: SourceDirectorySet = project.objects.sourceDirectorySet(
            "moko-resources",
            "moko-resources"
        )

        val resourcesSourceSet: Provider<KotlinSourceSet> = mrExtension
            .getResourcesSourceSetName()
            .map { kmpExtension.sourceSets.getByName(it) }

        resourcesSourceDirectory.srcDirs(
            resourcesSourceSet.map { sourceSet ->
                val sources = File(project.projectDir, "src")
                val resourceSourceSetDir = File(sources, sourceSet.name)
                File(resourceSourceSetDir, "moko-resources")
            }
        )

        val generatedDir = File(project.buildDir, "generated/moko-resources")

        val settings = MRGenerator.Settings(
            packageName = mrExtension.getResourcesPackage(project),
            className = mrExtension.getResourcesClassName(),
            visibility = mrExtension.getResourcesVisibility(),
            generatedDir = generatedDir,
            isStrictLineBreaks = project.isStrictLineBreaks,
            iosLocalizationRegion = mrExtension.getIosBaseLocalizationRegion(),
            resourcesSourceDirectory = resourcesSourceDirectory,
            resourcesSourceSet = resourcesSourceSet,
            androidRClassPackage = project.getAndroidRClassPackage()
        )

        kmpExtension.targets.configureEach { kotlinTarget ->
            configureKotlinTargetGenerator(
                target = kotlinTarget,
                settings = settings
            )
        }

        setupProjectForApple(project)
    }

    private fun configureKotlinTargetGenerator(
        target: KotlinTarget,
        settings: MRGenerator.Settings
    ) {
        val features = listOf(
            StringsGenerator.Feature(settings),
            PluralsGenerator.Feature(settings),
            ImagesGenerator.Feature(settings, target.project.logger),
            FontsGenerator.Feature(settings),
            FilesGenerator.Feature(settings),
            ColorsGenerator.Feature(settings),
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
        features: List<ResourceGeneratorFeature<out MRGenerator.Generator>>
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
            KonanTarget.WATCHOS_X86 -> configureAppleTargetGenerator(
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
