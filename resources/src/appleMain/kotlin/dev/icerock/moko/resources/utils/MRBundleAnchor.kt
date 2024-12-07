/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.utils

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.objcPtr
import platform.Foundation.NSBundle
import platform.Foundation.NSLog
import platform.darwin.NSObject

// can't be object, because https://github.com/JetBrains/kotlin-native/issues/3855#issuecomment-586990632
// in case of object we got error - java.lang.AssertionError: Assertion failed
@OptIn(BetaInteropApi::class)
@ExportObjCClass
class MRBundleAnchor : NSObject() {

    @OptIn(ExperimentalForeignApi::class)
    fun getBundle(): NSBundle {
//        return NSBundle.mainBundle
        NSLog("MOKO now i will search class anchor from $this")
        val anchor: ObjCClass = requireNotNull(this.`class`()) {
            "can't get class of $this"
        }

        NSLog("MOKO now i see anchor ${anchor.objcPtr()} and super is ${this.superclass().objcPtr()}")
        return NSBundle.bundleForClass(anchor).also {
            NSLog("MOKO i see bundle $it")
        }
    }
}
