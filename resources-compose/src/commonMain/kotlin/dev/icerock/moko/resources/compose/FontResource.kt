/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import dev.icerock.moko.resources.FontResource

@Composable
fun fontFamilyResource(fontResource: FontResource): FontFamily {
    return fontResource.asFont()
        ?.let { FontFamily(it) }
        ?: FontFamily.Default
}

@Composable
expect fun FontResource.asFont(
    weight: FontWeight = FontWeight.Normal,
    style: FontStyle = FontStyle.Normal,
): Font?
