/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc.color

import dev.icerock.moko.graphics.Color
import dev.icerock.moko.resources.internal.isDarkMode
import org.w3c.dom.Window

fun ColorDesc.getColor(window: Window): Color {
    return when (this) {
        is ColorDescResource -> resource.getColor(window)
        is ColorDescSingle -> color
        is ColorDescThemed -> if (window.isDarkMode()) darkColor else lightColor

        else -> throw IllegalArgumentException("unknown class ${this::class}")
    }
}
