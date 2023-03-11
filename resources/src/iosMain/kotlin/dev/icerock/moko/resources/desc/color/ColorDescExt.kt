/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc.color

import dev.icerock.moko.graphics.toUIColor
import dev.icerock.moko.resources.getUIColor
import platform.UIKit.UIColor
import platform.UIKit.UIScreen
import platform.UIKit.UIUserInterfaceStyle

fun ColorDesc.getUIColor(): UIColor {
    return when (this) {
        is ColorDescResource -> resource.getUIColor()
        is ColorDescSingle -> color.toUIColor()
        is ColorDescThemed -> {
            val style: UIUserInterfaceStyle = UIScreen.mainScreen.traitCollection.userInterfaceStyle
            when (style) {
                UIUserInterfaceStyle.UIUserInterfaceStyleDark -> darkColor
                UIUserInterfaceStyle.UIUserInterfaceStyleLight -> lightColor
                UIUserInterfaceStyle.UIUserInterfaceStyleUnspecified -> lightColor
                else -> lightColor
            }.toUIColor()
        }

        else -> throw IllegalArgumentException("unknown class ${this::class}")
    }
}
