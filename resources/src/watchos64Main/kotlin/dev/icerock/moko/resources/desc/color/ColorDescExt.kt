/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc.color

import dev.icerock.moko.graphics.Color
import dev.icerock.moko.resources.getUIColor
import kotlinx.cinterop.UnsafeNumber
import platform.UIKit.UIColor

// This probably should be migrated to moko-graphics? keep it internal for the moment
internal actual fun Color.toUIColor(): UIColor {
    val maxColorValue = 0xFF
    return UIColor(
        red = red.toDouble() / maxColorValue,
        green = green.toDouble() / maxColorValue,
        blue = blue.toDouble() / maxColorValue,
        alpha = alpha.toDouble() / maxColorValue
    )
}
