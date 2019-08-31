/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.android.build.gradle.LibraryExtension
import dev.icerock.gradle.generator.*
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
        val androidExtension = target.extensions.getByType(LibraryExtension::class)
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

            val mainAndroidSet = androidExtension.sourceSets.getByName("main")
            val manifestFile = mainAndroidSet.manifest.srcFile
            mainAndroidSet.res.srcDir("build/generated/moko/androidMain/res")

            val androidPackage = getAndroidPackage(manifestFile)

            generateMultiplatformResources(
                project = target,
                stringsFileTree = strings,
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
        sourceSets: List<KotlinSourceSet>,
        extension: MultiplatformResourcesPluginExtension,
        multiplatformExtension: KotlinMultiplatformExtension,
        androidPackage: String
    ) {
        val generatedDir = File(project.buildDir, "generated/moko")

        // language - key - value
        val languageStrings: Map<LanguageType, Map<KeyType, String>> = loadStrings(stringsFileTree)

        sourceSets.forEach { sourceSet ->
            val generator = createGenerator(
                multiplatformExtension = multiplatformExtension,
                generatedDir = generatedDir,
                sourceSet = sourceSet,
                languagesStrings = languageStrings,
                mrClassPackage = extension.multiplatformResourcesPackage!!,
                androidRClassPackage = androidPackage
            ) ?: return@forEach


            val name = sourceSet.name
            val genTask = project.task("generateMR$name") {
                group = "multiplatform"

                doLast {
                    generator.generate()
                }
            }

            generator.configureTasks(project = project, generationTask = genTask)
        }
    }

    private fun loadStrings(stringsFileTree: FileTree): Map<LanguageType, Map<KeyType, String>> {
        return stringsFileTree.associate { file ->
            val language: LanguageType = file.parentFile.name
            val strings: Map<KeyType, String> = loadLanguageStrings(file)
            language to strings
        }
    }

    private fun loadLanguageStrings(stringsFile: File): Map<KeyType, String> {
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(stringsFile)

        val stringNodes = doc.getElementsByTagName("string")
        val mutableMap = mutableMapOf<KeyType, String>()

        for (i in 0 until stringNodes.length) {
            val stringNode = stringNodes.item(i)
            val name = stringNode.attributes.getNamedItem("name").textContent
            val value = stringNode.textContent

            mutableMap[name] = value
        }

        return mutableMap
    }

    private fun createGenerator(
        multiplatformExtension: KotlinMultiplatformExtension,
        generatedDir: File,
        sourceSet: KotlinSourceSet,
        languagesStrings: Map<LanguageType, Map<KeyType, String>>,
        mrClassPackage: String,
        androidRClassPackage: String
    ): Generator? {
        if (sourceSet.name == KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME) {
            return CommonGenerator(
                generatedDir = generatedDir,
                sourceSet = sourceSet,
                languagesStrings = languagesStrings,
                mrClassPackage = mrClassPackage
            )
        }

        val target = multiplatformExtension.targets.firstOrNull { target ->
            val sourceSets = target.compilations.flatMap { it.kotlinSourceSets }
            sourceSets.any { it == sourceSet }
        } ?: return null

        return when (target) {
            is KotlinAndroidTarget -> {
                AndroidGenerator(
                    generatedDir = generatedDir,
                    sourceSet = sourceSet,
                    languagesStrings = languagesStrings,
                    mrClassPackage = mrClassPackage,
                    androidRClassPackage = androidRClassPackage
                )
            }
            is KotlinNativeTarget -> {
                val family = target.konanTarget.family
                if (family == Family.IOS) {
                    IosGenerator(
                        generatedDir = generatedDir,
                        sourceSet = sourceSet,
                        languagesStrings = languagesStrings,
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
}
