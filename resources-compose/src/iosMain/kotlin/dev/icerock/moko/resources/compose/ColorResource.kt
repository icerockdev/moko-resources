/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.getUIColor
import platform.CoreImage.CIColor

@Composable
actual fun colorResource(resource: ColorResource): Color {
    // read darkmode to call recomposition when system settings will be changed
    val darkMode: Boolean = isSystemInDarkTheme()
    // FIXME: -CIColor not defined for the UIColor <UIDynamicCatalogColor: 0x60000371b610; name = textColor>; need to first convert colorspace.'
    val ciColor: CIColor = resource.getUIColor().CIColor
    return Color(
        red = ciColor.red.toFloat(),
        green = ciColor.green.toFloat(),
        blue = ciColor.blue.toFloat(),
        alpha = ciColor.alpha.toFloat()
    )
}
