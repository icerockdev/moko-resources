/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.AndroidSourceSet
import dev.icerock.gradle.generator.ColorsGenerator
import dev.icerock.gradle.generator.FilesGenerator
import dev.icerock.gradle.generator.FontsGenerator
import dev.icerock.gradle.generator.GenerateMultiplatformResourcesTask
import dev.icerock.gradle.generator.ImagesGenerator
import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.PluralsGenerator
import dev.icerock.gradle.generator.ResourceGeneratorFeature
import dev.icerock.gradle.generator.SourceInfo
import dev.icerock.gradle.generator.StringsGenerator
import dev.icerock.gradle.generator.android.AndroidMRGenerator
import dev.icerock.gradle.generator.common.CommonMRGenerator
import dev.icerock.gradle.generator.ios.IosMRGenerator
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
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
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class MultiplatformResourcesPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val mrExtension =
            target.extensions.create<MultiplatformResourcesPluginExtension>("multiplatformResources")

        target.plugins.withType<KotlinMultiplatformPluginWrapper> {
            val multiplatformExtension = target.extensions.getByType(this.projectExtensionClass)

            target.plugins.withType<LibraryPlugin> {
                val androidExtension = target.extensions.getByName("android") as LibraryExtension

                target.afterEvaluate {
                    configureGenerators(
                        target = target,
                        mrExtension = mrExtension,
                        multiplatformExtension = multiplatformExtension,
                        androidExtension = androidExtension
                    )
                }
            }
        }
    }

    private fun configureGenerators(
        target: Project,
        mrExtension: MultiplatformResourcesPluginExtension,
        multiplatformExtension: KotlinMultiplatformExtension,
        androidExtension: LibraryExtension
    ) {
        val androidMainSourceSet = androidExtension.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)

        val commonSourceSet = multiplatformExtension.sourceSets.getByName(mrExtension.sourceSetName)
        val commonResources = commonSourceSet.resources

        val manifestFile = androidMainSourceSet.manifest.srcFile
        val androidPackage = getAndroidPackage(manifestFile)

        val generatedDir = File(target.buildDir, "generated/moko")
        val mrClassPackage: String = requireNotNull(mrExtension.multiplatformResourcesPackage)
        val sourceInfo = SourceInfo(
            generatedDir,
            commonResources,
            mrExtension.multiplatformResourcesPackage!!,
            androidPackage
        )
        val iosLocalizationRegion = mrExtension.iosBaseLocalizationRegion
        val features = listOf(
            StringsGenerator.Feature(sourceInfo, iosLocalizationRegion),
            PluralsGenerator.Feature(sourceInfo, iosLocalizationRegion),
            ImagesGenerator.Feature(sourceInfo),
            FontsGenerator.Feature(sourceInfo),
            FilesGenerator.Feature(sourceInfo),
            ColorsGenerator.Feature(sourceInfo)
        )
        val targets: List<KotlinTarget> = multiplatformExtension.targets.toList()

        setupCommonGenerator(commonSourceSet, generatedDir, mrClassPackage, features, target)
        setupAndroidGenerator(targets, androidMainSourceSet, generatedDir, mrClassPackage, features, target)
        if(HostManager.hostIsMac) {
            setupIosGenerator(
                targets,
                generatedDir,
                mrClassPackage,
                features,
                target,
                iosLocalizationRegion
            )
        } else {
            target.logger.warn("MR file generation for iOS is not supported on your system!")
        }

        val generationTasks = target.tasks.filterIsInstance<GenerateMultiplatformResourcesTask>()
        val commonGenerationTask = generationTasks.first { it.name == "generateMRcommonMain" }
        generationTasks.filter { it != commonGenerationTask }
            .forEach { it.dependsOn(commonGenerationTask) }
    }

    private fun setupCommonGenerator(
        commonSourceSet: KotlinSourceSet,
        generatedDir: File,
        mrClassPackage: String,
        features: List<ResourceGeneratorFeature<out MRGenerator.Generator>>,
        target: Project
    ) {
        val commonGeneratorSourceSet: MRGenerator.SourceSet = createSourceSet(commonSourceSet)
        CommonMRGenerator(
            generatedDir,
            commonGeneratorSourceSet,
            mrClassPackage,
            generators = features.map { it.createCommonGenerator() }
        ).apply(target)
    }

    @Suppress("LongParameterList")
    private fun setupAndroidGenerator(
        targets: List<KotlinTarget>,
        androidMainSourceSet: AndroidSourceSet,
        generatedDir: File,
        mrClassPackage: String,
        features: List<ResourceGeneratorFeature<out MRGenerator.Generator>>,
        target: Project
    ) {
        val kotlinSourceSets: List<KotlinSourceSet> = targets
            .filterIsInstance<KotlinAndroidTarget>()
            .flatMap { it.compilations }
            .filterNot { it.name.endsWith("Test") } // remove tests compilations
            .map { it.defaultSourceSet }

        val androidSourceSet: MRGenerator.SourceSet = createSourceSet(androidMainSourceSet, kotlinSourceSets)
        AndroidMRGenerator(
            generatedDir,
            androidSourceSet,
            mrClassPackage,
            generators = features.map { it.createAndroidGenerator() }
        ).apply(target)
    }

    @Suppress("LongParameterList")
    private fun setupIosGenerator(
        targets: List<KotlinTarget>,
        generatedDir: File,
        mrClassPackage: String,
        features: List<ResourceGeneratorFeature<out MRGenerator.Generator>>,
        target: Project,
        iosLocalizationRegion: String
    ) {
        val compilations = targets
            .filterIsInstance<KotlinNativeTarget>()
            .filter { it.konanTarget.family == Family.IOS }
            .map { kotlinNativeTarget ->
                kotlinNativeTarget.compilations
                    .getByName(KotlinCompilation.MAIN_COMPILATION_NAME)
            }

        val defSourceSets = compilations.map { it.defaultSourceSet }
        compilations.forEach { compilation ->
            val kss = compilation.defaultSourceSet
            val depend = kss.getDependedFrom(defSourceSets)

            val sourceSet = createSourceSet(depend ?: kss)
            IosMRGenerator(
                generatedDir,
                sourceSet,
                mrClassPackage,
                generators = features.map { it.createIosGenerator() },
                compilation = compilation,
                baseLocalizationRegion = iosLocalizationRegion
            ).apply(target)
        }
    }

    private fun KotlinSourceSet.getDependedFrom(sourceSets: Collection<KotlinSourceSet>): KotlinSourceSet? {
        return sourceSets.firstOrNull { this.dependsOn.contains(it) } ?: this.dependsOn
            .mapNotNull { it.getDependedFrom(sourceSets) }
            .firstOrNull()
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

    private fun createSourceSet(
        androidSourceSet: AndroidSourceSet,
        kotlinSourceSets: List<KotlinSourceSet>
    ): MRGenerator.SourceSet {
        return object : MRGenerator.SourceSet {
            override val name: String
                get() = "android${androidSourceSet.name.capitalize()}"

            override fun addSourceDir(directory: File) {
                kotlinSourceSets.forEach { it.kotlin.srcDir(directory) }
            }

            override fun addResourcesDir(directory: File) {
                androidSourceSet.res.srcDir(directory)
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
