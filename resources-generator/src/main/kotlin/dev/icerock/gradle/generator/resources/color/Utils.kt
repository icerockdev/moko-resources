/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.color

import com.squareup.kotlinpoet.CodeBlock
import dev.icerock.gradle.metadata.resource.ColorMetadata


internal fun createColorResourceCodeInitializer(color: ColorMetadata): CodeBlock {
    return when (color.value) {
        is ColorMetadata.ColorItem.Single -> {
            val colorHex: String = color.value.color.toRgbaHex()
            "ColorResource(lightColor = Color(0x$colorHex), darkColor = Color(0x$colorHex))"
        }

        is ColorMetadata.ColorItem.Themed -> {
            val lightHex: String = color.value.light.toRgbaHex()
            val darkHex: String = color.value.dark.toRgbaHex()
            "ColorResource(lightColor = Color(0x$lightHex), darkColor = Color(0x$darkHex))"
        }
    }.let { CodeBlock.of(it) }
}
