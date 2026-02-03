/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

/**
 * Resolves the Android R-class package name (namespace) specifically for
 * Kotlin Multiplatform (KMP) Android Library projects.
 *
 * It looks up the [KotlinMultiplatformAndroidLibraryExtension] within the
 * [KotlinProjectExtension] to retrieve the configured `namespace`.
 *
 * @param project The Gradle project to inspect.
 * @return The resolved namespace [String], or `null` if the KMP Android
 * extension is missing or the namespace property has not been set.
 */
internal fun getAndroidKmpRClassPackage(project: Project): String? {
    val kotlinProjectExtension = project.extensions.findByType<KotlinProjectExtension>()
    val androidLibraryExtension: KotlinMultiplatformAndroidLibraryExtension = kotlinProjectExtension
        ?.extensions
        ?.findByType<KotlinMultiplatformAndroidLibraryExtension>()
        ?: return null

    return androidLibraryExtension.namespace
}

/**
 * Enables Android resources support for all [KotlinMultiplatformAndroidLibraryTarget] targets
 * in this project.
 *
 * This function does **not** track plugin application and assumes that
 * the caller invokes it only after the `com.android.kotlin.multiplatform.library` plugin
 * has already been applied.
 *
 * It configures each Android library target to have `androidResources.enable = true`,
 * which allows the module to use Android resource folders (`res/`, `assets/`, etc.)
 * in the Kotlin Multiplatform Android source sets.
 */
internal fun Project.enableAndroidResources() {
    val kmpExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

    kmpExtension.targets.withType<KotlinMultiplatformAndroidLibraryTarget>()
        .configureEach { target ->
            target.androidResources.enable = true
        }
}
