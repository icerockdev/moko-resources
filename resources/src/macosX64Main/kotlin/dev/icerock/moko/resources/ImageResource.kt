/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import platform.AppKit.NSImage
import platform.AppKit.imageForResource
import platform.Foundation.NSBundle

actual class ImageResource(
    val assetImageName: String,
    val bundle: NSBundle = NSBundle.mainBundle
) {
    fun toNSImage(): NSImage? = NSImage.imageNamed(assetImageName)
        ?: bundle.imageForResource(assetImageName)
}
