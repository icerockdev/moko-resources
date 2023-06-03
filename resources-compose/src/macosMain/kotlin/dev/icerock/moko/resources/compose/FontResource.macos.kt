/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import dev.icerock.moko.resources.FontResource
import dev.icerock.moko.resources.compose.internal.toByteArray

@Composable
actual fun FontResource.asFont(
    weight: FontWeight,
    style: FontStyle,
): Font? = remember(filePath) {
    Font(
        identity = fontName,
        data = data.toByteArray(),
        weight = weight,
        style = style,
    )
}
