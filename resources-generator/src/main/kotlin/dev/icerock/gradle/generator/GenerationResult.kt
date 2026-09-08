/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.metadata.container.ContainerMetadata

internal data class GenerationResult(
    val typeSpec: TypeSpec,
    val metadata: ContainerMetadata,
    val fileSpecs: List<FileSpec> = emptyList()
)
