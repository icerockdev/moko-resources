/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.rework

import org.gradle.api.file.FileTree
import org.gradle.api.tasks.util.PatternFilterable

data class ResourcesFiles(
    val ownSourceSet: SourceSetResources,
    val upperSourceSets: List<SourceSetResources>
) {
    fun matching(filter: PatternFilterable.() -> Unit): ResourcesFiles {
        return ResourcesFiles(
            ownSourceSet = ownSourceSet.copy(fileTree = ownSourceSet.fileTree.matching(filter)),
            upperSourceSets = upperSourceSets.map { it.copy(fileTree = it.fileTree.matching(filter)) }
        )
    }

    data class SourceSetResources(
        val sourceSetName: String,
        val fileTree: FileTree
    )
}
