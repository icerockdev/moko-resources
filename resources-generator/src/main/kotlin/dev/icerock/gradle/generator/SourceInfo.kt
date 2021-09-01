/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import org.gradle.api.file.FileTree
import org.gradle.api.file.SourceDirectorySet
import java.io.File

data class SourceInfo(
    val generatedDir: File,
    val commonResources: SourceDirectorySet,
    val mrClassPackage: String,
    val androidRClassPackage: String
)
