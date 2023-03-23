/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import cnames.structs.CGColorSpace
import cnames.structs.CGImage
import dev.icerock.moko.resources.ImageResource
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.readBytes
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGContextRef
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGImageRef
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIImage
import platform.posix.free
import platform.posix.malloc
import platform.posix.size_t

@Composable
actual fun painterResource(imageResource: ImageResource): Painter {
    return remember(imageResource) {
        val uiImage: UIImage = imageResource.toUIImage()
            ?: throw IllegalArgumentException("can't read UIImage of $imageResource")

        val cgImage: CGImageRef = uiImage.CGImage()
            ?: throw IllegalArgumentException("can't read CGImage of $imageResource")

        val skiaImage: Image = cgImage.toSkiaImage()

        BitmapPainter(image = skiaImage.toComposeImageBitmap())
    }
}

private fun CGImageRef.toSkiaImage(): Image {
    val cgImage: CPointer<CGImage> = this
    val width: size_t = CGImageGetWidth(cgImage)
    val height: size_t = CGImageGetHeight(cgImage)
    val space: CPointer<CGColorSpace>? = CGColorSpaceCreateDeviceRGB()
    val bitsPerComponent: ULong = 8u
    val bitsPerPixel: ULong = bitsPerComponent * 4u
    val bytesPerPixel: ULong = bitsPerPixel / bitsPerComponent
    val bytesPerRow: ULong = width * bytesPerPixel
    val bufferSize: ULong = width * height * bytesPerPixel

    val data: CPointer<out CPointed> = malloc(bufferSize)
        ?: throw IllegalArgumentException("can't allocate memory for render bitmap of $cgImage")

    val ctx: CGContextRef = CGBitmapContextCreate(
        data = data,
        width = width,
        height = height,
        bitsPerComponent = bitsPerComponent,
        bytesPerRow = bytesPerRow,
        space = space,
        bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value
    ) ?: throw IllegalArgumentException("can't create bitmap context for $cgImage")

    CGContextDrawImage(
        ctx,
        rect = CGRectMake(
            x = 0.0,
            y = 0.0,
            width = width.toDouble(),
            height = height.toDouble()
        ),
        image = cgImage
    )

    CGContextRelease(ctx)

    val bytes: ByteArray = data.readBytes(bufferSize.toInt())

    free(data)

    return Image.makeRaster(
        imageInfo = ImageInfo(
            width = width.toInt(),
            height = height.toInt(),
            colorType = ColorType.RGBA_8888,
            alphaType = ColorAlphaType.PREMUL
        ),
        bytes = bytes,
        rowBytes = bytesPerRow.toInt()
    )
}
