/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import dev.icerock.gradle.generator.platform.android.AndroidPluginType
import dev.icerock.gradle.generator.platform.android.hasAndroidApplicationPlugin
import dev.icerock.gradle.generator.platform.android.hasAndroidKmpLibraryPlugin
import dev.icerock.gradle.generator.platform.android.hasAndroidLibraryPlugin
import dev.icerock.gradle.generator.platform.android.hasAnyAndroidPlugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider

/**
 * Resolves the Android R-class package name (namespace) for the current project.
 *
 * This function determines the package name used for generated Android resource classes.
 * The resolution is performed lazily within a [Provider] to ensure it captures
 * configuration changes during the Gradle configuration phase.
 *
 * The logic evaluates plugins in the following order:
 * 1. **KMP Android Library**: Checks for `com.android.kotlin.multiplatform.library`.
 * 2. **Standard AGP**: Checks for `com.android.application` or `com.android.library`.
 *
 * @return A [Provider] supplying:
 * - The resolved package name (e.g., "com.example.project").
 * - `null`: If no supported Android plugins are applied.
 *
 * @throws IllegalStateException If an Android plugin is present but the namespace
 * cannot be resolved through supported methods.
 */
internal fun Project.getAndroidRClassPackage(): Provider<String> {
    return provider {
        // Before accessing Android-specific classes, ensure an Android plugin is on the classpath.
        // This prevents NoClassDefFoundError in non-Android projects or modules.
        if (!project.hasAnyAndroidPlugin()) {
            return@provider null
        }

        // 1. Check for Kotlin Multiplatform Android Library
        if (project.hasAndroidKmpLibraryPlugin()) {
            return@provider getAndroidKmpRClassPackage(project)
        }

        // 2. Check for standard Android Application or Android Library
        if (project.hasAndroidApplicationPlugin() || project.hasAndroidLibraryPlugin()) {
            return@provider getAndroidTargetRClassPackage(project)
        }

        error(
            "Android R class package not found for project '${project.path}'. " +
                "Expected one of Android plugins:" +
                " ${AndroidPluginType.entries.joinToString { it.pluginId }}," +
                " and configured android namespace."
        )
    }
}
