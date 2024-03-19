/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalForeignApi::class)

package dev.icerock.moko.resources

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGFontRef
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIFont

actual class FontResource(
    val fontName: String,
    val bundle: NSBundle = NSBundle.mainBundle
) {
    val filePath: String
        get() {
            return bundle.pathForResource(
                name = fontName,
                ofType = null
            ) ?: error("file $fontName not found in $bundle")
        }

    val data: NSData
        get() {
            val filePath: String = this.filePath
            return NSData.create(contentsOfFile = filePath)
                ?: error("can't read $filePath file")
        }

    internal val fontRef: CGFontRef = initializeFontRef()
}

expect fun FontResource.initializeFontRef() : CGFontRef
expect fun FontResource.uiFont(withSize: Double): UIFont