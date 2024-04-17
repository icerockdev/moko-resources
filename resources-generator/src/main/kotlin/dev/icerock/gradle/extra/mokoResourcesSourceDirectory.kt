/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("Filename")

package dev.icerock.gradle.extra

import dev.icerock.gradle.MultiplatformResourcesPluginExtension
import org.gradle.api.file.SourceDirectorySet
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.tooling.core.extrasKeyOf
import java.io.File

private fun mokoResourcesSourceDirectoryKey() =
    extrasKeyOf<SourceDirectorySet>("moko-resources-source-directory")

internal fun KotlinSourceSet.getOrCreateResourcesSourceDirectory(
    mrExtension: MultiplatformResourcesPluginExtension
): SourceDirectorySet {
    val currentSourceDirectory: SourceDirectorySet? = this.extras[mokoResourcesSourceDirectoryKey()]
    if (currentSourceDirectory != null) return currentSourceDirectory

    val sources = File(project.projectDir, "src")
    val resourceSourceSetDir = File(sources, this.name)
    val mokoResourcesDir = File(resourceSourceSetDir, "moko-resources")

    val resourcesSourceDirectory: SourceDirectorySet = project.objects.sourceDirectorySet(
        this.name,
        "moko-resources for ${this.name} sourceSet"
    )
    mrExtension.resourcesSourceSets.add(resourcesSourceDirectory)
    resourcesSourceDirectory.srcDirs(mokoResourcesDir)

    this.extras[mokoResourcesSourceDirectoryKey()] = resourcesSourceDirectory

    return resourcesSourceDirectory
}
