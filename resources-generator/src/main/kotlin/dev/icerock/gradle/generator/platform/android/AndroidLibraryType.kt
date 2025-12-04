/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.platform.android

internal enum class AndroidLibraryType(val plugins: List<String>) {
    Library(
        plugins = listOf(
            "com.android.application",
            "com.android.library"
        )
    ),
    KmpLibrary(
        plugins = listOf(
            "com.android.kotlin.multiplatform.library"
        )
    )
}

internal fun androidLibraryPlugins(): List<String> {
    return AndroidLibraryType.entries.flatMap { it.plugins }
}
