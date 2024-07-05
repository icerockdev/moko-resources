/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.test

import dev.icerock.moko.resources.FontResource
import dev.icerock.moko.resources.ImageResource

actual fun createImageResourceMock(): ImageResource = ImageResource("")
actual fun createFontResourceMock(): FontResource = FontResource("")
