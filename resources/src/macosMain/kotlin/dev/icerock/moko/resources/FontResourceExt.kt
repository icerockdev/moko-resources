/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import cnames.structs.__CTFont
import kotlinx.cinterop.CPointer
import platform.AppKit.NSFont
import platform.CoreText.CTFontCreateWithGraphicsFont
import platform.Foundation.CFBridgingRelease

@Suppress("unused")
fun FontResource.nsFont(withSize: Double): NSFont {
    val ctFont: CPointer<__CTFont>? = CTFontCreateWithGraphicsFont(
        graphicsFont = fontRef,
        size = withSize,
        matrix = null,
        attributes = null
    )
    return CFBridgingRelease(ctFont) as NSFont
}
