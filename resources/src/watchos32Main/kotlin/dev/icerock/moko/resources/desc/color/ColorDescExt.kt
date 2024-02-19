/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc.color

import dev.icerock.moko.graphics.Color
import platform.UIKit.UIColor

// This probably should be migrated to moko-graphics?
internal actual fun Color.toUIColor(): UIColor {
    val maxColorValue = 0xFF
    return UIColor(
        red = red.toFloat() / maxColorValue,
        green = green.toFloat() / maxColorValue,
        blue = blue.toFloat() / maxColorValue,
        alpha = alpha.toFloat() / maxColorValue
    )
}
