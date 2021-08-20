/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import org.jetbrains.skija.Bitmap
import org.jetbrains.skija.ColorAlphaType
import org.jetbrains.skija.ColorType
import org.jetbrains.skija.ImageInfo
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte

// TODO this needs to be updated, would be nice to have a converter in compose
fun BufferedImage.toImageBitmap(): ImageBitmap {
    val byteArray = (raster.dataBuffer as DataBufferByte).data
    val imageInfo = ImageInfo(width, height, ColorType.RGB_565, ColorAlphaType.OPAQUE)
    return Bitmap().apply {
        installPixels(imageInfo, byteArray, imageInfo.minRowBytes)
    }.asImageBitmap()
}