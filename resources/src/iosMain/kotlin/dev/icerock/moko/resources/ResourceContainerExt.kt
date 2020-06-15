/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

actual fun ResourceContainer<ImageResource>.getImageByFileName(fileName: String): ImageResource? {
    return ImageResource(fileName, nsBundle).let { imgRes ->
        if (imgRes.toUIImage() != null) {
            imgRes
        } else {
            null
        }
    }
}
