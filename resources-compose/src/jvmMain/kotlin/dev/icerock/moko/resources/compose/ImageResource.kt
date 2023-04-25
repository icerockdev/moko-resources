/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import dev.icerock.moko.resources.ImageResource

@Composable
actual fun painterResource(imageResource: ImageResource): Painter =
    painterResource(resourcePath = imageResource.filePath)
