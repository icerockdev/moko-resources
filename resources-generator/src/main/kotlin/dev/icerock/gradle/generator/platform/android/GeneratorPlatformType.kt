/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.platform.android

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

/**
 * Resolves the platform used by moko-resources generators.
 *
 * AGP's Kotlin Multiplatform Android target reports `jvm`, even though its
 * resources must be generated using Android resource generators. The concrete target type is
 * used to distinguish it from a regular Kotlin JVM target in the same project.
 */
internal fun KotlinTarget.resourcesPlatformTypeName(project: Project): String {
    val isKmpAndroidTarget: Boolean = project.hasAndroidKmpLibraryPlugin() &&
        this is KotlinMultiplatformAndroidLibraryTarget

    return normalizeResourcesPlatformTypeName(
        reportedPlatformType = platformType.name,
        isKmpAndroidTarget = isKmpAndroidTarget,
    )
}

internal fun normalizeResourcesPlatformTypeName(
    reportedPlatformType: String,
    isKmpAndroidTarget: Boolean,
): String {
    return if (isKmpAndroidTarget) ANDROID_PLATFORM_TYPE else reportedPlatformType
}

private const val ANDROID_PLATFORM_TYPE = "androidJvm"
