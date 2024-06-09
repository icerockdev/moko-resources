/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.PropertySpec
import dev.icerock.gradle.metadata.resource.ResourceMetadata
import java.io.File

internal interface ResourceGenerator<T : ResourceMetadata> {
    fun generateMetadata(files: Set<File>): List<T>

    fun generateProperty(metadata: T): PropertySpec.Builder
}
