/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo

@Deprecated(
    message = "Compose added this function to its API",
    replaceWith = ReplaceWith(
        expression = "toComposeImageBitmap()",
        imports = arrayOf("androidx.compose.ui.graphics.toComposeImageBitmap")
    )
)
fun BufferedImage.toImageBitmap(): ImageBitmap {
    val byteArray = (raster.dataBuffer as DataBufferByte).data
    val imageInfo = ImageInfo(width, height, ColorType.RGB_565, ColorAlphaType.OPAQUE)
    return Bitmap().apply {
        installPixels(imageInfo, byteArray, imageInfo.minRowBytes)
    }.asImageBitmap()
}
