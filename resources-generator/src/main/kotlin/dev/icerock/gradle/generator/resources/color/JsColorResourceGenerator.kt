/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.color

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addEmptyPlatformResourceProperty
import dev.icerock.gradle.metadata.resource.ColorMetadata

internal class JsColorResourceGenerator : PlatformResourceGenerator<ColorMetadata> {
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
        builder.addEmptyPlatformResourceProperty(modifier)
    }

    override fun generateAfterProperties(
        builder: Builder,
        metadata: List<ColorMetadata>,
        modifier: KModifier?,
    ) {
        val languageKeysList: String = metadata.joinToString { it.key }

        val valuesFun: FunSpec = FunSpec.builder("values")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return listOf($languageKeysList)")
            .returns(
                ClassName("kotlin.collections", "List")
                    .parameterizedBy(Constants.colorResourceName)
            )
            .build()

        builder.addFunction(valuesFun)
    }
}
