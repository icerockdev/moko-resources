/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import platform.Foundation.NSBundle
import kotlin.native.concurrent.ThreadLocal

actual interface StringDesc {
    fun localized(): String

    actual sealed class LocaleType {
        actual object System : LocaleType() {
            override fun getLocaleBundle(rootBundle: NSBundle): NSBundle {
                return rootBundle
            }
        }

        actual class Custom actual constructor(private val locale: String) : LocaleType() {
            override fun getLocaleBundle(rootBundle: NSBundle): NSBundle {
                return rootBundle.pathForResource(locale, "lproj")
                    ?.let { NSBundle.bundleWithPath(it) }
                    ?: rootBundle
            }
        }

        abstract fun getLocaleBundle(rootBundle: NSBundle): NSBundle
    }

    @ThreadLocal
    actual companion object {
        actual var localeType: LocaleType = LocaleType.System
    }
}