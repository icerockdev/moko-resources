/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import dev.icerock.moko.resources.FileResource
import dev.icerock.moko.resources.ImageResource
//import dev.icerock.moko.resources.compose.internal.UrlBitmapPainter
import kotlinx.browser.window

@Composable
actual fun painterResource(imageResource: ImageResource): Painter {
    TODO()
//    return remember(imageResource) {
//        ImageBitmap()
//            .readPixels()
//        BitmapPainter()
//        UrlBitmapPainter()
//    }
}
