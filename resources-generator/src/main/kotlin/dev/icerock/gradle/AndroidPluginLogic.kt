/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.android.build.gradle.api.AndroidSourceSet
import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.ResourceGeneratorFeature
import dev.icerock.gradle.generator.android.AndroidMRGenerator
import dev.icerock.gradle.utils.isDependsOn
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import java.io.File

internal class AndroidPluginLogic(
    private val commonSourceSet: KotlinSourceSet,
    private val targets: List<KotlinTarget>,
    private val generatedDir: File,
    private val mrClassPackage: String,
    private val features: List<ResourceGeneratorFeature<out MRGenerator.Generator>>,
    private val target: Project
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
        AndroidMRGenerator(
            generatedDir,
            androidSourceSet,
            mrClassPackage,
            generators = features.map { it.createAndroidGenerator() }
        ).apply(target)
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
