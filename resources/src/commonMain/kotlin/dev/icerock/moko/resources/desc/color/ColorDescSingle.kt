/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc.color

import dev.icerock.moko.graphics.Color

class ColorDescSingle(val color: Color) : ColorDesc

@Suppress("FunctionName")
fun ColorDesc.Companion.Single(color: Color): ColorDesc = ColorDescSingle(color)

fun Color.asColorDesc() = ColorDesc.Single(this)
