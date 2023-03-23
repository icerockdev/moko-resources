/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import cnames.structs.__CFData
import dev.icerock.moko.resources.ImageResource
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.UByteVarOf
import kotlinx.cinterop.get
import kotlinx.cinterop.useContents
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFIndex
import platform.CoreFoundation.CFRelease
import platform.CoreGraphics.CGDataProviderCopyData
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageGetAlphaInfo
import platform.CoreGraphics.CGImageGetBytesPerRow
import platform.CoreGraphics.CGImageGetDataProvider
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetPixelFormatInfo
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGImagePixelFormatInfo
import platform.CoreGraphics.CGImageRef
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.kCGImagePixelFormatMask
import platform.CoreGraphics.kCGImagePixelFormatPacked
import platform.CoreGraphics.kCGImagePixelFormatRGB101010
import platform.CoreGraphics.kCGImagePixelFormatRGB555
import platform.CoreGraphics.kCGImagePixelFormatRGB565
import platform.CoreGraphics.kCGImagePixelFormatRGBCIF10
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.darwin.UInt8
import platform.posix.size_t

@Composable
actual fun painterResource(imageResource: ImageResource): Painter {
    return remember(imageResource) {
        val uiImage: UIImage = imageResource.toUIImage()
            ?: throw IllegalArgumentException("can't read UIImage of $imageResource")

        val rerendered: UIImage = uiImage.rerender()

        val cgImage: CGImageRef = rerendered.CGImage()
            ?: throw IllegalArgumentException("can't read CGImage of $imageResource")

//        val cgImage: CPointer<CGImage> = CGImageCreateCopyWithColorSpace(
//            originalCgImage,
//            CGColorSpaceCreateDeviceRGB()
//        ) ?: throw IllegalArgumentException("can't copy CGImage of $imageResource")

        val width: size_t = CGImageGetWidth(cgImage)
        val height: size_t = CGImageGetHeight(cgImage)
        val bytesPerRow: size_t = CGImageGetBytesPerRow(cgImage)
        val pixelFormat: CGImagePixelFormatInfo = CGImageGetPixelFormatInfo(cgImage)
        val alphaInfo: CGImageAlphaInfo = CGImageGetAlphaInfo(cgImage)

        println(pixelFormat)
        println(alphaInfo)

        val colorType: ColorType
        val alphaType: ColorAlphaType

        when (pixelFormat) {
            kCGImagePixelFormatRGBCIF10 -> error("RGBCIF10")
            kCGImagePixelFormatRGB101010 -> {
                colorType = ColorType.RGB_101010X
                alphaType = ColorAlphaType.OPAQUE
            }

            kCGImagePixelFormatRGB555 -> error("RGB555")
            kCGImagePixelFormatRGB565 -> {
                colorType = ColorType.RGB_565
                alphaType = ColorAlphaType.OPAQUE
            }

            kCGImagePixelFormatMask -> error("Mask")
            kCGImagePixelFormatPacked -> {
                when (alphaInfo) {
                    CGImageAlphaInfo.kCGImageAlphaFirst -> {
                        colorType = ColorType.ARGB_4444
                        alphaType = ColorAlphaType.UNPREMUL
                    }

                    CGImageAlphaInfo.kCGImageAlphaLast -> {
                        colorType = ColorType.RGBA_8888
                        alphaType = ColorAlphaType.UNPREMUL
                    }

                    CGImageAlphaInfo.kCGImageAlphaOnly -> {
                        colorType = ColorType.ALPHA_8
                        alphaType = ColorAlphaType.UNPREMUL
                    }

                    CGImageAlphaInfo.kCGImageAlphaNone -> {
                        colorType = ColorType.RGB_888X
                        alphaType = ColorAlphaType.OPAQUE
                    }

                    CGImageAlphaInfo.kCGImageAlphaNoneSkipFirst -> {
                        colorType = ColorType.ARGB_4444
                        alphaType = ColorAlphaType.OPAQUE
                    }

                    CGImageAlphaInfo.kCGImageAlphaNoneSkipLast -> {
                        colorType = ColorType.RGBA_8888
                        alphaType = ColorAlphaType.OPAQUE
                    }

                    CGImageAlphaInfo.kCGImageAlphaPremultipliedFirst -> {
                        colorType = ColorType.ARGB_4444
                        alphaType = ColorAlphaType.PREMUL
                    }

                    CGImageAlphaInfo.kCGImageAlphaPremultipliedLast -> {
                        colorType = ColorType.RGBA_8888
                        alphaType = ColorAlphaType.PREMUL
                    }

                    else -> throw IllegalArgumentException("unknown alpha format $alphaInfo")
                }
            }

            else -> throw IllegalArgumentException("unknown pixel format $pixelFormat")
        }

        val bytes: ByteArray = cgImage.toByteArray()

        println("bytes: " + bytes.size)
        println("rowBytes: " + bytesPerRow.toInt())

        val image: Image = Image.makeRaster(
            imageInfo = ImageInfo(
                width = width.toInt(),
                height = height.toInt(),
                colorType = colorType,
                alphaType = alphaType
            ),
            bytes = bytes,
            rowBytes = bytesPerRow.toInt()
        )

        BitmapPainter(image = image.toComposeImageBitmap())
    }
}


private fun UIImage.rerender(): UIImage {
    val cgSize: CValue<CGSize> = this.size
    val cgRect: CValue<CGRect> = this.size.useContents {
        CGRectMake(0.0, 0.0, width, height)
    }
    val newImage: UIImage = UIGraphicsImageRenderer(
        size = cgSize,
        format = UIGraphicsImageRendererFormat()
    ).imageWithActions { _ ->
        drawInRect(cgRect)
    }
    return newImage //.imageWithRenderingMode(UIImageRenderingMode.UIImageRenderingModeAlwaysOriginal)
}

internal fun CGImageRef.toByteArray(): ByteArray {
    val data: CPointer<__CFData>? = CGDataProviderCopyData(CGImageGetDataProvider(this))
    val bytePointer: CPointer<UByteVarOf<UInt8>> = CFDataGetBytePtr(data)
        ?: throw IllegalArgumentException("can't read bytes of $this")
    val length: CFIndex = CFDataGetLength(data)
    val byteArray = ByteArray(length.toInt()) { index -> bytePointer[index].toByte() }
    CFRelease(data)
    return byteArray
}
