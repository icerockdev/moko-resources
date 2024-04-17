/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.Property

@Suppress("UnnecessaryAbstractClass")
abstract class MultiplatformResourcesPluginExtension {
    abstract val resourcesPackage: Property<String>
    abstract val resourcesClassName: Property<String>
    abstract val iosBaseLocalizationRegion: Property<String>
    abstract val resourcesVisibility: Property<MRVisibility>
    abstract val iosMinimalDeploymentTarget: Property<String>
    abstract val resourcesSourceSets: NamedDomainObjectContainer<SourceDirectorySet>
}

internal fun MultiplatformResourcesPluginExtension.setupConvention(project: Project) {
    resourcesPackage.convention(project.provider { "${project.group}.${project.name}" })
    resourcesClassName.convention("MR")
    iosBaseLocalizationRegion.convention("en")
    resourcesVisibility.convention(MRVisibility.Public)
    iosMinimalDeploymentTarget.convention("9.0")
}
