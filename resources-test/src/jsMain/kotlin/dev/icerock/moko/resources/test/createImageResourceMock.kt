/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.test

import dev.icerock.moko.resources.*

actual fun createImageResourceMock(): ImageResource = ImageResource("")

actual fun createStringResourceMock(): StringResource =
    StringResource("", SupportedLocales(emptyList()), "")
actual fun createFileResourceMock(): FileResource = FileResource("")

actual fun createFontResourceMock(): FontResource = FontResource("")