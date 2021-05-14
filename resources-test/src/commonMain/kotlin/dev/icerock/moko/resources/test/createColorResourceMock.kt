package dev.icerock.moko.resources.test

import dev.icerock.moko.graphics.Color
import dev.icerock.moko.resources.ColorResource

fun createColorResourceMock(): ColorResource = ColorResource.Single(Color(0, 0, 0, 0))
