/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.android

import com.android.build.gradle.tasks.GenerateResValues
import com.android.build.gradle.tasks.ManifestProcessorTask
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.MRGenerator
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.withType
import java.io.File

class AndroidMRGenerator(
    generatedDir: File,
    sourceSet: SourceSet,
    mrSettings: MRSettings,
    generators: List<Generator>
) : MRGenerator(
    generatedDir = generatedDir,
    sourceSet = sourceSet,
    mrSettings = mrSettings,
    generators = generators
) {

    override fun getMRClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun apply(generationTask: Task, project: Project) {
        project.tasks.withType<ManifestProcessorTask>().configureEach {
            generationTask.dependsOn(it)
        }
        project.tasks.withType<GenerateResValues>().configureEach {
            it.dependsOn(generationTask)
        }
    }
}
