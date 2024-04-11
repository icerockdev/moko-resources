/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.metadata.container

import com.squareup.kotlinpoet.TypeSpec

internal data class FileNode<T>(
    val current: String,
    val filePath: String? = null,
    val metadata: T? = null,
    val specObject: TypeSpec.Builder?,
    val children: MutableList<FileNode<T>> = mutableListOf(),
)

internal fun <T> List<FileNode<T>>.hasInChildren(file: String): Boolean {
    return firstOrNull { it.current == file } == null
}

internal fun <T> List<FileNode<T>>.hasInDir(dirName: String): FileNode<T>? {
    return firstOrNull { it.current == dirName }
}
