/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.metadata.resource.ResourceMetadata

internal interface PlatformResourceGenerator<T : ResourceMetadata> {
    fun imports(): List<ClassName>

    fun generateBeforeProperties(
        builder: TypeSpec.Builder,
        metadata: List<T>,
        modifiers: List<KModifier>,
    ) = Unit

    fun generateAfterProperties(
        builder: TypeSpec.Builder,
        metadata: List<T>,
        modifiers: List<KModifier>,
    ) = Unit

    fun generateInitializer(metadata: T): CodeBlock
    fun generateResourceFiles(data: List<T>)
}
