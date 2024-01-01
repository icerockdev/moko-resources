/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.gradle.BaseExtension
import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.ResourceGeneratorFeature
import dev.icerock.gradle.generator.android.AndroidMRGenerator
import dev.icerock.gradle.utils.isDependsOn
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import java.io.File

internal class AndroidPluginLogic(
    commonSourceSet: KotlinSourceSet,
    targets: List<KotlinTarget>,
    private val generatedDir: File,
    private val mrSettings: MRGenerator.MRSettings,
    features: List<ResourceGeneratorFeature<out MRGenerator.Generator>>,
    private val project: Project
) {
    private val kotlinSourceSets: List<KotlinSourceSet> = targets
        .filterIsInstance<KotlinAndroidTarget>()
        .flatMap { it.compilations }
        .filter { compilation ->
            compilation.kotlinSourceSets.any { it.isDependsOn(commonSourceSet) }
        }
        .map { it.defaultSourceSet }

    private val generators = features.map { it.createAndroidGenerator() }

    fun setup(androidMainSourceSet: AndroidSourceSet) {
        val androidSourceSet: MRGenerator.SourceSet =
            createSourceSet(androidMainSourceSet, kotlinSourceSets)

        setAssetsDirsRefresh()

        AndroidMRGenerator(
            generatedDir = generatedDir,
            sourceSet = androidSourceSet,
            mrSettings = mrSettings,
            generators = generators
        ).apply(project)
    }

    private fun setAssetsDirsRefresh() {
        // without this code Android Gradle Plugin not copy assets to aar
        project.tasks
            .matching { it.name.startsWith("package") && it.name.endsWith("Assets") }
            .configureEach { task ->
                // for gradle optimizations we should use anonymous object
                val android = project.extensions.getByType<BaseExtension>()
                val assets = android.sourceSets.getByName("main").assets
                assets.setSrcDirs(assets.srcDirs)
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

            override fun addAssetsDir(directory: File) {
                androidSourceSet.assets.srcDir(directory)
            }
        }
    }
}
