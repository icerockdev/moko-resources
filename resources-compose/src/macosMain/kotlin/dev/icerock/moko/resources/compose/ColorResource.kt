/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.getNSColor
import platform.AppKit.NSColor
import platform.AppKit.NSColorSpace.Companion.deviceRGBColorSpace

@Composable
actual fun colorResource(resource: ColorResource): Color {
    // TODO https://github.com/icerockdev/moko-resources/issues/443
    //  recompose when appearance changed (now not works in runtime!)
    val darkMode: Boolean = isSystemInDarkTheme()
    return remember(resource, darkMode) {
        val nsColor: NSColor = resource.getNSColor()
        val deviceColor: NSColor = nsColor.colorUsingColorSpace(deviceRGBColorSpace)
            ?: error("can't convert $nsColor to deviceRGBColorSpace")

        Color(
            red = deviceColor.redComponent.toFloat(),
            green = deviceColor.greenComponent.toFloat(),
            blue = deviceColor.blueComponent.toFloat(),
            alpha = deviceColor.alphaComponent.toFloat()
        )
    }
}
