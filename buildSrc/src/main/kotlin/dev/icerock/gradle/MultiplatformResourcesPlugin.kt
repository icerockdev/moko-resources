/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.android.build.gradle.LibraryExtension
import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.AndroidMRGenerator
import dev.icerock.gradle.generator.CommonMRGenerator
import dev.icerock.gradle.generator.IosMRGenerator
import dev.icerock.gradle.generator.plurals.AndroidPluralsGenerator
import dev.icerock.gradle.generator.plurals.CommonPluralsGenerator
import dev.icerock.gradle.generator.plurals.IosPluralsGenerator
import dev.icerock.gradle.generator.strings.AndroidStringsGenerator
import dev.icerock.gradle.generator.strings.CommonStringsGenerator
import dev.icerock.gradle.generator.strings.IosStringsGenerator
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
        val multiplatformExtension =
            target.extensions.getByType(KotlinMultiplatformExtension::class)
        val mrExtension =
            target.extensions.create<MultiplatformResourcesPluginExtension>("multiplatformResources")

        target.afterEvaluate {
            val sourceSets = multiplatformExtension.sourceSets
            val commonSourceSet =
                sourceSets.getByName(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME)
            val commonResources = commonSourceSet.resources

            val strings = commonResources.matching {
                include("MR/**/strings.xml")
            }
            val plurals = commonResources.matching {
                include("MR/**/plurals.xml")
            }

            val androidExtension = target.extensions.getByType(LibraryExtension::class)
            val mainAndroidSet = androidExtension.sourceSets.getByName("main")
            val manifestFile = mainAndroidSet.manifest.srcFile

            val androidPackage = getAndroidPackage(manifestFile)

            generateMultiplatformResources(
                project = target,
                stringsFileTree = strings,
                pluralsFileTree = plurals,
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
        stringsFileTree: FileTree,
        pluralsFileTree: FileTree,
        sourceSets: List<KotlinSourceSet>,
        extension: MultiplatformResourcesPluginExtension,
        multiplatformExtension: KotlinMultiplatformExtension,
        androidPackage: String
    ) {
        val generatedDir = File(project.buildDir, "generated/moko")
        generatedDir.deleteRecursively()

        sourceSets.forEach { sourceSet ->
            val generator = createGenerator(
                multiplatformExtension = multiplatformExtension,
                generatedDir = generatedDir,
                sourceSet = sourceSet,
                stringsFileTree = stringsFileTree,
                pluralsFileTree = pluralsFileTree,
                mrClassPackage = extension.multiplatformResourcesPackage!!,
                androidRClassPackage = androidPackage
            ) ?: return@forEach

            generator.apply(project = project)
        }
    }

    private fun createGenerator(
        multiplatformExtension: KotlinMultiplatformExtension,
        generatedDir: File,
        sourceSet: KotlinSourceSet,
        stringsFileTree: FileTree,
        pluralsFileTree: FileTree,
        mrClassPackage: String,
        androidRClassPackage: String
    ): MRGenerator? {
        if (sourceSet.name == KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME) {
            return createCommonGenerator(
                generatedDir = generatedDir,
                sourceSet = sourceSet,
                stringsFileTree = stringsFileTree,
                pluralsFileTree = pluralsFileTree,
                mrClassPackage = mrClassPackage
            )
        }

        val target = multiplatformExtension.targets.firstOrNull { target ->
            val sourceSets = target.compilations.flatMap { it.kotlinSourceSets }
            sourceSets.any { it == sourceSet }
        } ?: return null

        return when (target) {
            is KotlinAndroidTarget -> {
                createAndroidGenerator(
                    generatedDir = generatedDir,
                    sourceSet = sourceSet,
                    stringsFileTree = stringsFileTree,
                    pluralsFileTree = pluralsFileTree,
                    mrClassPackage = mrClassPackage,
                    androidRClassPackage = androidRClassPackage
                )
            }
            is KotlinNativeTarget -> {
                val family = target.konanTarget.family
                if (family == Family.IOS) {
                    createIosGenerator(
                        generatedDir = generatedDir,
                        sourceSet = sourceSet,
                        stringsFileTree = stringsFileTree,
                        pluralsFileTree = pluralsFileTree,
                        mrClassPackage = mrClassPackage
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

    private fun createCommonGenerator(
        generatedDir: File,
        sourceSet: KotlinSourceSet,
        stringsFileTree: FileTree,
        mrClassPackage: String,
        pluralsFileTree: FileTree
    ): MRGenerator {
        return CommonMRGenerator(
            generatedDir = generatedDir,
            sourceSet = sourceSet,
            mrClassPackage = mrClassPackage,
            generators = listOf(
                CommonStringsGenerator(
                    sourceSet = sourceSet,
                    stringsFileTree = stringsFileTree
                ),
                CommonPluralsGenerator(
                    sourceSet = sourceSet,
                    pluralsFileTree = pluralsFileTree
                )
            )
        )
    }

    private fun createAndroidGenerator(
        generatedDir: File,
        sourceSet: KotlinSourceSet,
        stringsFileTree: FileTree,
        mrClassPackage: String,
        androidRClassPackage: String,
        pluralsFileTree: FileTree
    ): MRGenerator {
        return AndroidMRGenerator(
            generatedDir = generatedDir,
            sourceSet = sourceSet,
            mrClassPackage = mrClassPackage,
            generators = listOf(
                AndroidStringsGenerator(
                    sourceSet = sourceSet,
                    stringsFileTree = stringsFileTree,
                    androidRClassPackage = androidRClassPackage
                ),
                AndroidPluralsGenerator(
                    sourceSet = sourceSet,
                    pluralsFileTree = pluralsFileTree,
                    androidRClassPackage = androidRClassPackage
                )
            )
        )
    }

    private fun createIosGenerator(
        generatedDir: File,
        sourceSet: KotlinSourceSet,
        stringsFileTree: FileTree,
        mrClassPackage: String,
        pluralsFileTree: FileTree
    ): MRGenerator {
        return IosMRGenerator(
            generatedDir = generatedDir,
            sourceSet = sourceSet,
            mrClassPackage = mrClassPackage,
            generators = listOf(
                IosStringsGenerator(
                    sourceSet = sourceSet,
                    stringsFileTree = stringsFileTree
                ),
                IosPluralsGenerator(
                    sourceSet = sourceSet,
                    pluralsFileTree = pluralsFileTree
                )
            )
        )
    }
}
