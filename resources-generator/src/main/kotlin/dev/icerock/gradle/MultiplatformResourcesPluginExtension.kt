/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

interface MultiplatformResourcesPluginExtension {
    val resourcesPackage: Property<String>
    val resourcesClassName: Property<String>
    val resourcesSourceSet: Property<String>
    val iosBaseLocalizationRegion: Property<String>
    val staticFrameworkWarningEnabled: Property<Boolean>
    val resourcesVisibility: Property<MRVisibility>
}

internal fun MultiplatformResourcesPluginExtension.getResourcesPackage(project: Project): Provider<String> =
    resourcesPackage.orElse(project.provider { "${project.group}.${project.name}" })

internal fun MultiplatformResourcesPluginExtension.getResourcesClassName(): Provider<String> =
    resourcesClassName.orElse("MR")

internal fun MultiplatformResourcesPluginExtension.getResourcesSourceSetName(): Provider<String> =
    resourcesSourceSet.orElse(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME)

internal fun MultiplatformResourcesPluginExtension.getIosBaseLocalizationRegion(): Provider<String> =
    iosBaseLocalizationRegion.orElse("en")

internal fun MultiplatformResourcesPluginExtension.getIsStaticFrameworkWarningEnabled(): Provider<Boolean> =
    staticFrameworkWarningEnabled.orElse(true)

internal fun MultiplatformResourcesPluginExtension.getResourcesVisibility(): Provider<MRVisibility> =
    resourcesVisibility.orElse(MRVisibility.Public)
