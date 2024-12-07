/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.utils

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.ObjCClass
import platform.Foundation.NSBundle
import platform.darwin.NSObject

@OptIn(BetaInteropApi::class)
@ExportObjCClass
internal object MRBundleAnchor : NSObject() {

    fun getBundle(): NSBundle {
        val anchor: ObjCClass = requireNotNull(MRBundleAnchor.`class`()) {
            "can't get class of $MRBundleAnchor"
        }
        return NSBundle.bundleForClass(anchor)
    }
}
