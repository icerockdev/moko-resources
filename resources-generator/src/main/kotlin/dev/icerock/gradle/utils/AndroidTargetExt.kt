/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType

/**
 * Resolves the Android package name (namespace) for standard Android application
 * and library modules.
 *
 * This function retrieves the namespace defined in the `android { ... }` DSL.
 *
 * @param project The Gradle project instance to inspect.
 * @return The resolved namespace [String], or `null` if the extension is missing
 * or the namespace is not defined.
 */
internal fun getAndroidTargetRClassPackage(project: Project): String? {
    return project.extensions.findByType<BaseExtension>()?.namespace
}
