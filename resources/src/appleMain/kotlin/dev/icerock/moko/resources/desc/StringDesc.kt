/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCountryCode
import platform.Foundation.NSLocaleLanguageCode
import platform.Foundation.NSLocaleVariantCode
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier
import platform.Foundation.localeIdentifierFromComponents
import kotlin.native.concurrent.ThreadLocal

actual interface StringDesc {
    fun localized(): String

    actual sealed class LocaleType {
        actual data object System : LocaleType() {
            override val locale: NSLocale
                get() = NSLocale.currentLocale

            override fun getLocaleBundle(rootBundle: NSBundle): NSBundle {
                return rootBundle
            }
        }

        actual class Custom actual constructor(locale: String) : LocaleType() {
            override val locale: NSLocale = NSLocale(
                NSLocale.localeIdentifierFromComponents(
                    buildMap {
                        val languageTagParts = locale.split("-")
                        put(NSLocaleLanguageCode, languageTagParts[0])
                        languageTagParts.getOrNull(1)?.let { country ->
                            put(NSLocaleCountryCode, country)
                        }
                        languageTagParts.getOrNull(2)?.let { variant ->
                            put(NSLocaleVariantCode, variant)
                        }
                    }
                )
            )

            override fun getLocaleBundle(rootBundle: NSBundle): NSBundle {
                // I don't like this, but I don't see a good way to get a proper BCP language tag from NSLocale
                // on Apple's dev documentation. This is a hack.
                val bcpLanguageTag = locale.localeIdentifier().replace("_", "-")
                return rootBundle.pathForResource(bcpLanguageTag, "lproj")
                    ?.let { NSBundle.bundleWithPath(it) }
                    ?: rootBundle
            }
        }

        abstract fun getLocaleBundle(rootBundle: NSBundle): NSBundle
        abstract val locale: NSLocale
    }

    actual companion object {
        actual var localeType: LocaleType = LocaleType.System
    }
}