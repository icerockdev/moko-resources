/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.metadata.resource.ResourceMetadata

internal interface PlatformResourceGenerator<T : ResourceMetadata> {
    fun imports(): List<ClassName>

    fun generateContainerProperties(
        builder: Builder,
        metadata: List<T>,
        modifier: KModifier? = null,
    ) = Unit

    fun generateContainerMembers(
        builder: Builder,
        metadata: List<T>,
        modifier: KModifier? = null,
    ) = Unit

    fun generateAccessorFilePreamble(
        builder: FileSpec.Builder,
        metadata: List<T>,
        objectName: String,
    ) = Unit

    fun generateAccessorInitializer(metadata: T): CodeBlock = generateInitializer(metadata)

    fun generateAccessorInitializer(
        metadata: T,
        accessorObjectName: String,
    ): CodeBlock = generateAccessorInitializer(metadata)

    fun generateAdditionalAccessorFiles(packageName: String): List<FileSpec> = emptyList()

    fun generateInitializer(metadata: T): CodeBlock
    fun generateResourceFiles(data: List<T>)
}
