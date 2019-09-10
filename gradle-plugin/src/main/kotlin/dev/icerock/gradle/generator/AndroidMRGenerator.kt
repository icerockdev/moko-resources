/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.android.build.gradle.LibraryExtension
import com.squareup.kotlinpoet.KModifier
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

class AndroidMRGenerator(
    generatedDir: File,
    sourceSet: KotlinSourceSet,
    mrClassPackage: String,
    generators: List<Generator>
) : MRGenerator(
    generatedDir = generatedDir,
    sourceSet = sourceSet,
    mrClassPackage = mrClassPackage,
    generators = generators
) {
    override fun getMRClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun apply(generationTask: Task, project: Project) {
        project.tasks.getByName("preBuild").dependsOn(generationTask)

        val androidExtension = project.extensions.getByType(LibraryExtension::class)
        val mainAndroidSet = androidExtension.sourceSets.getByName("main")
        mainAndroidSet.res.srcDir(resourcesGenerationDir)
    }
}