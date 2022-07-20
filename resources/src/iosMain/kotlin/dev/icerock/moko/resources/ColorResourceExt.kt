/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.graphics.Color
import platform.UIKit.UIColor
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.colorNamed

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

/**
 * Returns null if no color asset is found in the ios app.
 * update and configure copyColorAssetsToIOSApp in your ios run script to automate the copy process.
 *       val copyColorAssetsToIOSApp = tasks.register<Copy>("copyColorAssetsToIOSApp") {
 *              from("$rootDir/resources/build/generated/moko/iosMain/res/Assets.xcassets")
 *              into("$rootDir/iosApp/iosApp/Assets.xcassets/colors")
 *       }
 */
fun ColorResource.getThemeColor(): UIColor {
    return UIColor.colorNamed(this.name)!!
}
