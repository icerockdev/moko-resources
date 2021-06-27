/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.graphics.Color
import platform.UIKit.UIUserInterfaceStyle

fun ColorResource.getColor(userInterfaceStyle: UIUserInterfaceStyle): Color {
    return when (this) {
        is ColorResource.Single -> {
            color
        }
        is ColorResource.Themed -> {
            when (userInterfaceStyle) {
                UIUserInterfaceStyle.UIUserInterfaceStyleDark -> dark
                UIUserInterfaceStyle.UIUserInterfaceStyleLight -> light
                UIUserInterfaceStyle.UIUserInterfaceStyleUnspecified -> light
                else -> light
            }
        }
    }
}
