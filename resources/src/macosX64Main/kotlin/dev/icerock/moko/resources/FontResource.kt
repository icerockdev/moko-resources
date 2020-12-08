package dev.icerock.moko.resources

import kotlinx.cinterop.CPointer
import platform.AppKit.NSFont
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
import platform.darwin.UInt8Var

actual class FontResource(
    fontName: String,
    bundle: NSBundle = NSBundle.mainBundle
) {
    private val fontRef: CGFontRef

    init {
        val pathForResourceString = bundle.pathForResource(name = "$fontName.ttf", ofType = null)!!
        val fontData = NSData.create(contentsOfFile = pathForResourceString)!!
        val cfDataRef = CFDataCreate(
            kCFAllocatorDefault,
            fontData.bytes() as CPointer<UInt8Var>,
            fontData.length.toLong()
        )
        val dataProvider = CGDataProviderCreateWithCFData(cfDataRef)
        fontRef = CGFontCreateWithDataProvider(dataProvider)!!
    }

    fun nsFont(withSize: Double): NSFont {
        val ctFont = CTFontCreateWithGraphicsFont(fontRef, withSize, null, null)
        return CFBridgingRelease(ctFont) as NSFont
    }
}