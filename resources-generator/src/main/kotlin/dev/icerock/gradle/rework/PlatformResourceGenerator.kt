/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.rework

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.rework.metadata.resource.ResourceMetadata

interface PlatformResourceGenerator<T : ResourceMetadata> {
    fun imports(): List<ClassName>

    fun generateBeforeProperties(builder: TypeSpec.Builder) = Unit
    fun generateAfterProperties(builder: TypeSpec.Builder) = Unit

    fun generateInitializer(metadata: T): CodeBlock
    fun generateResourceFiles(data: List<T>)
}
