/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.platform.android

internal enum class AndroidLibraryType(val pluginId: String) {
    Application(pluginId = "com.android.application"),
    Library(pluginId = "com.android.library"),
    KmpLibrary(pluginId = "com.android.kotlin.multiplatform.library")
}

internal fun androidPlugins(): List<String> {
    return AndroidLibraryType.entries.map { it.pluginId }
}
