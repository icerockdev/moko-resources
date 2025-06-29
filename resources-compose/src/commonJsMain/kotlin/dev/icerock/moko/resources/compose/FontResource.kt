/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import dev.icerock.moko.resources.FontResource
import dev.icerock.moko.resources.compose.internal.produceByteArray

@Composable
actual fun FontResource.asFont(
    weight: FontWeight,
    style: FontStyle,
): Font? {
    val bytes: ByteArray? by produceByteArray(url = fileUrl)

    return remember(bytes, weight, style) {
        bytes?.let { b ->
            Font(
                identity = fontFamily,
                data = b,
                weight = weight,
                style = style,
            )
        }
    }
}
