/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import platform.CoreGraphics.CGFloat
import platform.Foundation.NSBundle
import platform.UIKit.UIFont
import platform.UIKit.UIFontDescriptor

actual class FontResource(private val fontName: String) {
    fun uiFont(withSize: Double): UIFont {
        return UIFont.fontWithDescriptor(
            UIFontDescriptor.fontDescriptorWithName(fontName, (withSize)),
            withSize)
    }
}