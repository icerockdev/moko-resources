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
import java.io.File
import java.util.Locale
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget

internal class AndroidPluginLogic(
    private val commonSourceSet: KotlinSourceSet,
    private val targets: List<KotlinTarget>,
    private val generatedDir: File,
    private val mrSettings: MRGenerator.MRSettings,
    private val features: List<ResourceGeneratorFeature<out MRGenerator.Generator>>,
    private val project: Project
) {
    fun setup(androidMainSourceSet: AndroidSourceSet) {
        val kotlinSourceSets: List<KotlinSourceSet> = targets
            .filterIsInstance<KotlinAndroidTarget>()
            .flatMap { it.compilations }
            .filter { compilation ->
                compilation.kotlinSourceSets.any { it.isDependsOn(commonSourceSet) }
            }
            .map { it.defaultSourceSet }

        val androidSourceSet: MRGenerator.SourceSet =
            createSourceSet(androidMainSourceSet, kotlinSourceSets)

        setAssetsDirsRefresh()

        AndroidMRGenerator(
            generatedDir = generatedDir,
            sourceSet = androidSourceSet,
            mrSettings = mrSettings,
            generators = features.map { it.createAndroidGenerator() }
        ).apply(project)
    }

    private fun setAssetsDirsRefresh() {
        // without this code Android Gradle Plugin not copy assets to aar
        project.tasks
            .matching { it.name.startsWith("package") && it.name.endsWith("Assets") }
            .configureEach { task ->
                // for gradle optimizations we should use anonymous object
                @Suppress("ObjectLiteralToLambda")
                task.doFirst(object : Action<Task> {
                    override fun execute(t: Task) {
                        val android = project.extensions.getByType<BaseExtension>()
                        val assets = android.sourceSets.getByName("main").assets
                        assets.setSrcDirs(assets.srcDirs)
                    }
                })
            }
    }

    private fun createSourceSet(
        androidSourceSet: AndroidSourceSet,
        kotlinSourceSets: List<KotlinSourceSet>
    ): MRGenerator.SourceSet {
        return object : MRGenerator.SourceSet {
            override val name: String
                get() = "android${androidSourceSet.name.replaceFirstChar { 
                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                }"

            override fun addSourceDir(directory: File) {
                androidSourceSet.kotlin.srcDirs(directory)
                androidSourceSet.java.srcDirs(directory)
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
