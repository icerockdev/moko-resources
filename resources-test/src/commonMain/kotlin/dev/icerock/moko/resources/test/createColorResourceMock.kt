/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.test

import dev.icerock.moko.graphics.Color
import dev.icerock.moko.resources.ColorResource

fun createColorResourceMock(): ColorResource = ColorResource.Single(Color(0, 0, 0, 0))
