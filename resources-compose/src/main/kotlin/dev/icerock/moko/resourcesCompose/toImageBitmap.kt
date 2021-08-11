package dev.icerock.moko.resourcesCompose

import androidx.compose.ui.graphics.ImageBitmap

// TODO this needs to be updated, would be nice to have a converter in compose
fun BufferedImage.toImageBitmap(): ImageBitmap {
    val byteArray = (raster.dataBuffer as DataBufferByte).data
    val imageInfo = ImageInfo(width, height, ColorType.RGB_565, ColorAlphaType.OPAQUE)
    return Bitmap().apply {
        installPixels(imageInfo, byteArray, imageInfo.minRowBytes)
    }.asImageBitmap()
}