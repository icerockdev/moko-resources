/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import java.awt.image.BufferedImage
import java.io.FileNotFoundException
import javax.imageio.ImageIO

actual class ImageResource(
    val resourcesClassLoader: ClassLoader,
    val filePath: String
) {
    val image: BufferedImage = run {
        val stream = resourcesClassLoader.getResourceAsStream(filePath)
            ?: throw FileNotFoundException("Couldn't open resource as stream at: $filePath")
        stream.use { ImageIO.read(it) }
    }
}
