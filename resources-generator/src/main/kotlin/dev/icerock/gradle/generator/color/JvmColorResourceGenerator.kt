/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.color

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.CodeConst
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.metadata.resource.ColorMetadata

internal class JvmColorResourceGenerator(
    private val className: String
) : PlatformResourceGenerator<ColorMetadata> {
    override fun imports(): List<ClassName> = listOf(CodeConst.graphicsColorName)

    override fun generateInitializer(metadata: ColorMetadata): CodeBlock {
        return createColorResourceCodeInitializer(metadata)
    }

    override fun generateResourceFiles(data: List<ColorMetadata>) = Unit

    override fun generateBeforeProperties(
        builder: TypeSpec.Builder,
        metadata: List<ColorMetadata>
    ) {
        // FIXME duplication
        val classLoaderProperty: PropertySpec = PropertySpec.builder(
            CodeConst.Jvm.resourcesClassLoaderPropertyName,
            CodeConst.Jvm.classLoaderName,
            KModifier.OVERRIDE
        )
            .initializer(CodeBlock.of(className + "." + CodeConst.Jvm.resourcesClassLoaderPropertyName))
            .build()

        builder.addProperty(classLoaderProperty)
    }
}
