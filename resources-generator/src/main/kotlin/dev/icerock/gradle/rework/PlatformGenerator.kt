/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.rework

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import dev.icerock.gradle.rework.metadata.resource.ResourceMetadata

interface PlatformGenerator<T : ResourceMetadata> {
    fun imports(): List<ClassName>

    fun generateInitializer(metadata: T): CodeBlock
    fun generateResourceFiles(data: List<T>)
}
