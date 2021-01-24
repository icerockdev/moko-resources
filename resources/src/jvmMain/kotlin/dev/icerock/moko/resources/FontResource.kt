/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import java.awt.Font
import java.awt.GraphicsEnvironment
import java.io.File

actual class FontResource(fontPath: String) {

    val font: Font = Font.createFont(Font.TRUETYPE_FONT, File(fontPath))

    init {
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font)
    }
}
