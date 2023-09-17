/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property

abstract class MultiplatformResourcesPluginExtension {
    abstract val resourcesPackage: Property<String>
    abstract val resourcesClassName: Property<String>
    abstract val iosBaseLocalizationRegion: Property<String>
    abstract val staticFrameworkWarningEnabled: Property<Boolean>
    abstract val resourcesVisibility: Property<MRVisibility>
}

internal fun MultiplatformResourcesPluginExtension.setupConvention(project: Project) {
    resourcesPackage.convention(project.provider { "${project.group}.${project.name}" })
    resourcesClassName.convention("MR")
    iosBaseLocalizationRegion.convention("en")
    staticFrameworkWarningEnabled.convention(true)
    resourcesVisibility.convention(MRVisibility.Public)
}
