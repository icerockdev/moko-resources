/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import java.awt.Font
import java.awt.GraphicsEnvironment
import java.io.File
import java.io.FileNotFoundException
import java.net.URL

actual class FontResource(
    val resourcesClassLoader: ClassLoader,
    val filePath: String
) {
    val font: Font = run {
        val resourceUrl: URL = resourcesClassLoader.getResource(filePath)
            ?: throw FileNotFoundException("Couldn't find font resource at: $filePath")
        val file: File = File(resourceUrl.toURI())
        Font.createFont(Font.TRUETYPE_FONT, file)
    }

    init {
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font)
    }
}
