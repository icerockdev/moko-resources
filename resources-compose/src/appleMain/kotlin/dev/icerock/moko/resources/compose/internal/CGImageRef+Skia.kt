/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose.internal

import cnames.structs.CGColorSpace
import cnames.structs.CGImage
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGContextClearRect
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGContextRef
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGImageRef
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.posix.free
import platform.posix.malloc
import platform.posix.memcpy
import platform.posix.size_t
import platform.posix.uint32_t

@OptIn(ExperimentalForeignApi::class)
internal fun CGImageRef.toSkiaImage(): Image {
    val cgImage: CPointer<CGImage> = this
    val width: size_t = CGImageGetWidth(cgImage)
    val height: size_t = CGImageGetHeight(cgImage)
    val space: CPointer<CGColorSpace>? = CGColorSpaceCreateDeviceRGB()
    val bitsPerComponent: ULong = 8u
    val bitsPerPixel: ULong = bitsPerComponent * 4u
    val bytesPerPixel: ULong = bitsPerPixel / bitsPerComponent
    val bytesPerRow: ULong = width * bytesPerPixel
    val bufferSize: ULong = width * height * bytesPerPixel
    val bitmapInfo: uint32_t = CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value

    check(bufferSize != 0UL) { "image can't be 0 bytes" }
    check(width > 0UL) { "width should be more then 0 px ($width)" }
    check(height > 0UL) { "height should be more then 0 px ($height)" }

    val data: CPointer<out CPointed> = malloc(bufferSize)
        ?: throw IllegalArgumentException("can't allocate memory for render bitmap of $cgImage")

    val rect: CValue<CGRect> = CGRectMake(
        x = 0.0,
        y = 0.0,
        width = width.toDouble(),
        height = height.toDouble()
    )

    val ctx: CGContextRef = CGBitmapContextCreate(
        data = data,
        width = width,
        height = height,
        bitsPerComponent = bitsPerComponent,
        bytesPerRow = bytesPerRow,
        space = space,
        bitmapInfo = bitmapInfo
    ) ?: throw IllegalArgumentException("can't create bitmap context for $cgImage")

    CGContextClearRect(c = ctx, rect = rect)
    CGContextDrawImage(c = ctx, rect = rect, image = cgImage)

    CGContextRelease(ctx)

    val bytes = ByteArray(bufferSize.toInt())
    memcpy(bytes.refTo(0), data, bufferSize)

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
