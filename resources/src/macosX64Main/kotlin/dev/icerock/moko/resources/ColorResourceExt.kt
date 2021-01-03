/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.graphics.Color
import platform.AppKit.NSAppearance

fun ColorResource.getColor(appearance: NSAppearance): Color {
    return when (this) {
        is ColorResource.Single -> {
            color
        }
        is ColorResource.Themed -> {
            if (appearance.name?.contains("Dark") == true) {
                dark
            } else {
                light
            }
        }
    }
}
