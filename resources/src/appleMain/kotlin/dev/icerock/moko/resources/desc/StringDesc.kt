/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.localeWithLocaleIdentifier
import kotlin.native.concurrent.ThreadLocal

actual interface StringDesc {
    fun localized(): String

    actual sealed class LocaleType {
        actual object System : LocaleType() {
            override val locale: NSLocale
                get() = NSLocale.currentLocale

            override fun getLocaleBundle(rootBundle: NSBundle): NSBundle {
                return rootBundle
            }
        }

        actual class Custom actual constructor(locale: String) : LocaleType() {
            private val localePath: String = locale

            override val locale: NSLocale
                get() = NSLocale.localeWithLocaleIdentifier(localePath)

            override fun getLocaleBundle(rootBundle: NSBundle): NSBundle {
                return rootBundle.pathForResource(localePath, "lproj")
                    ?.let { NSBundle.bundleWithPath(it) }
                    ?: rootBundle
            }
        }

        abstract fun getLocaleBundle(rootBundle: NSBundle): NSBundle
        abstract val locale: NSLocale
    }

    @ThreadLocal
    actual companion object {
        actual var localeType: LocaleType = LocaleType.System
    }
}
