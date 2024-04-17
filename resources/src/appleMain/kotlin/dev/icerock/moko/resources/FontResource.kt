/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import cnames.structs.__CFData
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFErrorRefVar
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFURLCreateWithFileSystemPath
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFURLPOSIXPathStyle
import platform.CoreGraphics.CGDataProviderCreateWithCFData
import platform.CoreGraphics.CGDataProviderRelease
import platform.CoreGraphics.CGFontCreateWithDataProvider
import platform.CoreGraphics.CGFontRef
import platform.CoreText.CTFontManagerRegisterFontsForURL
import platform.CoreText.kCTFontManagerScopeProcess
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.create
import platform.darwin.UInt8Var

actual class FontResource(
    val fontName: String,
    val bundle: NSBundle = NSBundle.mainBundle,
) {
    @OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
    internal val fontRef: CGFontRef by lazy {
        val fontData: NSData = this.data
        val cfDataRef: CPointer<__CFData>? = CFDataCreate(
            kCFAllocatorDefault,
            fontData.bytes() as CPointer<UInt8Var>,
            fontData.length.toLong().convert()
        )
        val dataProvider = CGDataProviderCreateWithCFData(cfDataRef)
        val cgFont = CGFontCreateWithDataProvider(dataProvider)!!

        CGDataProviderRelease(dataProvider)
        CFRelease(cfDataRef)

        cgFont
    }

    val filePath: String
        get() {
            return bundle.pathForResource(
                name = fontName,
                ofType = null
            ) ?: error("file $fontName not found in $bundle")
        }

    @OptIn(BetaInteropApi::class)
    val data: NSData
        get() {
            val filePath: String = this.filePath
            return NSData.create(contentsOfFile = filePath)
                ?: error("can't read $filePath file")
        }

    @OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
    @Throws(NSErrorException::class)
    @Suppress("unused")
    fun registerFont() {
        // CAST_NEVER_SUCCEEDS - String is final and isn't castable, but on iOS it's
        //  an NSString so `as NSString` is fine.
        // UNCHECKED_CAST - NSString and CFStringRef are toll-free bridged
        @Suppress("CAST_NEVER_SUCCEEDS", "UNCHECKED_CAST")
        val cfStringFilePath = CFBridgingRetain(filePath as NSString) as CFStringRef
        val cfFontUrlRef = CFURLCreateWithFileSystemPath(
            kCFAllocatorDefault,
            cfStringFilePath,
            kCFURLPOSIXPathStyle,
            false
        )

        var nsError: NSError? = null

        memScoped {
            val error = alloc<CFErrorRefVar>()
            if (!CTFontManagerRegisterFontsForURL(
                    cfFontUrlRef,
                    kCTFontManagerScopeProcess,
                    error.ptr
                )
            ) {
                error.value?.let {
                    nsError = CFBridgingRelease(it) as NSError
                }
            }
        }

        CFRelease(cfFontUrlRef)
        CFRelease(cfStringFilePath)

        nsError?.let {
            throw NSErrorException(it)
        }
    }
}
