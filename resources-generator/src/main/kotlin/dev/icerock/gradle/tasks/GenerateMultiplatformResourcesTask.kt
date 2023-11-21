/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.tasks

import dev.icerock.gradle.MRVisibility
import dev.icerock.gradle.configuration.getAndroidRClassPackage
import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.PluralsGenerator
import dev.icerock.gradle.generator.ResourceGeneratorFeature
import dev.icerock.gradle.generator.StringsGenerator
import dev.icerock.gradle.generator.android.AndroidMRGenerator
import dev.icerock.gradle.generator.apple.AppleMRGenerator
import dev.icerock.gradle.generator.common.CommonMRGenerator
import dev.icerock.gradle.generator.js.JsMRGenerator
import dev.icerock.gradle.generator.jvm.JvmMRGenerator
import dev.icerock.gradle.utils.isStrictLineBreaks
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.konan.target.KonanTarget

@CacheableTask
abstract class GenerateMultiplatformResourcesTask : DefaultTask() {

    @get:InputFiles
    @get:Classpath
    abstract val ownResources: ConfigurableFileCollection

    @get:InputFiles
    @get:Classpath
    abstract val lowerResources: ConfigurableFileCollection

    @get:InputFiles
    @get:Classpath
    abstract val upperResources: ConfigurableFileCollection

    @get:Optional
    @get:Input
    abstract val platformType: Property<String>

    @get:Optional
    @get:Input
    abstract val konanTarget: Property<String>

    @get:Input
    abstract val resourcesPackageName: Property<String>

    @get:Input
    abstract val resourcesClassName: Property<String>

    @get:Input
    abstract val iosBaseLocalizationRegion: Property<String>

    @get:Input
    abstract val resourcesVisibility: Property<MRVisibility>

    //TODO Realise
//    @get:OutputFile
//    abstract val generationReport: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    init {
        group = "moko-resources"
    }

    @TaskAction
    fun generate() {
        logger.warn("i $name have ownResources ${ownResources.from}")
        logger.warn("i $name have lowerResources ${lowerResources.from}")
        logger.warn("i $name have upperResources ${upperResources.from}")

        val settings: MRGenerator.Settings = createGeneratorSettings()
        val features: List<ResourceGeneratorFeature<*>> = createGeneratorFeatures(settings)
        val mrGenerator: MRGenerator = resolveGenerator(settings, features)

        logger.warn("i ${platformType.get()} generator type: ${mrGenerator::class.java.simpleName}")
        mrGenerator.generate()
    }

    private fun resolveGenerator(
        settings: MRGenerator.Settings,
        generators: List<ResourceGeneratorFeature<*>>,
    ): MRGenerator {
        return when (KotlinPlatformType.valueOf(platformType.get())) {
            KotlinPlatformType.common -> createCommonGenerator(settings, generators)
            KotlinPlatformType.jvm -> createJvmGenerator(settings, generators)
            KotlinPlatformType.js -> createJsGenerator(settings, generators)
            KotlinPlatformType.androidJvm -> createAndroidJvmGenerator(settings, generators)
            KotlinPlatformType.native -> createNativeGenerator(settings, generators)
            KotlinPlatformType.wasm -> error("moko-resources not support wasm target now")
        }
    }

    private fun createGeneratorSettings(): MRGenerator.Settings {
        return MRGenerator.Settings(
            packageName = resourcesPackageName.get(),
            className = resourcesClassName.get(),
            generatedDir = outputDirectory.get(),
            ownResourcesFileTree = ownResources.asFileTree,
            lowerResourcesFileTree = lowerResources.asFileTree,
            upperResourcesFileTree = upperResources.asFileTree,
            isStrictLineBreaks = project.isStrictLineBreaks,
            visibility = resourcesVisibility.get(),
            androidRClassPackage = project.getAndroidRClassPackage().get(),
            iosLocalizationRegion = iosBaseLocalizationRegion.get(),
        )
    }

    private fun createGeneratorFeatures(
        settings: MRGenerator.Settings,
    ): List<ResourceGeneratorFeature<*>> {
        return listOf(
            StringsGenerator.Feature(settings),
//            PluralsGenerator.Feature(settings),
//            ImagesGenerator.Feature(settings, logger),
//            FontsGenerator.Feature(settings),
//            FilesGenerator.Feature(settings),
//            ColorsGenerator.Feature(settings),
//            AssetsGenerator.Feature(settings)
        )
    }

    private fun createCommonGenerator(
        settings: MRGenerator.Settings,
        generators: List<ResourceGeneratorFeature<*>>,
    ): CommonMRGenerator {
        return CommonMRGenerator(
            project = project,
            settings = settings,
            generators = generators.map {
                it.createCommonGenerator()
            }
        )
    }

    private fun createAndroidJvmGenerator(
        settings: MRGenerator.Settings,
        generators: List<ResourceGeneratorFeature<*>>,
    ): AndroidMRGenerator {
        return AndroidMRGenerator(
            settings = settings,
            generators = generators.map {
                it.createAndroidGenerator()
            }
        )
    }

    private fun createJvmGenerator(
        settings: MRGenerator.Settings,
        generators: List<ResourceGeneratorFeature<*>>,
    ): JvmMRGenerator {
        return JvmMRGenerator(
            settings = settings,
            generators = generators.map {
                it.createJvmGenerator()
            }
        )
    }

    private fun createJsGenerator(
        settings: MRGenerator.Settings,
        generators: List<ResourceGeneratorFeature<*>>,
    ): JsMRGenerator {
        TODO()
//        return JsMRGenerator(
//            settings = settings,
//            generators = generators.map {
//                it.createJsGenerator()
//            }
//        )
    }

    private fun createNativeGenerator(
        settings: MRGenerator.Settings,
        generators: List<ResourceGeneratorFeature<*>>,
    ): MRGenerator {
        val konanTargetName: String = konanTarget.get()
        val konanTarget: KonanTarget = KonanTarget.predefinedTargets[konanTargetName]
            ?: error("can't find $konanTargetName in KonanTarget")

        return when (konanTarget) {
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
            -> createAppleGenerator(settings, generators)

            else -> error("$konanTarget is not supported by moko-resources now!")
        }
    }

    private fun createAppleGenerator(
        settings: MRGenerator.Settings,
        generators: List<ResourceGeneratorFeature<*>>,
    ): AppleMRGenerator {
        TODO()
    }
}
