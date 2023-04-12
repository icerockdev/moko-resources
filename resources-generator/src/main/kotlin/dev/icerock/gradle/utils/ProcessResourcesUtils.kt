/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import dev.icerock.gradle.generator.MRGenerator
import org.gradle.api.Project
import org.gradle.api.Task

fun dependsOnProcessResources(project: Project, sourceSet: MRGenerator.SourceSet, task: Task) {
    project.tasks
        .matching { it.name == sourceSet.name.removeSuffix("Main") + "ProcessResources" }
        .configureEach {
            it.dependsOn(task)
        }
}
