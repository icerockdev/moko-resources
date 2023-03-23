/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import dev.icerock.moko.resources.ImageResource
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.jetbrains.skia.Image
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.fetch.Response

@Composable
actual fun painterResource(imageResource: ImageResource): Painter {
    val bytes: ByteArray? by produceState<ByteArray?>(null) {
        val url: String = imageResource.fileUrl

        val response: Response = window.fetch(url).await()

        if (response.ok.not()) {
            console.error("can't load image from $url for painterResource")
            return@produceState
        }

        val buffer: ArrayBuffer = response.arrayBuffer().await()

        value = Int8Array(buffer)
            .unsafeCast<ByteArray>()
    }

    val localBytes: ByteArray? = bytes

    return remember(localBytes) {
        if (localBytes == null) {
            return@remember ColorPainter(color = Color.Transparent)
        }

        val skiaImage: Image = Image.makeFromEncoded(bytes = localBytes)
        val imageBitmap: ImageBitmap = skiaImage.toComposeImageBitmap()

        BitmapPainter(image = imageBitmap)
    }
}
