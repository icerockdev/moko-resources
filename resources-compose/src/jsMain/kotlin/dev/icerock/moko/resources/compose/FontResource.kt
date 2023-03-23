/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import dev.icerock.moko.resources.FontResource
import dev.icerock.moko.resources.compose.internal.produceByteArray

@Composable
actual fun fontFamilyResource(fontResource: FontResource): FontFamily {
    val bytes: ByteArray? by produceByteArray(url = fontResource.fileUrl)

    val localBytes: ByteArray? = bytes

    return remember(localBytes) {
        if (localBytes == null) {
            return@remember FontFamily.Default
        }

        val font = Font(
            identity = fontResource.fontFamily,
            data = localBytes
        )

        FontFamily(font)
    }
}
