/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.test

import dev.icerock.moko.graphics.Color
import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.FileResource
import dev.icerock.moko.resources.FontResource
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource

actual fun createImageResourceMock(): ImageResource =
    ImageResource(ClassLoader.getPlatformClassLoader(), "")

actual fun createStringResourceMock(): StringResource =
    StringResource(ClassLoader.getPlatformClassLoader(), "", "")

actual fun createFileResourceMock(): FileResource =
    FileResource(ClassLoader.getPlatformClassLoader(), "")

actual fun createFontResourceMock(): FontResource =
    FontResource(ClassLoader.getPlatformClassLoader(), "")

actual fun createColorResourceMock(): ColorResource =
    ColorResource(Color(0x0), Color(0x0))
