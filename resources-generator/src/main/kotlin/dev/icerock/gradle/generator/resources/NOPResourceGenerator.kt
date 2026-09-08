/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.metadata.resource.ResourceMetadata

internal class NOPResourceGenerator<T : ResourceMetadata> : PlatformResourceGenerator<T> {
    override fun imports(): List<ClassName> {
        return emptyList()
    }

    override fun generateResourceFiles(data: List<T>) {
        Unit
    }

    override fun generateInitializer(metadata: T): CodeBlock {
        return CodeBlock.of("error(%S)", "NOP resource generator should not create initializers")
    }
}
