/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.jsJvmCommon

import com.squareup.kotlinpoet.CodeBlock
import dev.icerock.gradle.generator.ColorNode

fun createColorResourceInitializer(color: ColorNode): CodeBlock {
    return if (color.isThemed()) {
        "ColorResource(lightColor = Color(0x${color.lightColor}), darkColor = Color(0x${color.darkColor}))"
    } else {
        "ColorResource(lightColor = Color(0x${color.singleColor}), darkColor = Color(0x${color.singleColor}))"
    }.let { CodeBlock.of(it) }
}
