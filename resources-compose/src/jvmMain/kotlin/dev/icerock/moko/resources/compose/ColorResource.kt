/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.icerock.moko.resources.ColorResource

@Composable
fun ColorResource.toColor(): Color {
    val mokoColor = if (isSystemInDarkTheme()) darkColor else lightColor
    return Color(mokoColor.argb)
}
