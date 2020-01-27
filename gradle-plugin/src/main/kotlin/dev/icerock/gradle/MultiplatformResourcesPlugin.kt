/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.android.build.gradle.LibraryExtension
import dev.icerock.gradle.generator.AndroidMRGenerator
import dev.icerock.gradle.generator.CommonMRGenerator
import dev.icerock.gradle.generator.IosMRGenerator
import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.image.ImagesGeneratorFeature
import dev.icerock.gradle.generator.plurals.PluralsGeneratorFeature
import dev.icerock.gradle.generator.strings.StringsGeneratorFeature
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class MultiplatformResourcesPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val mrExtension =
            target.extensions.create<MultiplatformResourcesPluginExtension>("multiplatformResources")

        mrExtension.onChange = {
            configureGenerators(
                target = target,
                mrExtension = mrExtension
            )
        }
    }

    private fun configureGenerators(
        target: Project,
        mrExtension: MultiplatformResourcesPluginExtension
    ) {
        target.afterEvaluate {
            val multiplatformExtension =
                target.extensions.getByType(KotlinMultiplatformExtension::class)

            val sourceSets = multiplatformExtension.sourceSets
            val commonSourceSet =
                sourceSets.getByName(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME)
            val commonResources = commonSourceSet.resources

            val androidExtension = target.extensions.getByType(LibraryExtension::class)
            val mainAndroidSet = androidExtension.sourceSets.getByName("main")
            val manifestFile = mainAndroidSet.manifest.srcFile

            val androidPackage = getAndroidPackage(manifestFile)

            generateMultiplatformResources(
                project = target,
                commonResources = commonResources,
                sourceSets = sourceSets.filter { it.name.endsWith("Main") },
                extension = mrExtension,
                multiplatformExtension = multiplatformExtension,
                androidPackage = androidPackage
            )
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

    private fun generateMultiplatformResources(
        project: Project,
        commonResources: FileTree,
        sourceSets: List<KotlinSourceSet>,
        extension: MultiplatformResourcesPluginExtension,
        multiplatformExtension: KotlinMultiplatformExtension,
        androidPackage: String
    ) {
        val generatedDir = File(project.buildDir, "generated/moko")

        sourceSets.forEach { sourceSet ->
            val targetInfo = TargetInfo(
                generatedDir,
                sourceSet,
                commonResources,
                extension.multiplatformResourcesPackage!!,
                androidPackage
            )
            val features = with(targetInfo) {
                listOf(
                    StringsGeneratorFeature(this),
                    PluralsGeneratorFeature(this),
                    ImagesGeneratorFeature(this)
                )
            }
            val generator = createGenerator(
                multiplatformExtension = multiplatformExtension,
                info = targetInfo,
                features = features
            ) ?: return@forEach

            generator.apply(project = project)
        }
    }

    private fun createGenerator(
        multiplatformExtension: KotlinMultiplatformExtension,
        info: TargetInfo,
        features: List<ResourceGeneratorFeature>
    ): MRGenerator? {
        if (info.sourceSet.name == KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME) {
            return CommonMRGenerator(
                info.generatedDir,
                info.sourceSet,
                info.mrClassPackage,
                generators = features.map { it.createCommonGenerator() }
            )
        }

        val target = multiplatformExtension.targets.firstOrNull { target ->
            val sourceSets = target.compilations.flatMap { it.kotlinSourceSets }
            sourceSets.any { it == info.sourceSet }
        } ?: return null

        return when (target) {
            is KotlinAndroidTarget -> {
                AndroidMRGenerator(
                    info.generatedDir,
                    info.sourceSet,
                    info.mrClassPackage,
                    generators = features.map{ it.createAndroidGenerator() }
                )
            }
            is KotlinNativeTarget -> {
                val family = target.konanTarget.family
                if (family == Family.IOS) {
                    IosMRGenerator(
                        info.generatedDir,
                        info.sourceSet,
                        info.mrClassPackage,
                        generators = features.map{ it.createiOSGenerator() }
                    )
                } else {
                    println("unsupported native family $family")
                    null
                }
            }
            else -> {
                println("unsupported target $target")
                null
            }
        }
    }
}

data class TargetInfo(
    val generatedDir: File,
    val sourceSet: KotlinSourceSet,
    val commonResources: FileTree,
    val mrClassPackage: String,
    val androidRClassPackage: String
)

abstract class ResourceGeneratorFeature(info: TargetInfo) {
    abstract fun createCommonGenerator(): MRGenerator.Generator
    abstract fun createiOSGenerator(): MRGenerator.Generator
    abstract fun createAndroidGenerator(): MRGenerator.Generator
}
