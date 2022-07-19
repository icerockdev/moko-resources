/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.graphics.Color

sealed class ColorResource(val name:String) {
    class Single(val color: Color, name: String) : ColorResource(name)

    class Themed(val light: Color, val dark: Color, name: String) : ColorResource(name)
}
