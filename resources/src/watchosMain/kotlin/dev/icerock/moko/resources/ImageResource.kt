/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.parcelize.Parcelable
import platform.UIKit.UIImage

actual class ImageResource(
    val assetImageName: String
) : Parcelable {
    fun toUIImage(): UIImage? {
        return UIImage.imageNamed(name = assetImageName)
    }
}
