/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

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
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIFont
import platform.darwin.UInt8Var

@OptIn(ExperimentalForeignApi::class)
actual class FontResource(
    val fontName: String,
    val bundle: NSBundle = NSBundle.mainBundle
) {
    private val fontRef: CGFontRef

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

    init {
        val fontData: NSData = this.data
        val cfDataRef: CPointer<__CFData>? = CFDataCreate(
            kCFAllocatorDefault,
            fontData.bytes() as CPointer<UInt8Var>,
            fontData.length.toLong()
        )
        val dataProvider: CPointer<CGDataProvider>? = CGDataProviderCreateWithCFData(cfDataRef)
        fontRef = CGFontCreateWithDataProvider(dataProvider)!!
    }

    fun uiFont(withSize: Double): UIFont {
        val ctFont: CPointer<__CTFont>? = CTFontCreateWithGraphicsFont(
            graphicsFont = fontRef,
            size = withSize,
            matrix = null,
            attributes = null
        )
        return CFBridgingRelease(ctFont) as UIFont
    }
}
