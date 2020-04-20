/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import org.gradle.api.file.FileTree
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

data class SourceInfo(
    val generatedDir: File,
    val sourceSet: KotlinSourceSet,
    val commonResources: FileTree,
    val mrClassPackage: String,
    val androidRClassPackage: String
)
