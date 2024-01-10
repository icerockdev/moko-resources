/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.color

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.metadata.resource.ColorMetadata

internal class NOPColorResourceGenerator : PlatformResourceGenerator<ColorMetadata> {
    override fun imports(): List<ClassName> {
        TODO("Not yet implemented")
    }

    override fun generateResourceFiles(data: List<ColorMetadata>) {
        TODO("Not yet implemented")
    }

    override fun generateInitializer(metadata: ColorMetadata): CodeBlock {
        TODO("Not yet implemented")
    }
}
