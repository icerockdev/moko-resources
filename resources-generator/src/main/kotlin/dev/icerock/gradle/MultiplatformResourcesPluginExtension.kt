/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property

abstract class MultiplatformResourcesPluginExtension {
    abstract val resourcesPackage: Property<String>
    abstract val resourcesClassName: Property<String>
    abstract val resourcesSourceSet: Property<String>
    abstract val iosBaseLocalizationRegion: Property<String>
    abstract val staticFrameworkWarningEnabled: Property<Boolean>
    abstract val resourcesVisibility: Property<MRVisibility>
}

internal fun MultiplatformResourcesPluginExtension.resourcesPackageValue(project : Project): String {
    return resourcesPackage.getOrElse("${project.group}.${project.name}")
}

internal val MultiplatformResourcesPluginExtension.resourcesClassNameValue : String
    get() = resourcesClassName.getOrElse("MR")
internal val MultiplatformResourcesPluginExtension.resourcesSourceSetValue : String
    get() = resourcesSourceSet.getOrElse("commonMain")
internal val MultiplatformResourcesPluginExtension.iosBaseLocalizationRegionValue : String
    get() = iosBaseLocalizationRegion.getOrElse("en")
internal val MultiplatformResourcesPluginExtension.isStaticFrameworkWarningEnabledValue : Boolean
    get() = staticFrameworkWarningEnabled.getOrElse(true)
internal val MultiplatformResourcesPluginExtension.resourcesVisibilityValue : MRVisibility
    get() = resourcesVisibility.getOrElse(MRVisibility.Public)
