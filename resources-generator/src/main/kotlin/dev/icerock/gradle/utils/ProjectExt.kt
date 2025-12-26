/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import dev.icerock.gradle.generator.platform.android.AndroidLibraryType
import dev.icerock.gradle.generator.platform.android.androidPlugins
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
 * 1. **KMP Library**: Checks for the modern Kotlin Multiplatform Android extension.
 * 2. **Application/Library**: Checks for standard Android Gradle Plugin (AGP) extensions.
 *
 * @return A [Provider] that supplies the resolved namespace or a diagnostic string:
 * - `"android not enabled"`: If no known Android plugins are applied.
 * - `"RClass Error: namespace not found"`: If plugins are present but the namespace is missing.
 * - The actual package name (e.g., "com.example.project") if successfully resolved.
 */
internal fun Project.getAndroidRClassPackage(): Provider<String> {
    return provider {
        // before call android specific classes we should ensure that android plugin in classpath at all
        // it's required to support gradle projects without android target
        val isAndroidEnabled = androidPlugins().any { project.plugins.findPlugin(it) != null }
        if (!isAndroidEnabled) {
            return@provider "android not enabled"
        }

        // 1. Check for modern KMP Android Library Extension
        if (project.plugins.hasPlugin(AndroidLibraryType.KmpLibrary.pluginId)) {
            return@provider getAndroidKmpRClassPackage(project)
        }

        // 2. Check for Android Application
        if (project.plugins.hasPlugin(AndroidLibraryType.Application.pluginId)) {
            return@provider getAndroidTargetRClassPackage(project)
        }

        // 2. Check for Deprecated Android Library
        if (project.plugins.hasPlugin(AndroidLibraryType.Library.pluginId)) {
            return@provider getAndroidTargetRClassPackage(project)
        }

        return@provider "RClass Error namespace not found"
    }
}
