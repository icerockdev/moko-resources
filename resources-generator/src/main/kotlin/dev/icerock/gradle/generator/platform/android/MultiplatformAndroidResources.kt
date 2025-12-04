/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.platform.android

import com.android.build.api.variant.AndroidComponentsExtension
import dev.icerock.gradle.utils.capitalize
import org.gradle.api.Project
import org.gradle.api.Task

internal fun configureMultiplatformAndroidResources(project: Project) {
    project.plugins.withId(AndroidLibraryType.KmpLibrary.pluginId) {
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

        androidComponents.onVariants { variant ->
            val capitalizedName: String = variant.name.capitalize()
            val generateTask: Task? = project.tasks.findByName("generateMRandroidMain")

            generateTask?.let { task ->
                project.tasks.configureEach { projectTask ->
                    if (projectTask.name == "package${capitalizedName}Resources") {
                        projectTask.dependsOn(task)
                    }
                }
            }
        }
    }
}
