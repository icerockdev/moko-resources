/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import dev.icerock.gradle.generator.platform.apple.registerCopyXCFrameworkResourcesToAppTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

@Suppress("UnnecessaryAbstractClass")
abstract class MultiplatformResourcesPluginExtension {
    abstract val resourcesPackage: Property<String>
    abstract val resourcesClassName: Property<String>
    abstract val iosBaseLocalizationRegion: Property<String>
    abstract val resourcesVisibility: Property<MRVisibility>
    abstract val iosMinimalDeploymentTarget: Property<String>
    abstract val resourcesSourceSets: NamedDomainObjectContainer<SourceDirectorySet>

    fun Project.configureCopyXCFrameworkResources(xcFrameworkName: String = name) {
        NativeBuildType.values()
            .map { it.name.lowercase().capitalize() }
            .plus("")
            .map { xcFrameworkName.capitalize() + it + "XCFramework" }
            .forEach { registerCopyXCFrameworkResourcesToAppTask(project = this, xcFrameworkName = it) }
    }
}

internal fun MultiplatformResourcesPluginExtension.setupConvention(project: Project) {
    resourcesPackage.convention(project.provider { "${project.group}.${project.name}" })
    resourcesClassName.convention("MR")
    iosBaseLocalizationRegion.convention("en")
    resourcesVisibility.convention(MRVisibility.Public)
    iosMinimalDeploymentTarget.convention("9.0")
}
