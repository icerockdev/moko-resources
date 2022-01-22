/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.android.build.gradle.BaseExtension
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
import dev.icerock.gradle.generator.jvm.JvmMRGenerator
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import dev.icerock.gradle.utils.getDependedFrom
import dev.icerock.gradle.utils.isDependsOn
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.konan.target.HostManager
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

@Suppress("TooManyFunctions")
class MultiplatformResourcesPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val mrExtension = target.extensions.create(
            "multiplatformResources",
            MultiplatformResourcesPluginExtension::class.java
        )

        target.plugins.withType(KotlinMultiplatformPluginWrapper::class.java) {
            val multiplatformExtension =
                target.extensions.getByType(KotlinMultiplatformExtension::class.java)

            target.afterEvaluate {
                configureGenerators(
                    target = target,
                    mrExtension = mrExtension,
                    multiplatformExtension = multiplatformExtension
                )
            }
        }
    }

    @Suppress("LongMethod")
    private fun configureGenerators(
        target: Project,
        mrExtension: MultiplatformResourcesPluginExtension,
        multiplatformExtension: KotlinMultiplatformExtension
    ) {
        val commonSourceSet = multiplatformExtension.sourceSets.getByName(mrExtension.sourceSetName)
        val commonResources = commonSourceSet.resources

        val generatedDir = File(target.buildDir, "generated/moko")
        val mrClassPackage: String = requireNotNull(mrExtension.multiplatformResourcesPackage) {
            "multiplatformResources.multiplatformResourcesPackage is required!" +
                    " please configure moko-resources plugin correctly."
        }
        val mrSettings = MRGenerator.MRSettings(
            packageName = mrClassPackage,
            className = mrExtension.multiplatformResourcesClassName,
            visibility = mrExtension.multiplatformResourcesVisibility
        )
        val sourceInfo = SourceInfo(
            generatedDir,
            commonResources,
            mrExtension.multiplatformResourcesPackage!!
        )
        val iosLocalizationRegion = mrExtension.iosBaseLocalizationRegion
        val features = listOf(
            StringsGenerator.Feature(sourceInfo, iosLocalizationRegion, mrSettings),
            PluralsGenerator.Feature(sourceInfo, iosLocalizationRegion, mrSettings),
            ImagesGenerator.Feature(sourceInfo, mrSettings),
            FontsGenerator.Feature(sourceInfo, mrSettings),
            FilesGenerator.Feature(sourceInfo, mrSettings),
            ColorsGenerator.Feature(sourceInfo, mrSettings)
        )
        val targets: List<KotlinTarget> = multiplatformExtension.targets.toList()

        val commonGenerationTask = setupCommonGenerator(
            commonSourceSet = commonSourceSet,
            generatedDir = generatedDir,
            mrSettings = mrSettings,
            features = features,
            target = target
        )

        val setupAndroid = {
            val androidExtension = target.extensions.getByType(BaseExtension::class.java)

            val androidLogic = AndroidPluginLogic(
                commonSourceSet = commonSourceSet,
                targets = targets,
                generatedDir = generatedDir,
                mrSettings = mrSettings,
                features = features,
                target = target
            )

            target.afterEvaluate {
                val androidMainSourceSet = androidExtension.sourceSets
                    .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
                val manifestFile = androidMainSourceSet.manifest.srcFile
                val androidPackage = getAndroidPackage(manifestFile)

                sourceInfo.setAndroidRClassPackage(androidPackage)

                androidLogic.setup(androidMainSourceSet)
            }
        }

        target.plugins.withId("com.android.application") {
            setupAndroid()
        }
        target.plugins.withId("com.android.library") {
            setupAndroid()
        }

        setupJvmGenerator(
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
        generatedDir: File,
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
        targets: List<KotlinTarget>,
        generatedDir: File,
        mrSettings: MRGenerator.MRSettings,
        features: List<ResourceGeneratorFeature<out MRGenerator.Generator>>,
        target: Project
    ) {
        val kotlinSourceSets: List<KotlinSourceSet> = targets
            .filterIsInstance<KotlinJvmTarget>()
            .flatMap { it.compilations }
            .map { it.defaultSourceSet }
            .filter { it.isDependsOn(commonSourceSet) }

        kotlinSourceSets.forEach { kotlinSourceSet ->
            JvmMRGenerator(
                generatedDir = generatedDir,
                sourceSet = createSourceSet(kotlinSourceSet),
                mrSettings = mrSettings,
                generators = features.map { it.createJvmGenerator() }
            ).apply(target)
        }
    }

    @Suppress("LongParameterList")
    private fun setupAppleGenerator(
        commonSourceSet: KotlinSourceSet,
        targets: List<KotlinTarget>,
        generatedDir: File,
        mrSettings: MRGenerator.MRSettings,
        features: List<ResourceGeneratorFeature<out MRGenerator.Generator>>,
        target: Project,
        iosLocalizationRegion: String
    ) {
        val compilations = targets
            .filterIsInstance<KotlinNativeTarget>()
            .filter { it.konanTarget.family.isAppleFamily }
            .map { kotlinNativeTarget ->
                kotlinNativeTarget.compilations
                    .getByName(KotlinCompilation.MAIN_COMPILATION_NAME)
            }

        val defSourceSets = compilations.map { it.defaultSourceSet }
            .filter { it.isDependsOn(commonSourceSet) }
        compilations.forEach { compilation ->
            val kss = compilation.defaultSourceSet
            val depend = kss.getDependedFrom(defSourceSets)

            val sourceSet = createSourceSet(depend ?: kss)
            AppleMRGenerator(
                generatedDir = generatedDir,
                sourceSet = sourceSet,
                mrSettings = mrSettings,
                generators = features.map { it.createIosGenerator() },
                compilation = compilation,
                baseLocalizationRegion = iosLocalizationRegion
            ).apply(target)
        }
    }

    private fun createSourceSet(kotlinSourceSet: KotlinSourceSet): MRGenerator.SourceSet {
        return object : MRGenerator.SourceSet {
            override val name: String
                get() = kotlinSourceSet.name

            override fun addSourceDir(directory: File) {
                kotlinSourceSet.kotlin.srcDir(directory)
            }

            override fun addResourcesDir(directory: File) {
                kotlinSourceSet.resources.srcDir(directory)
            }
        }
    }

    private fun getAndroidPackage(manifestFile: File): String {
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(manifestFile)

        val manifestNodes = doc.getElementsByTagName("manifest")
        val manifest = manifestNodes.item(0)

        return manifest.attributes.getNamedItem("package").textContent
    }
}
