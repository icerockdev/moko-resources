/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import org.apache.batik.transcoder.Transcoder
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream
import javax.imageio.ImageIO

actual class ImageResource(
    val resourcesClassLoader: ClassLoader,
    val filePath: String
) {
    val image: BufferedImage by lazy {
        val stream = resourcesClassLoader.getResourceAsStream(filePath)
            ?: throw FileNotFoundException("Couldn't open resource as stream at: $filePath")
        stream.use {
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
        val outputStream = ByteArrayOutputStream()
        outputStream.use {
            val output = TranscoderOutput(it)

            // Save the image.
            t.transcode(input, output)
        }

        // Convert the byte stream into an image.
        val imgData: ByteArray = outputStream.toByteArray()
        return ImageIO.read(ByteArrayInputStream(imgData))
    }
}
