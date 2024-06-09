/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.ui.LocalSystemTheme
import androidx.compose.ui.SystemTheme
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import dev.icerock.moko.resources.ImageResource

@OptIn(InternalComposeApi::class)
@Composable
actual fun painterResource(imageResource: ImageResource): Painter {
    val filePath: String = if (LocalSystemTheme.current == SystemTheme.Dark) {
        imageResource.darkFilePath ?: imageResource.filePath
    } else {
        imageResource.filePath
    }

    return painterResource(resourcePath = filePath)
}
