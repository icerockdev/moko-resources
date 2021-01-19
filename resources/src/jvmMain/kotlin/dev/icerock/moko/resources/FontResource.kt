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
