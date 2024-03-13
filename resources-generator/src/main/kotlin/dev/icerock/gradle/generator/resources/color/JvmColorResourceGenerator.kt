/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.color

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addJvmPlatformResourceClassLoaderProperty
import dev.icerock.gradle.generator.addValuesFunction
import dev.icerock.gradle.metadata.resource.ColorMetadata

internal class JvmColorResourceGenerator(
    private val className: String,
) : PlatformResourceGenerator<ColorMetadata> {
    override fun imports(): List<ClassName> = listOf(Constants.graphicsColorName)

    override fun generateInitializer(metadata: ColorMetadata): CodeBlock {
        return createColorResourceCodeInitializer(metadata)
    }

    override fun generateResourceFiles(data: List<ColorMetadata>) = Unit

    override fun generateBeforeProperties(
        builder: Builder,
        metadata: List<ColorMetadata>,
        modifier: KModifier?,
    ) {
        builder.addJvmPlatformResourceClassLoaderProperty(
            modifier = modifier,
            resourcesClassName = className
        )
    }

    override fun generateAfterProperties(
        builder: Builder,
        metadata: List<ColorMetadata>,
        modifier: KModifier?,
    ) {
        builder.addValuesFunction(
            modifier = modifier,
            metadata = metadata,
            classType = Constants.colorResourceName
        )
    }
}
