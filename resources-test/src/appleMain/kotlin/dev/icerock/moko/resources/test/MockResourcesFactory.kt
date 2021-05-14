/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.test

import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.FileResource

actual fun createStringResourceMock(): StringResource = StringResource("")
actual fun createFileResourceMock(): FileResource = FileResource("", "")