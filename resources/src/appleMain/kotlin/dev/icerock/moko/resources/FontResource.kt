/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import cnames.structs.CGDataProvider
import cnames.structs.__CFData
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFErrorRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreGraphics.CGDataProviderCreateWithCFData
import platform.CoreGraphics.CGFontCreateWithDataProvider
import platform.CoreGraphics.CGFontRef
import platform.CoreText.CTFontManagerRegisterGraphicsFont
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.create
import platform.darwin.UInt8Var

actual class FontResource(
    val fontName: String,
    val bundle: NSBundle = NSBundle.mainBundle
) {
    internal val fontRef: CGFontRef

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

    @Throws(NSErrorException::class)
    @Suppress("unused")
    fun registerFont() =
        memScoped {
            val error = alloc<CFErrorRefVar>()
            if (!CTFontManagerRegisterGraphicsFont(fontRef, error.ptr)) {
                error.value?.let {
                    val nsError = CFBridgingRelease(it) as NSError
                    throw NSErrorException(nsError)
                }
            }
        }
}
