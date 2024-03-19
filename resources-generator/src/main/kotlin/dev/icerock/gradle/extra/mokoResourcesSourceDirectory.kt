/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("Filename")

package dev.icerock.gradle.extra

import org.gradle.api.file.SourceDirectorySet
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.tooling.core.extrasKeyOf
import java.io.File

private fun mokoResourcesSourceDirectoryKey() =
    extrasKeyOf<SourceDirectorySet>("moko-resources-source-directory")

internal fun KotlinSourceSet.getOrCreateResourcesSourceDirectory(): SourceDirectorySet {
    val currentSourceDirectory: SourceDirectorySet? = this.extras[mokoResourcesSourceDirectoryKey()]
    if (currentSourceDirectory != null) return currentSourceDirectory

    val sources = File(project.projectDir, "src")
    val resourceSourceSetDir = File(sources, this.name)
    val mokoResourcesDir = File(resourceSourceSetDir, "moko-resources")

    val resourcesSourceDirectory: SourceDirectorySet = project.objects.sourceDirectorySet(
        this.name + "MokoResources",
        "moko-resources for ${this.name} sourceSet"
    )
    resourcesSourceDirectory.srcDirs(mokoResourcesDir)

    this.extras[mokoResourcesSourceDirectoryKey()] = resourcesSourceDirectory

    return resourcesSourceDirectory
}
