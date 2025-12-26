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
 * Unlike standard Android projects, the KMP Android configuration is nested
 * within the [KotlinProjectExtension]. This function performs a hierarchical
 * lookup: Project -> Kotlin Extension -> KMP Android Library Extension.
 *
 * @param project The Gradle project to inspect.
 * @return
 * - The resolved namespace [String] if configured.
 * - `"KotlinMultiplatformAndroidLibraryExtension not found"` if the KMP Android extension is missing.
 * - `null` if the extension exists but the `namespace` property has not been set.
 */
internal fun getAndroidKmpRClassPackage(project: Project): String? {
    val kotlinProjectExtension: KotlinProjectExtension? = project
        .extensions.findByType<KotlinProjectExtension>()

    val androidLibraryExtension: KotlinMultiplatformAndroidLibraryExtension =
        kotlinProjectExtension?.extensions?.findByType<KotlinMultiplatformAndroidLibraryExtension>()
            ?: return "KotlinMultiplatformAndroidLibraryExtension not found"

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
