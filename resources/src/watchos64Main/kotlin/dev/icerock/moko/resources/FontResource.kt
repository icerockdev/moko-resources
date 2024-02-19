/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalForeignApi::class)

package dev.icerock.moko.resources

import cnames.structs.CGDataProvider
import cnames.structs.__CFData
import cnames.structs.__CTFont
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreGraphics.CGDataProviderCreateWithCFData
import platform.CoreGraphics.CGFontCreateWithDataProvider
import platform.CoreGraphics.CGFontRef
import platform.CoreText.CTFontCreateWithGraphicsFont
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSData
import platform.UIKit.UIFont
import platform.darwin.UInt8Var

actual fun FontResource.initializeFontRef() : CGFontRef {
    val fontData: NSData = this.data
    val cfDataRef: CPointer<__CFData>? = CFDataCreate(
        kCFAllocatorDefault,
        fontData.bytes() as CPointer<UInt8Var>,
        fontData.length.toLong()
    )
    val dataProvider: CPointer<CGDataProvider>? = CGDataProviderCreateWithCFData(cfDataRef)
    return CGFontCreateWithDataProvider(dataProvider)!!
}

actual fun FontResource.uiFont(withSize: Double): UIFont {
    val ctFont: CPointer<__CTFont>? = CTFontCreateWithGraphicsFont(
        graphicsFont = fontRef,
        size = withSize,
        matrix = null,
        attributes = null
    )
    return CFBridgingRelease(ctFont) as UIFont
}
