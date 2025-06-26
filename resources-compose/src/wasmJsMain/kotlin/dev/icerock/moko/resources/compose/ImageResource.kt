/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.LocalSystemTheme
import androidx.compose.ui.SystemTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.compose.internal.SVGPainter
import dev.icerock.moko.resources.compose.internal.produceByteArray
import org.jetbrains.skia.Data
import org.jetbrains.skia.Image
import org.jetbrains.skia.svg.SVGDOM

@OptIn(InternalComposeUiApi::class)
@Composable
actual fun painterResource(imageResource: ImageResource): Painter {
    val fileUrl: String = if (LocalSystemTheme.current == SystemTheme.Dark) {
        imageResource.darkFileUrl ?: imageResource.fileUrl
    } else {
        imageResource.fileUrl
    }

    val bytes: ByteArray? by produceByteArray(url = fileUrl)
    val localBytes: ByteArray? = bytes
    val density: Density = LocalDensity.current

    return remember(localBytes, density, fileUrl) {
        if (localBytes == null) {
            return@remember ColorPainter(color = Color.Transparent)
        }

        if (fileUrl.endsWith(".svg", ignoreCase = true)) {
            SVGPainter(SVGDOM(Data.makeFromBytes(localBytes)), density)
        } else {
            val skiaImage: Image = Image.makeFromEncoded(bytes = localBytes)
            val imageBitmap: ImageBitmap = skiaImage.toComposeImageBitmap()

            BitmapPainter(image = imageBitmap)
        }
    }
}
