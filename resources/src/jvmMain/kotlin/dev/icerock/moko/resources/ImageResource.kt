/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import java.awt.image.BufferedImage
import java.io.FileNotFoundException
import javax.imageio.ImageIO

actual class ImageResource(imagePath: String) {

    val image: BufferedImage = with(Thread.currentThread().contextClassLoader) {
        ImageIO.read(
            getResourceAsStream(imagePath)
                ?: throw FileNotFoundException("Couldn't open resource as stream at: $imagePath")
        )
    }

}