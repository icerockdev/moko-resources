/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc.color

import dev.icerock.moko.graphics.toNSColor
import dev.icerock.moko.resources.getNSColor
import platform.AppKit.NSAppearance
import platform.AppKit.NSAppearanceName
import platform.AppKit.NSColor

fun ColorDesc.getNSColor(): NSColor {
    return when (this) {
        is ColorDescResource -> resource.getNSColor()
        is ColorDescSingle -> color.toNSColor()
        is ColorDescThemed -> {
            val name: NSAppearanceName = NSAppearance.currentAppearance.name
            if (name?.contains("Dark") == true) {
                darkColor
            } else {
                lightColor
            }.toNSColor()
        }

        else -> throw IllegalArgumentException("unknown class ${this::class}")
    }
}
