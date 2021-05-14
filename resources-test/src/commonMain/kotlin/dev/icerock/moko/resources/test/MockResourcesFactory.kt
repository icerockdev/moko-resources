/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */
package dev.icerock.moko.resources.test

import dev.icerock.moko.resources.FileResource
import dev.icerock.moko.resources.FontResource
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource

expect fun createImageResourceMock(): ImageResource
expect fun createStringResourceMock(): StringResource
expect fun createFileResourceMock(): FileResource
expect fun createFontResourceMock(): FontResource
