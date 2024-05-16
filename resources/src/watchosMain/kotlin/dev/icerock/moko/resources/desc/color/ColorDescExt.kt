/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc.color

import dev.icerock.moko.graphics.Color
import dev.icerock.moko.resources.getUIColor
import kotlinx.cinterop.UnsafeNumber
import platform.CoreGraphics.CGFloat
import platform.UIKit.UIColor

fun ColorDesc.getUIColor(): UIColor {
    return when (this) {
        is ColorDescResource -> resource.getUIColor()
        is ColorDescSingle -> color.toUIColor()
        // light / dark mode seems unsupported on watchos, fallback to lightColor
        is ColorDescThemed -> lightColor.toUIColor()
        else -> throw IllegalArgumentException("unknown class ${this::class}")
    }
}

// TODO: Replace after update moko-graphics with support of WatchOS
@OptIn(UnsafeNumber::class)
private fun Color.toUIColor(): UIColor {
    val maxColorValue = 0xFF
    return UIColor(
        red = red.toFloat() as CGFloat / maxColorValue,
        green = green.toFloat() as CGFloat / maxColorValue,
        blue = blue.toFloat() as CGFloat / maxColorValue,
        alpha = alpha.toFloat() as CGFloat / maxColorValue
    )
}
