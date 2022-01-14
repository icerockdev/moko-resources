/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.awt.image.BufferedImage

@Deprecated(
    message = "Compose added this function to its API",
    replaceWith = ReplaceWith(
        expression = "toComposeImageBitmap()",
        imports = arrayOf("androidx.compose.ui.graphics.toComposeImageBitmap")
    )
)
fun BufferedImage.toImageBitmap(): ImageBitmap {
    return toComposeImageBitmap()
}
