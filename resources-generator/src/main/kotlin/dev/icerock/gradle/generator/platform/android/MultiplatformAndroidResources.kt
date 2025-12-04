/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.platform.android

import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Project

internal fun configureMultiplatformAndroidResources(project: Project) {
    project.plugins.withId("com.android.kotlin.multiplatform.library") {
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

        androidComponents.onVariants { variant ->
            val capName = variant.name.replaceFirstChar { it.titlecase() }

            val generateTask = project.tasks.findByName("generateMRandroidMain")

            generateTask?.let { task ->
                project.tasks.configureEach { projectTask ->
                    if (projectTask.name == "package${capName}Resources") {
                        projectTask.dependsOn(task)
                    }
                }
            }
        }
    }
}
