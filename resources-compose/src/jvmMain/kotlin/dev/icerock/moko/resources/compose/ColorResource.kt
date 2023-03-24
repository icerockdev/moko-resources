/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.icerock.moko.resources.ColorResource

@Composable
actual fun colorResource(resource: ColorResource): Color {
    val mokoColor: dev.icerock.moko.graphics.Color = if (isSystemInDarkTheme()) resource.darkColor
    else resource.lightColor

    return Color(mokoColor.argb)
}
