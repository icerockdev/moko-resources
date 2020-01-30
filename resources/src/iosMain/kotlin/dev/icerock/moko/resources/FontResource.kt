/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.pointed
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataCreateWithBytesNoCopy
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreGraphics.CGDataProviderCreateWithCFData
import platform.CoreGraphics.CGFloat
import platform.CoreGraphics.CGFontCreateWithDataProvider
import platform.CoreGraphics.CGFontRef
import platform.CoreText.CTFontCreateWithGraphicsFont
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIFont
import platform.UIKit.UIFontDescriptor
import platform.darwin.UInt8
import platform.darwin.UInt8Var
import platform.darwin.nil
import platform.objc.object_getClass

actual class FontResource(fontName: String) {
    private val fontRef: CGFontRef
    init {
        val bundle = NSBundle.bundleForClass(object_getClass(this)!!)
        val pathForResourceString = bundle.pathForResource(name = "$fontName.ttf", ofType = null)!!
        val fontData = NSData.create(contentsOfFile = pathForResourceString)!!
        val cfDataRef = CFDataCreate(
            kCFAllocatorDefault,
            fontData.bytes() as CPointer<UInt8Var>,
            fontData.length.toLong())
        val dataProvider = CGDataProviderCreateWithCFData(cfDataRef)
        fontRef = CGFontCreateWithDataProvider(dataProvider)!!
    }

    fun uiFont(withSize: Double): UIFont {
        val ctFont = CTFontCreateWithGraphicsFont(fontRef, withSize, null, null)
        return CFBridgingRelease(ctFont) as UIFont
    }
}