/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.graphics.Color
import dev.icerock.moko.resources.internal.Window
import dev.icerock.moko.resources.internal.getDarkModeFlow
import dev.icerock.moko.resources.internal.isDarkMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

actual class ColorResource(
    val lightColor: Color,
    val darkColor: Color
) {
    fun getColor(window: Window): Color {
        return if (window.isDarkMode()) darkColor else lightColor
    }

    fun getColorFlow(window: Window): Flow<Color> {
        return window.getDarkModeFlow().map { getColor(window) }
    }
}
