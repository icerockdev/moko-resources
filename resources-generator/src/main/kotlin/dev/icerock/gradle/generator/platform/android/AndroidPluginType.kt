/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.platform.android

import org.gradle.api.Project

/**
 * Enumeration of supported Android Gradle Plugin (AGP) types.
 *
 * @property pluginId The string identifier of the plugin (e.g., "com.android.application").
 */
internal enum class AndroidPluginType(val pluginId: String) {
    /** Plugin for building executable Android applications. */
    Application(pluginId = "com.android.application"),

    /** Plugin for building standard Android libraries. */
    Library(pluginId = "com.android.library"),

    /** Plugin for building Android libraries within a Kotlin Multiplatform project. */
    KmpLibrary(pluginId = "com.android.kotlin.multiplatform.library")
}

/**
 * Returns a list of all Android plugin identifiers defined in [AndroidPluginType].
 *
 * @return A list of plugin IDs.
 */
internal fun androidPlugins(): List<String> {
    return AndroidPluginType.entries.map { it.pluginId }
}

/**
 * Checks if any of the Android plugins are applied to this project.
 *
 * @return `true` if any plugin from [AndroidPluginType] is found.
 */
internal fun Project.hasAnyAndroidPlugin(): Boolean {
    return androidPlugins().any { plugins.findPlugin(it) != null }
}

/**
 * Checks if the Android Kotlin Multiplatform Library plugin is applied to the project.
 *
 * @return `true` if "com.android.kotlin.multiplatform.library" is applied.
 */
internal fun Project.hasAndroidKmpLibraryPlugin(): Boolean {
    return plugins.hasPlugin(AndroidPluginType.KmpLibrary.pluginId)
}

/**
 * Checks if the Android Application plugin is applied to the project.
 *
 * @return `true` if "com.android.application" is applied.
 */
internal fun Project.hasAndroidApplicationPlugin(): Boolean {
    return plugins.hasPlugin(AndroidPluginType.Application.pluginId)
}

/**
 * Checks if the Android Library plugin is applied to the project.
 *
 * @return `true` if "com.android.library" is applied.
 */
internal fun Project.hasAndroidLibraryPlugin(): Boolean {
    return plugins.hasPlugin(AndroidPluginType.Library.pluginId)
}
