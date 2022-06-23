/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import platform.Foundation.*
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

        actual class Custom actual constructor(languageTag: String) : LocaleType() {


            override val locale: NSLocale = NSLocale(
                NSLocale.localeIdentifierFromComponents(
                    buildMap {
                        val languageTagParts = languageTag.split("-")
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
                return rootBundle.pathForResource(locale.localeIdentifier, "lproj")
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