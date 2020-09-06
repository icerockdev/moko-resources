/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

data class ArgbColor(val a: Float, val r: Float, val g: Float, val b: Float)

@Suppress("MagicNumber")
internal fun parseArgbColor(argbColor: Long): ArgbColor {
    val r: Float = (argbColor shr 16 and 0xff) / 255.0f
    val g: Float = (argbColor shr 8 and 0xff) / 255.0f
    val b: Float = (argbColor and 0xff) / 255.0f
    val a: Float = (argbColor shr 24 and 0xff) / 255.0f
    return ArgbColor(a, r, g, b)
}

@Suppress("MagicNumber")
internal fun parseRgbaColor(rgbaColor: Long): ArgbColor {
    val r: Float = (rgbaColor shr 24 and 0xff) / 255.0f
    val g: Float = (rgbaColor shr 16 and 0xff) / 255.0f
    val b: Float = (rgbaColor shr 8 and 0xff) / 255.0f
    val a: Float = (rgbaColor and 0xff) / 255.0f
    return ArgbColor(a, r, g, b)
}
