/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.android

import com.android.build.gradle.tasks.GenerateResValues
import com.android.build.gradle.tasks.MergeSourceSetFolders
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.TargetMRGenerator
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType

class AndroidMRGenerator(
    project: Project,
    settings: Settings,
    generators: List<Generator>,
) : TargetMRGenerator(
    project = project,
    settings = settings,
    generators = generators
) {
    override fun getMRClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun apply(generationTask: GenerateMultiplatformResourcesTask, project: Project) {
        project.tasks.withType<GenerateResValues>().configureEach {
            it.dependsOn(generationTask)
        }
        project.tasks.withType<MergeSourceSetFolders>().configureEach {
            it.dependsOn(generationTask)
        }
    }
}
