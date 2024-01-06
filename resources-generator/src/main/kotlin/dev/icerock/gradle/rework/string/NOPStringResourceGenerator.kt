/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.rework.string

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import dev.icerock.gradle.rework.PlatformResourceGenerator
import dev.icerock.gradle.rework.metadata.resource.StringMetadata

class NOPStringResourceGenerator : PlatformResourceGenerator<StringMetadata> {
    override fun imports(): List<ClassName> {
        TODO("Not yet implemented")
    }

    override fun generateResourceFiles(data: List<StringMetadata>) {
        TODO("Not yet implemented")
    }

    override fun generateInitializer(metadata: StringMetadata): CodeBlock {
        TODO("Not yet implemented")
    }
}
