/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.test

import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.FileResource
import dev.icerock.moko.resources.StringResource

actual fun createStringResourceMock(): StringResource = StringResource("")
actual fun createFileResourceMock(): FileResource = FileResource("", "")
actual fun createColorResourceMock(): ColorResource = ColorResource("")
