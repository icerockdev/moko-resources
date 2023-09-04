/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import org.gradle.api.provider.Property

interface MultiplatformResourcesPluginExtension {
    val resourcesPackage: Property<String>
    val resourcesClassName: Property<String>
    val resourcesSourceSet: Property<String>
    val iosBaseLocalizationRegion: Property<String>
    val staticFrameworkWarningEnabled: Property<Boolean>
    val resourcesVisibility: Property<MRVisibility>
}

internal val MultiplatformResourcesPluginExtension.resourcesClassNameValue: String
    get() = resourcesClassName.getOrElse("MR")
internal val MultiplatformResourcesPluginExtension.resourcesSourceSetValue: String
    get() = resourcesSourceSet.getOrElse("commonMain")
internal val MultiplatformResourcesPluginExtension.iosBaseLocalizationRegionValue: String
    get() = iosBaseLocalizationRegion.getOrElse("en")
internal val MultiplatformResourcesPluginExtension.isStaticFrameworkWarningEnabledValue: Boolean
    get() = staticFrameworkWarningEnabled.getOrElse(true)
internal val MultiplatformResourcesPluginExtension.resourcesVisibilityValue: MRVisibility
    get() = resourcesVisibility.getOrElse(MRVisibility.Public)
