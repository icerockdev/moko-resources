/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import dev.icerock.gradle.generator.MRGenerator
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources

fun dependsOnProcessResources(
    project: Project,
    @Suppress("UNUSED_PARAMETER") sourceSet: Provider<MRGenerator.SourceSet>,
    task: Task,
) {
    val sourceSet: MRGenerator.SourceSet = sourceSet.get()
    project.logger.warn("source set name is ${sourceSet.name}")

    project.tasks
        .matching { it.name == sourceSet.name.removeSuffix("Main") + "ProcessResources" }
        .withType<ProcessResources>()
        .configureEach { processResourcesTask ->
            processResourcesTask.dependsOn(task)
        }
}
