/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.font

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.metadata.resource.FontMetadata

internal class NOPFontResourceGenerator : PlatformResourceGenerator<FontMetadata> {
    override fun imports(): List<ClassName> {
        TODO("Not yet implemented")
    }

    override fun generateResourceFiles(data: List<FontMetadata>) {
        TODO("Not yet implemented")
    }

    override fun generateInitializer(metadata: FontMetadata): CodeBlock {
        TODO("Not yet implemented")
    }
}
