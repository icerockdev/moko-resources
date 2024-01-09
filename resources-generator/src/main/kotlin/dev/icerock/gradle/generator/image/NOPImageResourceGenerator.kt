/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.image

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.metadata.resource.ImageMetadata

internal class NOPImageResourceGenerator() : PlatformResourceGenerator<ImageMetadata> {
    override fun imports(): List<ClassName> {
        TODO("Not yet implemented")
    }

    override fun generateResourceFiles(data: List<ImageMetadata>) {
        TODO("Not yet implemented")
    }

    override fun generateInitializer(metadata: ImageMetadata): CodeBlock {
        TODO("Not yet implemented")
    }
}
