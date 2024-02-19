/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc.color

import dev.icerock.moko.graphics.Color
import dev.icerock.moko.resources.getUIColor
import kotlinx.cinterop.UnsafeNumber
import platform.UIKit.UIColor

fun ColorDesc.getUIColor(): UIColor {
    return when (this) {
        is ColorDescResource -> resource.getUIColor()
        is ColorDescSingle -> color.toUIColor()
        is ColorDescThemed -> lightColor.toUIColor() //light / dark mode seems unsupported on watchos, fallback to lightColor
        else -> throw IllegalArgumentException("unknown class ${this::class}")
    }
}

// This probably should be migrated to moko-graphics? keep it internal for the moment
internal expect fun Color.toUIColor(): UIColor