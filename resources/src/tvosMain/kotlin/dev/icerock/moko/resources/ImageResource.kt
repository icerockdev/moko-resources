/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import platform.Foundation.NSBundle
import platform.UIKit.UIImage

actual data class ImageResource(
    val assetImageName: String,
    val bundle: NSBundle = NSBundle.mainBundle
) {
    fun toUIImage(): UIImage? {
        return UIImage.imageNamed(
            name = assetImageName,
            inBundle = bundle,
            compatibleWithTraitCollection = null
        )
    }
}