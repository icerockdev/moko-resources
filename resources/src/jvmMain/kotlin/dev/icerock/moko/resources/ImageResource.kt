/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import org.apache.batik.transcoder.Transcoder
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import javax.imageio.ImageIO

actual data class ImageResource(
    val resourcesClassLoader: ClassLoader,
    val filePath: String,
    val darkFilePath: String?
) {
    constructor(
        resourcesClassLoader: ClassLoader,
        filePath: String
    ) : this(resourcesClassLoader, filePath, null)

    fun getThemedImage(isDark: Boolean): BufferedImage {
        return readImage(
            filePath = if (isDark) (darkFilePath ?: filePath) else filePath
        )
    }

    val image: BufferedImage by lazy {
        readImage(filePath)
    }

    private fun readImage(filePath: String): BufferedImage {
        val stream = resourcesClassLoader.getResourceAsStream(filePath)
            ?: throw FileNotFoundException("Couldn't open resource as stream at: $filePath")
        return stream.use {
            if (filePath.endsWith(".svg", ignoreCase = true)) {
                readSvg(it)
            } else {
                ImageIO.read(it)
            }
        }
    }

    private fun readSvg(
        inputStream: InputStream
    ): BufferedImage {
        // Create a PNG transcoder.
        val t: Transcoder = PNGTranscoder()

        // Create the transcoder input.
        val input = TranscoderInput(inputStream)

        // Create the transcoder output.

        val tempFile: File = File.createTempFile("moko-resources", ".png")

        try {
            tempFile.outputStream().use {
                val output = TranscoderOutput(it)
                t.transcode(input, output)
            }

            return tempFile.inputStream().use {
                ImageIO.read(it)
            }
        } finally {
            tempFile.delete()
        }
    }
}
