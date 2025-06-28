/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.remember
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.LocalSystemTheme
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.compose.internal.toSkiaImage
import kotlinx.cinterop.ExperimentalForeignApi
import org.jetbrains.skia.Image
import platform.CoreGraphics.CGImageRef
import platform.UIKit.UIImage

@OptIn(ExperimentalForeignApi::class, InternalComposeApi::class, InternalComposeUiApi::class)
@Composable
actual fun painterResource(imageResource: ImageResource): Painter {
    return remember(LocalSystemTheme.current, imageResource) {
        val uiImage: UIImage = imageResource.toUIImage()
            ?: throw IllegalArgumentException("can't read UIImage of $imageResource")

        val cgImage: CGImageRef = uiImage.CGImage()
            ?: throw IllegalArgumentException("can't read CGImage of $imageResource")

        val skiaImage: Image = cgImage.toSkiaImage()

        BitmapPainter(image = skiaImage.toComposeImageBitmap())
    }
}
