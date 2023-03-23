/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import dev.icerock.moko.resources.ImageResource
import platform.AppKit.NSImage

@Composable
actual fun painterResource(imageResource: ImageResource): Painter {
    TODO()
//    return remember(imageResource) {
//        val nsImage: NSImage = imageResource.toNSImage()
//            ?: throw IllegalStateException("can't read NSImage from $imageResource")
//        MacosBitmapPainter(nsImage)
//    }
}
