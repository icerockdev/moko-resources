/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import java.awt.Font
import java.awt.GraphicsEnvironment
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

actual class FontResource(
    val resourcesClassLoader: ClassLoader,
    val filePath: String
) {
    val file: File by lazy {
        val resourceStream: InputStream = resourcesClassLoader.getResourceAsStream(filePath)
            ?: throw FileNotFoundException("Couldn't find font resource at: $filePath")

        resourceStream.use { inputStream ->
            val file = File.createTempFile("moko-resources-font-cache", null)
            file.deleteOnExit()

            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            file
        }
    }

    val font: Font by lazy {
        Font.createFont(Font.TRUETYPE_FONT, file)
    }

    init {
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font)
    }
}
